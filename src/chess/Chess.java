/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess;

import chess.game.ChessGUI;
import chess.game.Game;
import chess.game.Player;
import java.io.IOException;
import java.util.Observer;
import javax.swing.*;

/**
 *
 * @author Phoenix
 */
public class Chess {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
       
        Player gui1 = new ChessGUI();
        Player gui2 = new ChessGUI();
        Game game = new Game(gui1, gui2);
        gui1.update(game, null);
        gui2.update(game, null);
        game.startGame();
    }
    
}
