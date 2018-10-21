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
import static chess.move.MoveType.NORMAL;
import static chess.move.MoveType.TAKE;
import static java.lang.Math.abs;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Phoenix
 */
public class ChessRules {
         
    private final List<Coordinate> castleCoords= new LinkedList();;
    private final ChessGame game;
    private final Board board;
    
    public ChessRules(ChessGame game) {
        this.game = game;
        this.board = game.getBoard();
        createCastleCoords();
    }
    
        
    public boolean validateMove(Move move, ChessGame game) {
             
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
            if(!isCheck(auxCoord, piece.isColor()).isEmpty()) return false;
            auxCoord = auxCoord.getCoordInDir(dir);
        }        
        if(!isCheck(coordFrom, piece.isColor()).isEmpty()) return false;
        break;       
        }
    
        board.executeMove(move);
        if(game.getPlayersTurn() == WHITE && 
               !isCheck(board.getKing(WHITE).getCoordinate(), WHITE).isEmpty()){
            board.unexecuteMove(move);
            return false;
        }    
        if(game.getPlayersTurn() == BLACK && 
                !isCheck(board.getKing(BLACK).getCoordinate(), BLACK).isEmpty()){
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
    private LinkedList<Piece> isCheck(Coordinate kingCoord, ChessColor color) {
       
       LinkedList<Piece> givesCheckList = new LinkedList<>();
        
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
                    givesCheckList.add(auxPiece);
            }
       }       
       //check from a rook or queen? 
       for(Direction dir : rookList){
           auxCoord = startCoord.getCoordInDir(dir);
           while(auxCoord!=null && !board.isOccupied(auxCoord)){
               auxCoord = auxCoord.getCoordInDir(dir);
           }
           if(auxCoord!=null){
                auxPiece = board.getPieceOnCoord(auxCoord);
                PieceType PT = auxPiece.getPiecetype();
                if(auxPiece.isColor()!= color && 
                   (PT == ROOK || PT == QUEEN))
                    givesCheckList.add(auxPiece);
            }
       }              
       //check from a knight?
       for(Coordinate possCoord : knightList){
           auxPiece = board.getPieceOnCoord(possCoord);
           if(auxPiece!=null && auxPiece.getPiecetype() == KNIGHT 
                          && auxPiece.isColor()!=color )
               givesCheckList.add(auxPiece);
       }
       //check from a pawn?
       if(color==WHITE){
           auxPiece = board.getPieceOnCoord(startCoord.getCoordInDir(Direction.SW));
           if(auxPiece!=null && auxPiece.getPiecetype()==PAWN 
                   && auxPiece.isColor()==BLACK)
               givesCheckList.add(auxPiece);
           auxPiece = board.getPieceOnCoord(startCoord.getCoordInDir(Direction.SE));
           if(auxPiece!=null && auxPiece.getPiecetype()==PAWN 
                   && auxPiece.isColor()==BLACK)
               givesCheckList.add(auxPiece);           
       }
       else{
           auxPiece = board.getPieceOnCoord(startCoord.getCoordInDir(Direction.NW));
           if(auxPiece!=null && auxPiece.getPiecetype()==PAWN 
                   && auxPiece.isColor()==WHITE)
               givesCheckList.add(auxPiece);
           auxPiece = board.getPieceOnCoord(startCoord.getCoordInDir(Direction.NE));
           if(auxPiece!=null && auxPiece.getPiecetype()==PAWN 
                   && auxPiece.isColor()==WHITE)
               givesCheckList.add(auxPiece);           
       }       
       return givesCheckList;
    }

    protected boolean isCheckMate(ChessColor color){
        
        Piece king = board.getKing(color);
        Coordinate kingCoord = king.getCoordinate();
        LinkedList<Piece> pieceCheckList = isCheck(kingCoord, color);
        LinkedList<Piece> threatensCheckGiver;
        LinkedList<Piece> pieceBlocking;
        Piece givesCheck;
        Coordinate auxCoord;
        
        if(pieceCheckList.isEmpty()) return false;
        //Can the king get out of chess?
        for(Direction dir : Direction.values()){
            auxCoord = kingCoord.getCoordInDir(dir);
            if(auxCoord!=null && !board.isOccupied(auxCoord) && isCheck(auxCoord, color).isEmpty())
                return false;
        }
        //if the king cant move and double check is given -> checkmate
        if(pieceCheckList.size()>=2) return true;
        //is it possible to take the piece giving check?
        givesCheck = pieceCheckList.getFirst();
        threatensCheckGiver = isCheck(givesCheck.getCoordinate(), givesCheck.isColor());
        for(Piece threat : threatensCheckGiver){
            if(validateMove(new Move(threat, givesCheck.getCoordinate(), TAKE,
                                      givesCheck, null), game)) 
            return false;
        }
        //is it possible to block the check from queen, rook, bishop?
        PieceType auxPt = givesCheck.getPiecetype();
        if(auxPt==QUEEN || auxPt==BISHOP || auxPt==ROOK){
            Direction auxDir = kingCoord.
                                    diagonalLineDir(givesCheck.getCoordinate());
            if(auxDir==null) 
                auxDir =kingCoord.straightLineDir(givesCheck.getCoordinate());
            auxCoord = kingCoord.getCoordInDir(auxDir);
            while(!board.isOccupied(auxCoord)){
                pieceBlocking = canCoordBeOccupied(auxCoord, king.isColor());
                for(Piece pieceBL : pieceBlocking){
                    if(validateMove(new Move(pieceBL, auxCoord ,NORMAL), game)) 
                        return false;
                }
                auxCoord = auxCoord.getCoordInDir(auxDir);
            }
        }
    return true;    
    }
    
    private LinkedList<Piece> canCoordBeOccupied(Coordinate coord, ChessColor color){
        //pawns can give check to a field without being able to move to it,
        //also they can go to fields without giving check to the field
        LinkedList<Piece> potentialOccupants = isCheck(coord, color);
        LinkedList<Piece> occupants = canPawnMoveCoord(coord, color);
        
        for(Piece auxPiece : potentialOccupants){
            if(auxPiece.getPiecetype()!= PAWN) occupants.add(auxPiece);
        }
        return occupants;
    }
    
    private void createCastleCoords() {
        castleCoords.add(new Coordinate(0, 1));
        castleCoords.add(new Coordinate(0, 5));
        castleCoords.add(new Coordinate(7, 1));
        castleCoords.add(new Coordinate(7, 5));
    }
    
    //TODO: duplizierter Code
    private LinkedList<Piece> canPawnMoveCoord(Coordinate coord, 
                                                            ChessColor color) {
        
        LinkedList<Piece> pawns = new LinkedList<>();
        Piece auxPiece;
        if(color==WHITE){
            Coordinate auxCoord =coord.getCoordInDir(Direction.S);
            if(auxCoord!=null){ 
               auxPiece= board.getPieceOnCoord(auxCoord);
               if(auxPiece!= null && auxPiece.getPiecetype()==PAWN && 
                                                     auxPiece.isColor()==color)
                   pawns.add(auxPiece);
               
               if(!board.isOccupied(auxCoord)){
               auxCoord = auxCoord.getCoordInDir(Direction.S);
               if(auxCoord!=null){
               auxPiece= board.getPieceOnCoord(auxCoord);
               if(auxPiece!=null && auxPiece.getPiecetype()==PAWN && 
                    auxPiece.isColor()==color && auxPiece.getMoveCounter()==0)
                   pawns.add(auxPiece);
                }}
            }
        }
        else{
            Coordinate auxCoord =coord.getCoordInDir(Direction.N);
            if(auxCoord!=null){ 
               auxPiece= board.getPieceOnCoord(auxCoord);
               if(auxPiece!= null && auxPiece.getPiecetype()==PAWN && 
                                                     auxPiece.isColor()==color)
                   pawns.add(auxPiece);

               if(!board.isOccupied(auxCoord)){               
               auxCoord = auxCoord.getCoordInDir(Direction.N);
               if(auxCoord!=null){
               auxPiece= board.getPieceOnCoord(auxCoord);
               if(auxPiece!=null && auxPiece.getPiecetype()==PAWN && 
                    auxPiece.isColor()==color && auxPiece.getMoveCounter()==0)
                   pawns.add(auxPiece);                   
                }
            }}
        }
    return pawns;
    }

}
