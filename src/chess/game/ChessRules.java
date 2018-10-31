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
import static chess.coordinate.Direction.*;
import static chess.game.DrawType.*;
import static chess.move.MoveType.*;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Arrays;
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
        Piece piece = game.getBoard().getPieceOnCoord(move.getCoordFrom());        
        if (piece==null) return false;
        PieceType pieceType = piece.getPiecetype();
        if(pieceType==null) return false;
        ChessColor ownColor= piece.isColor();
        if(ownColor!= game.getPlayersTurn()) return false;
        Coordinate coordFrom = move.getCoordFrom();
        Coordinate coordTo = move.getCoordTo();
        if(coordFrom==null) return false;
        if(coordTo==null) return false;
        if(!coordFrom.equals(piece.getCoord())) return false;
        
        switch(move.getMoveType()){
            case NORMAL:
            if(board.isOccupied(move.getCoordTo())) return false;    
            if(!isMovePossible(move)) return false;

            if(pieceType==PAWN){
                Direction auxDirect=Direction.S; 
                int promoteParameter=7;
                if(ownColor==BLACK){
                    auxDirect = Direction.N;
                    promoteParameter=0;
                }    
                Coordinate sCoord= coordFrom.getCoordInDir(auxDirect);
                Coordinate s2Coord= sCoord.getCoordInDir(auxDirect);
                if(!coordTo.equals(sCoord) && !coordTo.equals(s2Coord)) 
                    return false;
                if(board.isOccupied(sCoord)) return false;
                if(coordTo.equals(s2Coord) && piece.getMoveCounter()!=0) 
                    return false;
                //promotion
                if(move.getPromoteTo()!=null && coordTo.getX()!=promoteParameter) 
                    return false;    
        }    
        break;
            
        case TAKE:
        if(!board.isOccupied(coordTo)) return false;    
        if(board.getPieceOnCoord(coordTo).isColor()== ownColor)
               return false;
        if(!this.isMovePossible(move)) return false;
        //TODO: duplicated code    
        if(pieceType==PAWN){
            if(ownColor==WHITE){
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
        Piece optPiece = game.getBoard().getPieceOnCoord(move.getOptPieceCoord());
        if(optPiece==null) return false;
        if(pieceType!=PAWN) return false;
        if(optPiece.getPiecetype()!=PAWN) return false;
        if(optPiece.isColor()==ownColor) return false;
        if(board.isOccupied(coordTo)) return false;
        if((ownColor==WHITE && coordFrom.getX()!=4)||
                (ownColor==BLACK && coordFrom.getX()!=3)) return false;
        Move lastMove = game.getLastMove();
        if(lastMove.getPieceType()!=PAWN) return false;
        if(!lastMove.getCoordTo().
                                equals(optPiece.getCoord())) return false;
        if(lastMove.getCoordFrom().
                               distance(lastMove.getCoordTo())!=2) return false; 
        break;
            
        case CASTLE:
        if(pieceType!=KING) return false;
        if(piece.getMoveCounter()!=0) return false;
        if(!castleCoords.contains(coordTo)) return false;
        ChessColor enemyColor= ownColor.getInverse();
        if(!isAttackedBy(coordFrom, enemyColor).isEmpty()) return false;        
        Coordinate rookCoord = coordTo.getRookCastleCoord();
        if(!rookCastleCheck(rookCoord)) return false;
        //check if all fields between king and rook are empty and not in check
        Direction dir = coordFrom.straightLineDir(rookCoord);
        Coordinate auxCoord = coordFrom.getCoordInDir(dir);
        for(int i=0; i<2; i++){
            if(board.isOccupied(auxCoord)) return false;
            if(!isAttackedBy(auxCoord, enemyColor).isEmpty()) return false;
            auxCoord = auxCoord.getCoordInDir(dir);
        }
        //large castling
        if(coordTo.getY()==2 && board.isOccupied(auxCoord)) return false;
        break;       
        }
    
        board.executeMove(move);
        Piece king = board.getKing(game.getPlayersTurn());
        if(!isAttackedBy(king.getCoord(), ownColor.getInverse()).isEmpty()){
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
        Direction auxDir;

        if(move.getPieceType()==null) return false;
        switch(move.getPieceType()){
            case KING:
            if(coordFrom.distance(coordTo)>1) return false;
            break;
            
            case QUEEN:
            auxDir = coordFrom.diagonalLineDir(coordTo);
            if(auxDir==null) auxDir = coordFrom.straightLineDir(coordTo);
            if(auxDir==null) return false;
            if(coordsOccupied(coordFrom, coordTo, auxDir)) return false;           
            break;
            
            case BISHOP:
            auxDir = coordFrom.diagonalLineDir(coordTo);
            if(auxDir==null) return false;            
            if(coordsOccupied(coordFrom, coordTo, auxDir)) return false;
            break;            
            
            case KNIGHT:
            int disX = abs(coordFrom.getX()-coordTo.getX());
            int disY = abs(coordFrom.getY()-coordTo.getY());
            if( (disX !=2||disY !=1) && (disX!=1 || disY !=2)) return false;
            break;
            
            case ROOK:
            auxDir = coordFrom.straightLineDir(coordTo);
            if(auxDir==null) return false;
            if(coordsOccupied(coordFrom, coordTo, auxDir)) return false;
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

    /*checks if a given coordinate is attacked by pieces of specified color 
        on current boardstate*/
    private LinkedList<Piece> isAttackedBy(Coordinate checkedCoord, 
                                                            ChessColor color) {
       LinkedList<Piece> attackerList = new LinkedList<>();
       List<Coordinate> knightList = checkedCoord.createKnightCoordinates();
       Coordinate auxCoord;
       Piece auxPiece;

        //attacked by a king?
        if(checkedCoord.distance(board.getKing(color).getCoord())==1)
            attackerList.add(board.getKing(color));

        //attacked by a bishop, rook or queen?
        for(Direction dir : Direction.values()){
           auxCoord = checkedCoord.getCoordInDir(dir);
           while(auxCoord!=null && !board.isOccupied(auxCoord)){
               auxCoord = auxCoord.getCoordInDir(dir);
           }
           if(auxCoord!=null){
                auxPiece = board.getPieceOnCoord(auxCoord);
                PieceType PT = auxPiece.getPiecetype();
                if(auxPiece.isColor()!= color.getInverse()){ 
                   if((checkedCoord.coordinatesOnDiag(auxCoord) && 
                           (PT == BISHOP || PT == QUEEN)) ||
                      (checkedCoord.coordinatesOnLine(auxCoord) &&
                           (PT == ROOK || PT == QUEEN)))        
                    attackerList.add(auxPiece);
                }
            }
       }  
       
       //attacked by a knight?
       for(Coordinate possCoord : knightList){
           auxPiece = board.getPieceOnCoord(possCoord);
           if(auxPiece!=null && auxPiece.getPiecetype() == KNIGHT 
                          && auxPiece.isColor()!=color.getInverse())
               attackerList.add(auxPiece);
       }
       //attacked by a pawn?
       Direction auxDirection1 = Direction.NW;
       Direction auxDirection2 = Direction.NE;
       if(color==BLACK){
           auxDirection1 = Direction.SW;
           auxDirection2 = Direction.SE;
        }
        auxPiece = board.getPieceOnCoord(checkedCoord.getCoordInDir(auxDirection1));        
        for(int i=0; i<2; i++){
            if(auxPiece!=null && auxPiece.getPiecetype()==PAWN 
                   && auxPiece.isColor()==color)
                attackerList.add(auxPiece);
        auxPiece = board.getPieceOnCoord(checkedCoord.getCoordInDir(auxDirection2));
        }             
       return attackerList;
    }

    public boolean isCheckmate(ChessColor color){
        
        Piece king = board.getKing(color);
        Coordinate kingCoord = king.getCoord();
        ChessColor enemyColor = color.getInverse();
        LinkedList<Piece> pieceCheckList = isAttackedBy(kingCoord, 
                                                                    enemyColor);
        LinkedList<Piece> threatensCheckGiver;
        LinkedList<Piece> pieceBlocking;
        Piece givesCheck;
        Coordinate auxCoord;
        
        if(pieceCheckList.isEmpty()) return false;
        //Can the king get out of chess?
        for(Direction dir : Direction.values()){
            auxCoord = kingCoord.getCoordInDir(dir);
            if(auxCoord!=null && (!board.isOccupied(auxCoord)||
                board.getPieceOnCoord(auxCoord).isColor()!=king.isColor()) && 
                             isAttackedBy(auxCoord, enemyColor).isEmpty())
                return false;
        }
        //if the king cant move and double check is given -> checkmate
        if(pieceCheckList.size()>=2) return true;
        //is it possible to take the piece giving check?
        givesCheck = pieceCheckList.getFirst();
        threatensCheckGiver = isAttackedBy(givesCheck.getCoord(), color);
        for(Piece threat : threatensCheckGiver){
            if(validateMove(new Move(threat.getPiecetype(), threat.getCoord(),
                    givesCheck.getCoord(), TAKE,
                                      givesCheck.getCoord(), null), game)) 
            return false;
        }
        //is it possible to block the check from queen, rook, bishop?
        PieceType auxPt = givesCheck.getPiecetype();
        if(auxPt==QUEEN || auxPt==BISHOP || auxPt==ROOK){
            Direction auxDir = kingCoord.
                                    diagonalLineDir(givesCheck.getCoord());
            if(auxDir==null) 
                auxDir =kingCoord.straightLineDir(givesCheck.getCoord());
            auxCoord = kingCoord.getCoordInDir(auxDir);
            while(!board.isOccupied(auxCoord)){
                pieceBlocking = canCoordBeOccupied(auxCoord, king.isColor());
                for(Piece pieceBL : pieceBlocking){
                    if(validateMove(new Move(pieceBL.getPiecetype(), pieceBL.getCoord(),
                                                    auxCoord ,NORMAL), game)) 
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
        LinkedList<Piece> potentialOccupants = isAttackedBy(coord, color);
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
    
    private LinkedList<Piece> canPawnMoveCoord(Coordinate coord, 
                                                            ChessColor color) {
        LinkedList<Piece> pawns = new LinkedList<>();
        Piece auxPiece;
        Direction dir = Direction.S;
        if (color==WHITE) dir = Direction.N;
        
        Coordinate auxCoord =coord.getCoordInDir(dir);
        if(auxCoord!=null){ 
            auxPiece= board.getPieceOnCoord(auxCoord);
            if(auxPiece!= null && auxPiece.getPiecetype()==PAWN && 
                                                    auxPiece.isColor()==color)
                pawns.add(auxPiece);
               
            if(!board.isOccupied(auxCoord)){
                auxCoord = auxCoord.getCoordInDir(dir);
                if(auxCoord!=null){
                    auxPiece= board.getPieceOnCoord(auxCoord);
                    if(auxPiece!=null && auxPiece.getPiecetype()==PAWN && 
                      auxPiece.isColor()==color && auxPiece.getMoveCounter()==0)
                   pawns.add(auxPiece);
                }   
            }            
        }
    return pawns;
    }

    private boolean coordsOccupied(Coordinate coordFrom, Coordinate coordTo, 
                                                            Direction auxDir) {
        Coordinate newCoord = coordFrom.getCoordInDir(auxDir);
        while(!newCoord.equals(coordTo)){
            if(board.isOccupied(newCoord)) return true;
            newCoord = newCoord.getCoordInDir(auxDir);
        } 
    return false;
    }

    public DrawType isDraw(){
        if(isTechnicalDraw()) return TECHNICAL;
        if(isStalemate()) return STALEMATE;
        if(game.getDrawTurnTimer()>=100) return FIFTYTURNS;
        if(isThreeRepetition()) return THREEFOLD;
    return null;
    }

    private boolean isTechnicalDraw() {
        ArrayList<Piece> whitePieces = board.getPiecesList(WHITE);
        ArrayList<Piece> blackPieces = board.getPiecesList(BLACK);        
        int countWhite = whitePieces.size();
        int countBlack = blackPieces.size();
        if(countWhite<=2 && countBlack <=2){
            if(countWhite==1 && countBlack==1) return true;            
            if(countWhite+countBlack==3){
               if(!hasMinorPiece(whitePieces).isEmpty() || 
                                !hasMinorPiece(blackPieces).isEmpty()) 
                   return true;              
            }
        //remaining: both sides have king+piece
        //TODO: king+bishop against king+bishop with same color is techn. draw      
        //TODO: king+2knights against king??
        } 
        return false;
    }

    private boolean isStalemate() {
        ChessColor playersTurn = game.getPlayersTurn();
        if(!isAttackedBy(board.getKing(playersTurn).getCoord(),
                playersTurn.getInverse()).isEmpty())
            return false;
        for(Piece piece : board.getPiecesList(playersTurn)){
            if(!getPossibleMoves(piece).isEmpty()) return false;
        }
    return true;
    }

    private boolean isThreeRepetition() {
        //TODO:
        return false;
    }

    private ArrayList<Piece> hasMinorPiece(ArrayList<Piece> pieceList) {
        ArrayList<Piece> returnList = new ArrayList<>();
        for(Piece piece : pieceList){
            if(piece.getPiecetype()==KNIGHT || piece.getPiecetype()==BISHOP) 
                returnList.add(piece);
            }
    return returnList;
    }

    public ArrayList<Move> getPossibleMoves(Piece piece) {
        
        ArrayList<Move> moveList = new ArrayList<>();
        Coordinate startCoord = piece.getCoord();
        Coordinate auxCoord;
        ChessColor pieceColor = piece.isColor();
        Move auxMove;
        
        switch(piece.getPiecetype()){
            case KING:
            for(Direction dir : Direction.values()){
                auxCoord = startCoord.getCoordInDir(dir);
                if(auxCoord!=null){
                    auxMove=createValidMove(piece, auxCoord, null);
                    if(auxMove!=null) moveList.add(auxMove);
                }
            }
            //Castling
            if(piece.getMoveCounter()==0){
                Direction dir= Direction.E;
                for(int i=0; i<2; i++){
                    auxCoord=startCoord.getCoordInDir(dir).getCoordInDir(dir);
                    auxMove=createValidMove(piece, auxCoord, CASTLE);
                    if(auxMove!=null) moveList.add(auxMove);            
                    dir= Direction.W;
                }
            }
            break;
            
            case QUEEN:   
            List<Direction> queenList = 
                         new ArrayList<>(Arrays.asList(Direction.values()));
            moveList = zoomPieceList(queenList, piece);
            break;

            case BISHOP:
            List<Direction> bishopList = Direction.createBishopList();
            moveList = zoomPieceList(bishopList, piece);
            break;                        

            case ROOK:
            List<Direction> rookList = Direction.createRookList();
            moveList = zoomPieceList(rookList, piece);
            break;    

            case KNIGHT:
            List<Coordinate> knightList = startCoord.createKnightCoordinates();
            for(Coordinate coord : knightList){
                auxMove=createValidMove(piece, coord, null);
                if(auxMove!=null) moveList.add(auxMove);
            }
            break;    
            
            //TODO: EN Passant
            case PAWN:            
            //one step normal move+take
            LinkedList<Direction> pawnDir =new LinkedList<>();
            if(pieceColor==WHITE){ 
                pawnDir.add(S);
                pawnDir.add(SW);
                pawnDir.add(SE);
            }
            else{ 
                pawnDir.add(N);
                pawnDir.add(NW);
                pawnDir.add(NE);
            }
            for(Direction auxDir : pawnDir){
                auxCoord = startCoord.getCoordInDir(auxDir);
                if(auxCoord!=null){
                    //promotion
                    if(auxCoord.getX()==0 || auxCoord.getX()==7){
                        auxMove=createValidMove(piece,auxCoord, QUEEN);
                        if(auxMove!=null){
                            moveList.add(auxMove);
                            moveList.add(createValidMove(piece,auxCoord, BISHOP));
                            moveList.add(createValidMove(piece,auxCoord, KNIGHT));
                            moveList.add(createValidMove(piece,auxCoord, ROOK));
                        }
                    }   
                    else{
                        auxMove=createValidMove(piece,auxCoord, null);
                        if(auxMove!=null) moveList.add(auxMove);
                    }
                }    
            }
            //double step when not moved before
            if(piece.getMoveCounter()==0){
                Coordinate doubleStep = startCoord.getCoordInDir(pawnDir
                                 .getFirst()).getCoordInDir(pawnDir.getFirst());
                auxMove=createValidMove(piece,doubleStep, null);
                if(auxMove!=null) moveList.add(auxMove);               
            }
            break;          
        }
        return moveList;
    }

    private ArrayList<Move> zoomPieceList(List<Direction> dirList, Piece piece) {
        
        ArrayList<Move> returnList = new ArrayList<>();
        Coordinate startCoord = piece.getCoord();
        
        dirList.forEach((dir) -> {
            Coordinate auxCoord = startCoord.getCoordInDir(dir);
            Move auxMove;
            while(auxCoord!=null){
                auxMove=createValidMove(piece, auxCoord, null);
                if(auxMove!=null) returnList.add(auxMove);
                auxCoord = auxCoord.getCoordInDir(dir);
            }
        });
    return returnList;    
    }

    private Move createValidMove(Piece piece, Coordinate coordTo, Object arg) {
        Move createdMove;
        if(coordTo==null) return null;
        
        if(!board.isOccupied(coordTo)){
            if(arg==CASTLE) createdMove=new Move(piece.getPiecetype(),piece.getCoord(),
                    coordTo, CASTLE);
            else createdMove = new Move(piece.getPiecetype(), piece.getCoord(), coordTo, NORMAL, null, 
                                                             (PieceType) arg);
        }
        else{
            if(arg==CASTLE) return null;
            createdMove = new Move(piece.getPiecetype(), piece.getCoord(), coordTo, TAKE, 
                              coordTo, (PieceType) arg);
        }   
    if(validateMove(createdMove, game)) return createdMove;
    return null;
    }
}
