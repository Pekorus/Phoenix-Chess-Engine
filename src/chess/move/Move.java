/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.move;

import chess.board.PieceType;
import chess.coordinate.Coordinate;
import static chess.move.MoveType.CASTLE;
import java.util.Objects;

/**
 *
 * @author Phoenix
 */
public class Move {
    
    private final PieceType pieceType;
    private final Coordinate coordFrom;
    private final Coordinate coordTo;
    private final MoveType moveType;
    private final PieceType promoteTo;
    
    public Move(PieceType pieceType, Coordinate coordFrom, Coordinate coordTo, 
                                                           MoveType moveType) {
        this.pieceType = pieceType;
        this.coordFrom = coordFrom;
        this.coordTo = coordTo;
        this.moveType = moveType;
        this.promoteTo = null;
    }

   public Move(PieceType pieceType, Coordinate coordFrom, Coordinate coordTo, 
            MoveType moveType, PieceType promoteTo) {
        this.pieceType = pieceType;
        this.coordFrom = coordFrom;
        this.coordTo = coordTo;
        this.moveType = moveType; 
        this.promoteTo=promoteTo;
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
        if(promoteTo!=null) return ""+coordFrom+moveType.toString()+coordTo+promoteTo;
        return ""+pieceType+coordFrom+moveType.toString()+coordTo;      
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
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
        if (this.promoteTo != other.promoteTo) {
            return false;
        }
        return true;
    }

    
}
