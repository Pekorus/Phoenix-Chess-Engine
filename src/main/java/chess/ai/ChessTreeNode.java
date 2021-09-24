package chess.ai;

import chess.move.Move;
import java.util.ArrayList;

/**
 *
 * Provides a traversable tree structure to store all legal moves of a chess 
 * position.
 */
public class ChessTreeNode{
    
    private final Move move;
    private ArrayList<ChessTreeNode> children;

    /**
    * Class constructor.
    * 
    * @param move   move represented by this node
    */
    public ChessTreeNode(Move move) {
        this.move = move;
        this.children = new ArrayList<>();
    }

    /**
     * Gets the number of children of this node.
     * 
     * @return  number of children
     */
    public int getChildCount(){
        return children.size();
    }
    
    /**
     * Gets the subtree of this node that represents given move. Returns null
     * if this node has no child with given move.
     * 
     * @param move  returned node will represent this move
     * @return      node that represents given move
     */
    public ChessTreeNode getSubTreeByMove(Move move){
        for(ChessTreeNode child : children){
            if(child.move.equals(move)){
                return child;
            }
        }
        return null;
    }
    
    /**
     * Adds given node to this node as a child node. 
     * 
     * @param node  node to be added as child
     */
    public void addChildNode(ChessTreeNode node){
        this.children.add(node);
    }

    /**
     * Moves the node with the specified move to front of children list.
     * 
     * @param move  specifies the node that will de sorted to front
     */
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

    @Override
    public String toString() {
        return move.toString();
    }
    
}
