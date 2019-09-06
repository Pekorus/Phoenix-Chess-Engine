/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.ai;

import chess.move.Move;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.util.ArrayList;

/**
 *
 * @author Phoenix
 */
public class ChessTreeNode implements Comparable{
    
    private Move move;
    private int gameValue;
    private int depth;
    private ChessTreeNode parent;
    private final ArrayList<ChessTreeNode> children;
    
    public ChessTreeNode(Move move, int gameValue, ChessTreeNode parent) {
        this.move = move;
        this.gameValue = gameValue;
        this.parent = parent;
        if(parent==null) this.depth=0;
        else this.depth = parent.depth+ 1;
        this.children = new ArrayList<>();
    }

    public int getChildCount(){
        return children.size();
    }

    public ChessTreeNode getSubTreeByMove(Move move){
        for(ChessTreeNode child : children){
            if(child.move.equals(move)){
                child.decreaseDepth();
                child.parent=null;
                return child;
            }
        }
        return null;
    }
    
    public void addChildNode(ChessTreeNode node){
        children.add(node);
    }

    /* Moves the node with the specified move to front of children list */
    public void moveNodeToFront(Move move){
        if(move != null){
            for(ChessTreeNode child : children){
                if(move.equals(child.getMove())){
                    children.remove(child);
                    children.add(0, child);
                    break;
                }
            }
        }
    }
    
    public void removeChildNode(ChessTreeNode node){
        children.remove(node);
    }

    public Move getMove() {
        return move;
    }

    public int getGameValue() {
        return gameValue;
    }

    public int getDepth() {
        return depth;
    }

    public ChessTreeNode getParent() {
        return parent;
    }

    public ArrayList<ChessTreeNode> getChildren() {
        return children;
    }

    public void setMove(Move move) {
        this.move = move;
    }

    public void setGameValue(int gameValue) {
        this.gameValue = gameValue;
    }

    void evaluateNodeMax() {
        int maxValue= -Integer.MAX_VALUE;
        for(ChessTreeNode child : children){
           maxValue = max(maxValue, child.getGameValue());
        }
        this.gameValue = maxValue;
    }

    void evaluateNodeMin() {
        int minValue= Integer.MAX_VALUE;
        for(ChessTreeNode child : children){
           minValue = min(minValue, child.getGameValue());
        }
        this.gameValue = minValue;        
    }

    boolean hasChildren() {
       if(children==null) return false;
       if(children.isEmpty()) return false;
       return true;
    }

    private void decreaseDepth() {
       this.depth -= 1;
       if(this.children!=null){
           for(ChessTreeNode child : children){
               child.decreaseDepth();
           }
       }
    } 

    @Override
    public int compareTo(Object o) {
       ChessTreeNode node = (ChessTreeNode) o;
       if(this.gameValue< node.gameValue) return 1;
       else if(this.gameValue > node.gameValue) return -1;
       else return 0;
    }
}
