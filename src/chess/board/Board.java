/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.board;

import static chess.board.ChessColor.*;
import static chess.board.PieceType.*;
import chess.coordinate.Coordinate;
import chess.coordinate.Direction;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Phoenix
 */
public class Board {
    
    private final Piece[][] board;
    private ChessColor playersTurn;
    private Piece whiteKing, blackKing;
    private final List<Move> moveList;
    private List<Coordinate> castleCoords;
    
    public Board() {
        this.playersTurn= WHITE;
        this.board = new Piece[8][8];
        createStartPosition();        
        this.moveList = new ArrayList();            
    }
    
    public void executeMove(Move move){
        
        Coordinate coordFrom = move.getCoordFrom();
        Coordinate coordTo = move.getCoordTo();
        Piece piece = move.getPiece();
        Piece optPiece = move.getOptionalPiece();
        
        switch(move.getMoveType()){
            case NORMAL:
            move(piece, coordFrom, coordTo);
            break;
            
            case TAKE:
            //TODO: geschlagene Figuren in Liste            
            move(piece, coordFrom, coordTo);
            optPiece.setCoordinate(null);            
            break;
            
            case ENPASSANT:
            //TODO: geschlageen Figur Liste            
            move(piece, coordFrom, coordTo);            
            //clear pawn that is taken by en passant
            this.clearField(move.getOptionalPieceCoord());            
            optPiece.setCoordinate(null);          
            break;
            
            case CASTLE:
            //move king
            move(piece, coordFrom, coordTo);          
            
            //move rook
            Coordinate rookFrom = coordTo.getRookCastleCoord();
            Piece rook = this.getPieceOnCoord(rookFrom);
            Coordinate rookTo = coordFrom.
                            getCoordInDir(coordFrom.straightLineDir(coordTo));
            move(rook, rookFrom, rookTo);
            rook.increaseMoveCounter();
            break;      
        
            case PROMOTION:
            //TODO
            break;       
        }
        piece.increaseMoveCounter();
        this.nextPlayer();
        moveList.add(move);
    }

    public boolean validateMove(Move move) {
        
        //preparation and fail safes
        if(move==null) return false;
        Piece piece = move.getPiece();        
        if (piece==null) return false;
        PieceType pieceType = piece.getPiecetype();
        if(pieceType==null) return false;
        if(piece.isColor()!= playersTurn) return false;
        Coordinate coordFrom = move.getCoordFrom();
        Coordinate coordTo = move.getCoordTo();
        if(coordFrom==null) return false;
        if(coordTo==null) return false;
        if(!coordFrom.equals(piece.getCoordinate())) return false;
        
        switch(move.getMoveType()){
        case NORMAL:
        if(this.isOccupied(move.getCoordTo())) return false;    
        if(!this.isMovePossible(move)) return false;
        
        //TODO: auslagern in Methode
        if(pieceType==PAWN){
            if(piece.isColor()==WHITE){
                Coordinate sCoord= coordFrom.getCoordInDir(Direction.S);
                Coordinate s2Coord= sCoord.getCoordInDir(Direction.S);
                if(!sCoord.equals(coordTo) && !s2Coord.equals(coordTo)) return false;
                if(this.isOccupied(sCoord)) return false;
                if(s2Coord.equals(coordTo) && piece.getMoveCounter()!=0) 
                        return false;
            }
            else{                   
                Coordinate nCoord= coordFrom.getCoordInDir(Direction.N);
                Coordinate n2Coord= nCoord.getCoordInDir(Direction.N);
                if(!nCoord.equals(coordTo) && !n2Coord.equals(coordTo)) return false;
                if(this.isOccupied(nCoord)) return false;
                if(n2Coord.equals(coordTo) && piece.getMoveCounter()!=0) 
                    return false;
                }
            }    
        break;
            
        case TAKE:
        if(!this.isOccupied(coordTo)) return false;    
        if(this.getPieceOnCoord(coordTo).isColor()== 
                    piece.isColor())
            return false;
        if(!this.isMovePossible(move)) return false;
            
        if(pieceType==PAWN){
            if(piece.isColor()==WHITE){
                if(!coordTo.equals(coordFrom.getCoordInDir(Direction.SW)) && 
                        !coordTo.equals(coordFrom.getCoordInDir(Direction.SE))) 
                    return false;
            }
            else{                   
                if(!coordTo.equals(coordFrom.getCoordInDir(Direction.NW)) && 
                        !coordTo.equals(coordFrom.getCoordInDir(Direction.NE))) 
                    return false;
            }
        }
        break;    
        
        case ENPASSANT:            
        //TODO
        break;
            
        case CASTLE:
        if(pieceType!=KING) return false;
        if(piece.getMoveCounter()!=0) return false;
        if(!castleCoords.contains(coordTo)) return false;
        Coordinate rookCoord = coordTo.getRookCastleCoord();
        if(!rookCastleCheck(rookCoord)) return false;
        //check if all fields between king and rook are empty and not in check
        Coordinate kingCoord = piece.getCoordinate();
        Direction dir = kingCoord.straightLineDir(rookCoord);
        Coordinate auxCoord = kingCoord.getCoordInDir(dir);
        while(!auxCoord.equals(rookCoord)){
            if(this.isOccupied(auxCoord)) return false;
            //TODO: kurze vs lange ROchade, ein Feld ist Schach egal
            if(isCheck(auxCoord, piece.isColor())) return false;
            auxCoord = auxCoord.getCoordInDir(dir);
        }        
        break;    
            
        case PROMOTION:
        //TODO
        break;    
        }
    
        this.executeMove(move);
        if(playersTurn == BLACK && isCheck(whiteKing.getCoordinate(), WHITE)){
            this.unexecuteMove(move);
            return false;
        }    
        if(playersTurn == WHITE && isCheck(blackKing.getCoordinate(), BLACK)){
            this.unexecuteMove(move);
            return false;
        }
        this.unexecuteMove(move);
    return true;        
    }

    private void clearField(Coordinate coord) {
        this.board[coord.getX()][coord.getY()] = null;
    }

    private void setField(Piece piece, Coordinate coord) {
        this.board[coord.getX()][coord.getY()] = piece;
    }

    private Piece getPieceOnCoord(Coordinate coord) {
        return this.board[coord.getX()][coord.getY()];
    }

    private boolean isOccupied(Coordinate coordTo) {
        return this.board[coordTo.getX()][coordTo.getY()]!= null;       
    }

//wird benötigt für KI
    private List<Coordinate> reachPiece(Piece piece) {
        
        List<Coordinate> coordList = new ArrayList<>();
        Coordinate startCoord = piece.getCoordinate();
        ChessColor pieceColor = piece.isColor();
        
        switch(piece.getPiecetype()){
            case KING: //TODO: Castling
            for(Direction dir : Direction.values()){
                Coordinate newCoord = piece.getCoordinate().getCoordInDir(dir);
                if(newCoord!=null&&(!this.isOccupied(newCoord)|| pieceColor
                    != this.getPieceOnCoord(newCoord).isColor())) 
                    coordList.add(newCoord);
            }
            break;
            
            case QUEEN:   
            List<Direction> queenList = 
                         new LinkedList<>(Arrays.asList(Direction.values()));
            coordList = zoomPieceList(queenList, startCoord, pieceColor);
            break;

            case BISHOP:
            List<Direction> bishopList = Direction.createBishopList();
            coordList = zoomPieceList(bishopList, startCoord, pieceColor);
            break;                        

            case ROOK:
            List<Direction> rookList = Direction.createRookList();
            coordList = zoomPieceList(rookList, startCoord, pieceColor);
            break;    

            case KNIGHT:
            
            break;    
            
            case PAWN:            
            //white pawn
            if(pieceColor==WHITE){
               //one step
               Coordinate newCoord = startCoord.getCoordInDir(Direction.S);
               if(newCoord!=null && !this.isOccupied(newCoord)) 
                      coordList.add(newCoord);
               //double step when not moved
               if(piece.getMoveCounter()==0){
                    Coordinate doubleStep = newCoord.getCoordInDir(Direction.S);
                    if(!this.isOccupied(doubleStep)) 
                      coordList.add(doubleStep);               
               }
               //take
               newCoord = startCoord.getCoordInDir(Direction.SW);
               if(newCoord!=null && this.isOccupied(newCoord) && 
                       this.getPieceOnCoord(newCoord).isColor()==BLACK) 
                      coordList.add(newCoord);
               newCoord = startCoord.getCoordInDir(Direction.SE);
               if(newCoord!=null && this.isOccupied(newCoord) && 
                       this.getPieceOnCoord(newCoord).isColor()==BLACK) 
                      coordList.add(newCoord);               
               }        
            
            break;    
            //TODO
        
        }
        return coordList;
    }

    private List<Coordinate> zoomPieceList(List<Direction> dirList, 
                        Coordinate startCoord, ChessColor pieceColor) {
        
        List<Coordinate> returnList = new ArrayList<>();
       
        dirList.forEach((dir) -> {
            Coordinate newCoord = startCoord.getCoordInDir(dir);
            while(newCoord!=null){
                if(!this.isOccupied(newCoord)|| pieceColor
                        != this.getPieceOnCoord(newCoord).isColor()) 
                    returnList.add(newCoord);
                newCoord = newCoord.getCoordInDir(dir);
            }
        });
    return returnList;    
    }

//checks if move from start coordinate to target coordinate is possible
//NOT checking if target is legit in case of taking, just considers if the way is free
    private boolean isMovePossible(Move move) {
        
        Coordinate coordFrom = move.getCoordFrom();
        Coordinate coordTo = move.getCoordTo();
        Direction dir;
        Coordinate newCoord;

        if(move.getPiece()==null) return false;
        switch(move.getPiece().getPiecetype()){
            case KING:
            //TODO: gleiches FEld ausschließen ?
            if(abs(coordFrom.getX()-coordTo.getX())>1)
                return false;
            if(abs(coordFrom.getY()-coordTo.getY())>1)
                return false;
            break;
            
            case QUEEN:
            //diagFlag states if coordinates are on diagonal, strightFlag lines
            //TODO: Methoden Coordinatesonlien/diag
            boolean diagFlag = abs(coordFrom.getX()-coordTo.getX())== 
                    abs(coordFrom.getY()-coordTo.getY());
            boolean straightFlag = coordFrom.getX()== coordTo.getX()
                    || coordFrom.getY()== coordTo.getY();
            if (!diagFlag && !straightFlag) return false;
            
            if(diagFlag) dir = coordFrom.diagonalLineDir(coordTo);
                else dir = coordFrom.straightLineDir(coordTo);
            newCoord = coordFrom.getCoordInDir(dir);
            while(!newCoord.equals(coordTo)){
                if(this.isOccupied(newCoord)) return false;
                newCoord = newCoord.getCoordInDir(dir);
            }                
            break;
            
            case BISHOP:
            if (abs(coordFrom.getX()-coordTo.getX())!= 
                    abs(coordFrom.getY()-coordTo.getY()))
                return false;
            dir = coordFrom.diagonalLineDir(coordTo);
            newCoord = coordFrom.getCoordInDir(dir);
            while(!newCoord.equals(coordTo)){
                if(this.isOccupied(newCoord)) return false;
                newCoord = newCoord.getCoordInDir(dir);
            }
            break;            
            
            case KNIGHT:
            int disX = abs(coordFrom.getX()-coordTo.getX());
            int disY = abs(coordFrom.getY()-coordTo.getY());
            if( (disX !=2||disY !=1) && (disX!=1 || disY !=2)) return false;
            break;
            
            case ROOK:
            if (coordFrom.getX()!= coordTo.getX()
                    && coordFrom.getY()!= coordTo.getY())
                return false;
            dir = coordFrom.straightLineDir(coordTo);
            newCoord = coordFrom.getCoordInDir(dir);
            while(!newCoord.equals(coordTo)){
                if(this.isOccupied(newCoord)) return false;
                newCoord = newCoord.getCoordInDir(dir);
            }
            break;
            
            case PAWN:    
            //is coded in validateMove                                
            break;    
        }
    return true;
    }

    //checks if a given field is in check on current boardstate
    private boolean isCheck(Coordinate kingCoord, ChessColor color) {
       
       Coordinate startCoord = kingCoord;
       Coordinate auxCoord;
       List<Direction> bishopList= Direction.createBishopList();
       List<Direction> rookList = Direction.createRookList();
       List<Coordinate> knightList = startCoord.createKnightCoordinates();
       Piece auxPiece;
       
       //check from a bishop or queen?
       for(Direction dir : bishopList){
           auxCoord = startCoord.getCoordInDir(dir);
           while(auxCoord!=null && !this.isOccupied(auxCoord)){
               auxCoord = auxCoord.getCoordInDir(dir);
           }
           if(auxCoord!=null){
                auxPiece = this.getPieceOnCoord(auxCoord);
                PieceType PT = auxPiece.getPiecetype();
                if(auxPiece.isColor()!= color && 
                   (PT == BISHOP || PT == QUEEN))
                    return true;
            }
       }       
       //check from a rook or queen? 
       for(Direction dir : rookList){
           auxCoord = startCoord.getCoordInDir(dir);
           while(auxCoord!=null && !this.isOccupied(auxCoord)){
               auxCoord = auxCoord.getCoordInDir(dir);
           }
           if(auxCoord!=null){
                Piece occuPiece = this.getPieceOnCoord(auxCoord);
                PieceType PT = occuPiece.getPiecetype();
                if(occuPiece.isColor()!= color && 
                   (PT == ROOK || PT == QUEEN))
                    return true;
            }
       }              
       //check from a knight?
       for(Coordinate possCoord : knightList){
           Piece piece = this.getPieceOnCoord(possCoord);
           if(piece!=null && piece.getPiecetype() == KNIGHT 
                          && piece.isColor()!=color )
               return true;
       }
       //check from a pawn?
       if(color==WHITE){
           auxPiece = getPieceOnCoord(startCoord.getCoordInDir(Direction.SW));
           if(auxPiece!=null && auxPiece.getPiecetype()==PAWN 
                   && auxPiece.isColor()==BLACK)
               return true;
           auxPiece = getPieceOnCoord(startCoord.getCoordInDir(Direction.SE));
           if(auxPiece!=null && auxPiece.getPiecetype()==PAWN 
                   && auxPiece.isColor()==BLACK)
               return true;           
       }
       else{
           auxPiece = getPieceOnCoord(startCoord.getCoordInDir(Direction.NW));
           if(auxPiece!=null && auxPiece.getPiecetype()==PAWN 
                   && auxPiece.isColor()==WHITE)
               return true;
           auxPiece = getPieceOnCoord(startCoord.getCoordInDir(Direction.NE));
           if(auxPiece!=null && auxPiece.getPiecetype()==PAWN 
                   && auxPiece.isColor()==WHITE)
               return true;           
       }       
       return false;
    }

    public void unexecuteMove(Move move) {
        
        Coordinate coordFrom = move.getCoordFrom();
        Coordinate coordTo = move.getCoordTo();
        Piece piece = move.getPiece();
        Piece optPiece = move.getOptionalPiece();
        
        switch(move.getMoveType()){
            case NORMAL:
            //reverse move
            move(piece, coordTo, coordFrom);
            break;
            
            case TAKE:
            //TODO: geschlagene Figuren in Liste            
            move(piece, coordTo, coordFrom);
            //reset taken piece
            this.setField(optPiece, coordTo);            
            optPiece.setCoordinate(coordTo);            
            break;
            
            case ENPASSANT:
            //TODO: geschlageen Figur Liste  
            move(piece, coordTo, coordFrom);    
            //reset taken pawn
            this.setField(optPiece, move.getOptionalPieceCoord());            
            optPiece.setCoordinate(move.getOptionalPieceCoord());                       
            break;
            
            case CASTLE:
            //move king
            move(piece, coordTo, coordFrom);    
            
            //move rook
            Coordinate rookFrom = coordTo.getRookCastleCoord();
            Coordinate rookTo = coordFrom.
                            getCoordInDir(coordFrom.straightLineDir(coordTo));            
            Piece rook = this.getPieceOnCoord(rookTo);
            
            move(rook, rookTo, rookFrom);  
            rook.decreaseMoveCounter();
            break;       
        
            case PROMOTION:
            //TODO
            break;       
        }
        piece.decreaseMoveCounter();
        this.nextPlayer();
        moveList.remove(move);
    }     

    private void nextPlayer() {
        if(this.playersTurn== WHITE) this.playersTurn= BLACK;
        else this.playersTurn= WHITE;
    }

    public boolean isCheckmate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public ChessColor getPlayersTurn() {
        return playersTurn;
    }

    public void setPlayersTurn(ChessColor playersTurn) {
        this.playersTurn = playersTurn;
    }

    public Piece[][] getPieceArray() {
        return board;
    }

    private void createStartPosition() {
        //creating starting position        
        //setting kings and queens
        board[0][3] = new Piece(PieceType.KING, WHITE, new Coordinate(0,3)); 
        board[0][4] = new Piece(PieceType.QUEEN, WHITE, new Coordinate(0,4)); 
        board[7][3] = new Piece(PieceType.KING, BLACK, new Coordinate(7,3)); 
        board[7][4] = new Piece(PieceType.QUEEN, BLACK, new Coordinate(7,4)); 
        //setting bishops
        board[0][2] = new Piece(PieceType.BISHOP, WHITE, new Coordinate(0,2)); 
        board[0][5] = new Piece(PieceType.BISHOP, WHITE, new Coordinate(0,5)); 
        board[7][2] = new Piece(PieceType.BISHOP, BLACK, new Coordinate(7,2)); 
        board[7][5] = new Piece(PieceType.BISHOP, BLACK, new Coordinate(7,5)); 
        //setting knights
        board[0][1] = new Piece(PieceType.KNIGHT, WHITE, new Coordinate(0,1)); 
        board[0][6] = new Piece(PieceType.KNIGHT, WHITE, new Coordinate(0,6)); 
        board[7][1] = new Piece(PieceType.KNIGHT, BLACK, new Coordinate(7,1)); 
        board[7][6] = new Piece(PieceType.KNIGHT, BLACK, new Coordinate(7,6));        
        //setting rooks
        board[0][0] = new Piece(PieceType.ROOK, WHITE, new Coordinate(0,0)); 
        board[0][7] = new Piece(PieceType.ROOK, WHITE, new Coordinate(0,7)); 
        board[7][0] = new Piece(PieceType.ROOK, BLACK, new Coordinate(7,0)); 
        board[7][7] = new Piece(PieceType.ROOK, BLACK, new Coordinate(7,7));
        //setting pawns
        for(int i=0; i<8; i++) {
            board[1][i] = new Piece(PieceType.PAWN, WHITE, new Coordinate(1,i)); 
            board[6][i] = new Piece(PieceType.PAWN, BLACK, new Coordinate(6,i));
        }
        this.whiteKing= board[0][3];
        this.blackKing= board[7][3];    
        castleCoords = new LinkedList();
        castleCoords.add(new Coordinate(0,1));
        castleCoords.add(new Coordinate(0,5));
        castleCoords.add(new Coordinate(7,1));
        castleCoords.add(new Coordinate(7,5));
    }

    private boolean rookCastleCheck(Coordinate rookCoord) {
        
        if(rookCoord==null) return false;

        Piece rook = board[rookCoord.getX()][rookCoord.getY()];
        if(rook==null) return false;
        if(rook.getPiecetype()!=ROOK) return false;
        return rook.getMoveCounter()==0; 
    }

    private void move(Piece piece, Coordinate coordFrom, Coordinate coordTo) {
            //clear old field
            this.clearField(coordFrom);
            //set figure to new field
            this.setField(piece, coordTo);
            piece.setCoordinate(coordTo);
    }

}
