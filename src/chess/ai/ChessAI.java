/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.ai;

import chess.board.Board;
import chess.board.ChessColor;
import static chess.board.ChessColor.*;
import chess.board.Piece;
import static chess.board.PieceType.KING;
import static chess.board.PieceType.PAWN;
import chess.coordinate.Coordinate;
import chess.game.ChessGame;
import chess.game.ChessRules;
import chess.game.GameController;
import chess.game.Player;
import chess.move.Move;
import static java.lang.Boolean.*;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author Phoenix
 */
public class ChessAI implements Player {

    private static final int SEARCH_DEPTH = 4; 
    private static final int DOUBLE_BISHOP_BONUS = 20;
    private static final int DOUBLE_ROOK_FILE_BONUS = 20;
    private static final int OPEN_ROOK_FILE_BONUS = 10;
    private static final int ROOK_SEVENTH_RANK_BONUS = 25;
    private static final int PASSED_PAWN_BONUS = 10;
    private static final int DOUBLE_PAWN_MALUS = 5;
    private static final int ISOLATED_PAWN_MALUS = 5;
    private static final int CASTLING_BONUS = 20;
    
    private static final int[][] KNIGHT_BONUS = {{ -15, -10, -5, -5},
                                                 { -10,  -5,  0,  5},
                                                 {  -5,   0, 10, 10},
                                                 {   0,   5, 10, 15}};
    
    private int evaluatedPositions=0;
    
    private final GameController controller;
    private final ChessGame ownGame;
    private final ChessRules rules;
    private final Board board;
    private final ArrayList<Piece> ownPieces;
    private final ArrayList<Piece> enemyPieces;
    private final ChessColor ownColor;
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
            ownGame.executeMove(lastMove, false);
            if(currentTree.hasChildren()) currentTree= currentTree.getSubTreeByMove(lastMove);
        }
    }    

    private double evaluateBoard(){

        int value=0;        
        value += castlingBonus(ownColor);
        value += pieceValue(ownPieces)-pieceValue(enemyPieces);
        evaluatedPositions++;
        return 0.01*(double)value;
    }

    private int pieceValue(ArrayList<Piece> pieces) {
        int value=0;
        int bishopCount=0;
        int otherRookFile=-1;
        
        for(Piece piece : pieces){
            switch(piece.getPiecetype()){
                case PAWN:
                    value +=100;                    
                    value += pawnBonus(piece);
                    break;
                case QUEEN:
                    value +=900;
                    //int distance = piece.getCoord().distance(
                    //    board.getKing(piece.isColor().getInverse()).getCoord());
                    //value += (8-distance);
                    break;
                case BISHOP:
                    value +=300;
                    bishopCount++;
                    if(bishopCount>=2) value+=DOUBLE_BISHOP_BONUS;
                    break;
                case KNIGHT:
                    value+=310;
                    value+= knightBonus(piece.getCoord());
                    break;
                case ROOK:
                    value +=500;
                    int rookFile = piece.getCoord().getY();
                    if(otherRookFile==rookFile) value+=DOUBLE_ROOK_FILE_BONUS;
                    value += rookBonus(piece);
                    otherRookFile = rookFile;
                    break; 
                case KING:
                    value +=10000;
            }
        }
        return value;
    }     
    
    private ArrayList<Move> allPossibleMoves(ArrayList<Piece> pieces) {
        ArrayList<Move> allMoves = new ArrayList<>();
        for(Piece piece : pieces){
            allMoves.addAll(rules.getPossibleMoves(piece));
        }
    return allMoves;
    }

    private Move findNextMove() {
        //this.currentTree= new ChessTreeNode(null,0, null);
        builtTree(currentTree, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, TRUE);
        ArrayList<Move> bestMoves = bestMovesFromTree(currentTree);
        //Random random = new Random();
        //return bestMoves.get(random.nextInt(bestMoves.size()));
        return bestMoves.get(0);
    }

    private double builtTree(ChessTreeNode chessTree, double alpha, double beta, boolean maximizing) {
        
        if(chessTree.getDepth()<SEARCH_DEPTH){
            //try to reuse already calculated possible moves 
            ArrayList<Move> allMoves;            
            //calculate all possible moves if not already done
            if(!chessTree.hasChildren()){
                if(maximizing) allMoves = allPossibleMoves((ArrayList<Piece>) ownPieces);
                else allMoves= allPossibleMoves((ArrayList<Piece>) enemyPieces);
                for(Move move : allMoves){
                    chessTree.addChildNode(new ChessTreeNode(move,Double.NEGATIVE_INFINITY,chessTree));
                }
            }            
            //sort all moves to improve cutoff of alpha-beta-pruning
            else Collections.sort(chessTree.getChildren());
         
            //value which will be used in alpha-beta-pruning
            double auxValue;               
            if(maximizing) auxValue = Double.NEGATIVE_INFINITY;
            else auxValue = Double.POSITIVE_INFINITY;
            //build nodes recursive with a cutoff thorugh alpha-beta-pruning
            for(ChessTreeNode node : chessTree.getChildren()){
                ownGame.executeMove(node.getMove(), false);
                if(maximizing){                    
                    auxValue = max(auxValue, builtTree(node, alpha, beta, FALSE));
                    ownGame.unexecuteMove(node.getMove());                    
                    alpha = max(alpha, auxValue);
                    if(alpha >= beta) break;
                }
                else{
                    auxValue = min(auxValue, builtTree(node, alpha, beta, TRUE));
                    ownGame.unexecuteMove(node.getMove());                    
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
        SwingWorker moveCalculation = new SwingWorker<Move, Void>() {
            @Override
            public Move doInBackground() {
                Move nextMove = findNextMove();
                return nextMove;
            }

            @Override
            public void done() {
                System.out.println("GameValue: " + currentTree.getGameValue());
                System.out.println("Evaluated Positions: " + evaluatedPositions);
                System.out.println("Hash value of position: " + Long.toHexString(board.getHashValue()));
                evaluatedPositions = 0;
                try {
                    controller.nextMove(ownColor, get());
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(ChessAI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        moveCalculation.execute();
        //Move nextMove = findNextMove();
    }

    private int knightBonus(Coordinate coord) {
        int quadrX = coord.getX(), quadrY = coord.getY();
        //change coordinates to first quadrant (up left)
        if(quadrX>=4) quadrX = 7- quadrX;
        if(quadrY>=4) quadrY = 7- quadrY;        
        return KNIGHT_BONUS[quadrX][quadrY];
    }

    private int pawnBonus(Piece pawn) {
        int bonus = 0;
        int rank = pawn.getCoord().getX();
        int file = pawn.getCoord().getY();
        //doubled pawns
        if(board.getPawnStruct(pawn.isColor(), file)>1) bonus -=DOUBLE_PAWN_MALUS;
        //isolated pawn
        if(board.getPawnStruct(ownColor, file-1)==0&&
                board.getPawnStruct(ownColor, file+1)==0)
            bonus -= ISOLATED_PAWN_MALUS;
        //pawn rank
        //if(pawn.isColor()==WHITE) bonus += 3*rank;
        //else bonus+= 3*(7-rank);
    
        return bonus;
    }

    private int rookBonus(Piece rook) {
       int value = 0;
       int rank = rook.getCoord().getX();
       int file = rook.getCoord().getY();
       
       //open file
       if(board.getPawnStruct(ownColor, file)==0 && 
               board.getPawnStruct(ownColor.getInverse(), file)==0)
            value += OPEN_ROOK_FILE_BONUS;   
       //seventh rank
       if(rook.isColor()==BLACK) rank = 7-rank;
       if(rank==6) value += ROOK_SEVENTH_RANK_BONUS;       
       return value;
    }

    private int castlingBonus(ChessColor ownColor) {
        int bonus = 0;
        if(board.hasCastled(ownColor)) bonus+= CASTLING_BONUS;
        if(board.hasCastled(ownColor.getInverse())) bonus-=CASTLING_BONUS; 
        return bonus;
    }
}
