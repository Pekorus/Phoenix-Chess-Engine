/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.ai;

import chess.board.Board;
import chess.board.ChessColor;
import chess.board.Piece;
import static chess.board.PieceType.KING;
import chess.game.ChessGame;
import chess.game.ChessRules;
import chess.game.GameController;
import chess.game.Player;
import chess.move.Move;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Phoenix
 */
public class ChessAI implements Player {

    private static final int SEARCH_DEPTH = 4;
    private static final double FIREPOWER = 0.1; 
    
    private final GameController controller;
    private ChessRules rules;
    private Board board;
    private ArrayList<Piece> ownPieces;
    private ArrayList<Piece> enemyPieces;
    private boolean ownTurn;
    private ChessColor ownColor;
    private ChessTreeNode chessTree;
    
    
    public ChessAI(GameController controller, ChessGame game, ChessColor ownColor) {
        this.controller = controller;
        this.rules = game.getRules();
        //this.board = new Board();
        this.board = game.getBoard();
        this.ownColor = ownColor;
        this.ownPieces = board.getPiecesList(ownColor);
        this.enemyPieces = board.getPiecesList(ownColor.getInverse());
        //create root of tree
        this.chessTree= new ChessTreeNode(null,0, null);
    }
    
    @Override
    public void update(ChessGame game, Move lastMove, Object arg) {
        ownTurn = true;
        if(lastMove!=null){
            board.executeMove(lastMove);
            Move nextMove = findNextMove();
            controller.nextMove(nextMove);
        }
    }    

    private double evaluateBoard(){
        int material, firepower;
    
        material= materialValue(ownPieces)-materialValue(enemyPieces);
        firepower= firePower(ownPieces)-firePower(enemyPieces);
    
        return material+FIREPOWER*firepower;
    }

    private int materialValue(ArrayList<Piece> pieces) {
        int materialValue=0;
        for(Piece piece : pieces){
            switch(piece.getPiecetype()){
                case PAWN:
                    materialValue +=1;                    
                    break;
                case QUEEN:
                    materialValue +=9;
                    break;
                case BISHOP:
                case KNIGHT:
                    materialValue +=3;
                    break;
                case ROOK:
                    materialValue +=5;
                case KING:
                    materialValue +=100;
                    break;
            }
        }
        return materialValue;
    }    

    private int firePower(ArrayList<Piece> pieces) {
        int firePower=0;
        for(Piece piece : pieces){
           if(piece.getPiecetype()!=KING)
               firePower += rules.getPossibleMoves(piece).size();
        }
        return firePower;
    }

    private ArrayList<Move> allPossibleMoves(ArrayList<Piece> pieces) {
        ArrayList<Move> allMoves = new ArrayList<>();
        for(Piece piece : pieces){
            allMoves.addAll(rules.getPossibleMoves(piece));
        }
    return allMoves;
    }

    private Move findNextMove() {
        builtTree(chessTree);
        ArrayList<Move> bestMoves = bestMovesFromTree(chessTree);
        Random random = new Random();
        return bestMoves.get(random.nextInt(bestMoves.size()));
    }

    private void builtTree(ChessTreeNode chessTree) {
        //recursive building of the tree
        if(chessTree.getDepth()<SEARCH_DEPTH){
            ArrayList<Move> allMoves;
            if(ownTurn) allMoves = allPossibleMoves(ownPieces);
            else allMoves= allPossibleMoves(enemyPieces);
            
            //build all nodes for possible moves of current tree (recursive)
            ChessTreeNode newNode;
            for(Move move : allMoves){
                board.executeMove(move);
                flipOwnTurn();
                newNode = new ChessTreeNode(move, 0, chessTree);
                builtTree(newNode);
                chessTree.addChildNode(newNode);
                board.unexecuteMove(move);
                flipOwnTurn();
            }            
            if(ownTurn) chessTree.evaluateNodeMax();
            else chessTree.evaluateNodeMin();
        }
        //depth==SEARCH_DEPTH
        else{
            board.executeMove(chessTree.getMove());
            chessTree.setGameValue(evaluateBoard());
            board.unexecuteMove(chessTree.getMove());
        }
    }

    private void flipOwnTurn() {
        ownTurn = !ownTurn;
    }

    private ArrayList<Move> bestMovesFromTree(ChessTreeNode chessTree) {
        double bestValue = chessTree.getGameValue();
        ArrayList<Move> bestMoves = new ArrayList<>();
        for(ChessTreeNode child : chessTree.getChildren()){
            if(bestValue==child.getGameValue()) 
                bestMoves.add(child.getMove());
        }
        return bestMoves;
    }
    
    
}
