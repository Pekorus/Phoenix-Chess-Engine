/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.game;

import static chess.board.ChessColor.*;
import chess.board.Move;
import java.io.IOException;

/**
 *
 * @author Phoenix
 */
public class GameController {

    ChessGame game;
    Player player1;
    Player player2;
    
    public GameController() throws IOException {
        player1 = new ChessGUI(this, WHITE);
        player2 = new ChessGUI(this, BLACK);
        game = new ChessGame(player1, player2);
    }    
    
    public void startGame(){
        notifyObservers();
    }
            
    public boolean nextMove(Move move){
        if(game.nextMove(move)){
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
