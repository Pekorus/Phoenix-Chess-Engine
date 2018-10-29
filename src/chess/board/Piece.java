/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.board;

import chess.coordinate.Coordinate;
import java.util.Objects;

/**
 *
 * @author Phoenix
 */
public class Piece {
    
    private final PieceType piecetype;
    private Coordinate coordinate;
    private final ChessColor color;
    private int moveCounter;
    
    public Piece(PieceType piecetype, ChessColor color, Coordinate coord) {
        this.piecetype = piecetype;
        this.color = color;
        this.coordinate = coord;
        this.moveCounter = 0;
    }
    
    public Coordinate getCoord() {
        return coordinate;
    }

    public int getMoveCounter() {
        return moveCounter;
    }

    public void setMoveCounter(int count) {
        this.moveCounter = count;
    }
    
    public void increaseMoveCounter(){
        this.moveCounter = this.moveCounter+1;
    }
    
    public void decreaseMoveCounter(){
        this.moveCounter = this.moveCounter-1;        
    }
    
    public void setCoord(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public PieceType getPiecetype() {
        return piecetype;
    }

    public ChessColor isColor() {
        return color;
    }

    @Override
    public int hashCode() {
        int hash = 5;
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
        final Piece other = (Piece) obj;
        if (this.color != other.color) {
            return false;
        }
        if (this.moveCounter != other.moveCounter) {
            return false;
        }
        if (this.piecetype != other.piecetype) {
            return false;
        }
        if (!Objects.equals(this.coordinate, other.coordinate)) {
            return false;
        }
        return true;
    }


    
    
}
