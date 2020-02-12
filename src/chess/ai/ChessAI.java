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
import static chess.move.MoveType.NORMAL;
import static chess.move.MoveType.TAKE;
import chess.options.AIOptions;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;
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
    private int QUIET_SEARCH_DEPTH;
    private static final int TRANSPOSITION_TABLE_SIZE = 200000;
    private static final int DRAW_THRESHOLD_VALUE = -50;
    private static final int MATEVALUE = Integer.MAX_VALUE / 2;
    private static final int NULLMOVE_REDUCTION = 2;

    /* constants for board evaluation */
    private static final int BISHOP_BASIC_VALUE = 310;
    private static final int BISHOP_DOUBLE_BONUS = 20;
    private static final int BISHOP_DIAG_PAWN_MALUS = 20;

    private static final int ROOK_BASIC_VALUE = 500;
    private static final int ROOK_DOUBLE_FILE_BONUS = 20;
    private static final int ROOK_OPEN_FILE_BONUS = 15;
    private static final int ROOK_HALF_OPEN_FILE_BONUS = 10;

    private static final int PAWN_BASIC_VALUE = 100;
    private static final int PAWN_DOUBLE_MALUS = 15;
    private static final int PAWN_ISOLATED_MALUS = 15;

    private static final int KNIGHT_BASIC_VALUE = 310;

    private static final int QUEEN_BASIC_VALUE = 900;

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
    private int evaluatedPositions = 0;
    private int visitedNodes = 0;
    private int visitedQuietNodes = 0;
    private int transpositionsUsed = 0;
    private int reachedDepth = 0;
    private int iterationDepth = 0;
    private long moveDuration = 0;
    private final JTextArea analyticsLabel = new JTextArea();

    /* regular AI fields */
    private final GameController controller;
    private final ChessGame ownGame;
    private final ChessRules rules;
    private final Board board;
    private final ArrayList<Piece> ownPieces;
    private final ArrayList<Piece> enemyPieces;
    private final ChessColor ownColor;
    private final ChessTransTable transTable;
    private Move storedBestMove;
    private int storedBestValue;
    private ChessTreeNode currentTree;
    private ChessGameStage gameStage;
    private final Move[][] killerMoves;
    private SwingWorker moveCalculation;
    private boolean firstMove = true;
    private boolean peterMode;

    public ChessAI(GameController controller, ChessColor ownColor, AIOptions aiOptions) {
        this.controller = controller;
        this.ownGame = new ChessGame();
        this.board = ownGame.getBoard();
        this.rules = ownGame.getRules();
        this.ownColor = ownColor;
        this.ownPieces = board.getPiecesList(ownColor);
        this.enemyPieces = board.getPiecesList(ownColor.getInverse());
        this.transTable = new ChessTransTable(TRANSPOSITION_TABLE_SIZE);

        setOptions(aiOptions);

        /* create root of tree */
        this.currentTree = new ChessTreeNode(null);

        gameStage = OPENING;
        killerMoves = new Move[SEARCH_DEPTH + 1][2];
        createAnalyticsFrame();
    }

    public ChessAI(GameController controller, ChessColor ownColor, AIOptions aiOptions, Piece[][] pieceArray, ChessColor colorToMove, boolean[] castleRights) {
        this.controller = controller;
        this.ownGame = new ChessGame(pieceArray, colorToMove, castleRights);
        this.board = ownGame.getBoard();
        this.rules = ownGame.getRules();
        this.ownColor = ownColor;
        this.ownPieces = board.getPiecesList(ownColor);
        this.enemyPieces = board.getPiecesList(ownColor.getInverse());
        this.transTable = new ChessTransTable(TRANSPOSITION_TABLE_SIZE);

        /* create root of tree */
        this.currentTree = new ChessTreeNode(null);

        setOptions(aiOptions);

        gameStage = OPENING;
        killerMoves = new Move[SEARCH_DEPTH + 1][2];
        createAnalyticsFrame();
    }

    @Override
    public void update(ChessGame game, Move lastMove, Object arg) {
        if (lastMove != null) {
            ownGame.executeMove(lastMove, false);
            /* if enemy queen is traded, change gamestage flag to endgame */
            gameStage = ENDGAME;
            for (Piece piece : enemyPieces) {
                if (piece.getPiecetype() == QUEEN) {
                    gameStage = MIDDLEGAME;
                }
            }
            if (currentTree.hasChildren()) {
                currentTree = currentTree.getSubTreeByMove(lastMove);
            }
        }
    }

    private int evaluateBoard(ChessColor colorToMove) {
        evaluatedPositions++;
        if (colorToMove == ownColor) {
            return pieceValue(ownPieces) - pieceValue(enemyPieces);
        } else {
            return pieceValue(enemyPieces) - pieceValue(ownPieces);
        }
    }

    private int pieceValue(ArrayList<Piece> pieces) {
        int value = 0;
        int bishopCount = 0;
        int otherRookFile = -1;

        for (Piece piece : pieces) {
            switch (piece.getPiecetype()) {
                case PAWN:
                    value += PAWN_BASIC_VALUE + pawnBonus(piece);
                    break;
                case QUEEN:
                    /* distance to enemy king, weighted with 0.5 */
                    value += QUEEN_BASIC_VALUE - 0.5 * (piece.getCoord().distance(
                            board.getKing(piece.isColor().getInverse()).getCoord()));
                    //value += queenBonus(piece);
                    break;
                case BISHOP:
                    bishopCount++;
                    if (bishopCount > 1) {
                        /* more than one bishop */
                        value += BISHOP_DOUBLE_BONUS + BISHOP_BASIC_VALUE + bishopBonus(piece);
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
                        value += ROOK_DOUBLE_FILE_BONUS + ROOK_BASIC_VALUE + rookBonus(piece);
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

    private ArrayList<Move> allPossibleMoves(ArrayList<Piece> pieces) {
        ArrayList<Move> allMoves = new ArrayList<>();
        for (Piece piece : pieces) {
            allMoves.addAll(rules.getPossibleMoves(piece));
        }
        return allMoves;
    }

    /* Builds and evaluates the tree for this position and returns its value
     */
    private int builtTreeAndEvaluate(ChessTreeNode chessTree, int alpha,
            int beta, ChessColor colorToMove, int depth, boolean allowNullMove) {

        visitedNodes++;

        /* fast check for repetition to avoid threefold repetition */
        if (fastRepetitionCheck(board.getHashValue())) {
            return DRAW_THRESHOLD_VALUE;
        }

        /* check if position is already in transposition table */
        //TransTableEntry auxEntry = null;
        TransTableEntry auxEntry = transTable.getEntryByZobrisKey(board.getHashValue());
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
        if (depth <= 0) /* only evaluate quiet positions to avoid horizon effect */ {
            return quiescenceSearch(chessTree, alpha, beta, depth, colorToMove);
        } /* inner node */ else {

            int gameValue;

            /* conduct a null-move search */
            if (allowNullMove && !ownGame.isInCheck(colorToMove)) {
                ownGame.executeNullMove();
                gameValue = -builtTreeAndEvaluate(new ChessTreeNode(null), -beta, -beta + 1,
                        colorToMove.getInverse(), depth - NULLMOVE_REDUCTION - 1, false);
                ownGame.unexecuteNullMove();
                if (gameValue >= beta) {
                    return gameValue;
                }
            }

            /* value of the position to be returned after evaluation */
            gameValue = -MATEVALUE - depth;

            /* store best Move found in evaluation of this node and the evaluation flag to be stored 
            in the transposition table
             */
            Move bestMove = null;
            EvaluationFlag evalFlag = EXACT;
            boolean anyMoveBeatAlpha = false;

            /* Create all possible moves as children of the node 
               if not already done.
             */
            if (!chessTree.hasChildren()) {
                createChildrenFromPieceList(chessTree, colorToMove);
                /* no  legal moves: not in check => stalemate, in check => 
                    checkmate */
                if (!chessTree.hasChildren()) {
                    if (!ownGame.isInCheck(colorToMove)) {
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
            chessTree.moveNodeToFront(tableBestMove);

            boolean firstChild = true;
            /* build nodes recursively with a cutoff through alpha-beta-pruning 
             */
            for (ChessTreeNode node : chessTree.getChildren()) {

                ownGame.executeMove(node.getMove(), false);
                if (firstChild) {
                    gameValue = -builtTreeAndEvaluate(node, -beta, -alpha, colorToMove.getInverse(), depth - 1, true);
                    firstChild = false;
                } /* zero window search to quickly prove that first move is best */ else {
                    gameValue = -builtTreeAndEvaluate(node, -alpha - 1, -alpha, colorToMove.getInverse(), depth - 1, true);
                    /* research if alpha < gameValue < beta */
                    if (gameValue > alpha && gameValue < beta) {
                        gameValue = -builtTreeAndEvaluate(node, -beta,
                                -alpha, colorToMove.getInverse(), depth - 1, true);
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
                    /* store the refutation move as killermove to use
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
        moveCalculation = new SwingWorker<Move, Void>() {
            @Override
            public Move doInBackground() {
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
                Instant start = Instant.now();
                iterativeDeepeningSearch(SEARCH_DEPTH);
                Instant finish = Instant.now();
                moveDuration = Duration.between(start, finish).getSeconds();
                if (iterationDepth < SEARCH_DEPTH) {
                    return storedBestMove;
                }
                return transTable.getEntryByZobrisKey(board.getHashValue())
                        .getBestMove();
            }

            @Override
            public void done() {
                printAndResetAnalytics();
                transTable.clear();
                try {
                    controller.nextMove(ownColor, get());
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(ChessAI.class.getName()).log(Level.SEVERE, 
                            null, ex);

                }
            }
        };

        moveCalculation.execute();
    }

    private int bishopBonus(Piece bishop) {
        int bonus = 0;

        for (Coordinate auxCoord : bishop.getCoord().getDiagCoordFront(bishop.isColor())) {
            if (board.getPieceTypeOnCoord(auxCoord) == PAWN) {
                bonus -= BISHOP_DIAG_PAWN_MALUS;
            }
        }
        if (bishop.isColor() == BLACK) {
            bonus += BLACK_BISHOP_BONUS_MATRIX[bishop.getCoord().getX()][bishop.getCoord().getY()];
        } else {
            bonus += WHITE_BISHOP_BONUS_MATRIX[bishop.getCoord().getX()][bishop.getCoord().getY()];
        }
        return bonus;
    }

    private int knightBonus(Coordinate coord) {
        return KNIGHT_BONUS_MATRIX[coord.getX()][coord.getY()];
    }

    private int pawnBonus(Piece pawn) {
        int bonus = 0;
        int file = pawn.getCoord().getY();
        /* doubled pawns */
        if (board.getPawnStruct(pawn.isColor(), file) > 1) {
            bonus -= PAWN_DOUBLE_MALUS;
        }
        /* isolated pawn */
        if (board.getPawnStruct(pawn.isColor(), file - 1) == 0
                && board.getPawnStruct(pawn.isColor(), file + 1) == 0) {
            bonus -= PAWN_ISOLATED_MALUS;
        }
        /* pawn position */
        if (pawn.isColor() == BLACK) {
            bonus += BLACK_PAWN_BONUS_MATRIX[pawn.getCoord().getX()][file];
        } else {
            bonus += WHITE_PAWN_BONUS_MATRIX[pawn.getCoord().getX()][file];
        }

        return bonus;
    }

    private int rookBonus(Piece rook) {
        int bonus = 0;
        int file = rook.getCoord().getY();

        /* open file */
        if (board.getPawnStruct(rook.isColor(), file) == 0) {
            if (board.getPawnStruct(rook.isColor().getInverse(), file) == 0) {
                bonus += ROOK_OPEN_FILE_BONUS;
            } else {
                bonus += ROOK_HALF_OPEN_FILE_BONUS;
            }
        }
        /* rook position */
        if (rook.isColor() == BLACK) {
            bonus += BLACK_ROOK_BONUS_MATRIX[rook.getCoord().getX()][file];
        } else {
            bonus += WHITE_ROOK_BONUS_MATRIX[rook.getCoord().getX()][file];
        }
        return bonus;
    }

    private int queenBonus(Piece queen) {
        int bonus = 0;
        Coordinate coord = queen.getCoord();
        if (queen.isColor() == BLACK) {
            bonus += QUEEN_BONUS_MATRIX[coord.getX()][coord.getY()];
        } else {
            bonus += QUEEN_BONUS_MATRIX[7 - coord.getX()][7 - coord.getY()];
        }
        return bonus;
    }

    private int kingBonus(Piece king) {
        if (gameStage == ENDGAME) {
            return KING_BONUS_MATRIX_ENDGAME[king.getCoord().getX()][king.getCoord().getY()];
        } else {
            if (king.isColor() == BLACK) {
                return BLACK_KING_BONUS_MATRIX_MIDDLEGAME[king.getCoord().getX()][king.getCoord().getY()];
            } else {
                return WHITE_KING_BONUS_MATRIX_MIDDLEGAME[king.getCoord().getX()][king.getCoord().getY()];
            }
        }
    }

    private boolean evaluateDraw() {
        return ownGame.isDraw(false);
    }

    private ArrayList<Move> bestVariation() {
        ArrayList<Move> list = new ArrayList<>();
        TransTableEntry auxEntry = transTable.getEntryByZobrisKey(board.getHashValue());
        int counter = -1;

        while (auxEntry != null && counter < 8) {

            if (auxEntry.getBestMove() == null /*|| auxEntry.getEvaluationFlag()!= EXACT*/) {
                break;
            }
            list.add(auxEntry.getBestMove());
            board.executeMove(auxEntry.getBestMove());
            counter++;
            auxEntry = transTable.getEntryByZobrisKey(board.getHashValue());
        }

        for (int i = counter; i >= 0; i--) {
            board.unexecuteMove(list.get(i));
        }

        return list;
    }

    private void printAndResetAnalytics() {
        analyticsLabel.append("Search duration: " + moveDuration + " sec");
        analyticsLabel.append("\nBest variation: " + bestVariation());
        TransTableEntry tableEntry = transTable.getEntryByZobrisKey(board.getHashValue());
        int gameValue = 0;
        if (tableEntry != null) {
            gameValue = tableEntry.getValue();
        }
        if (iterationDepth < SEARCH_DEPTH) {
            gameValue = storedBestValue;
        }
        if (gameValue > 10000) {
            analyticsLabel.append("\nGame value: " + ownColor + " mates in " + (SEARCH_DEPTH - gameValue + MATEVALUE + 1) / 2);
        } else if (gameValue < -10000) {
            analyticsLabel.append("\nGame value: " + ownColor.getInverse() + " mates in " + (SEARCH_DEPTH + gameValue + MATEVALUE) / 2);
        } else {
            analyticsLabel.append("\nGame value: " + String.format("%.2f", 0.01 * gameValue));
        }
        analyticsLabel.append("\nEvaluated positions: " + evaluatedPositions);
        analyticsLabel.append("\nVisited nodes: " + visitedNodes
                + ", visited quiet nodes: " + visitedQuietNodes);
        analyticsLabel.append("\nTransp. used: " + transpositionsUsed);
        analyticsLabel.append("\nTransp. Table entries: " + transTable.getHashFilled() + " / " + TRANSPOSITION_TABLE_SIZE);
        analyticsLabel.append("\nIterated to depth: " + iterationDepth);
        analyticsLabel.append("\nMaximum Depth: " + (Math.abs(reachedDepth) + SEARCH_DEPTH));
        analyticsLabel.append("\n\n");

        evaluatedPositions = 0;
        visitedNodes = 0;
        visitedQuietNodes = 0;
        transpositionsUsed = 0;
        iterationDepth = 0;
        reachedDepth = 0;

    }

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
                    if (!ownGame.isInCheck(colorToMove)) {
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

            /*If player is not in check, consider only takes. Otherwise take
            all moves into consideration */
            if (!ownGame.isInCheck(colorToMove)) {
                quietList = createTakeList(quietList, true);
                quietList.sort(new SortByMVVLVA());
            } else {
                quietList.sort(new SortingMovesWithoutKiller());
            }

            /* Move best move from transposition table to front of node list if 
                it is a valid move.
             */
            for (ChessTreeNode node : quietList) {

                visitedQuietNodes++;
                anyMove = true;
                ownGame.executeMove(node.getMove(), false);
                gameValue = -quiescenceSearch(node, -beta, -alpha, depth - 1, colorToMove.getInverse());
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

    private void createChildrenFromPieceList(ChessTreeNode chessTree, ChessColor color) {
        ArrayList<Move> allMoves;
        if (color == ownColor) {
            allMoves = allPossibleMoves((ArrayList<Piece>) ownPieces);
        } else {
            allMoves = allPossibleMoves((ArrayList<Piece>) enemyPieces);
        }

        for (Move move : allMoves) {
            chessTree.addChildNode(new ChessTreeNode(move));
        }
    }

    private void createAnalyticsFrame() {
        analyticsView = new JFrame("AI Analytics");
        analyticsView.setSize(315, 700);
        JScrollPane analyticsScroll = new JScrollPane(analyticsLabel, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_NEVER);

        analyticsView.add(analyticsScroll);
        analyticsView.setVisible(true);
    }

    /* creates a new list of moves with 
        - mode == true: only TAKE moves
        - mode == false: only non-TAKE move
     */
    private ArrayList<ChessTreeNode> createTakeList(ArrayList<ChessTreeNode> nodeList, boolean mode) {

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

    @Override
    public ChessGuiView getView() {
        return null;
    }

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

    private void storeKillerMove(Move move, int depth) {
        if (move.equals(killerMoves[depth - 1][0])) {
            return;
        }
        killerMoves[depth - 1][1] = killerMoves[depth - 1][0];
        killerMoves[depth - 1][0] = move;
    }

    private void setOptions(AIOptions aiOptions) {
        SEARCH_DEPTH = aiOptions.getSearchDepth();
        QUIET_SEARCH_DEPTH = aiOptions.getQuietSearchDepth();
        peterMode = aiOptions.isPeterMode();
    }

    @Override
    public void endGame() {
        //if(moveCalculation != null) moveCalculation.cancel(true);
        analyticsView.dispose();
    }

    private void iterativeDeepeningSearch(int searchDepth) {

        boolean quietExplosionFlag = false;

        for (int depth = 2; depth <= searchDepth; depth++) {
            builtTreeAndEvaluate(currentTree, -Integer.MAX_VALUE, Integer.MAX_VALUE, ownColor, depth, true);
            iterationDepth = max(iterationDepth, depth);
            storedBestMove = transTable.getEntryByZobrisKey(board.getHashValue()).getBestMove();
            storedBestValue = transTable.getEntryByZobrisKey(board.getHashValue()).getValue();
            //System.out.print("visited nodes: "+visitedNodes+" quiet nodes:"+ visitedQuietNodes+" "+(visitedQuietNodes/visitedNodes) );
            //System.out.print("\n");
            /* If the ratio visited nodes in quiescence search to visited 
            regular nodes is larger than 3 on any of the last 2 plies, a quiet 
            search explosion seems to be likely (based on experience). In that 
            case reduce the number of iterations (and final depth) by one.
             */
            if (depth >= SEARCH_DEPTH - 2 && visitedQuietNodes / visitedNodes >= 3) {
                quietExplosionFlag = true;
            }
            if (quietExplosionFlag && visitedNodes + visitedQuietNodes > 200000 && depth == searchDepth - 1) {
                break;
            }
        }
    }

    /* sorts chessTree by : 
        -taking moves, sorted by MVVLVA
        -killer moves 1 and 2
        -all other unsorted moves
     */
    private void sortNodes(ChessTreeNode chessTree, int depth) {

        /* List that contains only the taking moves from nodeList */
        ArrayList<ChessTreeNode> takeList = createTakeList(chessTree.getChildren(), true);
        takeList.sort(new SortByMVVLVA());

        ChessTreeNode auxNode = null;

        /* List that contains all NORMAL moves from nodeList */
        ArrayList<ChessTreeNode> regularList = createTakeList(chessTree.getChildren(), false);

        /* add killer moves */
        ListIterator<ChessTreeNode> iter = regularList.listIterator();
        while (iter.hasNext()) {
            ChessTreeNode node = iter.next();
            if (depth > 0 && node.getMove().equals(killerMoves[depth - 1][0])) {
                takeList.add(node);
                iter.remove();
            } else if (depth > 0 && node.getMove().equals(killerMoves[depth - 1][1])) {
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

    /* sort moves by most valuable victim - least valuable attacker
     */
    class SortByMVVLVA implements Comparator<ChessTreeNode> {

        @Override
        public int compare(ChessTreeNode a, ChessTreeNode b) {
            return score(b) - score(a);
        }

        private int score(ChessTreeNode node) {
            return board.getPieceTypeOnCoord(node.getMove().getCoordTo()).getMaterialValue()
                    - node.getMove().getPieceType().getMaterialValue();
        }
    }

    class SortingMovesWithoutKiller implements Comparator<ChessTreeNode> {

        @Override
        public int compare(ChessTreeNode a, ChessTreeNode b) {
            return score(b) - score(a);
        }

        private int score(ChessTreeNode node) {
            int score = 0;
            Move move = node.getMove();

            /* Taking moves get +1000 score, also additional score for 
                       MVVLVA */
            if (move.getMoveType() == TAKE) {
                score = +1000 + board.getPieceTypeOnCoord(move.getCoordTo()).getMaterialValue()
                        - move.getPieceType().getMaterialValue();
            }
            return score;
        }
    }
}
