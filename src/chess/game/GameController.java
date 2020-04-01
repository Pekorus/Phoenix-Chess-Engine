package chess.game;

import chess.ai.ChessAI;
import chess.board.ChessColor;
import chess.gui.ChessGuiController;
import static chess.board.ChessColor.*;
import chess.board.Piece;
import chess.gui.ChessGuiView;
import chess.gui.ChessOptions;
import chess.move.Move;
import chess.options.AIOptions;

/**
 *
 * Provides a controller of a chess game that handles the game and player moves.
 */
public class GameController {

    /* the chess game to handle */
    ChessGame game;
    /* players */
    Player whitePlayer;
    Player blackPlayer;
    /* color of the human player */
    ChessColor humanPlayer; 
    Boolean checkmate = false, draw =false;
    
    /**
     * Class constructor for a chess game from regular starting position.
     * 
     * @param gameType      gameType
     * @param options       options to control gui
     * @param aiOptions     options to control ai player behaviour
     */
    public GameController(ChessGameType gameType, ChessOptions options,
                                                        AIOptions aiOptions) {
        
        game = new ChessGame();                
        handlePlayers(gameType, options, aiOptions);
        //MainView mainView2= new MainView(700,900);        
    }    

    /**
     * Class constructor for a chess game from custom position.
     * 
     * @param gameType      gameType
     * @param options       options to control gui
     * @param aiOptions     options to control ai player behaviour
     * @param pieceArray    board position represented by piece array
     * @param colorToMove   color to move first
     * @param castleRights  castling rights in order 0-0 white, 0-0-0 white,
     *                      0-0 black, 0-0-0 black
     */
    public GameController(ChessGameType gameType, ChessOptions options,
                AIOptions aiOptions, Piece[][] pieceArray, 
                ChessColor colorToMove, boolean[] castleRights) {
        
        game = new ChessGame(pieceArray, colorToMove, castleRights);
        
        switch(gameType){            
            
            case WHITEPLAYER:
                
                whitePlayer = new ChessGuiController(this, game, WHITE, options,
                        "Human", "ChessAI");                
                blackPlayer = new ChessAI(this, BLACK, aiOptions, pieceArray, 
                     colorToMove, castleRights);
                humanPlayer = WHITE;
            break;
        
            case BLACKPLAYER:
                
                blackPlayer = new ChessGuiController(this, game, BLACK, options,
                        "Human", "ChessAI");                
                whitePlayer = new ChessAI(this, WHITE, aiOptions, pieceArray, 
                        colorToMove, castleRights);                
                humanPlayer = BLACK;
            break;
        }
    }
    
    public void startGame(){
        
        notifyObservers(null);
        if(game.getPlayersTurn()==WHITE) whitePlayer.getNextMove();
        else blackPlayer.getNextMove();
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
        whitePlayer.update(nextMove);
        blackPlayer.update(nextMove);
    }

    private void demandNextMove(ChessColor playersTurn) {
        if(playersTurn==WHITE) whitePlayer.getNextMove();
        else blackPlayer.getNextMove();
    }

    public ChessGuiView getView(){
        if(humanPlayer==WHITE) 
            return ((ChessGuiController)whitePlayer).getView();
        else return ((ChessGuiController)blackPlayer).getView();
    }

    public void endGame() {
        whitePlayer.endGame();
        blackPlayer.endGame();
    }

    private void handlePlayers(ChessGameType gameType, ChessOptions options,
                                                         AIOptions aiOptions) {
        
        switch(gameType){            
            case WHITEPLAYER:
                
                whitePlayer = new ChessGuiController(this, game, WHITE, options,
                        "Human", "ChessAI");                
                blackPlayer = new ChessAI(this, BLACK, aiOptions);
                humanPlayer = WHITE;
            break;
        
            case BLACKPLAYER:
                
                blackPlayer = new ChessGuiController(this, game, BLACK, options,
                        "Human", "ChessAI");                
                whitePlayer = new ChessAI(this, WHITE, aiOptions);                
                humanPlayer = BLACK;
            break;
        }
    }
    
}
