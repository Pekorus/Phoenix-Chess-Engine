/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.game;

import chess.ai.ChessAI;
import chess.board.ChessColor;
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
    Player whitePlayer;
    Player blackPlayer;
    Boolean checkmate= false, draw=false;
    
    public GameController() throws IOException {
        game = new ChessGame();        
        whitePlayer = new ChessGuiController(this, WHITE, "Player1", "ChessAI");
        //player2 = new ChessGuiController(this, BLACK, "Player1", "Player2");        
        blackPlayer = new ChessAI(this, BLACK);

    }    
    
    public void startGame(){
        notifyObservers(null);
        whitePlayer.getNextMove();
    }
            
    public boolean nextMove(Move move){
        if(checkmate) return false;
        if(draw) return false;
        if(game.executeMove(move)){
            checkmate = game.isCheckmate();
            draw = game.isDraw();
            notifyObservers(move);
            demandNextMove(game.getPlayersTurn());
            return true;
        }
    return false;    
    } 

    private void notifyObservers(Move nextMove) {
        whitePlayer.update(game, nextMove, null);
        blackPlayer.update(game, nextMove, null);
    }

    private void demandNextMove(ChessColor playersTurn) {
        if(playersTurn==WHITE) whitePlayer.getNextMove();
        else blackPlayer.getNextMove();
    }

    
}
