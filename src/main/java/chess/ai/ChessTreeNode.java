package chess.ai;

import chess.move.Move;
import java.util.ArrayList;
import java.util.List;

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
        children = new ArrayList<>(0);
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
            if(child.move.equals(move)) return child;
        }
        return null;
    }
    
    /**
     * Adds given node to this node as a child node. 
     * 
     * @param move  move to be added as child node
     */
    public void addChildNodes(Move move){

        children.add(new ChessTreeNode(move));
    }

    /**
     * Adds given moves to this node as children.
     *
     * @param moveList list of moves to be added as child
     */
    public void addChildNodes(List<Move> moveList){

        for (Move move : moveList) {
            children.add(new ChessTreeNode(move));
        }
    }

    /**
     * Moves the node with the specified move to front of children list.
     * 
     * @param move  specifies the node that will de sorted to front
     */
    public void moveNodeToFront(Move move){

        for(ChessTreeNode child : children){
            if(child.getMove().equals(move)){
                children.remove(child);
                children.add(0, child);
                break;
            }
        }
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
       return children!=null && !children.isEmpty();
    }

    @Override
    public String toString() {
        return move.toString();
    }
    
}
