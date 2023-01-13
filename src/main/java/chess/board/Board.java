package chess.board;

import chess.move.Move;
import static chess.board.ChessColor.*;
import static chess.board.PieceType.KING;
import static chess.board.PieceType.PAWN;
import static chess.board.PieceType.ROOK;
import chess.coordinate.Coordinate;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

/**
 *
 * Provides a chess board to execute/unexecute moves on. The legality of moves
 * is not verified. The Zobris hash value of the position is updated everytime
 * a move is executed/unexecuted, therefore hashing is relatively cheap.
 */
public class Board {
    
    private final Piece[][] board;
    private final ArrayList<Piece> blackPieces= new ArrayList<>(16);
    private final ArrayList<Piece> whitePieces= new ArrayList<>(16);
    /* stack of all captured pieces (used for unmaking of moves) */
    private final Stack<Piece> takenPieces= new Stack<>();
    /* pawn structures of both colors. Provides a fast way to check the pawn
        structure for an AI.
    */
    private final int[] pawnStructWhite = {0,0,0,0,0,0,0,0};
    private final int[] pawnStructBlack = {0,0,0,0,0,0,0,0};
    /* Stores position of both Kings for fast access to them, for example to 
        verify checks.
    */
    private Piece whiteKing, blackKing;
    
    /* fields for hashing, hashValue is updated in execute/unexecute move */
    private long[][][] zobrisMatrix;
    private long zobrisSideToMove;
    private long hashValue;
    
    /**
     * Class constructor for a regular chess starting position.
     */
    public Board() {

        board = new Piece[8][8];
        createStartPosition();                    
        createPieceFields();      
        initializeZobrisValues(1235674);
        hashValue = zobrisHashBoard();
    }

    /**
     * Class constructor for a custom chess position. Legality of the position
     * is not verified. 
     * 
     * @param pieceArray        custom position represented by array of pieces
     * @param colorToMove       color to move first
     * @param castleRights      castling rights in order 0-0 white, 0-0-0 white, 
     *                          0-0 black, 0-0-0 black
     *                          
     */
    public Board(Piece[][] pieceArray, ChessColor colorToMove, 
                                                    boolean[] castleRights) {
        
        board = copyPieceArray(pieceArray);
        createPieceFields();         
        verifyAndSetCastleRights(castleRights);
        initializeZobrisValues(1);
        hashValue = zobrisHashBoard();
        if(colorToMove ==BLACK) hashValue ^= zobrisSideToMove;
    }
    
    public void executeMove(Move move){

        Coordinate coordFrom = move.getCoordFrom();
        Coordinate coordTo = move.getCoordTo();

        Piece piece = getPieceOnCoord(coordFrom);
        updatePieceHashValue(piece, coordFrom);

        switch(move.getMoveType()){

            case NORMAL:

                move(piece, coordFrom, coordTo);
                /* promotion */
                if(move.getPromoteTo()!=null){
                    piece.setPiecetype(move.getPromoteTo());
                    decreasePawn(piece.getColor(), coordFrom.getY());
                }

            break;

            case TAKE:

                Piece optPiece = getPieceOnCoord(coordTo);
                takenPieces.push(optPiece);
                /* remove taken piece from piece list */
                removePieceFromList(optPiece);
                updatePieceHashValue(optPiece, coordTo);

                move(piece, coordFrom, coordTo);

                /* promotion */
                if(move.getPromoteTo()!=null){
                    piece.setPiecetype(move.getPromoteTo());
                    decreasePawn(piece.getColor(), coordFrom.getY());
                }

                /* update pawn structure */
                if(optPiece.getType()==PAWN){
                    decreasePawn(optPiece.getColor(), coordTo.getY());
                }
                if(piece.getType()==PAWN){
                    decreasePawn(piece.getColor(), coordFrom.getY());
                    increasePawn(piece.getColor(), coordTo.getY());
                }
            break;

            case ENPASSANT:

                move(piece, coordFrom, coordTo);
                ChessColor pawnColor = piece.getColor();

                /* clear pawn that is taken by en passant */
                Coordinate optCoord = coordTo.takenCoordEP(pawnColor);
                Piece optPawn = getPieceOnCoord(optCoord);
                takenPieces.push(optPawn);
                removePieceFromList(optPawn);
                clearField(optCoord);
                updatePieceHashValue(optPawn, optCoord);

                /* update pawn structure */
                decreasePawn(pawnColor, coordFrom.getY());
                increasePawn(pawnColor, coordTo.getY());
                decreasePawn(optPawn.getColor(), coordTo.getY());
            break;

            case CASTLE:
                /* move king */
                move(piece, coordFrom, coordTo);
                /* move rook */
                Coordinate[] rookCoords = coordTo.getRookCastleCoords();
                Piece rook = getPieceOnCoord(rookCoords[0]);
                move(rook, rookCoords[0], rookCoords[1]);
                rook.increaseMoveCounter();

                updatePieceHashValue(rook, rookCoords[0]);
                updatePieceHashValue(rook, rookCoords[1]);
            break;
        }

        updatePieceHashValue(piece, coordTo);
        piece.increaseMoveCounter();
        hashValue ^= zobrisSideToMove;
    }

    private void clearField(Coordinate coord) {

        board[coord.getX()][coord.getY()] = null;
    }

    private void setField(Piece piece, Coordinate coord) {
        board[coord.getX()][coord.getY()] = piece;
    }

    public Piece getPieceOnCoord(Coordinate coord) {
        if(coord==null) return null;
        return board[coord.getX()][coord.getY()];
    }

    public boolean isOccupied(Coordinate coordTo) {
        return board[coordTo.getX()][coordTo.getY()]!= null;
    }

    public void unexecuteMove(Move move) {

        Coordinate coordFrom = move.getCoordFrom();
        Coordinate coordTo = move.getCoordTo();

        Piece piece = getPieceOnCoord(coordTo);
        updatePieceHashValue(piece, coordTo);
        /* reverse move */
        move(piece, coordTo, coordFrom);

        switch(move.getMoveType()){
            
            case NORMAL:

            /* promotion */
            if(move.getPromoteTo()!=null){
                piece.setPiecetype(PAWN);
                increasePawn(piece.getColor(), coordFrom.getY());
            }

            break;
            
            case TAKE:           

            /* reset taken piece */
            Piece takenPiece = takenPieces.pop();
            setField(takenPiece, coordTo);
            updatePieceHashValue(takenPiece, coordTo);

            /* insert taken piece back to list */
            addPieceToList(takenPiece);

            /* promotion */
            if(move.getPromoteTo()!=null){
                piece.setPiecetype(PAWN);              
                increasePawn(piece.getColor(), coordFrom.getY());
            }

            /* update pawn structure */
            if(takenPiece.getType()==PAWN){
                increasePawn(takenPiece.getColor(), coordTo.getY());
            }
            if(piece.getType()==PAWN){
                increasePawn(piece.getColor(), coordFrom.getY());
                decreasePawn(piece.getColor(), coordTo.getY());
            }
            break;
            
            case ENPASSANT: 

            /* reset taken pawn */
            Piece takenPawn = takenPieces.pop();
            setField(takenPawn, takenPawn.getCoord());
            addPieceToList(takenPawn);
            updatePieceHashValue(takenPawn, takenPawn.getCoord());

            ChessColor pawnColor = piece.getColor();

            decreasePawn(pawnColor, coordTo.getY());
            increasePawn(pawnColor, coordFrom.getY());            
            increasePawn(pawnColor.getInverse(), coordTo.getY());            

            break;
            
            case CASTLE:

            /* move rook */
            Coordinate[] rookCoords = coordTo.getRookCastleCoords();
            Piece rook = getPieceOnCoord(rookCoords[1]);
            
            move(rook, rookCoords[1], rookCoords[0]);
            rook.decreaseMoveCounter();

            updatePieceHashValue(rook, rookCoords[1]);
            updatePieceHashValue(rook, rookCoords[0]);
            break;           
        }

        updatePieceHashValue(piece, coordFrom);
        piece.decreaseMoveCounter();
        hashValue ^= zobrisSideToMove;
    }     

    public Piece[][] getPieceArray() {
        return board;
    }

    private void createStartPosition() {
       
        /* set kings and queens */
        board[0][3] = new Piece(PieceType.KING, WHITE, new Coordinate(0,3),0); 
        board[0][4] = new Piece(PieceType.QUEEN, WHITE, new Coordinate(0,4),0); 
        board[7][3] = new Piece(PieceType.KING, BLACK, new Coordinate(7,3),0); 
        board[7][4] = new Piece(PieceType.QUEEN, BLACK, new Coordinate(7,4),0); 
        /* set bishops */
        board[0][2] = new Piece(PieceType.BISHOP, WHITE, new Coordinate(0,2),0); 
        board[0][5] = new Piece(PieceType.BISHOP, WHITE, new Coordinate(0,5),0); 
        board[7][2] = new Piece(PieceType.BISHOP, BLACK, new Coordinate(7,2),0); 
        board[7][5] = new Piece(PieceType.BISHOP, BLACK, new Coordinate(7,5),0); 
        /* set knights */
        board[0][1] = new Piece(PieceType.KNIGHT, WHITE, new Coordinate(0,1),0); 
        board[0][6] = new Piece(PieceType.KNIGHT, WHITE, new Coordinate(0,6),0); 
        board[7][1] = new Piece(PieceType.KNIGHT, BLACK, new Coordinate(7,1),0); 
        board[7][6] = new Piece(PieceType.KNIGHT, BLACK, new Coordinate(7,6),0);        
        /* set rooks */
        board[0][0] = new Piece(PieceType.ROOK, WHITE, new Coordinate(0,0),0); 
        board[0][7] = new Piece(PieceType.ROOK, WHITE, new Coordinate(0,7),0); 
        board[7][0] = new Piece(PieceType.ROOK, BLACK, new Coordinate(7,0),0); 
        board[7][7] = new Piece(PieceType.ROOK, BLACK, new Coordinate(7,7),0);
        /* set pawns */
        for(int i=0; i<8; i++) {
            board[1][i] = new Piece(PieceType.PAWN, WHITE, 
                                                        new Coordinate(1,i),0); 
            board[6][i] = new Piece(PieceType.PAWN, BLACK, 
                                                        new Coordinate(6,i),0);
        }   
    }

    private void move(Piece piece, Coordinate coordFrom, Coordinate coordTo) {

        clearField(coordFrom);
        /* set piece to new field */
        setField(piece, coordTo);
        piece.setCoord(coordTo);

    }

    public Piece getKing(ChessColor color) {
        if(color==WHITE) return whiteKing;
        else return blackKing;
    }    

    public Coordinate getKingCoord(ChessColor color){
        if(color==WHITE) return whiteKing.getCoord();
        else return blackKing.getCoord();
    }
    
    public Coordinate getWhiteKingCoord(){
        return whiteKing.getCoord();
    }

    public Coordinate getBlackKingCoord(){
        return blackKing.getCoord();
    }
    
    /**
     * Sets the fields whiteKing, blackKing, pawnStructWhite, pawnStructBlack,
     * whitePieces, blackPieces as given by the current board piece array.
     */
    private void createPieceFields() {
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                Piece auxPiece = board[i][j];
                if(auxPiece!=null){
                    if(auxPiece.getColor()==WHITE){
                        if(auxPiece.getType()==KING) whiteKing = auxPiece;
                        if(auxPiece.getType()==PAWN) pawnStructWhite[j]++;
                        whitePieces.add(auxPiece);
                    }
                    else{
                        if(auxPiece.getType()==KING) blackKing = auxPiece;
                        if(auxPiece.getType()==PAWN) pawnStructBlack[j]++;
                        blackPieces.add(auxPiece);
                    }
                }
            }
        }
    }

    private void removePieceFromList(Piece piece) {
        if(piece.getColor()==WHITE) whitePieces.remove(piece);
        else blackPieces.remove(piece);
    }

    private void addPieceToList(Piece optPiece) {
        if(optPiece.getColor()==WHITE) whitePieces.add(optPiece);
        else blackPieces.add(optPiece);
    }

    public ArrayList<Piece> getPiecesList(ChessColor color){
        if(color==WHITE) return whitePieces;
        else return blackPieces;
    }

    private void decreasePawn(ChessColor color, int y) {
        if(color==WHITE) pawnStructWhite[y]--;
        else pawnStructBlack[y]--;
    }

    private void increasePawn(ChessColor color, int y) {
        if(color==WHITE) pawnStructWhite[y]++;
        else pawnStructBlack[y]++;
    }

    /**
     * Gets number of pawns of given color at given file.
     * 
     * @param color     color of pawns 
     * @param file      file at which pawns will be counted
     * @return 
     */
    public int getPawnStruct(ChessColor color, int file) {
        if(file<0 || file>7) return 0;
        if(color==WHITE) return pawnStructWhite[file];
        else return pawnStructBlack[file];
    }    

    /**
     * Gets number of white pawns at given file.
     * 
     * @param file      file at which pawns will be counted
     * @return 
     */
    public int getPawnStructWhite(int file) {
        if(file<0 || file>7) return 0;
        return pawnStructWhite[file];
    }      

    /**
     * Gets number of black pawns at given file.
     * 
     * @param file      file at which pawns will be counted
     * @return 
     */
    public int getPawnStructBlack(int file) {
        if(file<0 || file>7) return 0;
        return pawnStructBlack[file];
    }  
    
    public boolean enPassantPossible() {
        //TODO
        return true;
    }
 
    /**
     * Initializes the zobris matrix with given seed that will be used in 
     * zobris hashing. 
     * 
     * @param seed  seed for the random function
     */
    private void initializeZobrisValues(long seed){
        
        /*matrix represents every combination of a chess square (8x8) and 
        every piece with an index (0-11) specified in method "pieceIndex" */
        Random randomLong = new Random(seed);
        zobrisMatrix = new long[8][8][12];
        //randomLong.setSeed(seed);
        for(int i=0; i<8; i++){
           for(int j=0; j<8; j++){ 
               for(int k=0; k<12; k++){
                   zobrisMatrix[i][j][k] = randomLong.nextLong();
               }
           }
        }
        /* value represents the side to move */
        zobrisSideToMove = randomLong.nextLong();
    }
    
    /**
     * Calculates the zobris hash value of current board (without side to move).
     * 
     * @return  zobris hash value
     */
    private long zobrisHashBoard(){
        long hash = 0;
        for(int i=0; i<8; i++){
           for(int j=0; j<8; j++){ 
               if(board[i][j]!= null)hash ^= 
                                    zobrisMatrix[i][j][pieceIndex(board[i][j])];
           }       
        }
        return hash;
    }   
    
    /**
     * Gets the index of given piece (type and color) in the zobris matrix.
     * Auxiliary method for hashing.
     * 
     * @param piece     index of this type of piece will be returned
     * @return          index of given piece type and color
     */
    private int pieceIndex(Piece piece) {
        
        int index;
        if(piece.getColor()==WHITE){
            index = switch (piece.getType()) {
                case KING -> 0;
                case QUEEN -> 1;
                case BISHOP -> 2;
                case KNIGHT -> 3;
                case ROOK -> 4;
                case PAWN -> 5;
            };
        }
        else{
            index = switch (piece.getType()) {
                case KING -> 6;
                case QUEEN -> 7;
                case BISHOP -> 8;
                case KNIGHT -> 9;
                case ROOK -> 10;
                case PAWN -> 11;
            };
        }
        return index;
    }

    public long getHashValue() {
        return hashValue;
    }

    private void updatePieceHashValue(Piece piece, Coordinate coord) {
        hashValue ^= zobrisMatrix[coord.getX()][coord.getY()][pieceIndex(piece)];
    }

    public PieceType getPieceTypeOnCoord(Coordinate coord) {
        Piece piece = board[coord.getX()][coord.getY()];
        if(piece!=null) return piece.getType();      
        else return null;
    }

    /**
     * Verifies castle rights and sets the move counter of king pieces 
     * accordingly. If any castle is possible, move counter of corresponding 
     * king will be set to 0, otherwise to 1. Array castleRights is in order
     * 0-0 white, 0-0-0 white, 0-0 black, 0-0-0 black.
     * 
     * @param castleRights  array of castle rights to be verified
     */
    private void verifyAndSetCastleRights(boolean[] castleRights) {
        
        whiteKing.increaseMoveCounter();
        blackKing.increaseMoveCounter();
        
        boolean whiteCastle, blackCastle;
        
        /* white small castle possible */
        whiteCastle = castleRights[0] && board[0][0] != null && 
                                            board[0][0].getType()== ROOK;
        /* white large castle possible */
        whiteCastle = whiteCastle || (castleRights[1] && board[0][7] != null && 
                                            board[0][7].getType()== ROOK);
        if(whiteCastle) whiteKing.decreaseMoveCounter();
        
        /* black small castle possible */
        blackCastle = castleRights[2] && board[7][0] != null && 
                                            board[7][0].getType()== ROOK;
        /* black large castle possible */
        blackCastle = blackCastle || (castleRights[3] && board[7][7] != null && 
                                            board[7][7].getType()== ROOK);    
        if(blackCastle) blackKing.decreaseMoveCounter();
    }
    
    /**
     * Provides a deep copy of the given piece array.
     * 
     * @param oldArray  array to be deep copied
     * @return          same array, different object
     */
    private Piece[][] copyPieceArray(Piece[][] oldArray){
        
        Piece[][] newArray = new Piece[8][8];
    
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                if(oldArray[i][j]!=null) newArray[i][j] = 
                                                    oldArray[i][j].deepCopy();
            }
        }
        return newArray;
    }

    /**
     * Represents a null move (side to move just passes) for null move search.
     */
    public void executeNullMove() {
        hashValue ^= zobrisSideToMove;
    }
    
    /**
     * Represents unexecution of a null move (side to move
     * just passes) for null move search .
     */
    public void unexecuteNullMove() {
        hashValue ^= zobrisSideToMove;        
    }
}        