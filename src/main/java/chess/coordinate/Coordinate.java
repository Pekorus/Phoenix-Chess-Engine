package chess.coordinate;

import chess.board.ChessColor;
import static chess.board.ChessColor.*;
import static chess.coordinate.Direction.*;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Provides a two-dimensional coordinate of a square on a chess board.
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

        int xNew = x + dir.getOffsetX();
        int yNew = y + dir.getOffsetY();

        // out of bounds //
        if(!isOnBoard(xNew, yNew)) return null;

        return new Coordinate(xNew, yNew);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * Gets direction to get from this to target coordinate in a straight
     * line. Orthogonal and diagonal lines are supported. If the two
     * coordinates are not on a line null is returned.
     *
     * @param coordTo   coordinate to get to
     * @return          direction from this to target coordinate
     */
    public Direction lineDir(Coordinate coordTo) {

        /* diagonal line */
        if(abs( x-coordTo.x)==abs( y-coordTo.y)){

            if ( x > coordTo.x){
                if (y > coordTo.y) return NW;
                else return NE;
            }
            else{
                if (y > coordTo.y) return SW;
                else return SE;
            }
        }

        /* straight line */
        if( x == coordTo.x){
            if( y > coordTo.y) return Direction.W;
            else return Direction.E;
        }
        if( y == coordTo.y){
            if( x > coordTo.x) return Direction.N;
            else return Direction.S;
        }

        return null;
    }

    /**
     * Gets direction to get from this to target coordinate in a line
     * a rook or queen could use. If the squares are not on a line
     * null is returned.
     * 
     * @param coordTo   coordinate to get to
     * @return          direction from this to target coordinate
     */
    public Direction orthoLineDir(Coordinate coordTo) {
        
        if( x == coordTo.x){
            if( y > coordTo.y) return Direction.W;
            else return Direction.E;
        }
        if( y == coordTo.y){
            if( x > coordTo.x) return Direction.N;
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
        return this.y == other.y;
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
        
        if(abs( x-coordTo.x)==abs( y-coordTo.y)){

            if ( x > coordTo.x){
                if (y > coordTo.y) return NW;
                else return NE;
            }
            else{
                if (y > coordTo.y) return SW;
                else return SE;
            }
        } 

        return null;
    }
    
    /**
     * Gets all coordinates a knight could move to from this.
     * 
     * @return  list of coordinates a knight could move to from this
     */
    public List<Coordinate> createKnightCoordinates() {
        
        ArrayList<Coordinate> knightList = new ArrayList<>(8);
        
        /* north-east */
        if(isOnBoard(x+1, y+2)) knightList.add(new Coordinate(x+1,y+2)); 
        if(isOnBoard(x+2, y+1)) knightList.add(new Coordinate(x+2,y+1));         
        
        /* north-west */
        if(isOnBoard(x-1, y+2)) knightList.add(new Coordinate(x-1,y+2)); 
        if(isOnBoard(x-2, y+1)) knightList.add(new Coordinate(x-2,y+1));   
        
        /* south-east */
        if(isOnBoard(x+2, y-1)) knightList.add(new Coordinate(x+2,y-1)); 
        if(isOnBoard(x+1, y-2)) knightList.add(new Coordinate(x+1,y-2));           
                
        /* south-west */
        if(isOnBoard(x-2, y-1)) knightList.add(new Coordinate(x-2,y-1));                
        if(isOnBoard(x-1, y-2)) knightList.add(new Coordinate(x-1,y-2));                

        return knightList;    
    }

    /**
     * Calculates the discreet euclidean distance between this coordinate
     * and target coordinate.
     * 
     * @param coordTo   target coordinate
     * @return          distance between this and target coordinate
     */
    public int distance(Coordinate coordTo) {

        return Math.max(Math.abs(x-coordTo.x), Math.abs(y-coordTo.y));

    }

    /**
     * Gets a list of all coordinates that border at the corners of this.
     * 
     * @return    list of all coordinates in diagonal direction
     */
    public ArrayList<Coordinate> getAllDiagCoord(){
        
        ArrayList<Coordinate> list = new ArrayList<>(4);
        Coordinate auxCoord;
        
        for(Direction dir : Direction.createBishopList()){
            auxCoord = getCoordInDir(dir);
            if(auxCoord != null) list.add(auxCoord);
        }
        
        return list;
    }

    /**
     * Gets coordinates directly diagonally in front (depending on color) of
     * this Coordinate. Can be used to get all coordinates for a pawn capture.
     * 
     * @param color     color to reference which directions mean in front
     * @return          list of coordinates diagonally in front of this
     */
    public ArrayList<Coordinate> getDiagCoordFront(ChessColor color){
        
        ArrayList<Coordinate> list = new ArrayList<>(2);
        Coordinate auxCoord;
        
        if(color == BLACK){
            auxCoord = getCoordInDir(NW);
            if(auxCoord != null) list.add(auxCoord);
            auxCoord = getCoordInDir(NE);
            if(auxCoord != null) list.add(auxCoord);
        }
        else{
            auxCoord = getCoordInDir(SW);
            if(auxCoord != null) list.add(auxCoord);
            auxCoord = getCoordInDir(SE);
            if(auxCoord != null) list.add(auxCoord);
        }
        return list;
    }
    
    /**
     * Gets an array with two coordinates for the rook corresponding to this king coordinate
     * at castling. First coordinate represents where the rook stands before castling, second
     * coordinate represents where the rook stands after castling.
     *  
     * @return    coordinate of the rook involved in castling to this king 
     *            coordinate
     */
    public Coordinate[] getRookCastleCoords(){

        if(x==0){
            if(y==1) return new Coordinate[] {new Coordinate(0,0), new Coordinate(0, 2)};
            if(y==5) return new Coordinate[] {new Coordinate(0,7), new Coordinate(0,4)};
        }
        if(x==7){
            if(y==1) return new Coordinate[] {new Coordinate(7,0), new Coordinate(7,2)};
            if(y==5) return new Coordinate[] {new Coordinate(7,7), new Coordinate(7, 4)};
        }
    
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
        return new Coordinate(7-x, 7-y);
    }

    /**
     * Verifies if this and given coordinate are on a straight line 
     * (same file or rank).
     * 
     * @param coordTo   target coordinate
     * @return 
     */
    public Boolean coordinatesOnLine(Coordinate coordTo){
        
        return x == coordTo.getX() || y == coordTo.getY();
    }

    /**
     * Verifies if this coordinate and given coordinate are on same diagonal.
     * 
     * @param coordTo   target coordinate
     * @return 
     */
    public Boolean coordinatesOnDiag(Coordinate coordTo){
        
        return abs(x-coordTo.getX()) == abs(y-coordTo.getY());
    }    

    @Override
    public String toString() {
        return ""+((char) (104-y))+(x+1);
    }

    /**
     * Gets coordinate on which capture takes place if a pawn moves en passant
     * to this coordinate (depends on color of pawn).
     * 
     * @param color   color of pawn
     * @return        coordinate on which capture takes place
     */
    public Coordinate takenCoordEP(ChessColor color) {
        
        if(color==WHITE) return getCoordInDir(N);
        else return getCoordInDir(S);
    }    

    public Coordinate deepCopy(){
        return new Coordinate (x,y);
    }

    private boolean isOnBoard(int x, int y) {
               
        return !((x > 7 || x < 0) || (y > 7 || y < 0)) ;
    }
}