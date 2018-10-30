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
public class ChessTreeNode {
    
    private Move move;
    private double gameValue;
    private int depth;
    private ChessTreeNode parent;
    private ArrayList<ChessTreeNode> children;
    
    
    public ChessTreeNode(Move move, double gameValue, ChessTreeNode parent) {
        this.move = move;
        this.gameValue = gameValue;
        this.parent = parent;
        if(parent==null) this.depth=0;
        else this.depth = parent.depth+1;
        this.children = new ArrayList<>();
    }

    public int getChildCount(){
        return children.size();
    }

    public ChessTreeNode getSubTreeByMove(Move move){
        for(ChessTreeNode child : children){
            if(child.move.equals(move)) return child;
        }
        return null;
    }
    
    public void addChildNode(ChessTreeNode node){
        children.add(node);
    }

    public void removeChildNode(ChessTreeNode node){
        children.remove(node);
    }

    public Move getMove() {
        return move;
    }

    public double getGameValue() {
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

    public void setGameValue(double gameValue) {
        this.gameValue = gameValue;
    }

    void evaluateNodeMax() {
        double maxValue= -Double.MAX_VALUE;
        for(ChessTreeNode child : children){
           maxValue = max(maxValue, child.getGameValue());
        }
        this.gameValue = maxValue;
    }

    void evaluateNodeMin() {
        double minValue= Double.MAX_VALUE;
        for(ChessTreeNode child : children){
           minValue = min(minValue, child.getGameValue());
        }
        this.gameValue = minValue;        
    }

    
    
}
