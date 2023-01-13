package chess.coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Provides a set of directions from one square of a chess board to another.
 * Integer values represent the offsets if a cartesian coordinate system is used 
 * where top left is (0,0).
 */
public enum Direction {
    N(-1,0), NE(-1,1), E(0,1), SE(1,1), S(1,0), SW(1,-1), W(0,-1), NW(-1,-1);

    private final int offsetX;
    private final int offsetY;
    
    Direction(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    /**
     * Gets a list of directions a rook could move to on a chess board 
     * (diagonals).
     * 
     * @return  list of directions for a bishop (diagonals)
     */
    public static List<Direction> createBishopList(){
        
        List<Direction> bishopList = new ArrayList<>(4);
        bishopList.add(Direction.NE);
        bishopList.add(Direction.NW);
        bishopList.add(Direction.SE);
        bishopList.add(Direction.SW);
        
        return bishopList;    
    }

    /**
     * Gets a list of directions a rook could move to on a chess board 
     * (straight lines).
     * 
     * @return  list of directions for a rook (straight lines)
     */
    public static List<Direction> createRookList(){
        
        List<Direction> rookList = new ArrayList<>(4);
        rookList.add(Direction.N);
        rookList.add(Direction.E);
        rookList.add(Direction.S);
        rookList.add(Direction.W);
        
        return rookList;    
    }

    /**
     * Gets direction opposite of this.
     * 
     * @return  opposite direction
     */
    public Direction oppositeDir(){
        return switch (this) {
            case N -> S;
            case NE -> SW;
            case E -> W;
            case SE -> NW;
            case S -> N;
            case SW -> NE;
            case W -> E;
            case NW -> SE;
        };
    }
    
    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }
    
    
}
