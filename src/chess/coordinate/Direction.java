/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.coordinate;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Phoenix
 */
public enum Direction {
    N(-1,0), NE(-1,1), E(0,1), SE(1,1), S(1,0), SW(1,-1), W(0,-1), NW(-1,-1);

    private final int offsetX;
    private final int offsetY;

    private Direction(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public static List<Direction> createBishopList(){
        List<Direction> bishopList = new LinkedList<>();    
            bishopList.add(Direction.NE);
            bishopList.add(Direction.NW);
            bishopList.add(Direction.SE);
            bishopList.add(Direction.SW);
        return bishopList;    
    }

    public static List<Direction> createRookList(){
        List<Direction> rookList = new LinkedList<>();    
            rookList.add(Direction.N);
            rookList.add(Direction.E);
            rookList.add(Direction.S);
            rookList.add(Direction.W);
        return rookList;    
    }
    
    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }
    
    
}
