/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.game;

import chess.gui.ChessGuiView;
import chess.move.Move;

/**
 *
 * @author Phoenix
 */

public interface Player {
    
    public abstract void update(ChessGame game, Move move, Object arg);

    public abstract void getNextMove();

    public ChessGuiView getView();

    public void endGame();
}
