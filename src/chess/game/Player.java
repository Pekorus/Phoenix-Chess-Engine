/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.game;

import chess.move.Move;

/**
 *
 * @author Phoenix
 */

public interface Player {
    
    public abstract void update(ChessGame game, Move move, Object arg);
}
