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
    private Piece whiteKing, blackKing;
    
    public Board() {
        this.board = new Piece[8][8];
        createStartPosition();                    
        createPieceLists();
    }
    
    public void executeMove(Move move){
        Coordinate coordFrom = move.getCoordFrom();
        Coordinate coordTo = move.getCoordTo();
        Piece piece = getPieceOnCoord(coordFrom);
        Piece optPiece = getPieceOnCoord(move.getOptPieceCoord());
        
        switch(move.getMoveType()){
            case NORMAL:
            move(piece, coordFrom, coordTo);
            //TODO: promote method
            if(move.getPromoteTo()!=null){
                Piece auxPiece = new Piece(move.getPromoteTo(), piece.isColor(),
                                        coordTo);    
                auxPiece.setMoveCounter(piece.getMoveCounter());
                auxPiece.increaseMoveCounter();
                this.setField(auxPiece, coordTo);
                removePieceFromList(piece);
                addPieceToList(auxPiece);
            }
            break;
            
            case TAKE:          
            move(piece, coordFrom, coordTo);
            optPiece.setCoord(null);            
            takenPieces.push(optPiece);
            if(move.getPromoteTo()!=null){
                Piece auxPiece = new Piece(move.getPromoteTo(), piece.isColor(),
                                            coordTo);    
                auxPiece.setMoveCounter(piece.getMoveCounter());                
                auxPiece.increaseMoveCounter();
                this.setField(auxPiece, coordTo);
                removePieceFromList(piece);
                addPieceToList(auxPiece);
            }
            //remove taken piece from piece list
            removePieceFromList(optPiece);
            break;
            
            case ENPASSANT:          
            move(piece, coordFrom, coordTo);            
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
                removePieceFromList(piece);
                Piece pawn = new Piece(PAWN, piece.isColor(), coordFrom);
                pawn.setMoveCounter(piece.getMoveCounter());
                pawn.decreaseMoveCounter();
                addPieceToList(pawn);
                setField(pawn, coordFrom);
            }
            break;
            
            case TAKE:           
            move(piece, coordTo, coordFrom);
            //reset taken piece
            this.setField(takenPieces.peek(), coordTo);            
            takenPieces.peek().setCoord(coordTo);            
            //insert taken piece back to list
            addPieceToList(takenPieces.pop());           
            //promotion
            if(move.getPromoteTo()!=null){
                removePieceFromList(piece);                
                Piece pawn = new Piece(PAWN, piece.isColor(), coordFrom);
                pawn.setMoveCounter(piece.getMoveCounter());
                pawn.decreaseMoveCounter();
                addPieceToList(piece);
                setField(pawn, coordFrom);                
            }
            break;
            
            case ENPASSANT: 
            move(piece, coordTo, coordFrom);    
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
}
