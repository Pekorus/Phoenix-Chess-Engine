/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
 * @author Phoenix
 */
public class Coordinate {
    private final int x;
    private final int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
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

    //gives dir one has to go to get from this coord to target coord in straight line
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

    //gives dir one has to go to get from this coord to target coord in diagonal
    public Direction diagonalLineDir(Coordinate coordTo) {
        if(abs(this.x-coordTo.x)==abs(this.y-coordTo.y)){
            if(this.x > coordTo.x && this.y > coordTo.y) return NW;
            if(this.x > coordTo.x && this.y < coordTo.y) return NE;            
            if(this.x < coordTo.x && this.y < coordTo.y) return SE;
            if(this.x < coordTo.x && this.y > coordTo.y) return SW;
        } 
        return null;
    }
    
    //creates list of all coordinates a knight could jump to from this coordinate
    public List<Coordinate> createKnightCoordinates() {
        List<Coordinate> knightList = new LinkedList<>();
        Coordinate auxCoord, aux2Coord;
        //TODO: get rid of duplicated code
        auxCoord = this.getCoordInDir(Direction.NE);
        if(auxCoord!=null){ 
        aux2Coord = auxCoord.getCoordInDir(Direction.N);        
        if(aux2Coord!=null) knightList.add(aux2Coord);
        aux2Coord = auxCoord.getCoordInDir(Direction.E);
        if(aux2Coord!=null) knightList.add(aux2Coord);
        }
        auxCoord = this.getCoordInDir(Direction.NW);
        if(auxCoord!=null){ 
        aux2Coord = auxCoord.getCoordInDir(Direction.N);        
        if(aux2Coord!=null) knightList.add(aux2Coord);
        aux2Coord = auxCoord.getCoordInDir(Direction.W);
        if(aux2Coord!=null) knightList.add(aux2Coord);
        }
        auxCoord = this.getCoordInDir(Direction.SE);
        if(auxCoord!=null){ 
        aux2Coord = auxCoord.getCoordInDir(Direction.S);        
        if(aux2Coord!=null) knightList.add(aux2Coord);
        aux2Coord = auxCoord.getCoordInDir(Direction.E);
        if(aux2Coord!=null) knightList.add(aux2Coord);
        }        
        auxCoord = this.getCoordInDir(Direction.SW);
        if(auxCoord!=null){ 
        aux2Coord = auxCoord.getCoordInDir(Direction.S);        
        if(aux2Coord!=null) knightList.add(aux2Coord);
        aux2Coord = auxCoord.getCoordInDir(Direction.W);
        if(aux2Coord!=null) knightList.add(aux2Coord);
        }    
    return knightList;    
    }

    public int distance(Coordinate coordTo) {
        return (int)sqrt(Math.pow(this.x-coordTo.x,2)
                                        +Math.pow(this.y-coordTo.y,2));
    }

    public ArrayList<Coordinate> getAllDiagCoord(){
        ArrayList<Coordinate> list = new ArrayList<>();
        Coordinate auxCoord;
        auxCoord = this.getCoordInDir(NW);
        if(auxCoord!=null) list.add(auxCoord);
        auxCoord = this.getCoordInDir(NE);
        if(auxCoord!=null) list.add(auxCoord);
        auxCoord = this.getCoordInDir(SW);
        if(auxCoord!=null) list.add(auxCoord);
        auxCoord = this.getCoordInDir(SE);
        if(auxCoord!=null) list.add(auxCoord);
        
        return list;
    }

    public ArrayList<Coordinate> getDiagCoordFront(ChessColor color){
        ArrayList<Coordinate> list = new ArrayList<>();
        Coordinate auxCoord;
        if(color == BLACK){
            auxCoord = this.getCoordInDir(NW);
            if(auxCoord!=null) list.add(auxCoord);
            auxCoord = this.getCoordInDir(NE);
            if(auxCoord!=null) list.add(auxCoord);
        }
        else{
            auxCoord = this.getCoordInDir(SW);
            if(auxCoord!=null) list.add(auxCoord);
            auxCoord = this.getCoordInDir(SE);
            if(auxCoord!=null) list.add(auxCoord);
        }
        return list;
    }
    
    public Coordinate getRookCastleCoord(){
        if(this.x==0 && this.y==1) return new Coordinate(0,0);
        if(this.x==0 && this.y==5) return new Coordinate(0,7);
        if(this.x==7 && this.y==1) return new Coordinate(7,0);
        if(this.x==7 && this.y==5) return new Coordinate(7,7);
    return null;
    }

    public Coordinate enPassantTake(Coordinate coordTo){
        Direction dir = this.diagonalLineDir(coordTo);
        if(dir==NW) return this.getCoordInDir(W);
        if(dir==NE) return this.getCoordInDir(E);
        if(dir==SW) return this.getCoordInDir(W);
        if(dir==SE) return this.getCoordInDir(E);        
    return null;
    }

    public Coordinate pointSymmCoordinate(){
        return new Coordinate(7-this.x, 7-this.y);
    }

    public Boolean coordinatesOnLine(Coordinate coordTo){
        return this.getX()== coordTo.getX()
                    || this.getY()== coordTo.getY();
    }

    public Boolean coordinatesOnDiag(Coordinate coordTo){
        return abs(this.getX()-coordTo.getX())== 
                    abs(this.getY()-coordTo.getY());
    }    

    @Override
    public String toString() {
        return ""+((char) (104-this.y))+(this.x+1);     
    }

    public Coordinate takenCoordEP(ChessColor color) {
        if(color==WHITE){
            return this.getCoordInDir(N);
        }
        else return this.getCoordInDir(S);
    }    

    public Coordinate deepCopy(){
        return new Coordinate (x,y);
    }
}