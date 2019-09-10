/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.ai;

import static chess.ai.EvaluationFlag.*;
import chess.board.Board;
import chess.board.ChessColor;
import static chess.board.ChessColor.*;
import chess.board.Piece;
import static chess.board.PieceType.*;
import chess.coordinate.Coordinate;
import chess.game.ChessGame;
import chess.game.ChessRules;
import chess.game.GameController;
import chess.game.Player;
import chess.gui.ChessGuiView;
import chess.move.Move;
import static chess.move.MoveType.TAKE;
import static java.lang.Boolean.*;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
import javax.swing.SwingWorker;

/**
 *
 * @author Phoenix
 */
public class ChessAI implements Player {

    //constants to control general AI behaviour
    private static final byte SEARCH_DEPTH = 4;
    private static final int  MAX_QUIET_SEARCH = 4;
    private static final boolean QUIET_SEARCH_ON = true;
    private static final int  TRANSPOSITION_TABLE_SIZE = 10000000;
    private static final int MAX_EV_POSITIONS = 500000;
    private static final int MAX_EV_POSITIONS_QUIET = 700000;
    
    //constants for board evaluation
    private static final int BISHOP_BASIC_VALUE = 310;
    private static final int BISHOP_DOUBLE_BONUS = 20;
    private static final int BISHOP_DIAG_PAWN_MALUS = 10;

    private static final int ROOK_BASIC_VALUE = 500;
    private static final int ROOK_DOUBLE_FILE_BONUS = 20;
    private static final int ROOK_OPEN_FILE_BONUS = 10;
    private static final int ROOK_HALF_OPEN_FILE_BONUS = 5;
    private static final int ROOK_SEVENTH_RANK_BONUS = 25;

    private static final int PAWN_BASIC_VALUE = 100;
    private static final int PAWN_PASSED_BONUS = 10;
    private static final int PAWN_DOUBLE_MALUS = 5;
    private static final int PAWN_ISOLATED_MALUS = 5;

    private static final int KNIGHT_BASIC_VALUE = 310;
    
    private static final int QUEEN_BASIC_VALUE = 900;

    private static final int KING_MOVE_NO_CASTLE_MALUS = 20;
    private static final int ROOK_MOVE_NO_CASTLE_MALUS = 15;
    private static final int CASTLING_BONUS = 30;

    private static final int[][] KNIGHT_BONUS_MATRIX
            = {{-15, -5, -5, -5},
            {-10, -5, 0, 5},
            {-5, 0, 10, 10},
            {0, 5, 10, 15}};

    private static final int[][] PAWN_BONUS_MATRIX
            = {{0, 0, 0, 0, 0, 0, 0, 0},
            {20, 20, 30, 30, 30, 30, 20, 20},
            {15, 15, 20, 25, 25, 20, 15, 15},
            {0, 0, 15, 20, 20, 15, 0, 0},
            {0, 0, 10, 16, 16, 10, 0, 0},
            {0, 0, 0, 5, 5, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0}};

    private int evaluatedPositions = 0;
    private int alphaCuts = 0;
    private int betaCuts = 0;
    private int quietAlphaCuts = 0;
    private int quietBetaCuts = 0;        
    private int visitedNodes = 0;
    private int visitedQuietNodes = 0;
    private int transpositionsUsed = 0;
    private long moveDuration = 0;
    private final JTextArea analyticsLabel = new JTextArea();
    
    private final GameController controller;
    private final ChessGame ownGame;
    private final ChessRules rules;
    private final Board board;
    private final ArrayList<Piece> ownPieces;
    private final ArrayList<Piece> enemyPieces;
    private final ChessColor ownColor;
    private final ChessTransTable transTable;
    private ChessTreeNode currentTree;
    
    public ChessAI(GameController controller, ChessColor ownColor) {
        this.controller = controller;
        this.ownGame = new ChessGame();
        this.board = ownGame.getBoard();
        this.rules = ownGame.getRules();
        this.ownColor = ownColor;
        this.ownPieces = board.getPiecesList(ownColor);
        this.enemyPieces = board.getPiecesList(ownColor.getInverse());
        this.transTable = new ChessTransTable(TRANSPOSITION_TABLE_SIZE);
        //create root of tree
        this.currentTree = new ChessTreeNode(null, 0, 0);
        createAnalyticsFrame();
    }

    @Override
    public void update(ChessGame game, Move lastMove, Object arg) {
        if (lastMove != null) {
            ownGame.executeMove(lastMove, false);
            if (currentTree.hasChildren()) {
                currentTree = currentTree.getSubTreeByMove(lastMove);
            }
        }
    }

    private int evaluateBoard() {
        evaluatedPositions++;
        if (evaluateDraw()) {
            return 0;
        }
        int value = 0;
        value += castlingBonus(ownColor);
        value += pieceValue(ownPieces) - pieceValue(enemyPieces);
        return value;
    }

    private int pieceValue(ArrayList<Piece> pieces) {
        int value = 0;
        int bishopCount = 0;
        int otherRookFile = -1;

        for (Piece piece : pieces) {
            switch (piece.getPiecetype()) {
                case PAWN:
                    value += PAWN_BASIC_VALUE;
                    value += pawnBonus(piece);
                    break;
                case QUEEN:
                    value += QUEEN_BASIC_VALUE;
                    /*int distance = piece.getCoord().distance(
                        board.getKing(piece.isColor().getInverse()).getCoord());
                    value += 2*(8-distance);*/
                    break;
                case BISHOP:
                    value += BISHOP_BASIC_VALUE;
                    bishopCount++;
                    if (bishopCount >= 2) {
                        value += BISHOP_DOUBLE_BONUS;
                    }
                    //value += bishopBonus(piece.getCoord());
                    break;
                case KNIGHT:
                    value += KNIGHT_BASIC_VALUE;
                    value += knightBonus(piece.getCoord());
                    break;
                case ROOK:
                    value += ROOK_BASIC_VALUE;
                    int rookFile = piece.getCoord().getY();
                    if (otherRookFile == rookFile) {
                        value += ROOK_DOUBLE_FILE_BONUS;
                    }
                    value += rookBonus(piece);
                    otherRookFile = rookFile;

                    if (piece.getMoveCounter() > 0 && !board.hasCastled(ownColor)) {
                        value -= ROOK_MOVE_NO_CASTLE_MALUS;
                    }
                    break;
                case KING:
                    if (piece.getMoveCounter() > 0 && !board.hasCastled(ownColor)) {
                        value -= KING_MOVE_NO_CASTLE_MALUS;
                    }
                    value += 10000;
            }
        }
        return value;
    }

    private ArrayList<Move> allPossibleMoves(ArrayList<Piece> pieces) {
        ArrayList<Move> allMoves = new ArrayList<>();
        for (Piece piece : pieces) {
            allMoves.addAll(rules.getPossibleMoves(piece));
        }
        return allMoves;
    }

    private Move findNextMove() {
        /* measure time to calculate move */
        Instant start = Instant.now();
        builtTreeAndEvaluate(currentTree, Integer.MIN_VALUE, Integer.MAX_VALUE, TRUE);
        ArrayList<Move> bestMoves = bestMovesFromTree(currentTree);
        Instant finish = Instant.now();
        moveDuration = Duration.between(start, finish).getSeconds();
        //Random random = new Random();
        //return bestMoves.get(random.nextInt(bestMoves.size()));
        return bestMoves.get(0);
    }

    /* Builds and evaluates the tree for this position and returns its value
    */ 
    private int builtTreeAndEvaluate(ChessTreeNode chessTree, int alpha, 
            int beta, boolean maximizing) {
        
        visitedNodes++;
        
        /* value of the position to be returned after evaluation */
        int gameValue;
        /* store best Move found in evaluation of this node, the value of the 
        position resulting from bestMove and the evaluation flag to be stored 
        in the transposition table
        */
        Move bestMove = null;
        int bestValue = Integer.MAX_VALUE;
        if(maximizing) bestValue = Integer.MIN_VALUE;
        EvaluationFlag evalFlag = EXACT; 
        
        /* check if position is already in transposition table */
        TransTableEntry auxEntry = transTable.getEntryByZobrisKey(board.getHashValue());
        Move tableBestMove = null;
        if(auxEntry!=null){
            /* if the searched number of plies is lower than the current
               SEARCH_DEPTH, the value can't be used. Instead the best move 
               calculated in this position is used to improve alpha-beta-
               pruning by being evaluated first. S_D - chessTree.getDepth 
               describes how many plies are needed to evaluate the position
               from this node. Also prevent that no evaluation happens if 
               node is root when a position on the board occurs a second time.
            */
            if(chessTree.getDepth()!=0 && auxEntry.getDepth() == SEARCH_DEPTH-chessTree.getDepth()){
                chessTree.setGameValue(auxEntry.getValue());
                transpositionsUsed++;
                return auxEntry.getValue();
            }
            else tableBestMove = auxEntry.getBestMove();
        }

        /* depth < SEARCH_DEPTH => inner node, else leaf of tree */
        if (chessTree.getDepth() < SEARCH_DEPTH) {
                
            /* Create all possible moves as children of the node 
               if not already done.
            */
            if (!chessTree.hasChildren()) {
                if (maximizing) {
                    createChildrenFromPieceList(chessTree, ownPieces);
                } else {
                    createChildrenFromPieceList(chessTree, enemyPieces);
                }

            } /* sort all moves to improve cutoff of alpha-beta-pruning */
            else {
                if(SEARCH_DEPTH-chessTree.getDepth() > 2) Collections.sort(chessTree.getChildren());
            }

            /* Move best move from table to front of node list if it is a valid
                move.
            */
            chessTree.moveNodeToFront(tableBestMove);
            
            //if the node has no legal moves => stalemate or mate
            if (!chessTree.hasChildren() && ownGame.isStalemate()) {
                chessTree.setGameValue(0);
                return 0;
            }

            /* value which will be used in alpha-beta-pruning */
            if (maximizing) {
                gameValue = Integer.MIN_VALUE;
            } else {
                gameValue = Integer.MAX_VALUE;
            }

            /* restrict number of evaluated positions, but make sure that tree 
               is searched to a depth of at least SEARCH_DEPTH-1  */
             if(evaluatedPositions >= MAX_EV_POSITIONS && chessTree.getDepth() ==SEARCH_DEPTH-1){
                if(QUIET_SEARCH_ON) gameValue = quiescenceSearch(chessTree, alpha, beta, maximizing);
                chessTree.setGameValue(gameValue);
                return gameValue;
             }

            /* build nodes recursively with a cutoff through alpha-beta-pruning 
            */
            for (ChessTreeNode node : chessTree.getChildren()) {
                ownGame.executeMove(node.getMove(), false);
                if (maximizing) {
                    gameValue = max(gameValue, builtTreeAndEvaluate(node, alpha, beta, FALSE));
                    ownGame.unexecuteMove(node.getMove());
                    alpha = max(alpha, gameValue);
                    if (alpha >= beta) {
                        evalFlag = BETA;
                        betaCuts++;
                        break;
                    }
                    /* evaluation was between alpha and beta => exact flag */
                    if(gameValue > bestValue){
                        bestValue = gameValue;
                        bestMove = node.getMove();
                    }                
                    
                } else {
                    gameValue = min(gameValue, builtTreeAndEvaluate(node, alpha, beta, TRUE));
                    ownGame.unexecuteMove(node.getMove());
                    beta = min(beta, gameValue);
                    if (alpha >= beta) {
                        evalFlag = ALPHA;
                        alphaCuts++;
                        break;
                    }
                /* evaluation was between alpha and beta => exact flag */
                    if(gameValue < bestValue){
                        bestValue = gameValue;
                        bestMove = node.getMove();
                    }                
                }

            }
        } //depth==SEARCH_DEPTH
        else {
            /* only evaluate quiet positions to avoid horizon effect */
            if(QUIET_SEARCH_ON) gameValue = quiescenceSearch(chessTree, alpha, beta, maximizing);
            else gameValue = evaluateBoard();
        }
        
        /* store position in transposition table */
        transTable.insertEntry(new TransTableEntry(board.getHashValue(), 
                gameValue, (byte) (SEARCH_DEPTH-chessTree.getDepth()), bestMove,
                evalFlag));
       
        chessTree.setGameValue(gameValue);        
        return gameValue;
    }

    private ArrayList<Move> bestMovesFromTree(ChessTreeNode chessTree) {
        int bestValue = chessTree.getGameValue();
        ArrayList<Move> bestMoves = new ArrayList<>();
        for (ChessTreeNode child : chessTree.getChildren()) {
            if (bestValue == child.getGameValue()) {
                bestMoves.add(child.getMove());
            }
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
                printAndResetAnalytics();
                //transTable.clear();
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

    private int bishopBonus(Coordinate coord) {
        int bonus = 0;
        for (Coordinate auxCoord : coord.getAllDiagCoord()) {
            if (board.getPieceTypeOnCoord(auxCoord) == PAWN) {
                bonus -= BISHOP_DIAG_PAWN_MALUS;
            }
        }
        return bonus;
    }

    private int knightBonus(Coordinate coord) {
        int quadrX = coord.getX(), quadrY = coord.getY();
        //change coordinates to first quadrant (up left)
        if (quadrX >= 4) {
            quadrX = 7 - quadrX;
        }
        if (quadrY >= 4) {
            quadrY = 7 - quadrY;
        }
        return KNIGHT_BONUS_MATRIX[quadrX][quadrY];
    }

    private int pawnBonus(Piece pawn) {
        int bonus = 0;
        int rank = pawn.getCoord().getX();
        int file = pawn.getCoord().getY();
        //doubled pawns
        if (board.getPawnStruct(pawn.isColor(), file) > 1) {
            bonus -= PAWN_DOUBLE_MALUS;
        }
        //isolated pawn
        if (board.getPawnStruct(ownColor, file - 1) == 0
                && board.getPawnStruct(ownColor, file + 1) == 0) {
            bonus -= PAWN_ISOLATED_MALUS;
        }
        //pawn position
        if (pawn.isColor() == BLACK) {
            bonus += PAWN_BONUS_MATRIX[rank][file];
        } else {
            bonus += PAWN_BONUS_MATRIX[7 - rank][7 - file];
        }

        return bonus;
    }

    private int rookBonus(Piece rook) {
        int value = 0;
        int rank = rook.getCoord().getX();
        int file = rook.getCoord().getY();

        //open file
        if (board.getPawnStruct(ownColor, file) == 0) {
            if (board.getPawnStruct(ownColor.getInverse(), file) == 0) {
                value += ROOK_OPEN_FILE_BONUS;
            } else {
                value += ROOK_HALF_OPEN_FILE_BONUS;
            }
        }
        //seventh rank
        if (rook.isColor() == BLACK) {
            rank = 7 - rank;
        }
        if (rank == 6) {
            value += ROOK_SEVENTH_RANK_BONUS;
        }
        return value;
    }

    private int castlingBonus(ChessColor ownColor) {
        int bonus = 0;
        if (board.hasCastled(ownColor)) {
            bonus += CASTLING_BONUS;
        }
        return bonus;
    }

    private boolean evaluateDraw() {
        return ownGame.isDraw(false);
    }

    private ArrayList<Move> bestVariation() {
        ArrayList<Move> list = new ArrayList<>();
        TransTableEntry auxEntry = transTable.getEntryByZobrisKey(board.getHashValue());
        int counter = -1;
        
        while(auxEntry != null && counter<8) {

            if(auxEntry.getBestMove()==null) break;
            list.add(auxEntry.getBestMove());
            board.executeMove(auxEntry.getBestMove());
            counter++;
            auxEntry = transTable.getEntryByZobrisKey(board.getHashValue());                        
        }
    
        for(int i=counter; i>=0; i--) {
            board.unexecuteMove(list.get(i));
        }
    
        return list;
    }

    private void printAndResetAnalytics(){
                analyticsLabel.append("Search duration: "+moveDuration+" sec");
                analyticsLabel.append("\nBest variation: "+bestVariation());
                analyticsLabel.append("\nGame value: " + String.format("%.2f" ,0.01 * currentTree.getGameValue()));
                analyticsLabel.append("\nEvaluated positions: " + evaluatedPositions);
                analyticsLabel.append("\nVisited nodes: " + visitedNodes +
                        ", visited quiet nodes: " + visitedQuietNodes);
                analyticsLabel.append("\nTranspositions used: "+transpositionsUsed);
                analyticsLabel.append("\n\n");

                /* System.out.print("Search duration: "+moveDuration+" sec"+"\n");
                System.out.println("Best variation: "+bestVariation());
                System.out.println("Game value: " + String.format("%.2f" ,0.01 * currentTree.getGameValue()));
                System.out.println("Evaluated positions: " + evaluatedPositions);
                System.out.println("Alpha cuts: " + alphaCuts + 
                        ", Quiet Alpha cuts: " + quietAlphaCuts);
                System.out.println("Beta cuts: " + betaCuts + 
                        ", Quiet Beta cuts: " + quietBetaCuts);             
                System.out.println("Visited nodes: " + visitedNodes +
                        ", visited quiet nodes: " + visitedQuietNodes);                
                System.out.println("Transpositions used: "+transpositionsUsed); */                
                
                evaluatedPositions = 0;
                alphaCuts = 0; betaCuts =0;
                quietAlphaCuts = 0; quietBetaCuts =0;
                visitedNodes = 0; visitedQuietNodes =0;        
                transpositionsUsed = 0;
    }

    private int quiescenceSearch(ChessTreeNode chessTree, int alpha, int beta, boolean maximizing) {
        
        int posValue = evaluateBoard();
        int gameValue;
        
        /* restrict number of evaluated positions to stop quiescence search 
           explosion */
        if(evaluatedPositions >= MAX_EV_POSITIONS_QUIET){
            chessTree.setGameValue(posValue);
            return posValue;
        }
        
        if(maximizing){
            /* beta cutoff */
            if(posValue >= beta) return beta;
            alpha = max(alpha, posValue);
        }
        else{
            /* alpha cutoff */
            if(posValue <= alpha) return alpha;
            beta = min(beta, posValue);
        }
    
        if (chessTree.getDepth() < SEARCH_DEPTH + MAX_QUIET_SEARCH) {        
            
            /* Calculate all possible moves if not already done.
            */
            if (!chessTree.hasChildren()) {
                if (maximizing) {
                    createChildrenFromPieceList(chessTree, ownPieces);
                } else {
                    createChildrenFromPieceList(chessTree, enemyPieces);
                }
            } 
                
            boolean anyMove = false;        
             
            ArrayList<ChessTreeNode> takeList = createTakeList(chessTree.getChildren());            
            if(takeList.size() >1) sortCaptureList(takeList);
            
            for (ChessTreeNode node : takeList) {
                
                Move currentMove = node.getMove();                
                visitedQuietNodes++;
                anyMove = true;
                ownGame.executeMove(currentMove, false);
                if(maximizing){
                    gameValue = quiescenceSearch(node, alpha, beta, FALSE);
                    ownGame.unexecuteMove(currentMove);                        
                    /* beta cutoff */
                    if(gameValue >= beta){
                        quietBetaCuts++;
                        return beta;
                    }
                    alpha = max(gameValue, alpha);
                }                    
                else{
                    gameValue = quiescenceSearch(node, alpha, beta, TRUE);
                    ownGame.unexecuteMove(currentMove);                        
                    /* alpha cutoff */
                    if(gameValue <= alpha){
                        quietAlphaCuts++;
                        return alpha;
                    }
                    beta = min(gameValue, beta);                        
                }
            }
            if(!anyMove) return posValue;
            if(maximizing) return alpha;
            else return beta;
        }
        else return posValue;
    
    }

    private void createChildrenFromPieceList(ChessTreeNode chessTree, ArrayList<Piece> pieces) {
        ArrayList<Move> allMoves= allPossibleMoves((ArrayList<Piece>) pieces);      
        for (Move move : allMoves) {
            chessTree.addChildNode(new ChessTreeNode(move, Integer.MIN_VALUE, chessTree.getDepth()+1));
        }         
    }    

    private void createAnalyticsFrame() {
        JFrame analyticsView = new JFrame("AI Analytics");
        analyticsView.setSize(300,700);
        JScrollPane analyticsScroll = new JScrollPane(analyticsLabel, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_NEVER);

        analyticsView.add(analyticsScroll);
        analyticsView.setVisible(true);
    }

    private ArrayList<ChessTreeNode> createTakeList(ArrayList<ChessTreeNode> nodeList) {
    
        ArrayList<ChessTreeNode> takeList = new ArrayList<>();
        
        for(ChessTreeNode node : nodeList){
            Move currentMove = node.getMove(); 
            if(currentMove.getMoveType() == TAKE) takeList.add(node);       
        }
    
    return takeList;
    }

    private void sortCaptureList(ArrayList<ChessTreeNode> nodeList) {
        
        /* sort moves by most valuable victim - least valuable attacker
        */
        class sortByMVVLVA implements Comparator<ChessTreeNode>
        {
            @Override
            public int compare(ChessTreeNode a, ChessTreeNode b){
                int ascore = board.getPieceTypeOnCoord(a.getMove().getCoordTo()).getMaterialValue()
                        -a.getMove().getPieceType().getMaterialValue();
                int bscore = board.getPieceTypeOnCoord(b.getMove().getCoordTo()).getMaterialValue()
                        -b.getMove().getPieceType().getMaterialValue();
                return bscore-ascore;
            }
        }
    
        nodeList.sort(new sortByMVVLVA());
    }

    @Override
    public ChessGuiView getView() {
        return null;
    }

}
