/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.game;

import chess.ai.ChessAI;
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
        game = new ChessGame();        
        player1 = new ChessGuiController(this, WHITE, "Player1", "ChessAI");
        player2 = new ChessAI(this, BLACK);

    }    
    
    public void startGame(){
        notifyObservers(null);
    }
            
    public boolean nextMove(Move move){
        if(checkmate) return false;
        if(draw) return false;
        if(game.executeMove(move)){
            checkmate = game.isCheckmate();
            draw = game.isDraw();
            notifyObservers(move);
            return true;
        }
    return false;    
    } 

    private void notifyObservers(Move nextMove) {
        player1.update(game, nextMove, null);
        player2.update(game, nextMove, null);
    }

    
}
