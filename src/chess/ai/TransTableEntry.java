/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.ai;

import chess.move.Move;

/**
 *
 * @author Phoenix
 */
public class TransTableEntry {
    
    private final long zobristKey;
    private final int value;
    private final int depth;    
    private final Move bestMove;
    private final EvaluationFlag EvaluationFlag;
    private boolean oldFlag;
    
    public TransTableEntry(long zobristKey, int value, int depth, Move bestMove, EvaluationFlag flag) {
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
