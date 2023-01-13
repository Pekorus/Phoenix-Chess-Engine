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
import static chess.game.DrawType.*;
import chess.move.MoveType;
import static chess.move.MoveType.*;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * Provides a legal move verifier and generator for a regular chess game. This 
 * includes methods to verify a move, check, check mate, draws like stalemate 
 * etc.
 */
public class ChessRules {
    
    /* store coordinates the king moves to in castling */
    private final List<Coordinate> castleCoords = new ArrayList<>(4);
    /* game and board state to verify for */
    private final ChessGame game;
    private final Board board;

    /**
     * Class constructor.
     * 
     * @param game  game to verify rules for
     */
    public ChessRules(ChessGame game) {
        this.game = game;
        this.board = game.getBoard();
        createCastleCoords();
    }

    /**
     * Verifies if given move is legal in game state of this game. Doesn't set
     * promotion field of a move if no promotion is intended. This can return
     * false even if the move would be valid depending on piece type and 
     * coordinates.
     * 
     * @param move  move to be validated
     * @return      true if move is valid
     */
    public boolean validateMove(Move move) {
        
        Piece piece = board.getPieceOnCoord(move.getCoordFrom());
        ChessColor ownColor = piece.getColor();
        
        if (ownColor != game.getPlayersTurn()) {
            return false;
        }
        Coordinate coordFrom = move.getCoordFrom();
        Coordinate coordTo = move.getCoordTo();

        switch (move.getMoveType()) {
            
            case NORMAL:
                
                /* target square is not occupied and own piece must be able to
                    get to target coordinate
                */
                if (board.isOccupied(coordTo) || !isMovePossible(move)) {
                    return false;
                }

                if (move.getPieceType() == PAWN) {
                    
                    Direction auxDirect = ownColor.getFrontDir();
                    
                    Coordinate sCoord = coordFrom.getCoordInDir(auxDirect);
                    /* coordinate in front of pawn blocked => pawn can't move */
                    if (board.isOccupied(sCoord)) {
                        return false;
                    }                    
                    /* target coordinate is not directly in front => must be
                        pawns first move to be valid
                    */
                    if (!coordTo.equals(sCoord)) {
                        
                        /* if target coordinate is not two spaces in front of
                            pawn or the pawn already moved, move is invalid
                        */
                        if (!coordTo.equals(sCoord.getCoordInDir(auxDirect)) || 
                                                piece.getMoveCounter() != 0) {
                            return false;
                        }
                    }
                }
                
                break;

            case TAKE:
                
                /* target coordinate is occupied by enemy piece and own piece
                    must be able to get to target coordinate
                */
                if (!board.isOccupied(coordTo) || !this.isMovePossible(move)
                      || board.getPieceOnCoord(coordTo).getColor() == ownColor) {
                    return false;
                }
                
                /* if capturing piece is a pawn, verify that target coordinate 
                    is a coordinate the pawn can capture on
                */
                if (move.getPieceType() == PAWN && 
                        !coordTo.equals(coordFrom.getCoordInDir(
                                                    ownColor.getPawnCapture1()))
                        && !coordTo.equals(coordFrom.getCoordInDir(ownColor
                                                        .getPawnCapture2()))) {
                        
                        return false;
                }

                break;

            case ENPASSANT:
                
                /* target coordinate not occupied and capturing piece is pawn */
                if (move.getPieceType() != PAWN && board.isOccupied(coordTo)) {
                    return false;                
                }                          
                
                /* get the piece taken by en passant */
                Piece optPiece = game.getBoard().getPieceOnCoord(
                                                coordTo.takenCoordEP(ownColor));
                /* taken piece has to be a pawn of opposite color */
                if (optPiece == null || optPiece.getType() != PAWN || 
                                            optPiece.getColor() == ownColor) {
                    return false;
                }

                /* last move has to be a pawn that moved besides capturing
                    pawn with its first move*/
                Move lastMove = game.getLastMove();
                
                if (lastMove.getPieceType() != PAWN
                        || !lastMove.getCoordTo().equals(optPiece.getCoord())
                        || lastMove.getCoordFrom().distance(
                                                 lastMove.getCoordTo()) != 2) {
                    
                    return false;
                }
                
                break;

            case CASTLE:
                
                /* Moving piece must be king that never moved before and
                   target coordinate has to be in list castleCoords.
                   Verify that rook is on required square and has not 
                   moved before.
                */
                if (move.getPieceType() != KING || piece.getMoveCounter() != 0
                        || !castleCoords.contains(coordTo)
                        || !rookCastleCheck(coordTo.getRookCastleCoords()[0])) {
                    
                    return false;
                }

                /* verify that all squares between king and rook are not 
                    occupied and king is not in check on them
                */
                Coordinate auxCoord = coordFrom;
                
                for (int i = 0; i < 3; i++) {
                    
                    /* i==0 is square with king */
                    if ( i!=0 && board.isOccupied(auxCoord)) return false;
                    /* square attacked by opponents piece */
                    if (isAttacked(auxCoord, ownColor.getInverse(), null)) {
                        return false;
                    }
                    /* get to next coordinate */
                    auxCoord = auxCoord.getCoordInDir(coordFrom
                                                    .orthoLineDir(coordTo));
                }
                /* When castling large, one more square has to be checked */
                if (coordTo.getY() == 5 && board.isOccupied(auxCoord)) {
                    return false;
                }
                
                break;
        }
        
        /* handle pawn promotion */
        if(move.getPieceType() == PAWN && move.getPromoteTo() != null && 
                                coordTo.getX() != ownColor.getPromotionRank()){
            
            return false;   
        }
        
        /* verify king is not in check after move is made */
        return !isCheckAfterMove(move, ownColor);    
    }

    /**
     * Auxiliary method to verify if given move is legal in game state of this 
     * game. This method is designed to only be used in getAllLegalMoves (move
     * generation). It is in functionality equal to validate move but does not
     * verify information already handled by getAllLegalMoves (like if a piece
     * is able to move to a square).
     * 
     * @param move  move to be validated
     * @return      true if move is valid
     */    
    
    private boolean validateMoveAux(Move move){
        
        Coordinate coordTo = move.getCoordTo();
        ChessColor ownColor = board.getPieceOnCoord(move.getCoordFrom())
                                                                    .getColor();
        
        switch(move.getMoveType()){
            
            case NORMAL:
                
                /* Square not occupied and piece can move to is already
                   verified in generateAllLegalMoves. Is also true for 
                   pawns.
                */
                break;
                
            case TAKE:
                
                if (board.getPieceOnCoord(coordTo).getColor() == ownColor) {
                    return false;
                }   
                break;
                
            case ENPASSANT:
                
                /* get the piece taken by en passant */
                Piece optPiece = game.getBoard().getPieceOnCoord(
                                                coordTo.takenCoordEP(ownColor));
                /* taken piece has to be a pawn of opposite color */
                if (optPiece == null || optPiece.getType() != PAWN || 
                                            optPiece.getColor() == ownColor) {
                    return false;
                }

                /* last move has to be a pawn that moved besides capturing
                    pawn with its first move*/
                Move lastMove = game.getLastMove();
                if(lastMove == null) return false;
                
                if (lastMove.getPieceType() != PAWN
                        || !lastMove.getCoordTo().equals(optPiece.getCoord())
                        || lastMove.getCoordFrom().distance(
                                                 lastMove.getCoordTo()) != 2) {
                    
                    return false;
                }
                break;
                
            case CASTLE:

                Coordinate coordFrom = move.getCoordFrom();
                /* Moving piece must be king that never moved before and
                   target coordinate has to be in list castleCoords.
                   Verify that rook is on required square and has not 
                   moved before.
                */
                if (!castleCoords.contains(coordTo)
                        || !rookCastleCheck(coordTo.getRookCastleCoords()[0])) {
                    
                    return false;
                }

                /* verify that all squares between king and rook are not 
                    occupied and king is not in check on them
                */
                Coordinate auxCoord = coordFrom;
                
                for (int i = 0; i < 3; i++) {
                    
                    /* i==0 is square with king */
                    if ( i!=0 && board.isOccupied(auxCoord)) return false;
                    /* square attacked by opponents piece */
                    if (isAttacked(auxCoord, ownColor.getInverse(), null)) {
                        return false;
                    }
                    /* get to next coordinate */
                    auxCoord = auxCoord.getCoordInDir(coordFrom
                                                    .orthoLineDir(coordTo));
                }
                /* When castling large, one more square has to be checked */
                if (coordTo.getY() == 5 && board.isOccupied(auxCoord)) {
                    return false;
                }
                
                break;                
        }
    
        /* verify king is not in check after move is made */
        return !isCheckAfterMove(move, ownColor); 
    }
    
    /**
     * Verifies if the piece type given by move can reach target coordinate
     * from starting coordinate given by move. Does not check pawns, only
     * the other pieces (King, Queen, Bishop, Knight and Rook). Does not verify
     * if target is legit to be captured if there is an opponent's piece on
     * target square.
     * 
     * @param move  move to be verified
     * @return      
     */
    private boolean isMovePossible(Move move) {

        Coordinate coordFrom = move.getCoordFrom();
        Coordinate coordTo = move.getCoordTo();
        Direction auxDir;
        
        switch (move.getPieceType()) {
            
            case KING:
                
                if (coordFrom.distance(coordTo) != 1) {
                    return false;
                }
                
                break;

            case QUEEN:
                
                /* Are coordinates on diagonal? */
                auxDir = coordFrom.diagonalLineDir(coordTo);
                if (auxDir == null) {
                    /* Coordinates not on diagonal. Are Coordinates on line? */
                    auxDir = coordFrom.orthoLineDir(coordTo);
                    /* Coordinates not on diagonal or line */
                    if (auxDir == null) return false;                
                }
                /* verify that no square is occupied between coordFrom and
                    coordTo
                */
                if (coordsOccupied(coordFrom, coordTo, auxDir)) return false;
                
                break;

            case BISHOP:
                
                /* Are coordinates on diagonal? */
                auxDir = coordFrom.diagonalLineDir(coordTo);
                if (auxDir == null) return false;
                /* verify that no square is occupied between coordFrom and
                    coordTo
                */
                if (coordsOccupied(coordFrom, coordTo, auxDir)) return false;
                
                break;

            case KNIGHT:
                
                /* All squares a knight can reach have a distance 2 in one 
                    cardinal direction and distance 1 in another one 
                */
                int disX = abs(coordFrom.getX() - coordTo.getX());
                int disY = abs(coordFrom.getY() - coordTo.getY());
                if ((disX != 2 || disY != 1) && (disX != 1 || disY != 2)) {
                    return false;
                }
                
                break;

            case ROOK:
                
                /* Are coordinates on line? */
                auxDir = coordFrom.orthoLineDir(coordTo);
                if (auxDir == null) return false;
                /* verify that no square is occupied between coordFrom and
                    coordTo
                */
                if (coordsOccupied(coordFrom, coordTo, auxDir)) return false;
                
                break;

            case PAWN:
                /* is coded in validateMove because move pattern of pawn is
                    depending on if he already moved, its color and if it is a 
                    regular move or a capture
                */                               
                break;
        }
        
        return true;
    }

    /**
     * Verifies if a rook is on given coordinate and if it can be used for 
     * castling (rook never moved before).
     * 
     * @param rookCoord     coordinate to be verified
     * @return 
     */
    private boolean rookCastleCheck(Coordinate rookCoord) {
        
        Piece rook = board.getPieceOnCoord(rookCoord);
        
        return rook != null && rook.getType() == ROOK 
                                                 && rook.getMoveCounter() == 0;
    }

    /**
     * Gets list of all attacking pieces of given color for a given coordinate
     * on current board state.
     * 
     * @param checkedCoord  all pieces in returned list attack this coordinate
     * @param color         color of attacking pieces
     * @return              all pieces of given color that attack coordinate
     * 
     */
    private ArrayList<Piece> attackedBy(Coordinate checkedCoord, 
                                                            ChessColor color) {
        
        ArrayList<Piece> attackerList = new ArrayList<>(4);
        Coordinate auxCoord;
        Piece auxPiece;

        /* attacked by a king */
        if (checkedCoord.distance(board.getKingCoord(color)) == 1) {
            attackerList.add(board.getKing(color));
        }

        /* attacked by a bishop, rook or queen */
        for (Direction dir : Direction.values()) {
            
            /* Search extends  in all directions to cover all diagonals and 
                lines. */
            auxCoord = checkedCoord.getCoordInDir(dir);
            
            /* Search until an occupied square is found, then end search for 
                this direction.
            */
            while (auxCoord != null) {
                
                auxPiece = board.getPieceOnCoord(auxCoord);
                
                /* auxPiece not null => piece found */
                if(auxPiece != null){
                   
                    PieceType PT = auxPiece.getType();
                        
                    if (auxPiece.getColor() == color) {
                        /* Verify if coordinates are on diagonal and piece
                                type is bishop or queen. Also verify if 
                                coordinates are on a line and piece type is 
                                rook or queen. 
                         */
                        if ((checkedCoord.coordinatesOnDiag(auxCoord)
                                && (PT == BISHOP || PT == QUEEN))
                                || (checkedCoord.coordinatesOnLine(auxCoord)
                                && (PT == ROOK || PT == QUEEN))) {

                            attackerList.add(auxPiece);
                        }
                    }
                    
                    break;                    
                }                
                /* extend further into this direction if no piece was found */
                auxCoord = auxCoord.getCoordInDir(dir);
            }

        }

        /* attacked by knight */
        for (Coordinate possCoord : checkedCoord.createKnightCoordinates()) {
            
            /* Get piece and add to list if it is a knight in matching color */
            auxPiece = board.getPieceOnCoord(possCoord);
            if (auxPiece != null && auxPiece.getType() == KNIGHT
                    && auxPiece.getColor() == color) {
                
                attackerList.add(auxPiece);
            }
        }
        
        /* attacked by pawn */        
        auxPiece = board.getPieceOnCoord(checkedCoord.getCoordInDir(color
                                             .getInverse().getPawnCapture1()));
        /* Verify that piece is pawn of matching color */
        if (auxPiece != null && auxPiece.getType() == PAWN 
                                              && auxPiece.getColor() == color) {
                attackerList.add(auxPiece);
        }
        auxPiece = board.getPieceOnCoord(checkedCoord.getCoordInDir(color
                                             .getInverse().getPawnCapture2()));
        if (auxPiece != null && auxPiece.getType() == PAWN 
                                              && auxPiece.getColor() == color) {
                attackerList.add(auxPiece);
        }        
        
        
        return attackerList;
    }
    /**
     * Verifies if given coordinate is attacked by pieces of given color on 
     * current board state. A piece on coordinate ignore will be ignored in the
     * search, that means the method treats this square as empty. If no piece
     * has to be ignored, set ignore as null.
     * 
     * @param checkedCoord  coordinate to be checked
     * @param color         color of attacking pieces
     * @param ignore        piece on this coordinated will be ignored
     * @return              
     * 
     */
    private boolean isAttacked(Coordinate checkedCoord, 
                                          ChessColor color, Coordinate ignore){
    
        Coordinate auxCoord;
        Piece auxPiece;

        /* attacked by a king */
        if (checkedCoord.distance(board.getKingCoord(color)) == 1) {
            return true;
        }

        /* attacked by a bishop, rook or queen */
        for (Direction dir : Direction.values()) {
            
            /* Search extends  in all directions to cover all diagonals and 
                lines. */
            auxCoord = checkedCoord.getCoordInDir(dir);
            
            /* Search until an occupied square is found, then end search for 
                this direction.
            */
            while (auxCoord != null) {
                
                auxPiece = board.getPieceOnCoord(auxCoord);
                
                /* auxPiece not null => piece found */
                if(auxPiece != null){
                    
                    /* for mode == true: Ignore piece found if it is king of
                        enemy color. (In case of a check on that king, the king
                        can't move on the square in this direction, he would 
                        still be in check.)
                       for mode == false: if-statement is always true 
                    */
                    if(!auxCoord.equals(ignore)){
                        
                        PieceType PT = auxPiece.getType();
                        
                        if (auxPiece.getColor() == color) {
                            /* Verify if coordinates are on diagonal and piece
                                type is bishop or queen. Also verify if 
                                coordinates are on a line and piece type is 
                                rook or queen. 
                            */
                            if ((checkedCoord.coordinatesOnDiag(auxCoord)
                                && (PT == BISHOP || PT == QUEEN))
                                || (checkedCoord.coordinatesOnLine(auxCoord)
                                && (PT == ROOK || PT == QUEEN))) {
                                    
                                return true;
                            }
                        }
                        
                        break;
                    }
                }
                
                /* extend further into this direction */
                auxCoord = auxCoord.getCoordInDir(dir);
            }

        }

        /* attacked by knight */
        for (Coordinate possCoord : checkedCoord.createKnightCoordinates()) {
            
            /* Get piece and add to list if it is a knight in matching color */
            auxPiece = board.getPieceOnCoord(possCoord);
            if (auxPiece != null && auxPiece.getType() == KNIGHT
                    && auxPiece.getColor() == color) {
                
                return true;
            }
        }
        
        /* attacked by pawn */        
        auxPiece = board.getPieceOnCoord(checkedCoord.getCoordInDir(color
                                             .getInverse().getPawnCapture1()));
        /* Verify that piece is pawn of matching color */
        if (auxPiece != null && auxPiece.getType() == PAWN 
                                              && auxPiece.getColor() == color) {
                return true;
        }
        auxPiece = board.getPieceOnCoord(checkedCoord.getCoordInDir(color
                                             .getInverse().getPawnCapture2()));

        return auxPiece != null && auxPiece.getType() == PAWN
                && auxPiece.getColor() == color;

    }
    
    /**
     * Verifies if player with given color is checkmate on current board state.
     * 
     * @param color     color of player to be verified
     * @return          
     */
    public boolean isCheckmate(ChessColor color) {

        Coordinate kingCoord = board.getKingCoord(color);
        ChessColor enemyColor = color.getInverse();

        /* Get all pieces that attack the king */
        ArrayList<Piece> pieceCheckList = attackedBy(kingCoord, enemyColor);

        /* If no piece attacks king => no check mate */
        if (pieceCheckList.isEmpty()) return false;

        Coordinate auxCoord;

        /* Can king get out of chess? */
        for (Direction dir : Direction.values()) {
            
            auxCoord = kingCoord.getCoordInDir(dir);
            if (auxCoord != null && (!board.isOccupied(auxCoord)
                    || board.getPieceOnCoord(auxCoord).getColor() != color)
                    && !isAttacked(auxCoord, enemyColor, kingCoord)) {
                /* if king moves, he could have blocked the attack from R,Q or B
                for the new field which he isn't after moving, so isAttacked
                has to ignore king */
                return false;
            }
        }
       
        /* if king can't move and double check is given => checkmate */
        if (pieceCheckList.size() >= 2) {
            return true;
        }
        
        /* Is it possible to take the piece giving check? */
        Piece givesCheck = pieceCheckList.get(0);
        ArrayList<Piece>  threatensCheckGiver = 
                                       attackedBy(givesCheck.getCoord(), color);
        for (Piece threat : threatensCheckGiver) {
            /* If king is not in check after taking piece giving check 
                => no checkmate
            */
            if (!isCheckAfterMove(new Move(threat.getType(), threat.getCoord(),
                    givesCheck.getCoord(), TAKE), color)) {
                return false;
            }
        }
        
        /* Is it possible to block the check from queen, rook or bishop?
        *   (Check from pawn or knight can't be blocked). */
        PieceType auxPt = givesCheck.getType();
        if (auxPt == QUEEN || auxPt == BISHOP || auxPt == ROOK) {

            /* Get direction from king to piece giving check */
            Direction auxDir = kingCoord.lineDir(givesCheck.getCoord());
            
            /* Verify if a piece can move to a square between king and piece 
                giving check
            */
            auxCoord = kingCoord.getCoordInDir(auxDir);
            while (!board.isOccupied(auxCoord)) {
                
                for (Piece blocker : attackedBy(auxCoord, color)) {

                    PieceType blockerType = blocker.getType();

                    if (blockerType == KING || blockerType == PAWN) continue;
                    /* verify that blocking check doesn't get king into another
                        check or the blocking piece is a pawn (pawns can't go to
                        squares they "attack" if there is no enemy piece)
                    */
                    if (!isCheckAfterMove(new Move(blockerType,
                            blocker.getCoord(), auxCoord, NORMAL), color)) {
                        return false;
                    }
                }

                /* Pawns can move to a square without attacking it. */
                Piece pawn = getPawnMoveCoord(auxCoord, color);
                if (!(pawn == null || isCheckAfterMove(new Move(PAWN, pawn.
                                       getCoord(), auxCoord, NORMAL), color))) {                    
                    return false;
                }

                auxCoord = auxCoord.getCoordInDir(auxDir);
            }
        }
        return true;
    }

    /**
     * Adds all coordinates a king can castle to into castleCoords field.
     */
    private void createCastleCoords() {
        castleCoords.add(new Coordinate(0, 1));
        castleCoords.add(new Coordinate(0, 5));
        castleCoords.add(new Coordinate(7, 1));
        castleCoords.add(new Coordinate(7, 5));
    }

    /**
     * Gets the pawn of given color that can move to target coordinate in 
     * current board state. If there is no pawn that can do that, null is 
     * returned. Only normal moves are considered, no captures.
     * 
     * @param targetCoord   coordinate to move to
     * @param color         color of pawn
     * @return              pawn that can move to target coordinate
     */
    private Piece getPawnMoveCoord(Coordinate targetCoord, ChessColor color) {
        
        Piece auxPiece;
        /* Search in opposite direction of pawn move direction */
        Direction dir = color.getFrontDir().oppositeDir();
        
        /* Verify if there is a pawn on coordinate that can move to target 
            coordinate */
        Coordinate auxCoord = targetCoord.getCoordInDir(dir);        
        /* Do two steps in Direction dir to search for pawns */
        for(int i=0; i<2; i++){
            /* Coordinate exists */
            if(auxCoord == null) break;
            else {                
                auxPiece = board.getPieceOnCoord(auxCoord);
                if (auxPiece != null){
                    /* Return piece if it is pawn of matching color */
                    if(auxPiece.getType() == PAWN && 
                                                  auxPiece.getColor() == color){
                        /* If pawn is 2 squares away, it has to be its first
                           move to be able to move to target coordinate
                        */
                        if(i==1 && auxPiece.getMoveCounter() != 0) return null;
                        return auxPiece;
                    }                
                    /* If the coordinate is occupied but not by a pawn of same
                       color, then a pawn move to target coordinate is 
                       impossible.
                    */
                    return null;
                }
                if(i==0) auxCoord = auxCoord.getCoordInDir(dir);
            }
        }
        return null;
    }

    private boolean coordsOccupied(Coordinate coordFrom, Coordinate coordTo,
            Direction auxDir) {
        Coordinate newCoord = coordFrom.getCoordInDir(auxDir);
        while (!newCoord.equals(coordTo)) {
            if (board.isOccupied(newCoord)) {
                return true;
            }
            newCoord = newCoord.getCoordInDir(auxDir);
        }
        return false;
    }

    /**
     * Verifies if current game state is a draw and gets the draw type. Argument 
     * "mode" controls if stalemate is considered (False gives better 
     * performance if stalemate validation is not needed). Returns null if game 
     * is not draw, type of draw otherwise.
     * 
     * @param mode  controls validation of stalemate
     * @return      type of draw found or null
     */
    public DrawType isDraw(boolean mode) {
        
        if (game.getDrawTurnTimer() >= 100) return FIFTYTURNS;
        if (isThreeRepetition()) return THREEFOLD;
        if (isTechnicalDraw()) return TECHNICAL;
        if (mode && isStalemate()) return STALEMATE;
        
        return null;
    }

    /**
     * Verifies if current board state is a technical draw. Technical draws in
     * chess are positions in which both sides can't mate anymore.
     * 
     * @return 
     */
    private boolean isTechnicalDraw() {
        
        /* If any side has more than two pieces => no technical draw
            (fast return to improve performance on boards with lots of
            pieces).
        */      
        if (board.getPiecesList(WHITE).size() > 2) return false;
        if (board.getPiecesList(BLACK).size() > 2) return false;

        ArrayList<Piece> whitePieces = board.getPiecesList(WHITE);
        ArrayList<Piece> blackPieces = board.getPiecesList(BLACK);        
        
        int countWhite = whitePieces.size();
        int countBlack = blackPieces.size();

        /* Both sides only have king => draw */
        if (countWhite == 1 && countBlack == 1) {
            return true;
        }
        /* One side only has king, the other side king and another piece. If
           that other piece is a minor piece => draw 
        */
        if (countWhite + countBlack == 3) {
            if ( getMinorPiece(whitePieces) != null
                    || getMinorPiece(blackPieces)!= null) {
                return true;
            }
        }
        
        /* With 4 pieces on the board, only the combination of king and bishop
           for both sides, with the bishops being on same colored squares is a
           technical draw. In all other positions it is theoretically possible 
           to mate (but some combinations don't allow forcing mate).
        */
        if(countWhite == 2 && countBlack == 2){
            
            Piece minorWhite = getMinorPiece(whitePieces);
            Piece minorBlack = getMinorPiece(blackPieces);
            
            if(minorWhite != null && minorBlack != null){
                
                if(minorWhite.getType() == BISHOP 
                                        && minorBlack.getType() == BISHOP){
                    /* Coordinates of a square on a chess board have the color
                       of the square coded in them. Even sum of x and y means
                       that the square is white, odd sum means that it is black.
                       By comparing the sums of both bishops, it is possible to
                       determine if both bishops are on the same square color. 
                    */
                    return (minorWhite.getCoord().getX()
                            + minorWhite.getCoord().getY()) % 2
                            == (minorBlack.getCoord().getX()
                            + minorBlack.getCoord().getY()) % 2;
                }
            }
        }
       
        return false;
    }

    /**
     * Verifies if current game state is a stalemate.
     * 
     * @return 
     */
    public boolean isStalemate() {
        
        for (Piece piece : board.getPiecesList(game.getPlayersTurn())) {
            if (!getAllLegalMoves(piece).isEmpty()) {
                return false;
            }
        }
        return !isInCheck(game.getPlayersTurn());
    }

    /**
     * Verifies if current game state is a draw by threefold repetition.
     * 
     * @return 
     */
    private boolean isThreeRepetition() {
        
        LinkedList<Long> positions = game.getRecentPositions();
        /* Retrieve hash value of last position and remove it from list */
        long hashValue = positions.removeLast();
        /* If the value isn't in the list anymore => no repetition */
        if (!positions.contains(hashValue)) {           
            
            positions.add(hashValue);
            return false;
        }
        /* Position repeated at least once. If first occurrence of hash value
            != last occurrence of hash value => repeated three times */
        boolean ret = positions.indexOf(hashValue) 
                                           != positions.lastIndexOf(hashValue);
        positions.add(hashValue);
        
        return ret;
    }

    /**
     * Gets the first minor piece (bishop or knight) of given piece list. If no 
     * minor pieces are in the list, null is returned.
     * 
     * @param pieceList     list to be examined
     * @return              first minor piece found in list
     */
    private Piece getMinorPiece(ArrayList<Piece> pieceList) {

        for (Piece piece : pieceList) {
            
            if (piece.getType() == KNIGHT || piece.getType() == BISHOP) {
                return piece;
            }
        }
        return null;
    }

    /**
     * Gets all legal moves for given piece.
     * 
     * @param piece     piece to generate legal moves for
     * @return          all legal moves for given piece
     */
    public ArrayList<Move> getAllLegalMoves(Piece piece) {

        Move auxMove;
        Coordinate auxCoord;
        
        switch (piece.getType()) {
            
            case KING:
                
                ArrayList<Move> moveList = new ArrayList<>(8);
                Coordinate kingCoord = piece.getCoord();
                /* move in all directions */               
                for (Direction dir : Direction.values()) {

                    auxCoord = kingCoord.getCoordInDir(dir);
                    if (auxCoord != null) {
                        auxMove = new Move(KING, kingCoord, auxCoord,
                                                     getTypeToCoord(auxCoord));
                        /* add move if it is legal */
                        if (validateMoveAux(auxMove)) moveList.add(auxMove);
                    }
                }
                /* castling */
                if (piece.getMoveCounter() == 0) {
                    
                    auxMove = new Move(KING, kingCoord, new Coordinate(kingCoord
                            .getX(), kingCoord.getY()+2), CASTLE);
                    if (validateMoveAux(auxMove)) moveList.add(auxMove);
                    
                    auxMove = new Move(KING, kingCoord, new Coordinate(kingCoord
                            .getX(), kingCoord.getY()-2), CASTLE);    
                    if (validateMoveAux(auxMove)) moveList.add(auxMove);
                }
                
                return moveList;

            case QUEEN:
                
                return zoomPieceList(Arrays.asList(Direction.values()), piece);

            case BISHOP:
                
                return zoomPieceList(Direction.createBishopList(), piece);

            case ROOK:
                
                return zoomPieceList(Direction.createRookList(), piece);

            case KNIGHT:
                
                ArrayList<Move> knightList = new ArrayList<>(8);
                for (Coordinate coord : piece.getCoord()
                                                  .createKnightCoordinates()) {
                    
                    auxMove = new Move(KNIGHT, piece.getCoord(), coord, 
                                                        getTypeToCoord(coord));
                    if (validateMoveAux(auxMove)) knightList.add(auxMove);
                }
                return knightList;

            case PAWN:
                
                ArrayList<Move> pawnList = new ArrayList<>(4);
                Coordinate pawnCoord = piece.getCoord();                 
                ChessColor color = piece.getColor();
                
                /* one step and capture */
                ArrayList<Direction> pawnDir = new ArrayList<>(3);
                pawnDir.add(color.getFrontDir());
                pawnDir.add(color.getPawnCapture1());
                pawnDir.add(color.getPawnCapture2());
               
                for (Direction auxDir : pawnDir) {
                    
                    auxCoord = pawnCoord.getCoordInDir(auxDir);
                    /* coordinate exists */
                    if (auxCoord != null) {
                        
                        /* determine move type */
                        MoveType type = null;
                        if(auxDir == pawnDir.get(0)){
                            if(!board.isOccupied(auxCoord)) type = NORMAL;
                        }
                        else if(board.isOccupied(auxCoord)) type = TAKE;
                        
                        if(type != null){
                            /* promotion */                            
                            if ((auxCoord.getX() == 0 || auxCoord.getX() == 7)){
                            
                                auxMove = new Move(PAWN, pawnCoord, auxCoord, 
                                              type, QUEEN);
                                if (validateMoveAux(auxMove)) {
                                
                                    pawnList.add(auxMove);
                                    /* if promotion to queen is valid, all other
                                        promotions are also valid 
                                    */
                                    pawnList.add(new Move(PAWN, pawnCoord, 
                                            auxCoord, type, ROOK));                                                                
                                    pawnList.add(new Move(PAWN, pawnCoord, 
                                            auxCoord, type, BISHOP));
                                    pawnList.add(new Move(PAWN, pawnCoord, 
                                            auxCoord, type, KNIGHT));
                                }
                            /* not promotion */    
                            } else {
                            
                                auxMove = new Move(PAWN, pawnCoord, 
                                                              auxCoord, type);
                                if (validateMoveAux(auxMove)) pawnList
                                                                  .add(auxMove);
                            }
                        }
                    }
                }
                /* double step when not moved before */
                if (piece.getMoveCounter() == 0) {
                    
                    if(color == WHITE){
                        /* Double step for white is only possible from rank 1 
                           to rank 3, for black only from rank 6 to rank 4.
                        */
                        auxCoord = new Coordinate(3, pawnCoord.getY());
                        /* verify both squares in front of pawn not occupied */
                        if(!board.isOccupied(auxCoord) && !board.isOccupied(
                                        new Coordinate(2, pawnCoord.getY()))){ 
                            auxMove = new Move(PAWN, pawnCoord, auxCoord, 
                                                                        NORMAL);
                        if (validateMoveAux(auxMove)) pawnList.add(auxMove);
                        }  
                    }
                    else{ 
                        auxCoord = new Coordinate(4, pawnCoord.getY());
                        /* verify both squares in front of pawn not occupied */
                        if(!board.isOccupied(auxCoord) && !board.isOccupied(
                                        new Coordinate(5, pawnCoord.getY()))){                                                 
                            auxMove = new Move(PAWN, pawnCoord, auxCoord, 
                                                                        NORMAL);
                        if (validateMoveAux(auxMove)) pawnList.add(auxMove);                        
                        }
                    }

                }

                /* en passant */
                if (board.enPassantPossible()) {

                    auxCoord = pawnCoord.getCoordInDir(color.getPawnCapture1());
                    if (auxCoord != null) {
                        auxMove = new Move(PAWN, pawnCoord, auxCoord, ENPASSANT);
                        if (validateMoveAux(auxMove)) pawnList.add(auxMove);
                    }

                    auxCoord = pawnCoord.getCoordInDir(color.getPawnCapture2());
                    if (auxCoord != null) {
                        auxMove = new Move(PAWN, pawnCoord, auxCoord, ENPASSANT);
                        if (validateMoveAux(auxMove)) pawnList.add(auxMove);
                    }
                }        
                
                return pawnList;
        }        
        
        return null;
    }

    /**
     * Gets all legal moves for given piece in a straight line or diagonal in
     * all directions given by dirList. Can be used to get all moves for Queen,
     * Rook or Bishop. Does not verify if directions are allowed for that piece!
     * Should be verified before calling method (by giving appropriate direction
     * list).
     * 
     * @param dirList   directions to search moves for
     * @param piece     generates moves for this piece
     * @return          all moves in a straight line or diagonal
     */
    private ArrayList<Move> zoomPieceList(List<Direction> dirList, Piece piece){

        ArrayList<Move> returnList = new ArrayList<>();
        Coordinate startCoord = piece.getCoord();
        Coordinate auxCoord;
        Move auxMove;
        
        for(Direction dir : dirList){
            
            auxCoord = startCoord.getCoordInDir(dir);
            /* coordinate exists */
            while (auxCoord != null) {
                
                auxMove = new Move(piece.getType(), startCoord, auxCoord, 
                                                    getTypeToCoord(auxCoord));
                if (validateMoveAux(auxMove)) returnList.add(auxMove);
                /* stop searching this direction, if a piece is on square */
                if(auxMove.getMoveType() == TAKE) break;
                /* proceed further in this direction */
                auxCoord = auxCoord.getCoordInDir(dir);
            }
        }
        
        return returnList;
    }

    /**
     * Gets the type of move (NORMAL or TAKE) for given coordinate. Returns TAKE if square is
     * occupied, NORMAL otherwise.
     * 
     * @param coordTo   coordinate to move to
     * @return          move type (normal or take)
     */
    private MoveType getTypeToCoord(Coordinate coordTo) {

        if (board.isOccupied(coordTo)) return TAKE;
        else return NORMAL;
    
    }  
    
    /**
     * Verifies if the player controlling given color is in check.
     * 
     * @param color     color of the player to verify
     * @return
     */
    public boolean isInCheck(ChessColor color) {
        
        return isAttacked(board.getKingCoord(color), color.getInverse(), null);
    }

    /**
     * Verifies if the player controlling given color is in check after given
     * move is executed.
     * 
     * @param move      move to be executed
     * @param color     color of the player to verify
     * @return 
     */
    private boolean isCheckAfterMove(Move move, ChessColor color) {
        
        board.executeMove(move);
        
        if (isInCheck(color)) {
            board.unexecuteMove(move);
            return true;
        }
        board.unexecuteMove(move);
        
        return false;    
    }
}
