/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.game;

import chess.board.Board;
import chess.board.ChessColor;
import static chess.board.ChessColor.*;
import chess.move.Move;
import chess.board.Piece;
import chess.board.PieceType;
import static chess.board.PieceType.*;
import chess.coordinate.Coordinate;
import chess.coordinate.Direction;
import static java.lang.Math.abs;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Phoenix
 */
public class ChessRules {
         
    private final List<Coordinate> castleCoords;
    Board board;
    
    public ChessRules() {
        castleCoords = new LinkedList();
        castleCoords.add(new Coordinate(0,1));
        castleCoords.add(new Coordinate(0,5));
        castleCoords.add(new Coordinate(7,1));
        castleCoords.add(new Coordinate(7,5));
    }
    
        
    public boolean validateMove(Move move, ChessGame game) {
        
        board = game.getBoard();
        
        //preparation and fail safes
        if(move==null) return false;
        Piece piece = move.getPiece();        
        if (piece==null) return false;
        PieceType pieceType = piece.getPiecetype();
        if(pieceType==null) return false;
        if(piece.isColor()!= game.getPlayersTurn()) return false;
        Coordinate coordFrom = move.getCoordFrom();
        Coordinate coordTo = move.getCoordTo();
        if(coordFrom==null) return false;
        if(coordTo==null) return false;
        if(!coordFrom.equals(piece.getCoordinate())) return false;
        
        switch(move.getMoveType()){
        case NORMAL:
        if(board.isOccupied(move.getCoordTo())) return false;    
        if(!isMovePossible(move)) return false;

        //TODO: auslagern in Methode
        if(pieceType==PAWN){
            if(piece.isColor()==WHITE){
                Coordinate sCoord= coordFrom.getCoordInDir(Direction.S);
                Coordinate s2Coord= sCoord.getCoordInDir(Direction.S);
                if(!sCoord.equals(coordTo) && !coordTo.equals(s2Coord)) return false;
                if(board.isOccupied(sCoord)) return false;
                if(coordTo.equals(s2Coord) && piece.getMoveCounter()!=0) 
                        return false;
                if(move.getPromoteTo()!=null && coordTo.getX()!=7) return false;        
            }
            else{                   
                Coordinate nCoord= coordFrom.getCoordInDir(Direction.N);
                Coordinate n2Coord= nCoord.getCoordInDir(Direction.N);
                if(!nCoord.equals(coordTo) && !coordTo.equals(n2Coord)) return false;
                if(board.isOccupied(nCoord)) return false;
                if(coordTo.equals(n2Coord) && piece.getMoveCounter()!=0) 
                    return false;
                if(move.getPromoteTo()!=null && coordTo.getX()!=0) return false;
                }
            }    
        break;
            
        case TAKE:
        if(!board.isOccupied(coordTo)) return false;    
        if(board.getPieceOnCoord(coordTo).isColor()== 
                    piece.isColor())
            return false;
        if(!this.isMovePossible(move)) return false;
            
        if(pieceType==PAWN){
            if(piece.isColor()==WHITE){
                if(!coordTo.equals(coordFrom.getCoordInDir(Direction.SW)) && 
                        !coordTo.equals(coordFrom.getCoordInDir(Direction.SE))) 
                    return false;
            if(move.getPromoteTo()!=null && coordTo.getX()!=7) return false;
            }
            else{                   
                if(!coordTo.equals(coordFrom.getCoordInDir(Direction.NW)) && 
                        !coordTo.equals(coordFrom.getCoordInDir(Direction.NE))) 
                    return false;
            if(move.getPromoteTo()!=null && coordTo.getX()!=0) return false;
            }
        }
        break;    
        
        case ENPASSANT:            
        Piece optPiece = move.getOptionalPiece();
        if(optPiece==null) return false;
        if(pieceType!=PAWN) return false;
        if(optPiece.getPiecetype()!=PAWN) return false;
        if(optPiece.isColor()==piece.isColor()) return false;
        if(board.isOccupied(coordTo)) return false;
        if((piece.isColor()==WHITE && coordFrom.getX()!=4)||
                (piece.isColor()==BLACK && coordFrom.getX()!=3)) return false;
        Move lastMove = game.getLastMove();
        if(lastMove.getPiece().getPiecetype()!=PAWN) return false;
        if(!lastMove.getCoordTo().
                                equals(optPiece.getCoordinate())) return false;
        if(lastMove.getCoordFrom().
                               distance(lastMove.getCoordTo())!=2) return false; 
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
            if(board.isOccupied(auxCoord)) return false;
            //TODO: kurze vs lange ROchade, ein Feld ist Schach egal
            if(isCheck(auxCoord, piece.isColor())) return false;
            auxCoord = auxCoord.getCoordInDir(dir);
        }        
        if(isCheck(coordFrom, piece.isColor())) return false;
        break;       
        }
    
        board.executeMove(move);
        if(game.getPlayersTurn() == WHITE && 
                isCheck(board.getWhiteKing().getCoordinate(), WHITE)){
            board.unexecuteMove(move);
            return false;
        }    
        if(game.getPlayersTurn() == BLACK && 
                isCheck(board.getBlackKing().getCoordinate(), BLACK)){
            board.unexecuteMove(move);
            return false;
        }
        board.unexecuteMove(move);
    return true;        
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
                if(board.isOccupied(newCoord)) return false;
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
                if(board.isOccupied(newCoord)) return false;
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
                if(board.isOccupied(newCoord)) return false;
                newCoord = newCoord.getCoordInDir(dir);
            }
            break;
            
            case PAWN:    
            //is coded in validateMove                                
            break;    
        }
    return true;
    }

    private boolean rookCastleCheck(Coordinate rookCoord) {
        
        if(rookCoord==null) return false;

        Piece rook = board.getPieceOnCoord(rookCoord);
        if(rook==null) return false;
        if(rook.getPiecetype()!=ROOK) return false;
        return rook.getMoveCounter()==0; 
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
           while(auxCoord!=null && !board.isOccupied(auxCoord)){
               auxCoord = auxCoord.getCoordInDir(dir);
           }
           if(auxCoord!=null){
                auxPiece = board.getPieceOnCoord(auxCoord);
                PieceType PT = auxPiece.getPiecetype();
                if(auxPiece.isColor()!= color && 
                   (PT == BISHOP || PT == QUEEN))
                    return true;
            }
       }       
       //check from a rook or queen? 
       for(Direction dir : rookList){
           auxCoord = startCoord.getCoordInDir(dir);
           while(auxCoord!=null && !board.isOccupied(auxCoord)){
               auxCoord = auxCoord.getCoordInDir(dir);
           }
           if(auxCoord!=null){
                Piece occuPiece = board.getPieceOnCoord(auxCoord);
                PieceType PT = occuPiece.getPiecetype();
                if(occuPiece.isColor()!= color && 
                   (PT == ROOK || PT == QUEEN))
                    return true;
            }
       }              
       //check from a knight?
       for(Coordinate possCoord : knightList){
           Piece piece = board.getPieceOnCoord(possCoord);
           if(piece!=null && piece.getPiecetype() == KNIGHT 
                          && piece.isColor()!=color )
               return true;
       }
       //check from a pawn?
       if(color==WHITE){
           auxPiece = board.getPieceOnCoord(startCoord.getCoordInDir(Direction.SW));
           if(auxPiece!=null && auxPiece.getPiecetype()==PAWN 
                   && auxPiece.isColor()==BLACK)
               return true;
           auxPiece = board.getPieceOnCoord(startCoord.getCoordInDir(Direction.SE));
           if(auxPiece!=null && auxPiece.getPiecetype()==PAWN 
                   && auxPiece.isColor()==BLACK)
               return true;           
       }
       else{
           auxPiece = board.getPieceOnCoord(startCoord.getCoordInDir(Direction.NW));
           if(auxPiece!=null && auxPiece.getPiecetype()==PAWN 
                   && auxPiece.isColor()==WHITE)
               return true;
           auxPiece = board.getPieceOnCoord(startCoord.getCoordInDir(Direction.NE));
           if(auxPiece!=null && auxPiece.getPiecetype()==PAWN 
                   && auxPiece.isColor()==WHITE)
               return true;           
       }       
       return false;
    }

}
