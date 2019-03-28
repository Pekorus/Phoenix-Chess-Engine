/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.board;

import chess.move.Move;
import static chess.board.ChessColor.*;
import static chess.board.PieceType.PAWN;
import chess.coordinate.Coordinate;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

/**
 *
 * @author Phoenix
 */
public class Board {
    
    private final Piece[][] board;
    private final ArrayList<Piece> blackPieces= new ArrayList<>();
    private final ArrayList<Piece> whitePieces= new ArrayList<>();    
    private final Stack<Piece> takenPieces= new Stack<>();
    private final int[] pawnStructWhite = {1,1,1,1,1,1,1,1};
    private final int[] pawnStructBlack = {1,1,1,1,1,1,1,1};
    private boolean whiteCastled, blackCastled;
    private Piece whiteKing, blackKing;
    
    //fields for hashing, hashValue is updated in execute/unexecute move
    private long[][][] zobrisMatrix;
    private long hashValue;
    
    public Board() {
        this.board = new Piece[8][8];
        createStartPosition();                    
        createPieceLists();      
        whiteCastled = false;
        blackCastled = false;
        initializeZobrisMatrix(1);
        hashValue = this.zobrisHashBoard();
    }
    
    public void executeMove(Move move){
        Coordinate coordFrom = move.getCoordFrom();
        Coordinate coordTo = move.getCoordTo();
        Piece piece = getPieceOnCoord(coordFrom);
        Piece optPiece;
        
        switch(move.getMoveType()){
            case NORMAL:
            updateHashValue(piece, coordFrom);                        
            move(piece, coordFrom, coordTo);
            if(move.getPromoteTo()!=null){
                piece.setPiecetype(move.getPromoteTo());
                decreasePawn(piece.isColor(), coordTo.getY());
            }
            updateHashValue(piece, coordTo);
            break;
            
            case TAKE:          
            updateHashValue(piece, coordFrom);
            optPiece = getPieceOnCoord(coordTo);
            move(piece, coordFrom, coordTo);
            optPiece.setCoord(null);            
            takenPieces.push(optPiece);
            //remove taken piece from piece list
            removePieceFromList(optPiece);            
           
            //promotion
            if(move.getPromoteTo()!=null){
                piece.setPiecetype(move.getPromoteTo());
                decreasePawn(piece.isColor(), coordTo.getY());
            }            
            
            updateHashValue(piece, coordTo);            
            updateHashValue(optPiece, coordTo);           
           
            //update PawnStruct
            if(optPiece.getPiecetype()==PAWN){
                decreasePawn(optPiece.isColor(), coordTo.getY());
            }    
            if(piece.getPiecetype()==PAWN){
                decreasePawn(piece.isColor(), coordFrom.getY());
                increasePawn(piece.isColor(), coordTo.getY());
            }
            break;
            
            case ENPASSANT:          
            move(piece, coordFrom, coordTo);            
            //clear pawn that is taken by en passant
            Coordinate optCoord = coordTo.takenCoordEP(piece.isColor());
            optPiece = getPieceOnCoord(optCoord); 
            this.clearField(optCoord);            
            optPiece.setCoord(null);          
            takenPieces.push(optPiece);            
            removePieceFromList(optPiece);
            
            //updateHashValue
            updateHashValue(piece, coordFrom);
            updateHashValue(piece, coordTo);            
            updateHashValue(optPiece, optCoord); 
            
            //update pawnStruct
            decreasePawn(piece.isColor(), coordFrom.getY());
            increasePawn(piece.isColor(), coordTo.getY());            
            decreasePawn(piece.isColor().getInverse(), coordTo.getY());
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
            inverseCastleflag(piece.isColor());
            
            //update hash value
            updateHashValue(piece, coordFrom);
            updateHashValue(piece, coordTo);            
            updateHashValue(rook, rookFrom); 
            updateHashValue(rook, rookTo);            
            break;            
        }
        piece.increaseMoveCounter();
    }

    private void clearField(Coordinate coord) {
        this.board[coord.getX()][coord.getY()] = null;
    }

    private void setField(Piece piece, Coordinate coord) {
        this.board[coord.getX()][coord.getY()] = piece;
    }

    public Piece getPieceOnCoord(Coordinate coord) {
        if(coord==null) return null;
        return this.board[coord.getX()][coord.getY()];
    }

    public boolean isOccupied(Coordinate coordTo) {
        return this.board[coordTo.getX()][coordTo.getY()]!= null;       
    }

    public void unexecuteMove(Move move) {
        Coordinate coordFrom = move.getCoordFrom();
        Coordinate coordTo = move.getCoordTo();
        Piece piece = getPieceOnCoord(coordTo);
        
        switch(move.getMoveType()){
            case NORMAL:
            updateHashValue(piece, coordTo);
            //reverse move
            move(piece, coordTo, coordFrom);
            //promotion
            if(move.getPromoteTo()!=null){
                piece.setPiecetype(PAWN);
                increasePawn(piece.isColor(), coordTo.getY());
            }
            updateHashValue(piece, coordFrom);
            break;
            
            case TAKE:           
            updateHashValue(piece, coordTo);            
            move(piece, coordTo, coordFrom);
            //reset taken piece
            Piece takenPiece = takenPieces.pop();
            this.setField(takenPiece, coordTo);            
            takenPiece.setCoord(coordTo);            
            //insert taken piece back to list
            addPieceToList(takenPiece);           
            //promotion
            if(move.getPromoteTo()!=null){
                piece.setPiecetype(PAWN);              
                increasePawn(piece.isColor(), coordTo.getY());
            }

            updateHashValue(piece, coordFrom);
            updateHashValue(takenPiece, coordTo);
            
            //update pawnStruct
            if(takenPiece.getPiecetype()==PAWN){
                increasePawn(takenPiece.isColor(), coordTo.getY());
            }
            if(piece.getPiecetype()==PAWN){
                increasePawn(piece.isColor(), coordFrom.getY());
                decreasePawn(piece.isColor(), coordTo.getY());
            }
            break;
            
            case ENPASSANT: 
            move(piece, coordTo, coordFrom);    
            //reset taken pawn
            Coordinate optCoord = coordTo.takenCoordEP(piece.isColor());
            Piece takenPawn = takenPieces.pop();
            this.setField(takenPawn, optCoord);            
            takenPawn.setCoord(optCoord);                       
            addPieceToList(takenPawn);
            
            updateHashValue(piece, coordTo);
            updateHashValue(piece, coordFrom);            
            updateHashValue(takenPawn, optCoord);             
            
            //update pawnStruct
            decreasePawn(piece.isColor(), coordTo.getY());
            increasePawn(piece.isColor(), coordFrom.getY());            
            increasePawn(piece.isColor().getInverse(), coordTo.getY());            
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
            inverseCastleflag(piece.isColor());
            
            updateHashValue(piece, coordTo);
            updateHashValue(piece, coordFrom);            
            updateHashValue(rook, rookTo); 
            updateHashValue(rook, rookFrom);              
            break;           
        }
        piece.decreaseMoveCounter();
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
    }

    private void move(Piece piece, Coordinate coordFrom, Coordinate coordTo) {
            //clear old field
            this.clearField(coordFrom);
            //set figure to new field
            this.setField(piece, coordTo);
            piece.setCoord(coordTo);  
    }

    public Piece getKing(ChessColor color) {
        if(color==WHITE) return whiteKing;
        return blackKing;
    }    

    private void createPieceLists() {
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                Piece auxPiece = board[i][j];
                if(auxPiece!=null){
                    if(auxPiece.isColor()==WHITE) whitePieces.add(auxPiece);
                    else blackPieces.add(auxPiece);
                }
            }
        }
    }

    private void removePieceFromList(Piece piece) {
        if(piece.isColor()==WHITE) whitePieces.remove(piece);
        else blackPieces.remove(piece);
    }

    private void addPieceToList(Piece optPiece) {
        if(optPiece.isColor()==WHITE) whitePieces.add(optPiece);
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

    public int[] getPawnStruct(ChessColor color) {
        if(color==WHITE) return pawnStructWhite;
        else return pawnStructBlack;
    }

    public int getPawnStruct(ChessColor color, int file) {
        if(file<0 || file>7) return 0;
        if(color==WHITE) return pawnStructWhite[file];
        else return pawnStructBlack[file];
    }  

    private void inverseCastleflag(ChessColor color) {
        if(color==WHITE) whiteCastled = !whiteCastled;
        else blackCastled = !blackCastled;
    }    

    public boolean hasCastled(ChessColor color) {
        if(color==WHITE) return whiteCastled;
        else return blackCastled;
    } 

    public boolean enPassantPossible() {
        //TODO
        return true;
    }

    /* Using Zobris hashing to generate hash code for a chess board state.*/ 
    private void initializeZobrisMatrix(long seed){
        
        /*matrix represents every combination of a chess square (8x8) and 
        every piece with an index (0-11) specified in method "pieceIndex" */
        Random randomLong = new Random();
        zobrisMatrix = new long[8][8][12];
        //randomLong.setSeed(seed);
        for(int i=0; i<8; i++){
           for(int j=0; j<8; j++){ 
               for(int k=0; k<12; k++){
                   zobrisMatrix[i][j][k] = randomLong.nextLong();
               }    
           }
        }
    }
    
    /* hash method */
    private long zobrisHashBoard(){
        long hash = 0;
        for(int i=0; i<8; i++){
           for(int j=0; j<8; j++){ 
               if(board[i][j]!= null)hash ^= zobrisMatrix[i][j][pieceIndex(board[i][j])];
           }       
        }
        return hash;
    }   
    
    /* auxiliary method to retrieve index of given piece on board for Zobris 
        matrix*/
    private int pieceIndex(Piece piece) {
        int index = -1;
        if(piece.isColor()==WHITE){
            switch(piece.getPiecetype()){
                case KING:
                   index = 0;
                   break; 
                case QUEEN:
                    index = 1;
                    break;
                case BISHOP:
                    index = 2;
                    break;
                case KNIGHT:
                    index = 3;
                    break;
                case ROOK:
                    index = 4;
                    break;
                case PAWN:
                    index = 5;
                    break;
            }
        }
        else{
            switch(piece.getPiecetype()){
                case KING:
                   index = 6;
                   break; 
                case QUEEN:
                    index = 7;
                    break;
                case BISHOP:
                    index = 8;
                    break;
                case KNIGHT:
                    index = 9;
                    break;
                case ROOK:
                    index = 10;
                    break;
                case PAWN:
                    index = 11;
                    break;
            }            
        }
        return index;
    }

    public long getHashValue() {
        return hashValue;
    }

    private void updateHashValue(Piece piece, Coordinate coord) {
        hashValue ^= zobrisMatrix[coord.getX()][coord.getY()][pieceIndex(piece)];
    }
    
}        