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
    private final float value;
    private final byte depth;    
    private final Move bestMove;
    private final EvaluationFlag flag;

    public TransTableEntry(long zobristKey, float value, byte depth, Move bestMove, EvaluationFlag flag) {
        this.zobristKey = zobristKey;
        this.value = value;
        this.depth = depth;
        this.bestMove = bestMove;
        this.flag = flag;
    }

    public long getZobristKey() {
        return zobristKey;
    }

    public float getValue() {
        return value;
    }

    public byte getDepth() {
        return depth;
    }

    public Move getBestMove() {
        return bestMove;
    }

    public EvaluationFlag getFlag() {
        return flag;
    }



    
}
