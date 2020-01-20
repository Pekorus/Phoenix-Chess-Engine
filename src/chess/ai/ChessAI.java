/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.ai;

import static chess.ai.ChessGameStage.*;
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
import chess.options.AIOptions;
import static java.lang.Boolean.*;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
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

    /* fields to control general AI behaviour */
    private int SEARCH_DEPTH;
    private int  QUIET_SEARCH_DEPTH;
    private static final int  TRANSPOSITION_TABLE_SIZE = 20000000;
    private static final int MAX_EV_POSITIONS = 1000000;
    private static final int MAX_EV_POSITIONS_QUIET = 2000000;
    private static final int DRAW_THRESHOLD_VALUE = -50;
    
    /* constants for board evaluation */
    private static final int BISHOP_BASIC_VALUE = 310;
    private static final int BISHOP_DOUBLE_BONUS = 20;
    private static final int BISHOP_DIAG_PAWN_MALUS = 20;

    private static final int ROOK_BASIC_VALUE = 500;
    private static final int ROOK_DOUBLE_FILE_BONUS = 20;
    private static final int ROOK_OPEN_FILE_BONUS = 10;
    private static final int ROOK_HALF_OPEN_FILE_BONUS = 5;

    private static final int PAWN_BASIC_VALUE = 100;
    private static final int PAWN_PASSED_BONUS = 10;
    private static final int PAWN_DOUBLE_MALUS = 5;
    private static final int PAWN_ISOLATED_MALUS = 5;

    private static final int KNIGHT_BASIC_VALUE = 310;
    
    private static final int QUEEN_BASIC_VALUE = 900;

    private static final int KING_MOVE_NO_CASTLE_MALUS = 25;
    private static final int ROOK_MOVE_NO_CASTLE_MALUS = 20;
    private static final int CASTLING_BONUS = 45;

    private static final int[][] KNIGHT_BONUS_MATRIX
            = {{-15, -5, -5, -5, -5, -5, -5, -15},
            {-10, -5, 0, 5, 5, 0, -5, -10},
            {-5, 0, 10, 10, 10, 10, 0, -5},
            {0, 5, 10, 15, 15, 10, 5, 0},
            {0, 5, 10, 15, 15, 10, 5, 0},
            {-5, 0, 10, 10, 10, 10, 0, -5},            
            {-10, -5, 0, 5, 5, 0, -5, -10},            
            {-15, -5, -5, -5, -5, -5, -5, -15}};            

    private static final int[][] PAWN_BONUS_MATRIX
            = {{0, 0, 0, 0, 0, 0, 0, 0},
            {30, 30, 30, 30, 30, 30, 30, 30},
            {15, 15, 20, 25, 25, 20, 15, 15},
            {0, 0, 15, 20, 20, 15, 0, 0},
            {0, 0, 10, 16, 16, 10, 0, 0},
            {0, 0, 0, 5, 5, 0, 0, 0},
            {0, 0, 0, -5, -5, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0}};

    private static final int[][] BISHOP_BONUS_MATRIX
            = {{-10, -15, -15, -20, -20, -15, -15, -10},
            {-10, 0, 0, 0, 0, 0, 0, -10},
            {-10, 0, 5, 10, 10, 5, 0, -10},
            {-10, 0, 10, 15, 15, 10, 0, -10},
            {-10, 0, 10, 15, 15, 10, 0, -10},
            {-10, 10, 10, 10, 10, 10, 10, -10},
            {-10, 5, 0, 5, 5, 0, 5, -10},
            {-10, -15, -15, -20, -20, -15, -15, -10}};
    
    private static final int[][] ROOK_BONUS_MATRIX
            = {{15, 15, 15, 15, 15, 15, 15, 15},
            {25, 25, 25, 25, 25, 25, 25, 25},
            {15, 15, 15, 15, 15, 15, 15, 15},
            {-5, 0, 0, 5, 5, 0, 0, -5},
            {-5, 0, 0, 5, 5, 0, 0, -5},
            {-5, 0, 0, 10, 10, 0, 0, -5},
            {-5, 0, 0, 10, 10, 0, 0, -5},
            {-10, 0, 5, 10, 10, 5, 0, -10}};    
    
    private static final int[][] QUEEN_BONUS_MATRIX
            = {{-20, -10, -5, 0, 0, -5, -10, -20},
            {5, 10, 10, 10, 10, 10, 10, 5},
            {0, 0, 5, 5, 5, 5, 0, 0},
            {-10, 0, 5, 5, 5, 5, 0, -10},
            {-10, 0, 5, 5, 5, 5, 0, -10},
            {-10, 0, 0, 0, 0, 0, 0, -10},
            {-15, -10, 0, 0, 0, 0, -10, -15},
            {-20, -20, -15, -10, -10, -15, -20, -20}};    

    private static final int[][] KING_BONUS_MATRIX_MIDDLEGAME
            = {{-30, -30, -30, -30, -30, -30, -30, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-40, -50, -50, -50, -50, -50, -50, -40},
            {-40, -40, -50, -50, -50, -50, -40, -40},
            {-20, -30, -30, -30, -30, -30, -30, -20},
            {-15, -20, -20, -20, -20, -20, -20, -15},
            {10, 10, 0, 0, 0, 0, 10, 10},
            {15, 20, 10, 0, 0, 15, 20, 15}};    
    
    private static final int[][] KING_BONUS_MATRIX_ENDGAME
            = {{-15, -5, -5, -5, -5, -5, -5, -15},
            {-10, -5, 0, 5, 5, 0, -5, -10},
            {-5, 0, 10, 10, 10, 10, 0, -5},
            {0, 5, 10, 15, 15, 10, 5, 0},
            {0, 5, 10, 15, 15, 10, 5, 0},
            {-5, 0, 10, 10, 10, 10, 0, -5},            
            {-10, -5, 0, 5, 5, 0, -5, -10},            
            {-15, -5, -5, -5, -5, -5, -5, -15}};

    /* fields for AI analytics */ 
    private JFrame analyticsView;
    private int evaluatedPositions = 0;
    private int alphaCuts = 0;
    private int betaCuts = 0;
    private int quietAlphaCuts = 0;
    private int quietBetaCuts = 0;        
    private int visitedNodes = 0;
    private int visitedQuietNodes = 0;
    private int transpositionsUsed = 0;
    private int transpositionsUsedQuiet = 0;
    private int reachedDepth = 0;
    private long moveDuration = 0;
    private final JTextArea analyticsLabel = new JTextArea();
    
    /* regular AI fields */
    private final GameController controller;
    private final AIOptions aiOptions;
    private final ChessGame ownGame;
    private final ChessRules rules;
    private final Board board;
    private final ArrayList<Piece> ownPieces;
    private final ArrayList<Piece> enemyPieces;
    private final ChessColor ownColor;
    private final ChessTransTable transTable;
    private ChessTreeNode currentTree;
    private ChessGameStage gameStage;
    private final Move[][] killerMoves; 

    
    public ChessAI(GameController controller, ChessColor ownColor, AIOptions aiOptions) {
        this.controller = controller;
        this.aiOptions = aiOptions;
        this.ownGame = new ChessGame();
        this.board = ownGame.getBoard();
        this.rules = ownGame.getRules();
        this.ownColor = ownColor;
        this.ownPieces = board.getPiecesList(ownColor);
        this.enemyPieces = board.getPiecesList(ownColor.getInverse());
        this.transTable = new ChessTransTable(TRANSPOSITION_TABLE_SIZE);
        
        //create root of tree
        this.currentTree = new ChessTreeNode(null, 0, 0);
        
        setOptions(aiOptions);
        
        gameStage = OPENING;
        killerMoves = new Move[SEARCH_DEPTH][2];
        createAnalyticsFrame();
    }

    @Override
    public void update(ChessGame game, Move lastMove, Object arg) {
        if (lastMove != null) {
            ownGame.executeMove(lastMove, false);
            /* if enemy queen is traded, change gamestage flag to endgame */
            //TODO: besseres merkmal implementieren für den Wechsel
            gameStage = ENDGAME;            
            for(Piece piece : enemyPieces){
                if(piece.getPiecetype()==QUEEN) gameStage = MIDDLEGAME;            
            }
            
            if (currentTree.hasChildren()) {
                currentTree = currentTree.getSubTreeByMove(lastMove);
            }
        }
    }

    private int evaluateBoard() {
        evaluatedPositions++;
        /*if (evaluateDraw()) {
            return 0;
        }*/
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
                    int distance = piece.getCoord().distance(
                        board.getKing(piece.isColor().getInverse()).getCoord());
                    value += 2*(8-distance);
                    //value += queenBonus(piece);
                    break;
                case BISHOP:
                    value += BISHOP_BASIC_VALUE;
                    bishopCount++;
                    if (bishopCount >= 2) {
                        value += BISHOP_DOUBLE_BONUS;
                    }
                    value += bishopBonus(piece);
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
                    value += kingBonus(piece);
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
       // ArrayList<Move> bestMoves = bestMovesFromTree(currentTree);
        Instant finish = Instant.now();
        moveDuration = Duration.between(start, finish).getSeconds();
        //Random random = new Random();
        //return bestMoves.get(random.nextInt(bestMoves.size()));
        //return bestMoves.get(0);
        return transTable.getEntryByZobrisKey(board.getHashValue()).getBestMove();
    }

    /* Builds and evaluates the tree for this position and returns its value
    */ 
    private int builtTreeAndEvaluate(ChessTreeNode chessTree, int alpha, 
            int beta, boolean maximizing) {
        
        visitedNodes++;
        
        /* fast check for repetition to avoid threefold repetition */
        if(fastRepetitionCheck(board.getHashValue())) return DRAW_THRESHOLD_VALUE;
        
        /* value of the position to be returned after evaluation */
        int gameValue;
        /* store best Move found in evaluation of this node, the value of the 
        position resulting from bestMove and the evaluation flag to be stored 
        in the transposition table
        */
        Move bestMove = null;
        int bestValue;        
        
        ChessColor colorToMove;
        
        /* set values and fields which will be used in search denpending on
        maximazing/minimizing current position
        */
        if(maximizing){
            bestValue = Integer.MIN_VALUE;
            colorToMove = ownColor;
            gameValue = Integer.MIN_VALUE;
        }
        else{
            bestValue = Integer.MAX_VALUE;
            colorToMove = ownColor.getInverse();
            gameValue = Integer.MAX_VALUE;
        }
        
        EvaluationFlag evalFlag = EXACT; 
        
        /* check if position is already in transposition table */
        TransTableEntry auxEntry = transTable.getEntryByZobrisKey(board.getHashValue());
        Move tableBestMove = null;
        if(auxEntry!=null){
            /* if the searched number of plies is lower than the current
               SEARCH_DEPTH, the value can't be used.
            */
            if(auxEntry.getEvaluationFlag() != null && chessTree.getDepth()!=0 &&
                   auxEntry.getDepth() <= chessTree.getDepth() ){
                
                transpositionsUsed++;
                int entryValue = auxEntry.getValue();
                
                switch(auxEntry.getEvaluationFlag()){
                
                    case EXACT: return entryValue;
                                
                    case ALPHA: if(entryValue <= alpha) return entryValue;
                                beta = min(beta, entryValue);
                                break;
                    
                    case BETA: if(entryValue >= beta) return entryValue;
                               alpha = max(alpha, entryValue);
                               break;           
                }
            }
            /* Store best move calculated in this position to improve 
            alpha-beta-pruning by being evaluated first. */
            tableBestMove = auxEntry.getBestMove();
        }

        /* depth < SEARCH_DEPTH => inner node, else leaf of tree */
        if (chessTree.getDepth() < SEARCH_DEPTH) {
                
            /* Create all possible moves as children of the node 
               if not already done.
            */
            if (!chessTree.hasChildren()) {
                    createChildrenFromPieceList(chessTree, colorToMove);
                /* no  legal moves: not in check => stalemate, in check => 
                    checkmate */    
                    if(!chessTree.hasChildren()){
                        if(!ownGame.isInCheck(colorToMove)) return 0;
                        else return gameValue;
                    }                           
            }      
            
            /* sort all moves to improve cutoff of alpha-beta-pruning */
            chessTree.getChildren().sort(new SortingMoves());
            /* Move best move from table to front of node list if it is a valid
                move.
            */
            chessTree.moveNodeToFront(tableBestMove);

            /* restrict number of evaluated positions, but make sure that tree 
               is searched to a depth of at least SEARCH_DEPTH-1  */
            if(evaluatedPositions >= MAX_EV_POSITIONS && chessTree.getDepth() ==SEARCH_DEPTH-1){
                gameValue = quiescenceSearch(chessTree, alpha, beta, maximizing);
             }

            /* build nodes recursively with a cutoff through alpha-beta-pruning 
            */
            for (ChessTreeNode node : chessTree.getChildren()) {
                ownGame.executeMove(node.getMove(), false);
                if (maximizing) {
                    gameValue = max(gameValue, builtTreeAndEvaluate(node, alpha, beta, FALSE));
                    ownGame.unexecuteMove(node.getMove());
                    alpha = max(alpha, gameValue);
                    //alpha!=MAX_VALUE catches mate
                    if (alpha >= beta && alpha!=Integer.MAX_VALUE) {
                        /* store the refutation move as killermove to use
                           at same depth */
                        if(node.getMove().getMoveType()!=TAKE)
                            storeKillerMove(node.getMove(), node.getDepth());
                        
                        evalFlag = BETA;
                        betaCuts++;
                        break;
                    }
                    /* evaluation was between alpha and beta => exact flag */
                    if(gameValue > bestValue || gameValue==Integer.MIN_VALUE){
                        bestValue = gameValue;
                        bestMove = node.getMove();
                    }                
                    
                } else {
                    gameValue = min(gameValue, builtTreeAndEvaluate(node, alpha, beta, TRUE));
                    ownGame.unexecuteMove(node.getMove());
                    beta = min(beta, gameValue);
                    if (alpha >= beta && beta!=Integer.MIN_VALUE) {
                        /* store the refutation move as killermove to use
                           at same depth */
                        if(node.getMove().getMoveType()!=TAKE)
                            storeKillerMove(node.getMove(), node.getDepth());                       
                        
                        evalFlag = ALPHA;
                        alphaCuts++;
                        break;
                    }
                /* evaluation was between alpha and beta => exact flag */
                    if(gameValue < bestValue || gameValue == Integer.MAX_VALUE){
                        bestValue = gameValue;
                        bestMove = node.getMove();
                    }                
                }

            }
        } //depth==SEARCH_DEPTH
        else {
            /* only evaluate quiet positions to avoid horizon effect */
            gameValue = quiescenceSearch(chessTree, alpha, beta, maximizing);
            evalFlag = null;
        }
        
        /* store position in transposition table */
        transTable.insertEntry(new TransTableEntry(board.getHashValue(), 
                gameValue, (byte) chessTree.getDepth(), bestMove,
                evalFlag));
               
        return gameValue;
    }

    /*private ArrayList<Move> bestMovesFromTree(ChessTreeNode chessTree) {
        for(ChessTreeNode node : chessTree.getChildren()){
            System.out.printf(node.getMove()+", "+node.getGameValue()+"\n");
        }
             System.out.printf("\n");       
        int bestValue = chessTree.getGameValue();
        ArrayList<Move> bestMoves = new ArrayList<>();
        for (ChessTreeNode child : chessTree.getChildren()) {
            if (bestValue == child.getGameValue()) {
                bestMoves.add(child.getMove());
            }
        }
        return bestMoves;
    }*/

    @Override
    public void getNextMove() {
        SwingWorker moveCalculation = new SwingWorker<Move, Void>() {
            @Override
            public Move doInBackground() {
                return findNextMove();
            }

            @Override
            public void done() {
                printAndResetAnalytics();
                transTable.clear();
                try {
                    controller.nextMove(ownColor, get());
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(ChessAI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        moveCalculation.execute();
    }

    private int bishopBonus(Piece bishop) {
        int bonus = 0;
        Coordinate coord = bishop.getCoord();
        
        for (Coordinate auxCoord : coord.getDiagCoordFront(bishop.isColor())) {
            if (board.getPieceTypeOnCoord(auxCoord) == PAWN) {
                bonus -= BISHOP_DIAG_PAWN_MALUS;
            }
        }
        if (bishop.isColor() == BLACK) {
            bonus += BISHOP_BONUS_MATRIX[coord.getX()][coord.getY()];
        } else {
            bonus += BISHOP_BONUS_MATRIX[7 - coord.getX()][7 - coord.getY()];
        }
        return bonus;
    }

    private int knightBonus(Coordinate coord) {
        return KNIGHT_BONUS_MATRIX[coord.getX()][coord.getY()];
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
        int bonus = 0;
        int rank = rook.getCoord().getX();
        int file = rook.getCoord().getY();

        //open file
        if (board.getPawnStruct(ownColor, file) == 0) {
            if (board.getPawnStruct(ownColor.getInverse(), file) == 0) {
                bonus += ROOK_OPEN_FILE_BONUS;
            } else {
                bonus += ROOK_HALF_OPEN_FILE_BONUS;
            }
        }
        //rook position
        if (rook.isColor() == BLACK) {
            bonus += ROOK_BONUS_MATRIX[rank][file];
        } else {
            bonus += ROOK_BONUS_MATRIX[7 - rank][7 - file];
        }
        return bonus;
    }

    private int queenBonus(Piece queen){
        int bonus = 0;
        Coordinate coord = queen.getCoord();
        if (queen.isColor() == BLACK) {
            bonus += QUEEN_BONUS_MATRIX[coord.getX()][coord.getY()];
        } else {
            bonus += QUEEN_BONUS_MATRIX[7 - coord.getX()][7 - coord.getY()];
        }        
        return bonus;
    }

    private int kingBonus(Piece king){
        int bonus = 0;
        Coordinate coord = king.getCoord();
        if (king.isColor() == BLACK) {
            if(gameStage != ENDGAME) bonus += KING_BONUS_MATRIX_MIDDLEGAME[coord.getX()][coord.getY()];
            else bonus += KING_BONUS_MATRIX_ENDGAME[coord.getX()][coord.getY()];
        } else {
            if(gameStage != ENDGAME) bonus += KING_BONUS_MATRIX_MIDDLEGAME[7 - coord.getX()][coord.getY()];
            else bonus += KING_BONUS_MATRIX_ENDGAME[7 - coord.getX()][coord.getY()];
        }        
        return bonus;
    }    
    
    private int castlingBonus(ChessColor ownColor) {
        if (board.hasCastled(ownColor)) {
            return CASTLING_BONUS;
        }
        return 0;
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
                analyticsLabel.append("\nGame value: " + String.format("%.2f" ,0.01 * transTable.getEntryByZobrisKey(board.getHashValue()).getValue()));
                analyticsLabel.append("\nEvaluated positions: " + evaluatedPositions);
                analyticsLabel.append("\nVisited nodes: " + visitedNodes +
                        ", visited quiet nodes: " + visitedQuietNodes);
                analyticsLabel.append("\nTransp. used: "+transpositionsUsed);
                analyticsLabel.append("\nTransp. quiet search: "+transpositionsUsedQuiet);
                //analyticsLabel.append("\nHash Collisions: "+transTable.getHashCollisions());
                analyticsLabel.append("\nTransp. Table entries: "+transTable.getHashFilled()+" / "+TRANSPOSITION_TABLE_SIZE);
                analyticsLabel.append("\nMaximum Depth: "+reachedDepth);
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
                transpositionsUsed = 0; transpositionsUsedQuiet =0;
                reachedDepth =0;
                transTable.resetCollisionCounter();
    }

    private int quiescenceSearch(ChessTreeNode chessTree, int alpha, int beta, boolean maximizing) {
        
        /* posValue represents the board value if one would do nothing, if 
        the position is far enough ahead no captures need to be considered */
        int posValue = evaluateBoard();
        int gameValue, bestValue;
        Move bestMove = null;
        ChessColor colorToMove;    
        
        if(chessTree.getDepth()> reachedDepth) reachedDepth = chessTree.getDepth();
        
        /* restrict number of evaluated positions to stop quiescence search 
           explosion */
        if(evaluatedPositions >= MAX_EV_POSITIONS_QUIET){
            return posValue;
        }
               
        /* check if position is already in transposition table */
        TransTableEntry auxEntry = transTable.getEntryByZobrisKey(board.getHashValue());
        Move tableBestMove = null;
        if(auxEntry!=null){
            /* if the searched number of plies is lower than the current
               SEARCH_DEPTH, the value can't be used. Instead the best move 
               calculated in this position is used to improve alpha-beta-
               pruning by being evaluated first. S_D - chessTree.getDepth 
               describes how many plies are needed to evaluate the position
               from this node.
            */
            if(auxEntry.getEvaluationFlag() != null &&
                   auxEntry.getDepth() <= chessTree.getDepth() ){
                
                transpositionsUsedQuiet++;
                int entryValue = auxEntry.getValue();
                
                switch(auxEntry.getEvaluationFlag()){
                
                    case EXACT: return entryValue;
                                                                
                    case ALPHA: if(entryValue <= alpha) return entryValue;
                                beta = min(beta, entryValue);
                                break;
                    
                    case BETA: if(entryValue >= beta) return entryValue;
                               alpha = max(alpha, entryValue);
                               break;           
                }
            }
            /* Store best move calculated in this position to improve 
                alpha-beta-pruning by being evaluated first. */
        tableBestMove = auxEntry.getBestMove();
        }
       
        /* check if stand pat is already better than threshold alpha or beta,
           if not: initialize variables needed for evaluation */
        if(maximizing){
            /* beta cutoff */ 
            if(posValue >= beta) return beta;
            alpha = max(alpha, posValue);

            gameValue = Integer.MIN_VALUE;
            bestValue = Integer.MIN_VALUE;           
            colorToMove = ownColor;
        }
        else{
            /* alpha cutoff */
            if(posValue <= alpha) return alpha;
            beta = min(beta, posValue);
            
            gameValue = Integer.MAX_VALUE;
            bestValue = Integer.MAX_VALUE;
            colorToMove = ownColor.getInverse();        
        }

        EvaluationFlag evalFlag = EXACT;
        
        if (chessTree.getDepth() < SEARCH_DEPTH + QUIET_SEARCH_DEPTH) {        
            
            boolean anyMove = false; 
            
            /* Calculate all possible moves if not already done.
            */
            if (!chessTree.hasChildren()) {
                    createChildrenFromPieceList(chessTree, colorToMove);
                /* no  legal moves: not in check => stalemate, in check => 
                    checkmate */    
                    if(!chessTree.hasChildren()){
                        if(!ownGame.isInCheck(colorToMove)) 
                            return 0;
                        if(maximizing) return Integer.MIN_VALUE; 
                        else return Integer.MAX_VALUE;
                    }                    
            } 

            ArrayList<ChessTreeNode> quietList = chessTree.getChildren();             
            
            /*If player is not in check, consider only takes. Otherwise take
            all moves into consideration */
            if(!ownGame.isInCheck(colorToMove)){
                quietList = createTakeList(quietList);            
                quietList.sort(new SortByMVVLVA());
            }
            else quietList.sort(new SortingMovesWithoutKiller());
            
            /* Move best move from transposition table to front of node list if 
                it is a valid move.
            */
            chessTree.moveNodeToFront(tableBestMove);
            
            for (ChessTreeNode node : quietList) {
                
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
                        evalFlag = BETA;
                        break;
                    }
                    alpha = max(gameValue, alpha);
                    /* evaluation was between alpha and beta => exact flag */
                    if(gameValue > bestValue || gameValue==Integer.MIN_VALUE){
                        bestValue = gameValue;
                        bestMove = currentMove;
                    }                  
                }                    
                else{
                    gameValue = quiescenceSearch(node, alpha, beta, TRUE);
                    ownGame.unexecuteMove(currentMove);                        
                    /* alpha cutoff */
                    if(gameValue <= alpha){
                        quietAlphaCuts++;
                        evalFlag = ALPHA;
                        break;
                    }
                    beta = min(gameValue, beta);                        
                    /* evaluation was between alpha and beta => exact flag */
                    if(gameValue < bestValue || gameValue==Integer.MAX_VALUE){
                        bestValue = gameValue;
                        bestMove = currentMove;
                    }  
                }
            }
            if(!anyMove) return posValue;
            
            // store position in transposition table
            transTable.insertEntry(new TransTableEntry(board.getHashValue(), 
                gameValue, (byte) chessTree.getDepth(), bestMove, evalFlag));
            
            switch(evalFlag){
                case EXACT: if(maximizing) return alpha;
                            else return beta;
                
                case BETA:  return beta;
                
                case ALPHA: return alpha;
            
                default: return posValue;
            }
            
        }
        else return posValue;
    
    }

    private void createChildrenFromPieceList(ChessTreeNode chessTree, ChessColor color) {
        ArrayList<Move> allMoves;
        if(color == ownColor) allMoves = allPossibleMoves((ArrayList<Piece>) ownPieces);      
        else allMoves = allPossibleMoves((ArrayList<Piece>) enemyPieces);
        
        for (Move move : allMoves) {
            chessTree.addChildNode(new ChessTreeNode(move, Integer.MIN_VALUE, chessTree.getDepth()+1));
        }         
    }    

    private void createAnalyticsFrame() {
        analyticsView = new JFrame("AI Analytics");
        analyticsView.setSize(315,700);
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

    @Override
    public ChessGuiView getView() {
        return null;
    }

    private boolean fastRepetitionCheck(long hashValue) {
        LinkedList<Long> positions = ownGame.getRecentPositions();
        int counter = 0;
        for(int i = positions.size()-5; i>=0; i-=4){
            if(positions.get(i)==hashValue){
                if(counter <1) counter++;
                else return true;
            }
        }
        return false;
    }

    private void storeKillerMove(Move move, int depth) {
        if(move.equals(killerMoves[depth-1][0])) return;
        killerMoves[depth-1][1] = killerMoves[depth-1][0];
        killerMoves[depth-1][0] = move;
    }

    private void setOptions(AIOptions aiOptions) {
        SEARCH_DEPTH = aiOptions.getSearchDepth();
        QUIET_SEARCH_DEPTH = aiOptions.getQuietSearchDepth();
    }

    @Override
    public void endGame() {
        analyticsView.dispose();
    }

    /* sort moves by most valuable victim - least valuable attacker
        */
    class SortByMVVLVA implements Comparator<ChessTreeNode>
    {
            @Override
            public int compare(ChessTreeNode a, ChessTreeNode b){
                return score(b)-score(a);
            }
        
            private int score(ChessTreeNode node){
                return board.getPieceTypeOnCoord(node.getMove().getCoordTo()).getMaterialValue()
                        -node.getMove().getPieceType().getMaterialValue();
            }
        }
        
    class SortingMoves implements Comparator<ChessTreeNode>
            {
                @Override
                public int compare(ChessTreeNode a, ChessTreeNode b){                                       
                    return score(b)-score(a);
                }


                private int score(ChessTreeNode node) {
                    int score = 0;                    
                    Move move = node.getMove();
                    
                    /* Score is awarded:
                       - taking moves get +1000 score
                       - value of MVVLVA is added  
                    */
                    if(move.getMoveType() == TAKE){
                        score =+ 1000 + board.getPieceTypeOnCoord(move.getCoordTo()).getMaterialValue()
                            -move.getPieceType().getMaterialValue();
                        /*if(ownGame.getLastMove().getCoordTo().equals(move.getCoordTo())){
                            score += 1000;
                        }*/
                    }
                    /* First Killer move gets a score of +600, second +500 
                        and  therefore they should be checked right after takes */
                    if(move.equals(killerMoves[node.getDepth()-1][0]))
                        score += 600;
                    if(move.equals(killerMoves[node.getDepth()-1][1]))
                        score += 500;
                    return score;
                }                
            }    

    class SortingMovesWithoutKiller implements Comparator<ChessTreeNode>
            {
                @Override
                public int compare(ChessTreeNode a, ChessTreeNode b){                                       
                    return score(b)-score(a);
                }


                private int score(ChessTreeNode node) {
                    int score = 0;                    
                    Move move = node.getMove();
                    
                    /* Taking moves get +1000 score, also additional score for 
                       MVVLVA */
                    if(move.getMoveType() == TAKE){
                        score =+ 1000 + board.getPieceTypeOnCoord(move.getCoordTo()).getMaterialValue()
                            -move.getPieceType().getMaterialValue();
                    }
                    return score;
                }                
            }   
}
