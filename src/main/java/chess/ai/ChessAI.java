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
import chess.game.ChessGameEndType;
import chess.game.ChessRules;
import chess.game.GameController;
import chess.game.Player;
import chess.move.Move;
import static chess.move.MoveType.NORMAL;
import static chess.move.MoveType.TAKE;
import chess.options.AIOptions;
import chess.options.CalcType;
import static chess.options.CalcType.TIME;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CancellationException;
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
 * Provides an AI which is able to play chess using a tree structure. 
 * Several standard techniques are used to improve performance:
 *  - Alpha-Beta pruning
 *  - Null-move pruning
 *  - Quiescence search
 *  - Killer heuristic
 *  - Transposition tables 
 *  - Iterative deepening
 *  - Position matrices for pieces
 */
public class ChessAI implements Player {

    /* fields to control general AI behaviour */
    
    private int SEARCH_DEPTH;
    private int QUIET_SEARCH_DEPTH;
    /* time to restrict duration of AI move */
    private long TURN_TIME;
    /* type of restriction on AI move calculation */
    private CalcType CALC_TYPE;
    private static final int TRANSPOSITION_TABLE_SIZE = 200000;
    private static final int DRAW_THRESHOLD_VALUE = -50;
    /* highest value for a position ( the lowest value is given by -MATEVALUE) */
    private static final int MATEVALUE = Integer.MAX_VALUE / 2;
    /* value to control the search depth reduction in null move search */ 
    private static final int NULLMOVE_REDUCTION = 2;

    /* constants for board evaluation */
    
    private static final int BISHOP_BASIC_VALUE = 310;
    /* given if player has a pair of bishops */
    private static final int BISHOP_DOUBLE_BONUS = 20;
    /* given for pawns directly in front of a bishop */
    private static final int BISHOP_DIAG_PAWN_MALUS = 20;

    private static final int ROOK_BASIC_VALUE = 500;
    /* given for two rooks on same file */
    private static final int ROOK_DOUBLE_FILE_BONUS = 20;
    /* given for a rook on open file */
    private static final int ROOK_OPEN_FILE_BONUS = 15;
    /* given for a rook on half-open file */
    private static final int ROOK_HALF_OPEN_FILE_BONUS = 10;

    private static final int PAWN_BASIC_VALUE = 100;
    /* given for multiple pawns on same file */
    private static final int PAWN_DOUBLE_MALUS = 15;
    /* given if a pawn is isolated (from own pawns) */
    private static final int PAWN_ISOLATED_MALUS = 15;

    private static final int KNIGHT_BASIC_VALUE = 310;

    private static final int QUEEN_BASIC_VALUE = 900;

    /* Position matrices for a piece. Bonus/ Malus given depends on the position
        on the board. Used in static evaluation to quickly evaluate if a field
        is desirable for that piece.       
    */
    private static final int[][] KNIGHT_BONUS_MATRIX
            = {{-15, -5, -5, -5, -5, -5, -5, -15},
            {-10, -5, 0, 5, 5, 0, -5, -10},
            {-5, 0, 10, 10, 10, 10, 0, -5},
            {0, 5, 10, 15, 15, 10, 5, 0},
            {0, 5, 10, 15, 15, 10, 5, 0},
            {-5, 0, 10, 10, 10, 10, 0, -5},
            {-10, -5, 0, 5, 5, 0, -5, -10},
            {-15, -5, -5, -5, -5, -5, -5, -15}};

    private static final int[][] BLACK_PAWN_BONUS_MATRIX
            = {{0, 0, 0, 0, 0, 0, 0, 0},
            {30, 30, 30, 30, 30, 30, 30, 30},
            {15, 15, 20, 25, 25, 20, 15, 15},
            {0, 0, 15, 20, 20, 15, 0, 0},
            {0, 0, 10, 16, 16, 10, 0, 0},
            {0, 0, 0, 5, 5, 0, 0, 0},
            {0, 0, 0, -5, -5, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0}};

    private static final int[][] WHITE_PAWN_BONUS_MATRIX
            = {{0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, -5, -5, 0, 0, 0},
            {0, 0, 0, 5, 5, 0, 0, 0},
            {0, 0, 10, 16, 16, 10, 0, 0},
            {0, 0, 15, 20, 20, 15, 0, 0},
            {15, 15, 20, 25, 25, 20, 15, 15},
            {30, 30, 30, 30, 30, 30, 30, 30},
            {0, 0, 0, 0, 0, 0, 0, 0}};

    private static final int[][] BLACK_BISHOP_BONUS_MATRIX
            = {{-10, -15, -15, -20, -20, -15, -15, -10},
            {-10, 0, 0, 0, 0, 0, 0, -10},
            {-10, 0, 5, 10, 10, 5, 0, -10},
            {-10, 0, 10, 15, 15, 10, 0, -10},
            {-10, 0, 10, 15, 15, 10, 0, -10},
            {-10, 10, 10, 10, 10, 10, 10, -10},
            {-10, 5, 0, 5, 5, 0, 5, -10},
            {-10, -15, -15, -20, -20, -15, -15, -10}};

    private static final int[][] WHITE_BISHOP_BONUS_MATRIX
            = {{-10, -15, -15, -20, -20, -15, -15, -10},
            {-10, 5, 0, 5, 5, 0, 5, -10},
            {-10, 10, 10, 10, 10, 10, 10, -10},
            {-10, 0, 10, 15, 15, 10, 0, -10},
            {-10, 0, 10, 15, 15, 10, 0, -10},
            {-10, 0, 5, 10, 10, 5, 0, -10},
            {-10, 0, 0, 0, 0, 0, 0, -10},
            {-10, -15, -15, -20, -20, -15, -15, -10}};

    private static final int[][] BLACK_ROOK_BONUS_MATRIX
            = {{15, 15, 15, 15, 15, 15, 15, 15},
            {25, 25, 25, 25, 25, 25, 25, 25},
            {15, 15, 15, 15, 15, 15, 15, 15},
            {-5, 0, 0, 5, 5, 0, 0, -5},
            {-5, 0, 0, 5, 5, 0, 0, -5},
            {-5, 0, 0, 10, 10, 0, 0, -5},
            {-5, 0, 0, 10, 10, 0, 0, -5},
            {-10, 0, 0, 10, 10, 0, 0, -10}};

    private static final int[][] WHITE_ROOK_BONUS_MATRIX
            = {{-10, 0, 0, 10, 10, 0, 0, -10},
            {-5, 0, 0, 10, 10, 0, 0, -5},
            {-5, 0, 0, 10, 10, 0, 0, -5},
            {-5, 0, 0, 5, 5, 0, 0, -5},
            {-5, 0, 0, 5, 5, 0, 0, -5},
            {15, 15, 15, 15, 15, 15, 15, 15},
            {25, 25, 25, 25, 25, 25, 25, 25},
            {15, 15, 15, 15, 15, 15, 15, 15},};

    private static final int[][] QUEEN_BONUS_MATRIX
            = {{-20, -10, -5, 0, 0, -5, -10, -20},
            {5, 10, 10, 10, 10, 10, 10, 5},
            {0, 0, 5, 5, 5, 5, 0, 0},
            {-10, 0, 5, 5, 5, 5, 0, -10},
            {-10, 0, 5, 5, 5, 5, 0, -10},
            {-10, 0, 0, 0, 0, 0, 0, -10},
            {-15, -10, 0, 0, 0, 0, -10, -15},
            {-20, -20, -15, -5, -5, -15, -20, -20}};

    private static final int[][] WHITE_KING_BONUS_MATRIX_MIDDLEGAME
            = {{15, 30, 10, 0, 0, 20, 25, 15},
            {10, 10, 0, 0, 0, 0, 10, 10},
            {-15, -20, -20, -20, -20, -20, -20, -15},
            {-20, -30, -30, -30, -30, -30, -30, -20},
            {-40, -40, -50, -50, -50, -50, -40, -40},
            {-40, -50, -50, -50, -50, -50, -50, -40},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-30, -30, -30, -30, -30, -30, -30, -30}};

    private static final int[][] BLACK_KING_BONUS_MATRIX_MIDDLEGAME
            = {{-30, -30, -30, -30, -30, -30, -30, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-40, -50, -50, -50, -50, -50, -50, -40},
            {-40, -40, -50, -50, -50, -50, -40, -40},
            {-20, -30, -30, -30, -30, -30, -30, -20},
            {-15, -20, -20, -20, -20, -20, -20, -15},
            {10, 10, 0, 0, 0, 0, 10, 10},
            {15, 30, 10, 0, 0, 20, 25, 15}};

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
    //private final AnalyticsWriter analyticsWriter;
    private int evaluatedPositions = 0;
    private int visitedNodes = 0;
    private int visitedQuietNodes = 0;
    private int transpositionsUsed = 0;
    private int reachedDepth = 0;
    private int iterationDepth = 0;
    private long moveDuration = 0;
    private final JTextArea analyticsLabel = new JTextArea();
    private String currentLabel;
    private Instant start;
    
    /* regular AI fields */
    private final GameController controller;
    /* AI has a copy of the game to execute/unexecute moves in the search tree 
    */
    private final ChessGame ownGame;
    /* the rules class to be used */
    private final ChessRules rules;
    
    /* own board to execute/unexecute moves in search tree */
    private final Board board;
    private final ArrayList<Piece> ownPieces;
    private final ArrayList<Piece> enemyPieces;
    private final ChessColor ownColor;
    
    /* transposition table */
    private final ChessTransTable transTable;
    private Move storedBestMove;
    private int storedBestValue;
    private ArrayList<Move> storedBestVariation;
    private ChessTreeNode currentTree;
    private ChessGameStage gameStage;
    /* array to store moves for the killer heuristic */
    private final Move[][] killerMoves;
    /* thread in which the calculation of the move takes place */
    private SwingWorker moveCalculation;
    private boolean firstMove;
    /* stores if AI plays the creators favorite openings */
    private boolean peterMode;

    /**
     * Class constructor for a regular chess game from the starting position.
     * 
     * @param controller    controller that started the game
     * @param ownColor      color that the AI should play
     * @param aiOptions     Options to control AI behaviour
     */
    public ChessAI(GameController controller, ChessColor ownColor, AIOptions 
            aiOptions) {
        
        this.controller = controller;
        this.ownGame = new ChessGame();
        this.board = ownGame.getBoard();
        this.rules = ownGame.getRules();
        this.ownColor = ownColor;
        this.ownPieces = board.getPiecesList(ownColor);
        this.enemyPieces = board.getPiecesList(ownColor.getInverse());
        this.transTable = new ChessTransTable(TRANSPOSITION_TABLE_SIZE);
        this.firstMove = true;
        
        setOptions(aiOptions);

        /* create root of tree */
        this.currentTree = new ChessTreeNode(null);

        gameStage = OPENING;
        killerMoves = new Move[100][2];
        createAnalyticsFrame();
        
        //this.analyticsWriter = new AnalyticsWriter();
        //analyticsWriter.writeNewGame();
    }

    /**
     * Class constructor for a regular chess game from a custom starting 
     * position (is used to start a game from board editor).
     * 
     * @param controller    the controller that started the game
     * @param ownColor      color that the AI should play
     * @param aiOptions     options to control AI behaviour
     * @param pieceArray    array of pieces of starting position     
     * @param colorToMove   color that begins the game in this position
     * @param castleRights  array of the castle rights of both players in this
     *                      position
     */
    public ChessAI(GameController controller, ChessColor ownColor, AIOptions 
            aiOptions, Piece[][] pieceArray, ChessColor colorToMove, 
            boolean[] castleRights) {
        
        this.controller = controller;
        this.ownGame = new ChessGame(pieceArray, colorToMove, castleRights);
        this.board = ownGame.getBoard();
        this.rules = ownGame.getRules();
        this.ownColor = ownColor;
        this.ownPieces = board.getPiecesList(ownColor);
        this.enemyPieces = board.getPiecesList(ownColor.getInverse());
        this.transTable = new ChessTransTable(TRANSPOSITION_TABLE_SIZE);
        /* custom board, AI shouldn't play creator mode on first move */
        this.firstMove = false;
        
        /* create root of tree */
        this.currentTree = new ChessTreeNode(null);

        setOptions(aiOptions);

        /* if enemy queen is traded, game stage flag is set to endgame, otherwise
            it is set to middle game */
        //TODO: endgame else case ?
        gameStage = ENDGAME;
        for (Piece piece : enemyPieces) {
            if (piece.getType() == QUEEN) {
                gameStage = MIDDLEGAME;
                break;
            }
        }
        killerMoves = new Move[100][2];
        createAnalyticsFrame();
        //this.analyticsWriter = new AnalyticsWriter();
        //analyticsWriter.writeNewGame();
    }

    @Override
    public void update(Move lastMove) {
        
        if (lastMove != null) {            
            
          //  if(ownGame.getPlayersTurn()!= ownColor) analyticsWriter
          //                                    .movePlayed(lastMove.toString());
            ownGame.executeMove(lastMove, false);

            /* if enemy queen is traded, change game stage flag to endgame */
            //TODO: endgame else case ?
            gameStage = ENDGAME;
            for (Piece piece : enemyPieces) {
                if (piece.getType() == QUEEN) {
                    gameStage = MIDDLEGAME;
                    break;
                }
            }
            if (currentTree.hasChildren()) {
                currentTree = currentTree.getSubTreeByMove(lastMove);
            }
        }
    }

    /**
     * Returns value of the static board evaluation function. Higher values
     * correspond to a better position for the specified color. It is always
     * true that evaluateBoard(WHITE) = -evaluateBoard(BLACK).
     * 
     * @param colorToMove color of the side that is evaluating
     * @return            value of the static board evaluation function
     */
    private int evaluateBoard(ChessColor colorToMove) {
        
        evaluatedPositions++;
        if (colorToMove == ownColor) {
            return pieceValue(ownPieces) - pieceValue(enemyPieces);
        } else {
            return pieceValue(enemyPieces) - pieceValue(ownPieces);
        }
    }

    /**
     * Returns sum of the piece values as determined by static board evaluation
     * function. Higher value corresponds to a better position.
     * 
     * @param pieces    array of pieces to be evaluated
     * @return          sum of piece values  
     */
    private int pieceValue(ArrayList<Piece> pieces) {
        
        int value = 0;
        int bishopCount = 0;
        int otherRookFile = -1;

        for (Piece piece : pieces) {
            switch (piece.getType()) {
                case PAWN:
                    
                    value += PAWN_BASIC_VALUE + pawnBonus(piece);
                    break;
                
                case QUEEN:
                    /* distance to enemy king, weighted with 0.5 */
                    value += QUEEN_BASIC_VALUE - 0.5 * (piece.getCoord()
                                    .distance(board.getKing(piece.getColor().
                                                    getInverse()).getCoord()));
                    //value += queenBonus(piece);
                    break;
                
                case BISHOP:
                
                    bishopCount++;
                    if (bishopCount > 1) {
                        /* more than one bishop */
                        value += BISHOP_DOUBLE_BONUS + BISHOP_BASIC_VALUE 
                                                        + bishopBonus(piece);
                        break;
                    }
                    value += BISHOP_BASIC_VALUE + bishopBonus(piece);
                    break;
                
                case KNIGHT:
                    
                    value += KNIGHT_BASIC_VALUE + knightBonus(piece.getCoord());
                    break;
                
                case ROOK:
                
                    if (otherRookFile == piece.getCoord().getY()) {
                        /* rooks on same file */
                        value += ROOK_DOUBLE_FILE_BONUS + ROOK_BASIC_VALUE 
                                                            + rookBonus(piece);
                        break;
                    } else {
                        otherRookFile = piece.getCoord().getY();
                    }

                    value += ROOK_BASIC_VALUE + rookBonus(piece);
                    break;
                
                case KING:
                    value += kingBonus(piece);
            }
        }
        return value;
    }

    /**
     * Generates all legal moves as stated by rules class for all pieces in this
     * game state.
     * 
     * @param pieces    pieces to generate all legal moves for
     * @return          list of all legal moves in this game
     */
    private ArrayList<Move> allPossibleMoves(ArrayList<Piece> pieces) {
        
        ArrayList<Move> allMoves = new ArrayList<>();
        for (Piece piece : pieces) {
            allMoves.addAll(rules.getAllLegalMoves(piece));
        }
        return allMoves;
    }

    /**
     * Builds and evaluates the tree for this position and returns its value.
     * Calculation is recursive: All legal moves are generated, executed and
     * evaluated. Then the value of the best scoring move is returned.
     * 
     * 
     * @param chessTree     tree of possible moves of current position
     * @param alpha         alpha value of alpha-beta pruning
     * @param beta          beta value of alpha-beta pruning
     * @param colorToMove   color to move from this position
     * @param depth         stores number of plies that are left to evaluate
     * @param allowNullMove stores if null move is allowed in evaluation
     * @return              value of best scoring move                  
     */
    private int builtTreeAndEvaluate(ChessTreeNode chessTree, int alpha,
            int beta, ChessColor colorToMove, int depth, boolean allowNullMove){

        visitedNodes++;

        /* fast check for repetition to avoid threefold repetition */
        if (fastRepetitionCheck(board.getHashValue())) {
            return DRAW_THRESHOLD_VALUE;
        }

        /* check if position is already in transposition table */
        //TransTableEntry auxEntry = null;
        TransTableEntry auxEntry = transTable.getEntryByZobrisKey(board
                                                               .getHashValue());
        Move tableBestMove = null;
        if (auxEntry != null) {
            /* if the searched depth of plies of the transposition table is 
            higher or equal to the still required depth, the entry can be used 
             */
            if (auxEntry.getDepth() >= depth) {

                transpositionsUsed++;
                int entryValue = auxEntry.getValue();

                switch (auxEntry.getEvaluationFlag()) {

                    case EXACT:
                        return entryValue;

                    case ALPHA:
                        if (entryValue <= alpha) {
                            return entryValue;
                        }
                        break;

                    case BETA:
                        if (entryValue >= beta) {
                            return entryValue;
                        }
                        break;
                }
            }
            /* Store best move calculated in this position to improve 
            alpha-beta-pruning by being evaluated first. */
            tableBestMove = auxEntry.getBestMove();
        }


        /* leaf node */
        if (depth <= 0) 
        /* only evaluate quiet positions to avoid horizon effect */ {
            return quiescenceSearch(chessTree, alpha, beta, depth, colorToMove);
        } /* inner node */
        else {

            int gameValue;

            /* conduct a null-move search */
            if (allowNullMove && ownGame.isNotInCheck(colorToMove)) {
                ownGame.executeNullMove();
                gameValue = -builtTreeAndEvaluate(new ChessTreeNode(null), 
                                 -beta, -beta + 1, colorToMove.getInverse(), 
                                        depth - NULLMOVE_REDUCTION - 1, false);
                ownGame.unexecuteNullMove();
                if (gameValue >= beta) {
                    return gameValue;
                }
            }

            /* value of the position to be returned after evaluation */
            gameValue = -MATEVALUE - depth;

            /* store best Move found in evaluation of this node and the 
            evaluation flag to be stored in the transposition table */
            Move bestMove = null;
            EvaluationFlag evalFlag = EXACT;
            boolean anyMoveBeatAlpha = false;

            /* Create all possible moves as children of the node 
               if not already done. */
            if (!chessTree.hasChildren()) {
                createChildrenFromPieceList(chessTree, colorToMove);
                /* no  legal moves: not in check => stalemate, in check => 
                    checkmate */
                if (!chessTree.hasChildren()) {
                    if (ownGame.isNotInCheck(colorToMove)) {
                        return 0;
                    } else {
                        return gameValue;
                    }
                }
            }

            /* sort all moves to improve cutoff of alpha-beta-pruning */
            sortNodes(chessTree, depth);

            /* Move best move from table to front of node list if it is a valid
                move.
             */
            if(tableBestMove != null) chessTree.moveNodeToFront(tableBestMove);

            boolean firstChild = true;
            /* build nodes recursively with a cutoff through alpha-beta-pruning 
             */
            for (ChessTreeNode node : chessTree.getChildren()) {
                
                /* Stop calculation of node if thread was cancelled */
                if(moveCalculation.isCancelled()) break;
                
                ownGame.executeMove(node.getMove(), false);
                if (firstChild) {
                    gameValue = -builtTreeAndEvaluate(node, -beta, -alpha, 
                                    colorToMove.getInverse(), depth - 1, true);
                    firstChild = false;
                } 
                /* zero window search to quickly prove that first move is best*/ 
                else {
                    gameValue = -builtTreeAndEvaluate(node, -alpha - 1, -alpha, 
                                    colorToMove.getInverse(), depth - 1, true);
                    /* research if alpha < gameValue < beta */
                    if (gameValue > alpha && gameValue < beta) {
                        gameValue = -builtTreeAndEvaluate(node, -beta,
                                -alpha, colorToMove.getInverse(), depth - 1, 
                                true);
                    }
                }
                ownGame.unexecuteMove(node.getMove());
                /* alpha = max(alpha, gamevalue) and store best move */
                if (gameValue > alpha) {
                    alpha = gameValue;
                    anyMoveBeatAlpha = true;
                    bestMove = node.getMove();
                }
                if (alpha >= beta) {
                    /* store the refutation move as killer move to use
                           at same depth */
                    if (node.getMove().getMoveType() != TAKE) {
                        //TODO: node.getDepth = depth+1 ?
                        storeKillerMove(node.getMove(), depth);
                    }
                    evalFlag = BETA;
                    break;
                }

            }
            //TODO: what if no move beats alpha?
            /* store position in transposition table */
            if (!anyMoveBeatAlpha) {
                evalFlag = ALPHA;
            }
            transTable.insertEntry(new TransTableEntry(board.getHashValue(),
                    alpha, depth, bestMove, evalFlag));

            return alpha;
        }
    }

    @Override
    public void getNextMove() {
        
        Timer timer = new Timer();         
        
        /* AI move calculation is done on a separate thread */     
        moveCalculation = new SwingWorker<Move, Void>() {
            
            @Override
            public Move doInBackground() {
                
                /* Play first move in creator mode */
                if (peterMode && firstMove) {
                    
                    firstMove = false;
                    if (ownColor == BLACK) {
                        return new Move(PAWN, new Coordinate(6, 6), 
                                new Coordinate(5, 6), NORMAL);
                    } else {
                        return new Move(PAWN, new Coordinate(1, 6), 
                                new Coordinate(2, 6), NORMAL);
                    }
                }

                /* measure time to calculate move */
                start = Instant.now();
                
                /* search function depends on restriction: time or depth */
                if(CALC_TYPE == TIME){                    
                    
                    iterDeepSearchTime();
                    /* stop timer when calculation has ended */
                    timer.cancel();
                
                }
                else iterDeepSearchDepth(SEARCH_DEPTH);
                
                
                return storedBestMove;
            }

            @Override
            public void done() {
                
                analyticsLabel.append(currentLabel);
               // analyticsWriter.writeAnalyticsTurn(currentLabel);
                resetAnalytics();
                transTable.clear();
                
                try {
                    controller.nextMove(ownColor, get());
                } catch (CancellationException ex) {
                    controller.nextMove(ownColor, storedBestMove);
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(ChessAI.class.getName())
                                                   .log(Level.SEVERE, null, ex);
                }
            }
        };

        moveCalculation.execute();
        
        if(CALC_TYPE == TIME){
            /* Cancel move execution after specified time */
            timer.schedule(new TimerTask(){
                @Override
                public void run() {
                    moveCalculation.cancel(false);
                }
            }, TURN_TIME);
        }
    }

    /**
     * Calculates value for this piece as stated by the static evaluation 
     * function. 
     * 
     * @param bishop    piece to be evaluated
     * @return          value of static evaluation function
     */
    private int bishopBonus(Piece bishop) {
        
        int bonus = 0;
        
        /* is the bishop blocked by pawns in front of him? */
        for (Coordinate auxCoord : bishop.getCoord().getDiagCoordFront(bishop
                                                                 .getColor())) {
            if (board.getPieceTypeOnCoord(auxCoord) == PAWN) {
                bonus -= BISHOP_DIAG_PAWN_MALUS;
            }
        }
        if (bishop.getColor() == BLACK) {
            bonus += BLACK_BISHOP_BONUS_MATRIX[bishop.getCoord().getX()][bishop
                                                            .getCoord().getY()];
        } else {
            bonus += WHITE_BISHOP_BONUS_MATRIX[bishop.getCoord().getX()][bishop
                                                            .getCoord().getY()];
        }
        return bonus;
    }

    /**
     * Calculates value for the piece on this coordinate as stated by the static 
     * evaluation function. 
     * 
     * @param coord     cord of the piece to be evaluated
     * @return          value of static evaluation function
     */
    private int knightBonus(Coordinate coord) {
        
        return KNIGHT_BONUS_MATRIX[coord.getX()][coord.getY()];
    }

    /**
     * Calculates value for this piece as stated by the static evaluation 
     * function. 
     * 
     * @param pawn    piece to be evaluated
     * @return        value of static evaluation function
     */    
    private int pawnBonus(Piece pawn) {
        
        int bonus = 0;
        int file = pawn.getCoord().getY();
        /* doubled pawns */
        if (board.getPawnStruct(pawn.getColor(), file) > 1) {
            bonus -= PAWN_DOUBLE_MALUS;
        }
        /* isolated pawn */
        if (board.getPawnStruct(pawn.getColor(), file - 1) == 0
                && board.getPawnStruct(pawn.getColor(), file + 1) == 0) {
            bonus -= PAWN_ISOLATED_MALUS;
        }
        /* pawn position */
        if (pawn.getColor() == BLACK) {
            bonus += BLACK_PAWN_BONUS_MATRIX[pawn.getCoord().getX()][file];
        } else {
            bonus += WHITE_PAWN_BONUS_MATRIX[pawn.getCoord().getX()][file];
        }

        return bonus;
    }

    /**
     * Calculates value for this piece as stated by the static evaluation 
     * function. 
     * 
     * @param rook    piece to be evaluated
     * @return        value of static evaluation function
     */    
    private int rookBonus(Piece rook) {
        
        int bonus = 0;
        int file = rook.getCoord().getY();

        /* open file */
        if (board.getPawnStruct(rook.getColor(), file) == 0) {
            if (board.getPawnStruct(rook.getColor().getInverse(), file) == 0) {
                bonus += ROOK_OPEN_FILE_BONUS;
            } else {
                bonus += ROOK_HALF_OPEN_FILE_BONUS;
            }
        }
        /* rook position */
        if (rook.getColor() == BLACK) {
            bonus += BLACK_ROOK_BONUS_MATRIX[rook.getCoord().getX()][file];
        } else {
            bonus += WHITE_ROOK_BONUS_MATRIX[rook.getCoord().getX()][file];
        }
        return bonus;
    }

    /**
     * Calculates value for this piece as stated by the static evaluation 
     * function. 
     * 
     * @param queen     piece to be evaluated
     * @return          value of static evaluation function
     */
    private int queenBonus(Piece queen) {
        
        int bonus = 0;
        Coordinate coord = queen.getCoord();
        if (queen.getColor() == BLACK) {
            bonus += QUEEN_BONUS_MATRIX[coord.getX()][coord.getY()];
        } else {
            bonus += QUEEN_BONUS_MATRIX[7 - coord.getX()][7 - coord.getY()];
        }
        return bonus;
    }

    /**
     * Calculates value for this piece as stated by the static evaluation 
     * function. 
     * 
     * @param king      piece to be evaluated
     * @return          value of static evaluation function
     */
    private int kingBonus(Piece king) {
        
        /* different matrices are used depending on stage of game */
        if (gameStage == ENDGAME) {
            return KING_BONUS_MATRIX_ENDGAME[king.getCoord().getX()]
                                                       [king.getCoord().getY()];
        } else {
            if (king.getColor() == BLACK) {
               return BLACK_KING_BONUS_MATRIX_MIDDLEGAME[king.getCoord().getX()]
                                                       [king.getCoord().getY()];
            } else {
               return WHITE_KING_BONUS_MATRIX_MIDDLEGAME[king.getCoord().getX()]
                                                       [king.getCoord().getY()];
            }
        }
    }

    private boolean evaluateDraw() {
        return ownGame.isDraw(false);
    }

    /**
     * Gets the best variation from transposition table for current position.
     * 
     * @return      best variation as list of plies for current position
     */
    private ArrayList<Move> bestVariation() {
        ArrayList<Move> list = new ArrayList<>();
        TransTableEntry auxEntry = transTable.getEntryByZobrisKey(board
                                                               .getHashValue());
        int counter = -1;

        while (auxEntry != null && counter < 8) {

            if (auxEntry.getBestMove() == null) {
                break;
            }
            list.add(auxEntry.getBestMove());
            /* execute move to get zobris key of next position to be searched
                for best move
            */ 
            board.executeMove(auxEntry.getBestMove());
            counter++;
            auxEntry = transTable.getEntryByZobrisKey(board.getHashValue());
        }

        for (int i = counter; i >= 0; i--) {
            board.unexecuteMove(list.get(i));
        }

        return list;
    }

    /**
     * Builds a string with the current state of the AI analytics.
     * 
     * @param depth     analytics for this label calculated to this depth
     */
    private String builtCurrentAnalytics(int depth) {
        
        String label = "";
        label = label.concat("Search duration: " + 
                                           ((float)moveDuration)/1000 + " sec");
        label = label.concat("\nBest variation: " + storedBestVariation);

        int gameValue = storedBestValue;
        if (gameValue > 10000) {
            label = label.concat("\nGame value: " + ownColor 
               + " mates in " + (depth - gameValue + MATEVALUE + 1) / 2);
        } else if (gameValue < -10000) {
            label = label.concat("\nGame value: " 
                    + ownColor.getInverse() + " mates in " 
                                 + (depth + gameValue + MATEVALUE) / 2);
        } else {
            label = label.concat("\nGame value: " 
                                    + String.format("%.2f", 0.01 * gameValue));
        }
        label = label.concat("\nEvaluated positions: " 
                                                          + evaluatedPositions);
        label = label.concat("\nVisited nodes: " + visitedNodes
                + ", visited quiet nodes: " + visitedQuietNodes);
        label = label.concat("\nTransp. used: " 
                                                          + transpositionsUsed);
        label = label.concat("\nTransp. Table entries: " 
               + transTable.getHashFilled() + " / " + TRANSPOSITION_TABLE_SIZE);
        label = label.concat("\nIterated to depth: " 
                                                             + iterationDepth);
        label = label.concat("\nMaximum Depth: " 
                                    + (Math.abs(reachedDepth) + depth));
        label = label.concat("\nNodes per second: "
                +(visitedNodes+visitedQuietNodes)/(moveDuration+1) +"k");
        label = label.concat("\nEvaluated Positions per second: "+
                                     (evaluatedPositions/(moveDuration+1)+"k"));
        label = label.concat("\n\n");
        
        return label;        
    }

    /**
     * Resets all AI analytics fields.
     */
    private void resetAnalytics(){
        
        evaluatedPositions = 0;
        visitedNodes = 0;
        visitedQuietNodes = 0;
        transpositionsUsed = 0;
        iterationDepth = 0;
        reachedDepth = 0;        
        moveDuration = 0;
    }
   
    /**
     * Calculates value of current position in tree by only using quiet 
     * positions. Quiet positions are positions in which the king is not in
     * check and there are no captures. If a position is not quiet, all takes
     * are considered (all legal moves in case of check position) and executed.
     * This is done recursively until a quiet position (or the maximum depth as
     * stated in the options) is reached which is then evaluated by the static 
     * evaluation function.
     * 
     * @param chessTree     tree representing current position
     * @param alpha         alpha value of alpha-beta pruning    
     * @param beta          beta value of alpha-beta pruning 
     * @param depth         stores number of plies that are left to evaluate
     * @param colorToMove   color to move in this position
     * @return              value of best scoring move
     */
    private int quiescenceSearch(ChessTreeNode chessTree, int alpha, int beta,
            int depth, ChessColor colorToMove) {

        /* posValue represents the board value if one would do nothing, if 
        the position is far enough ahead no captures need to be considered */
        int posValue = evaluateBoard(colorToMove);
        int gameValue;

        reachedDepth = min(reachedDepth, depth);

        /* check if stand pat is already better than threshold alpha or beta */
        if (posValue >= beta) {
            return beta;
        }
        alpha = max(alpha, posValue);

        if (depth >= -QUIET_SEARCH_DEPTH) {

            boolean anyMove = false;

            /* Calculate all possible moves if not already done.
             */                
            if (!chessTree.hasChildren()) {
                createChildrenFromPieceList(chessTree, colorToMove);
                /* no  legal moves: not in check => stalemate, in check => 
                    checkmate */
                if (!chessTree.hasChildren()) {
                    if (ownGame.isNotInCheck(colorToMove)) {
                        return DRAW_THRESHOLD_VALUE;
                    }
                    if (colorToMove == ownColor) {
                        return -MATEVALUE - depth;
                    } else {
                        return MATEVALUE - depth;
                    }
                }
            }

            ArrayList<ChessTreeNode> quietList = chessTree.getChildren();

            /*If player is not in check, consider only takes. Otherwise, take
            all moves into consideration */
            if (ownGame.isNotInCheck(colorToMove)) {
                quietList = createTakeList(quietList, true);
                quietList.sort(new SortByMVVLVA());
            } else {
                quietList.sort(new SortingMovesWithoutKiller());
            }

            /* Move best move from transposition table to front of node list if 
                it is a valid move.
             */
            for (ChessTreeNode node : quietList) {

                /* Stop calculation of node if thread was cancelled */
                if(moveCalculation.isCancelled()) break;
                
                visitedQuietNodes++;
                anyMove = true;
                ownGame.executeMove(node.getMove(), false);
                gameValue = -quiescenceSearch(node, -beta, -alpha, depth - 1, 
                                                      colorToMove.getInverse());
                ownGame.unexecuteMove(node.getMove());
                /* beta cutoff */
                if (gameValue >= beta) {
                    return beta;
                }
                alpha = max(gameValue, alpha);
            }
            if (!anyMove) {
                return posValue;
            }
            return alpha;
        } else {
            return posValue;
        }
    }

    /**
     * Creates and adds child nodes of all legal moves for current position to 
     * given tree.  
     * 
     * @param chessTree     nodes will be added to this tree
     * @param color         color of the pieces to generate legal moves for
     */
    private void createChildrenFromPieceList(ChessTreeNode chessTree, 
                                                            ChessColor color) {
        ArrayList<Move> allMoves;
        if (color == ownColor) {
            allMoves = allPossibleMoves(ownPieces);
        } else {
            allMoves = allPossibleMoves(enemyPieces);
        }

        chessTree.addChildNodes(allMoves);
    }
    
    /**
     * Creates a frame to display AI analytics to user.
     */
    private void createAnalyticsFrame() {
        
        analyticsView = new JFrame("AI Analytics");
        analyticsView.setSize(315, 700);
        JScrollPane analyticsScroll = new JScrollPane(analyticsLabel, 
                        VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_NEVER);

        analyticsView.add(analyticsScroll);
        analyticsView.setVisible(true);
    }

    /**
     * Filters a list of nodes to contain only one type of move (captures or
     * non-captures). If mode is true the returned list will only contain 
     * captures, otherwise it will only contain non-captures. Method does not
     * alter the given list, it creates a new object.
     * 
     * @param nodeList      list to be filtered
     * @param mode          mode to operate
     * @return              filtered list (only captures or only non-captures)
     */
    private ArrayList<ChessTreeNode> createTakeList(
                            ArrayList<ChessTreeNode> nodeList, boolean mode) {

        ArrayList<ChessTreeNode> takeList = new ArrayList<>();

        for (ChessTreeNode node : nodeList) {
            if (mode) {
                if (node.getMove().getMoveType() == TAKE) {
                    takeList.add(node);
                }
            } else if (node.getMove().getMoveType() != TAKE) {
                takeList.add(node);
            }
        }

        return takeList;
    }

    /**
     * Checks the game for three times repetition of the position given by its
     * hash value.
     * 
     * @param hashValue     hash value of position to be checked
     * @return              
     */
    private boolean fastRepetitionCheck(long hashValue) {
        LinkedList<Long> positions = ownGame.getRecentPositions();
        int counter = 0;
        for (int i = positions.size() - 3; i >= 0; i -= 2) {
            if (positions.get(i) == hashValue) {
                if (counter < 1) {
                    counter++;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Writes move found by the killer heuristic in the killer heuristic array.
     *
     * @param move      move to be stored in killer heuristic array
     * @param depth     depth at which the move was found
     */
    private void storeKillerMove(Move move, int depth) {
        if (move.equals(killerMoves[depth - 1][0])) {
            return;
        }
        killerMoves[depth - 1][1] = killerMoves[depth - 1][0];
        killerMoves[depth - 1][0] = move;
    }

    /**
     * Sets the values of AI constants as stated by the AIOptions.
     * 
     * @param aiOptions     the options to be implemented
     */
    private void setOptions(AIOptions aiOptions) {
        
        SEARCH_DEPTH = aiOptions.getSearchDepth();
        QUIET_SEARCH_DEPTH = aiOptions.getQuietSearchDepth();
        peterMode = aiOptions.isPeterMode();
        TURN_TIME = aiOptions.getTurnTime();
        CALC_TYPE = aiOptions.getCalcType();
    }

    @Override
    public void endGame(ChessGameEndType result) {
        
        if(moveCalculation != null) moveCalculation.cancel(true);
        analyticsView.dispose();
       // analyticsWriter.endGame();
    }

    /**
     * Conducts a search of current position with iterative deepening restricted 
     * by time. The first search is always 2 plies deep. Then the search is 
     * deepened one ply at a time until the maximal depth is reached.
     * 
     */
    private void iterDeepSearchTime() {

        int depth = 2;
        
        while(!moveCalculation.isCancelled()) {
            
            builtTreeAndEvaluate(currentTree, -Integer.MAX_VALUE, 
                                      Integer.MAX_VALUE, ownColor, depth, true);
            
            /* get entry from transposition table for current position, best
                move and value of position is stored there
            */
            TransTableEntry currentPosition = 
                           transTable.getEntryByZobrisKey(board.getHashValue());
            
            /* if bestMove exists (move calculation was not aborted), store 
               analytics of that move
            */
            if(!moveCalculation.isCancelled()){
                
                iterationDepth = max(iterationDepth, depth);
                storedBestMove = currentPosition.getBestMove();
                storedBestValue = currentPosition.getValue();                                 
                storedBestVariation = bestVariation();
                moveDuration = Duration.between(start, Instant.now()).toMillis();            
                currentLabel = builtCurrentAnalytics(depth);                                       
                
                /* if search found forced mate, don't iterate deeper */
                if(currentPosition.getValue() >= MATEVALUE ||
                        currentPosition.getValue() <= -MATEVALUE) 
                    break;
            } 
 
            /* increase depth by 1 for next search */
            depth += 1;
        }
    }

    /**
     * Conducts a search of current position with iterative deepening restricted 
     * by fixed depth. The first search is always 2 plies deep. Then the search 
     * is deepened one ply at a time until the maximal depth is reached. Search 
     * depth can be reduced by one if a quiescence search explosion is detected.
     * 
     * @param searchDepth   maximal depth to search at
     */
    private void iterDeepSearchDepth(int searchDepth) {

        boolean quietExplosionFlag = false;

        for (int depth = 2; depth <= searchDepth; depth++) {
            
            if(moveCalculation.isCancelled()) break;
            
            builtTreeAndEvaluate(currentTree, -Integer.MAX_VALUE, 
                                      Integer.MAX_VALUE, ownColor, depth, true);

            /* get entry from transposition table for current position, best
                move and value of position is stored there
            */
            TransTableEntry currentPosition = 
                           transTable.getEntryByZobrisKey(board.getHashValue());
            
            /* if bestMove exists (move calculation was not aborted), store 
               analytics of that move
            */
            if(!moveCalculation.isCancelled()){
                
                iterationDepth = max(iterationDepth, depth);
                storedBestMove = currentPosition.getBestMove();
                storedBestValue = currentPosition.getValue();                                 
                storedBestVariation = bestVariation();
            
            }
            moveDuration = Duration.between(start, Instant.now()).toMillis();            
            currentLabel = builtCurrentAnalytics(iterationDepth); 
            
            /* if search found forced mate, don't iterate deeper */
            if(currentPosition.getValue() >= MATEVALUE ||
                        currentPosition.getValue() <= -MATEVALUE) 
                break;              
            
            /* If the ratio visited nodes in quiescence search to visited 
            regular nodes is larger than 3 on any of the last 2 plies, a quiet 
            search explosion seems to be likely (based on experience). In that 
            case reduce the number of iterations (and final depth) by one.
             */

            if (depth >= SEARCH_DEPTH - 2 && visitedQuietNodes / 
                                                            visitedNodes >= 3) 
            {
                quietExplosionFlag = true;
            }
            if (quietExplosionFlag && visitedNodes + visitedQuietNodes > 200000 
                                                  && depth == searchDepth - 1) 
            {
                break;
            }
        }
    }
    
    
    /**
     * Sorts children of a tree by stated rules.
     *  -taking moves, sorted by MVVLVA
     *  -killer moves of killer heuristic
     *  -all other unsorted moves
     * 
     * @param chessTree     tree to be sorted
     * @param depth         depth of the tree
     */
    private void sortNodes(ChessTreeNode chessTree, int depth) {

        /* List that contains only the taking moves from nodeList */
        ArrayList<ChessTreeNode> takeList = createTakeList(chessTree.
                                                           getChildren(), true);
        takeList.sort(new SortByMVVLVA());

        ChessTreeNode auxNode = null;

        /* List that contains all NORMAL moves from nodeList */
        ArrayList<ChessTreeNode> regularList = createTakeList(chessTree
                                                        .getChildren(), false);

        /* add killer moves */
        ListIterator<ChessTreeNode> iter = regularList.listIterator();
        while (iter.hasNext()) {
            ChessTreeNode node = iter.next();
            if (depth > 0 && node.getMove().equals(killerMoves[depth - 1][0])) 
            {
                takeList.add(node);
                iter.remove();
            } else if (depth > 0 && node.getMove().equals(
                                                   killerMoves[depth - 1][1])) 
            {
                auxNode = node;
                iter.remove();
            }
        }
        /* add second killer move */
        if (auxNode != null) {
            takeList.add(auxNode);
        }

        /*add all remaining regular moves to takeList */
        takeList.addAll(regularList);

        chessTree.setChildren(takeList);
    }

    @Override
    public String getPlayerName() {
        return "ChessAI";
    }

    /**
     * Comparator to sort the nodes of a ChessTree by MVVLVA (most valuable 
     * victim - least valuable attacker).
     */
    class SortByMVVLVA implements Comparator<ChessTreeNode> {

        @Override
        public int compare(ChessTreeNode a, ChessTreeNode b) {
            return score(b) - score(a);
        }

        private int score(ChessTreeNode node) {
            return board.getPieceTypeOnCoord(node.getMove().getCoordTo())
                    .getMaterialValue() - node.getMove().getPieceType()
                                                           .getMaterialValue();
        }
    }

    /**
     * Comparator to sort the nodes of a ChessTree. Captures are sorted to the 
     * front of the list, sorted by MVVLVA.
     */
    class SortingMovesWithoutKiller implements Comparator<ChessTreeNode> {

        @Override
        public int compare(ChessTreeNode a, ChessTreeNode b) {
            return score(b) - score(a);
        }

        private int score(ChessTreeNode node) {

            Move move = node.getMove();

            /* Taking moves get +1000 score, also additional score for MVVLVA */
            if (move.getMoveType() == TAKE) {
                return 1000 + board.getPieceTypeOnCoord(move.getCoordTo())
                    .getMaterialValue()- move.getPieceType().getMaterialValue();
            }
            else return 0;
        }
    }
}
