package chess.board;

import chess.coordinate.Coordinate;
import java.util.Objects;

/**
 *
 * Provides a chess piece for a chess game.
 */
public class Piece {
    
    /* type of piece (king, queen, bishop, knight, rook, pawn) */
    private PieceType piecetype;
    /* coordinate of the piece on the chess board */
    private Coordinate coordinate;
    /* color of the pice */
    private final ChessColor color;
    /* counter to determine how many moves the piece already made (used to check
    if castling is possible for kings and rooks, and if pawns can move two 
    squares in their first move) */
    private int moveCounter;
    
    /**
     * Class constructor.
     * 
     * @param piecetype     piecetype of this piece
     * @param color         color of this piece
     * @param coord         coordinate on the board
     * @param moveCounter   counter of moves piece already made
     */
    public Piece(PieceType piecetype, ChessColor color, Coordinate coord, int
            moveCounter) {
        this.piecetype = piecetype;
        this.color = color;
        this.coordinate = coord;
        this.moveCounter = moveCounter;
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
        this.moveCounter++;
    }
    
    public void decreaseMoveCounter(){
        this.moveCounter--;        
    }
    
    public void setCoord(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public PieceType getType() {
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

    public void setPiecetype(PieceType piecetype) {
        this.piecetype = piecetype;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public String toString() {
        return "Piece{" + "piecetype=" + piecetype + ", coordinate=" 
                + coordinate + ", color=" + color + ", moveCounter=" 
                + moveCounter + '}';
    }
    
    public Piece deepCopy(){
        return new Piece(piecetype, color, coordinate.deepCopy(), 
                                                        this.getMoveCounter());
    }
}
