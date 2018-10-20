/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess;

import static chess.board.ChessColor.*;
import chess.game.ChessGUI;
import chess.game.ChessGame;
import chess.game.GameController;
import chess.game.Player;
import java.io.IOException;

/**
 *
 * @author Phoenix
 */
public class Chess {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
       
        GameController controller = new GameController();
        controller.startGame();

    }
    
}
