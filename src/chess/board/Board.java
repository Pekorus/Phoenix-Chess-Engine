/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.board;

import chess.move.Move;
import static chess.board.ChessColor.*;
import static chess.board.PieceType.*;
import chess.coordinate.Coordinate;
import chess.coordinate.Direction;
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
    private Piece whiteKing, blackKing;
    
    public Board() {
        this.board = new Piece[8][8];
        createStartPosition();                    
    }
    
    public void executeMove(Move move){
        Coordinate coordFrom = move.getCoordFrom();
        Coordinate coordTo = move.getCoordTo();
        Piece piece = move.getPiece();
        Piece optPiece = move.getOptionalPiece();
        
        switch(move.getMoveType()){
            case NORMAL:
            move(piece, coordFrom, coordTo);
            if(move.getPromoteTo()!=null){
            Piece auxPiece = new Piece(move.getPromoteTo(), piece.isColor(),
                                        coordTo);    
            auxPiece.increaseMoveCounter();
            this.setField(auxPiece, coordTo);
            }
            break;
            
            case TAKE:
            //TODO: geschlagene Figuren in Liste            
            move(piece, coordFrom, coordTo);
            optPiece.setCoordinate(null);            
            if(move.getPromoteTo()!=null){
            Piece auxPiece = new Piece(move.getPromoteTo(), piece.isColor(),
                                        coordTo);    
            auxPiece.increaseMoveCounter();
            this.setField(auxPiece, coordTo);
            }
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
        return this.board[coord.getX()][coord.getY()];
    }

    public boolean isOccupied(Coordinate coordTo) {
        return this.board[coordTo.getX()][coordTo.getY()]!= null;       
    }

//wird benötigt für KI
    /*private List<Coordinate> reachPiece(Piece piece) {
        
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
    }*/

    /*private List<Coordinate> zoomPieceList(List<Direction> dirList, 
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
    }*/

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
        }
        piece.decreaseMoveCounter();
    }     

    public boolean isCheckmate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
            piece.setCoordinate(coordTo);
    }

    public Piece getWhiteKing() {
        return whiteKing;
    }

    public Piece getBlackKing() {
        return blackKing;
    }

    
}
