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
    
    
    public Board() {
        this.board = new Piece[8][8];
        createStartPosition();                    
        createPieceLists();      
        whiteCastled = false;
        blackCastled = false;
    }
    
    public void executeMove(Move move){
        Coordinate coordFrom = move.getCoordFrom();
        Coordinate coordTo = move.getCoordTo();
        Piece piece = getPieceOnCoord(coordFrom);
        Piece optPiece = getPieceOnCoord(move.getOptPieceCoord());
        
        switch(move.getMoveType()){
            case NORMAL:
            move(piece, coordFrom, coordTo);
            if(move.getPromoteTo()!=null){
                piece.setPiecetype(move.getPromoteTo());
                decreasePawn(piece.isColor(), coordTo.getY());
            }
            break;
            
            case TAKE:          
            move(piece, coordFrom, coordTo);
            optPiece.setCoord(null);            
            takenPieces.push(optPiece);
            //remove taken piece from piece list
            removePieceFromList(optPiece);            
            
            //update PawnStruct
            if(optPiece.getPiecetype()==PAWN){
                decreasePawn(optPiece.isColor(), coordTo.getY());
            }    
            if(piece.getPiecetype()==PAWN){
                decreasePawn(piece.isColor(), coordFrom.getY());
                increasePawn(piece.isColor(), coordTo.getY());
            }
            //promotion
            if(move.getPromoteTo()!=null){
                piece.setPiecetype(move.getPromoteTo());
                decreasePawn(piece.isColor(), coordTo.getY());
            }
            break;
            
            case ENPASSANT:          
            move(piece, coordFrom, coordTo);            
            //update pawnStruct
            decreasePawn(piece.isColor(), coordFrom.getY());
            increasePawn(piece.isColor(), coordTo.getY());            
            decreasePawn(optPiece.isColor(), move.getOptPieceCoord().getY());
            //clear pawn that is taken by en passant
            this.clearField(move.getOptionalPieceCoord());            
            optPiece.setCoord(null);          
            takenPieces.push(optPiece);            
            removePieceFromList(optPiece);
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
            //reverse move
            move(piece, coordTo, coordFrom);
            //promotion
            //TODO: unpromote method
            if(move.getPromoteTo()!=null){
                piece.setPiecetype(PAWN);
                increasePawn(piece.isColor(), coordTo.getY());
            }
            break;
            
            case TAKE:           
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
            //update pawnStruct
            decreasePawn(piece.isColor(), coordTo.getY());
            increasePawn(piece.isColor(), coordFrom.getY());            
            increasePawn(piece.isColor().getInverse(), coordTo.getY());
            //reset taken pawn
            this.setField(takenPieces.peek(), move.getOptionalPieceCoord());            
            takenPieces.peek().setCoord(move.getOptionalPieceCoord());                       
            addPieceToList(takenPieces.pop());
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
}
