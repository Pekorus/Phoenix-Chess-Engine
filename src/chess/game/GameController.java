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
import chess.gui.ChessGuiView;
import chess.move.Move;

/**
 *
 * @author Phoenix
 */
public class GameController {

    ChessGame game;
    Player whitePlayer;
    Player blackPlayer;
    ChessColor humanPlayer; 
    Boolean checkmate= false, draw=false;
    
    public GameController(ChessGameType gameType) {
        game = new ChessGame();        
        
        switch(gameType){            
            case WHITEPLAYER:
            whitePlayer = new ChessGuiController(this, WHITE, "Human", "ChessAI");                
            blackPlayer = new ChessAI(this, BLACK);
            humanPlayer = WHITE;
            break;
        
            case BLACKPLAYER:
            blackPlayer = new ChessGuiController(this, BLACK, "Human", "ChessAI");                
            whitePlayer = new ChessAI(this, WHITE);                
            humanPlayer = BLACK;
            break;
        }
        //MainView mainView2= new MainView(700,900);        
    }    
    
    public void startGame(){
        notifyObservers(null);
        whitePlayer.getNextMove();
    }
            
    public void nextMove(ChessColor color, Move move){        
        if(checkmate || draw) return;
        if(color != game.getPlayersTurn()) return;
        if(game.executeMove(move, true)){
            checkmate = game.isCheckmate();
            draw = game.setDraw();
            notifyObservers(move);
            if(checkmate || draw) return;
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

    public ChessGuiView getView(){
        if(humanPlayer==WHITE) return whitePlayer.getView();
        else return blackPlayer.getView();
    }
    
}
