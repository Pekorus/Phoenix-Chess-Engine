/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.ai;

import chess.move.Move;
import java.util.ArrayList;

/**
 *
 * @author Phoenix
 */
public class ChessTreeNode{
    
    private final Move move;
    //private int depth;
    private ArrayList<ChessTreeNode> children;
    
    public ChessTreeNode(Move move/*, int depth*/) {
        this.move = move;
        this.children = new ArrayList<>();
        //this.depth = depth;
    }

    public int getChildCount(){
        return children.size();
    }

    public ChessTreeNode getSubTreeByMove(Move move){
        for(ChessTreeNode child : children){
            if(child.move.equals(move)){
                //child.increaseDepth();
                return child;
            }
        }
        return null;
    }
    
    public void addChildNode(ChessTreeNode node){
        this.children.add(node);
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

    /*public int getDepth() {
        return depth;
    }*/

    public ArrayList<ChessTreeNode> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<ChessTreeNode> children) {
        this.children = children;
    }    
    
    boolean hasChildren() {
       if(children==null) return false;
       if(children.isEmpty()) return false;
       return true;
    }

    /*private void increaseDepth() {
       this.depth += 1;
       if(this.children!=null){
           for(ChessTreeNode child : children){
               child.increaseDepth();
           }
       }
    }*/ 

    @Override
    public String toString() {
        return move.toString();
    }

    /*void setDepth(int depth) {
        this.depth = depth;
        if(this.children!=null){
           for(ChessTreeNode child : children){
               child.setDepth(depth-1);
           }
        }   
    }*/


}
