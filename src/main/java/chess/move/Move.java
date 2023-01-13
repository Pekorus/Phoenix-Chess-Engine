package chess.move;

import chess.board.PieceType;
import chess.coordinate.Coordinate;
import static chess.move.MoveType.CASTLE;
import java.util.Objects;

/**
 *
 * Provides a move for a chess game.
 */
public class Move {
    
    /* type of piece to be moved */
    private final PieceType pieceType;
    /* coordinate from which piece is moved */
    private final Coordinate coordFrom;
    /* coordinate to which piece is moved */
    private final Coordinate coordTo;
    /* type of move */
    private final MoveType moveType;
    /* if move represents promotion: type of piece to promote to */
    private final PieceType promoteTo;
    
    /**
     * Class constructor for a move without promotion.
     * 
     * @param pieceType     type of piece to be moved
     * @param coordFrom     coordinate from which piece is moved
     * @param coordTo       coordinate to which piece is moved
     * @param moveType      type of move 
     */
    public Move(PieceType pieceType, Coordinate coordFrom, Coordinate coordTo, 
                                                           MoveType moveType) {
        this.pieceType = pieceType;
        this.coordFrom = coordFrom;
        this.coordTo = coordTo;
        this.moveType = moveType;
        this.promoteTo = null;
    }

    /**
     * Class constructor for a move with promotion.
     * 
     * @param pieceType     type of piece to be moved
     * @param coordFrom     coordinate from which piece is moved
     * @param coordTo       coordinate to which piece is moved
     * @param moveType      type of move
     * @param promoteTo     type of piece to promote to
     */
    public Move(PieceType pieceType, Coordinate coordFrom, Coordinate coordTo, 
            MoveType moveType, PieceType promoteTo) {
        this.pieceType = pieceType;
        this.coordFrom = coordFrom;
        this.coordTo = coordTo;
        this.moveType = moveType; 
        this.promoteTo = promoteTo;
    }
    
    public PieceType getPieceType() {
        return pieceType;
    }

    public Coordinate getCoordFrom() {
        return coordFrom;
    }

    public Coordinate getCoordTo() {
        return coordTo;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public PieceType getPromoteTo() {
        return promoteTo;
    }

    @Override
    public String toString() {
        if(moveType == CASTLE){
           if(coordTo.getY()==1) return "0-0";
           else return "0-0-0";
        }   
        if(promoteTo!=null) return ""+coordFrom+moveType.toString()+
                coordTo+promoteTo;
        return ""+pieceType+coordFrom+moveType.toString()+coordTo;      
    }

    @Override
    public int hashCode() {
        return 3;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Move other = (Move) obj;
        if (this.pieceType != other.pieceType) {
            return false;
        }
        if (!Objects.equals(this.coordFrom, other.coordFrom)) {
            return false;
        }
        if (!Objects.equals(this.coordTo, other.coordTo)) {
            return false;
        }
        if (this.moveType != other.moveType) {
            return false;
        }
        return this.promoteTo == other.promoteTo;
    }

    
}
