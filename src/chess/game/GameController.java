/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.game;

import chess.gui.ChessGuiController;
import static chess.board.ChessColor.*;
import chess.move.Move;
import java.io.IOException;

/**
 *
 * @author Phoenix
 */
public class GameController {

    ChessGame game;
    Player player1;
    Player player2;
    Boolean checkmate= false, draw=false;
    
    public GameController() throws IOException {
        player1 = new ChessGuiController(this, WHITE, "Player1", "Player2");
        player2 = new ChessGuiController(this, BLACK, "Player2", "Player1");
        game = new ChessGame();
    }    
    
    public void startGame(){
        notifyObservers();
    }
            
    public boolean nextMove(Move move){
        if(checkmate) return false;
        if(draw) return false;
        if(game.nextMove(move)){
            checkmate = game.isCheckMate();
            draw = game.isDraw();
            notifyObservers();
            return true;
        }
    return false;    
    } 

    private void notifyObservers() {
        player1.update(game, null);
        player2.update(game, null);
    }

    
}
