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
import chess.gui.MainView;
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
    
    public GameController(MainView mainView) throws IOException {
        game = new ChessGame();        
        whitePlayer = new ChessGuiController(this, mainView, WHITE, "Player1", "ChessAI");
        //MainView mainView2= new MainView(700,900);
        //blackPlayer = new ChessGuiController(this, mainView2, BLACK, "Player1", "Player2");        
        blackPlayer = new ChessAI(this, BLACK);

    }    
    
    public void startGame(){
        notifyObservers(null);
        whitePlayer.getNextMove();
    }
            
    public void nextMove(Move move){        
        if(checkmate || draw);
        else if(game.executeMove(move)){
            checkmate = game.isCheckmate();
            draw = game.isDraw();
            notifyObservers(move);
            demandNextMove(game.getPlayersTurn());        
        }
        else demandNextMove(game.getPlayersTurn());
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
