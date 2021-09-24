package chess.coordinate;

import chess.board.ChessColor;
import static chess.board.ChessColor.*;
import static chess.coordinate.Direction.*;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * Provides a two dimensional coordinate of a square on a chess board.
 */
public class Coordinate {
    
    private final int x;
    private final int y;

    /**
     * Class constructor.
     * 
     * @param x     x value of coordinate
     * @param y     y value of coordinate
     */
    public Coordinate(int x, int y) {
        
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the coordinate in given direction of this coordinate. If that
     * coordinate is not on a chess board, null is returned.
     * 
     * @param dir   the direction to go
     * @return      coordinate in given direction
     */
    public Coordinate getCoordInDir(Direction dir) {            
        
        if(this.x+dir.getOffsetX()<0 || this.x+dir.getOffsetX()>7) return null;
        if(this.y+dir.getOffsetY()<0 || this.y+dir.getOffsetY()>7) return null;
        Coordinate newCoord = new Coordinate(this.x+dir.getOffsetX(),
                                                    this.y+dir.getOffsetY()); 
        return newCoord;
        }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * Gets direction to get from this to target coord in a straight line. Only 
     * straight lines are possible (especially no diagnonals are supported), 
     * otherwise null is returned.
     * 
     * @param coordTo   coordinate to get to
     * @return          direction from this to target coordinate
     */
    public Direction straightLineDir(Coordinate coordTo) {
        
        if(this.x == coordTo.x){
            if(this.y > coordTo.y) return Direction.W;
            else return Direction.E;
        }
        if(this.y == coordTo.y){
            if(this.x > coordTo.x) return Direction.N;
            else return Direction.S;            
        }
    return null;
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
        final Coordinate other = (Coordinate) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        return true;
    }

    /**
     * Gets direction to get from this coord to target coord in a diagonal line.
     * If it is not possible to get to target coordinate in a diagonal line,
     * null is returned.
     * 
     * @param coordTo   coordinate to get to
     * @return          direction from this to target coordinate
     */
    public Direction diagonalLineDir(Coordinate coordTo) {
        
        if(abs(this.x-coordTo.x)==abs(this.y-coordTo.y)){
            if(this.x > coordTo.x && this.y > coordTo.y) return NW;
            if(this.x > coordTo.x && this.y < coordTo.y) return NE;            
            if(this.x < coordTo.x && this.y < coordTo.y) return SE;
            if(this.x < coordTo.x && this.y > coordTo.y) return SW;
        } 
        return null;
    }
    
    /**
     * Gets all coordinates a knight could move to from this.
     * 
     * @return  list of coordinates a knight could move to from this
     */
    public List<Coordinate> createKnightCoordinates() {
        
        List<Coordinate> knightList = new LinkedList<>();
        
        /* north east */
        if(isOnBoard(x+1, y+2)) knightList.add(new Coordinate(x+1,y+2)); 
        if(isOnBoard(x+2, y+1)) knightList.add(new Coordinate(x+2,y+1));         
        
        /* north west */
        if(isOnBoard(x-1, y+2)) knightList.add(new Coordinate(x-1,y+2)); 
        if(isOnBoard(x-2, y+1)) knightList.add(new Coordinate(x-2,y+1));   
        
        /* south east */
        if(isOnBoard(x+2, y-1)) knightList.add(new Coordinate(x+2,y-1)); 
        if(isOnBoard(x+1, y-2)) knightList.add(new Coordinate(x+1,y-2));           
                
        /* south west */
        if(isOnBoard(x-2, y-1)) knightList.add(new Coordinate(x-2,y-1));                
        if(isOnBoard(x-1, y-2)) knightList.add(new Coordinate(x-1,y-2));                

        return knightList;    
    }

    /**
     * Calculates the euklidian distance (rounded to whole numbers) between this 
     * and target coordinate.
     * 
     * @param coordTo   target coordinate
     * @return          distance between this and target coordinate
     */
    public int distance(Coordinate coordTo) {
        
        return (int)sqrt(Math.pow(x-coordTo.x,2) + Math.pow(y-coordTo.y,2));
    }

    /**
     * Gets a list of all coordinates that border at the corners of this.
     * 
     * @return    list of all coordinates in diagonal direction
     */
    public ArrayList<Coordinate> getAllDiagCoord(){
        
        ArrayList<Coordinate> list = new ArrayList<>();
        Coordinate auxCoord;
        
        for(Direction dir : Direction.createBishopList()){
            auxCoord = this.getCoordInDir(dir);
            if(auxCoord != null) list.add(auxCoord);
        }
        
        return list;
    }

    /**
     * Gets coordinates directly diagonally in front (depending on color) of
     * this. Can be used to get all coordinates for a pawn capture.
     * 
     * @param color     color to reference which directions mean in front
     * @return          list of coordinates diagonally in front of this
     */
    public ArrayList<Coordinate> getDiagCoordFront(ChessColor color){
        
        ArrayList<Coordinate> list = new ArrayList<>();
        Coordinate auxCoord;
        
        if(color == BLACK){
            auxCoord = this.getCoordInDir(NW);
            if(auxCoord != null) list.add(auxCoord);
            auxCoord = this.getCoordInDir(NE);
            if(auxCoord != null) list.add(auxCoord);
        }
        else{
            auxCoord = this.getCoordInDir(SW);
            if(auxCoord != null) list.add(auxCoord);
            auxCoord = this.getCoordInDir(SE);
            if(auxCoord != null) list.add(auxCoord);
        }
        return list;
    }
    
    /**
     * Gets the coordinate of the rook corresponding to this king coordinate
     * at castling.
     *  
     * @return    coordinate of the rook involved in castling to this king 
     *            coordinate
     */
    public Coordinate getRookCastleCoord(){
        
        if(this.x==0 && this.y==1) return new Coordinate(0,0);
        if(this.x==0 && this.y==5) return new Coordinate(0,7);
        if(this.x==7 && this.y==1) return new Coordinate(7,0);
        if(this.x==7 && this.y==5) return new Coordinate(7,7);
    
        return null;
    }

    /**
     * Translates this to a coordinate that is  point symmetric to the 
     * middle point of the board. This correlates to a rotation of the board by
     * 180 degrees.
     * 
     * @return     mirrored coordinate
     */
    public Coordinate pointSymmCoordinate(){
        return new Coordinate(7-this.x, 7-this.y);
    }

    /**
     * Verifies if this and given coordinate are on a straight line 
     * (file or rank).
     * 
     * @param coordTo   target coordinate
     * @return 
     */
    public Boolean coordinatesOnLine(Coordinate coordTo){
        
        return this.getX()== coordTo.getX()
                    || this.getY()== coordTo.getY();
    }

    /**
     * Verifies if this and given coordinate are on same diagonal.
     * 
     * @param coordTo   target coordinate
     * @return 
     */
    public Boolean coordinatesOnDiag(Coordinate coordTo){
        
        return abs(this.getX()-coordTo.getX())== 
                    abs(this.getY()-coordTo.getY());
    }    

    @Override
    public String toString() {
        return ""+((char) (104-this.y))+(this.x+1);     
    }

    /**
     * Gets coordinate on which capture takes place if a pawn en passants to
     * this (depends on color of pawn).
     * 
     * @param color   color of pawn that en passants
     * @return        coordinate on which capture takes place
     */
    public Coordinate takenCoordEP(ChessColor color) {
        
        if(color==WHITE)return this.getCoordInDir(N);
        else return this.getCoordInDir(S);
    }    

    public Coordinate deepCopy(){
        return new Coordinate (x,y);
    }

    private boolean isOnBoard(int x, int y) {
               
        return !((x > 7 || x < 0) || (y > 7 || y < 0)) ;
    }
}