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
import static chess.board.PieceType.*;
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

    //constants to control general AI behaviour
    private static final int SEARCH_DEPTH = 4;
    private static final int TRANSPOSITION_TABLE_SIZE = 10000;

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
            = {{-15, -10, -5, -5},
            {-10, -5, 0, 5},
            {-5, 0, 10, 10},
            {0, 5, 10, 15}};

    private static final int[][] PAWN_BONUS_MATRIX
            = {{0, 0, 0, 0, 0, 0, 0, 0},
            {20, 20, 30, 30, 30, 30, 20, 20},
            {15, 15, 20, 25, 25, 20, 15, 15},
            {0, 0, 15, 20, 20, 15, 0, 0},
            {0, 0, 10, 15, 15, 10, 0, 0},
            {0, 0, 0, 5, 5, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0}};

    private int evaluatedPositions = 0;

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
        this.currentTree = new ChessTreeNode(null, 0, null);
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
        if (evaluateDraw()) {
            return 0;
        }
        int value = 0;
        value += castlingBonus(ownColor);
        value += pieceValue(ownPieces) - pieceValue(enemyPieces);
        evaluatedPositions++;
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
        //this.currentTree= new ChessTreeNode(null,0, null);
        builtTree(currentTree, Integer.MIN_VALUE, Integer.MAX_VALUE, TRUE);
        ArrayList<Move> bestMoves = bestMovesFromTree(currentTree);
        //Random random = new Random();
        //return bestMoves.get(random.nextInt(bestMoves.size()));
        return bestMoves.get(0);
    }

    private int builtTree(ChessTreeNode chessTree, int alpha, int beta, boolean maximizing) {

        if (chessTree.getDepth() < SEARCH_DEPTH) {
            //try to reuse already calculated possible moves 
            ArrayList<Move> allMoves;
            //calculate all possible moves if not already done
            if (!chessTree.hasChildren()) {
                if (maximizing) {
                    allMoves = allPossibleMoves((ArrayList<Piece>) ownPieces);
                } else {
                    allMoves = allPossibleMoves((ArrayList<Piece>) enemyPieces);
                }
                for (Move move : allMoves) {
                    chessTree.addChildNode(new ChessTreeNode(move, Integer.MIN_VALUE, chessTree));
                }
            } //sort all moves to improve cutoff of alpha-beta-pruning
            else {
                Collections.sort(chessTree.getChildren());
            }

            //if the node has no legal moves => stalemate or mate
            if (!chessTree.hasChildren() && ownGame.isStalemate()) {
                chessTree.setGameValue(0);
                return 0;
            }

            //value which will be used in alpha-beta-pruning
            int auxValue;
            if (maximizing) {
                auxValue = Integer.MIN_VALUE;
            } else {
                auxValue = Integer.MAX_VALUE;
            }
            //build nodes recursive with a cutoff through alpha-beta-pruning
            for (ChessTreeNode node : chessTree.getChildren()) {
                ownGame.executeMove(node.getMove(), false);
                if (maximizing) {
                    auxValue = max(auxValue, builtTree(node, alpha, beta, FALSE));
                    ownGame.unexecuteMove(node.getMove());
                    alpha = max(alpha, auxValue);
                    if (alpha >= beta) {
                        break;
                    }
                } else {
                    auxValue = min(auxValue, builtTree(node, alpha, beta, TRUE));
                    ownGame.unexecuteMove(node.getMove());
                    beta = min(beta, auxValue);
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            chessTree.setGameValue(auxValue);
            return auxValue;
        } //depth==SEARCH_DEPTH
        else {
            int gameValue = evaluateBoard();
            chessTree.setGameValue(gameValue);
            return gameValue;
        }
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
                System.out.println("GameValue: " + 0.01 * currentTree.getGameValue());
                System.out.println("Evaluated Positions: " + evaluatedPositions);
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
}
