package chess.ai;

import chess.move.Move;

/**
 *
 * Provides an entry to be stored in a transposition table for chess.
 */
public class TransTableEntry {
    
    /* The zobris hash key of this position */
    private final long zobristKey;
    /* Value of this position, as given by the search */
    private final int value;
    /* Depth at which the search encountered the position */
    private final int depth;    
    /* Best move that was found by the search for this position */
    private final Move bestMove;
    /* Flag to store the type of value; was the value acquired during a cutoff
        of the Alpha-Beta pruning (alpha or beta) or is it an exact value.
    */
    private final EvaluationFlag EvaluationFlag;
    /* Flag to decide if an entry is old (used for replacement strategies) */
    private boolean oldFlag;
    
    /**
     * Class constructor.
     * 
     * @param zobristKey    zobris key of this position
     * @param value         value of this position as found by search
     * @param depth         depth at which position was encountered
     * @param bestMove      best move the search found for this position
     * @param flag          type of value found by Alpha-Beta pruning (alpha, 
     *                      beta or exact)
     */
    public TransTableEntry(long zobristKey, int value, int depth, Move bestMove,
            EvaluationFlag flag) {
        this.zobristKey = zobristKey;
        this.value = value;
        this.depth = depth;
        this.bestMove = bestMove;
        this.EvaluationFlag = flag;
        this.oldFlag = false;
    }

    public long getZobristKey() {
        return zobristKey;
    }

    public int getValue() {
        return value;
    }

    public int getDepth() {
        return depth;
    }

    public Move getBestMove() {
        return bestMove;
    }

    public EvaluationFlag getEvaluationFlag() {
        return EvaluationFlag;
    }

    public boolean getOldFlag() {
        return oldFlag;
    }

    public void setOldFlag(boolean b) {
        this.oldFlag = b;
    }
    
}
