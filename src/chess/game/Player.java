/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.game;

import chess.board.Board;
import chess.move.Move;

/**
 *
 * @author Phoenix
 */

//vllt Interface ??
public interface Player {
    
    public abstract void update(ChessGame game, Object arg);
    public abstract Move nextMove(Board board);
}
