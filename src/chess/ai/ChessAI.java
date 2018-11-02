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
import static chess.board.PieceType.PAWN;
import chess.game.ChessGame;
import chess.game.ChessRules;
import chess.game.GameController;
import chess.game.Player;
import chess.move.Move;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Phoenix
 */
public class ChessAI implements Player {

    private static final int SEARCH_DEPTH = 4;
    private static final double FIREPOWER = 0.01; 
    private int evaluatedPositions=0;
    
    private final GameController controller;
    private final ChessGame ownGame;
    private ChessRules rules;
    private Board board;
    private ArrayList<Piece> ownPieces;
    private ArrayList<Piece> enemyPieces;
    private ChessColor ownColor;
    private ChessTreeNode currentTree;
    
    
    public ChessAI(GameController controller, ChessColor ownColor) {
        this.controller = controller;
        this.ownGame = new ChessGame();
        this.board = ownGame.getBoard();
        this.rules = ownGame.getRules();        
        this.ownColor = ownColor;
        this.ownPieces = board.getPiecesList(ownColor);
        this.enemyPieces = board.getPiecesList(ownColor.getInverse());
        //create root of tree
        this.currentTree= new ChessTreeNode(null,0, null);
    }
    
    @Override
    public void update(ChessGame game, Move lastMove, Object arg) {
        if(lastMove!=null){
            ownGame.executeMove(lastMove);
            //if(currentTree.hasChildren()) currentTree= currentTree.getSubTreeByMove(lastMove);
        }
    }    

    private double evaluateBoard(){

        //if(rules.isCheckmate(ownColor.getInverse())) return Double.MAX_VALUE;
        int material, firepower=0, development;        
        material= materialValue(ownPieces)-materialValue(enemyPieces);
        //development= development(ownPieces)-development(enemyPieces);
        firepower= firePower(ownPieces)-firePower(enemyPieces);
        evaluatedPositions++;
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
                    break; 
                case KING:
                    materialValue +=100;
            }
        }
        return materialValue;
    }    

    private int firePower(ArrayList<Piece> pieces) {
        int firePower=0;
        for(Piece piece : pieces){
           if(piece.getPiecetype()!=KING && piece.getPiecetype()!=PAWN)
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
        this.currentTree= new ChessTreeNode(null,0, null);
        builtTree(currentTree, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, TRUE);
        ArrayList<Move> bestMoves = bestMovesFromTree(currentTree);
        //Random random = new Random();
        //return bestMoves.get(random.nextInt(bestMoves.size()));
        return bestMoves.get(0);
    }

    private double builtTree(ChessTreeNode chessTree, double alpha, double beta, boolean maximizing) {
        
        if(chessTree.getDepth()<SEARCH_DEPTH){
            ArrayList<Move> allMoves;
            double auxValue;           
            if(maximizing){
                allMoves = allPossibleMoves(ownPieces);
                auxValue = Double.NEGATIVE_INFINITY;
            }
            else{
                allMoves= allPossibleMoves(enemyPieces);
                auxValue = Double.POSITIVE_INFINITY;
            }
            
            //build all nodes for possible moves of current tree (recursive)
            ChessTreeNode newNode;

            for(Move move : allMoves){
                ownGame.executeMoveWithoutValidation(move);
                newNode = new ChessTreeNode(move, 0, chessTree);
                if(maximizing){                    
                    auxValue = max(auxValue, builtTree(newNode, alpha, beta, FALSE));
                    chessTree.addChildNode(newNode);
                    ownGame.unexecuteMove(move);                    
                    alpha = max(alpha, auxValue);
                    if(alpha >= beta) break;
                }
                else{
                    auxValue = min(auxValue, builtTree(newNode, alpha, beta, TRUE));
                    chessTree.addChildNode(newNode);
                    ownGame.unexecuteMove(move);                    
                    beta = min(beta, auxValue);
                    if(alpha >= beta) break;         
                }
            }            
            chessTree.setGameValue(auxValue);
            return auxValue;
        }
        //depth==SEARCH_DEPTH
        else{
            double gameValue = evaluateBoard();
            chessTree.setGameValue(gameValue);
            return gameValue;
        }
    } 
    
    /*private void builtTree(ChessTreeNode chessTree) {
        //recursive building of the tree
        if(chessTree.hasChildren()){
           for(ChessTreeNode node : chessTree.getChildren()){
                ownGame.executeMoveWithoutValidation(node.getMove());
                builtTree(node);
                ownGame.unexecuteMove(node.getMove());                
           } 
            if(ownGame.getPlayersTurn()==ownColor) chessTree.evaluateNodeMax();
            else chessTree.evaluateNodeMin();
        }
        else if(chessTree.getDepth()<SEARCH_DEPTH){
            ArrayList<Move> allMoves;
            if(ownGame.getPlayersTurn()==ownColor) 
                allMoves = allPossibleMoves(ownPieces);
            else allMoves= allPossibleMoves(enemyPieces);
            
            //build all nodes for possible moves of current tree (recursive)
            ChessTreeNode newNode;
            for(Move move : allMoves){
                ownGame.executeMoveWithoutValidation(move);
                newNode = new ChessTreeNode(move, 0, chessTree);
                builtTree(newNode);
                chessTree.addChildNode(newNode);
                ownGame.unexecuteMove(move);
            }            
            if(ownGame.getPlayersTurn()==ownColor) chessTree.evaluateNodeMax();
            else chessTree.evaluateNodeMin();
        }
        //depth==SEARCH_DEPTH
        else{
            chessTree.setGameValue(evaluateBoard());
        }
    }*/

    private ArrayList<Move> bestMovesFromTree(ChessTreeNode chessTree) {
        double bestValue = chessTree.getGameValue();
        ArrayList<Move> bestMoves = new ArrayList<>();
        for(ChessTreeNode child : chessTree.getChildren()){
            if(bestValue==child.getGameValue()) 
                bestMoves.add(child.getMove());
        }
        return bestMoves;
    }

    @Override
    public void getNextMove() {
        Move nextMove = findNextMove();
        System.out.println("GameValue: "+currentTree.getGameValue());
        System.out.println("Evaluated Positions: "+evaluatedPositions);
        evaluatedPositions=0;
        controller.nextMove(nextMove);
    }
    
}
