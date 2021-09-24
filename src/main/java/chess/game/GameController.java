package chess.game;

import chess.ai.ChessAI;
import chess.ai.TestAI;
import chess.board.ChessColor;
import chess.gui.ChessGuiController;
import static chess.board.ChessColor.*;
import chess.board.Piece;
import static chess.game.ChessGameEndType.*;
import chess.gui.ChessGuiView;
import chess.options.ChessOptions;
import chess.move.Move;
import chess.options.AIOptions;
import java.util.ArrayList;
import java.util.List;

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
    /* saves the result of the game */
    ChessGameEndType endingState = NOTFINISHED;
    /* flags to handle the end of a game */
    Boolean checkmate = false, draw = false, gameEnded = false;
    /* Observers of game (can be a gui) */
    List<Observer> observers = new ArrayList<>(); 
    
    /**
     * Class constructor for a chess game from regular starting position.
     * 
     * @param gameType      gameType
     * @param options       options to control gui
     * @param aiOptions     options to control ai player behaviour
     * @param observer      observer to be added to the game
     */
    public GameController(ChessGameType gameType, ChessOptions options,
                                      AIOptions aiOptions, Observer observer) {
        
        game = new ChessGame();                
        /* castle right of a starting position */
        boolean[] castleRights = {true, true, true, true};
        /* start game with default starting position in chess */
        observers.add(observer);
        handlePlayers(gameType, options, aiOptions, game.getBoard()
                                  .getPieceArray(), WHITE, castleRights, true);       
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
        handlePlayers(gameType, options, aiOptions, pieceArray, colorToMove,
                castleRights, false);
        
    }
    
    public void startGame(){
        
        notifyObservers(null);
        if(game.getPlayersTurn()==WHITE) whitePlayer.getNextMove();
        else blackPlayer.getNextMove();
    }
            
    public void nextMove(ChessColor color, Move move){        
        
        if(gameEnded || color != game.getPlayersTurn()) return;
        /* executeMove validates legality of move with mode "true" */
        if(game.executeMove(move, true)){
            
            checkmate = game.isCheckmate();
            if(checkmate){
                if(color == WHITE) endingState = WHITEWIN;
                else endingState = BLACKWIN;
            }
            draw = game.setDraw();
            if(draw) endingState = DRAW;
            
            /* draw or checkmate => game is over */
            gameEnded = draw || checkmate;
            notifyObservers(move);
            if(!gameEnded) demandNextMove(game.getPlayersTurn());
            else endGame();
        }
        else demandNextMove(game.getPlayersTurn());
    } 

    private void notifyObservers(Move nextMove) {
        whitePlayer.update(nextMove);
        blackPlayer.update(nextMove);
        for(Observer o : observers) o.update(nextMove);
    }

    private void demandNextMove(ChessColor playersTurn) {
        if(playersTurn==WHITE) whitePlayer.getNextMove();
        else blackPlayer.getNextMove();
    }

    public ChessGuiView getView(){
        
        if(humanPlayer == null) 
            return ((ChessGuiController) observers.get(1)).getView();
        
        else if(humanPlayer==WHITE) 
            return ((ChessGuiController)whitePlayer).getView();
        else return ((ChessGuiController)blackPlayer).getView();
    }
    
    public void endGame() {
        gameEnded = true;
        whitePlayer.endGame(endingState);
        blackPlayer.endGame(endingState);
        for(Observer o : observers) o.endGame(endingState);        
    }

    public ChessGameEndType getResult(){
        return endingState;
    }
    
    private void handlePlayers(ChessGameType gameType, ChessOptions options,
                AIOptions aiOptions, Piece[][] pieceArray, 
                ChessColor colorToMove, boolean[] castleRights,
                boolean startingPos) {
        
        switch(gameType){            
            
            case WHITEPLAYER:
                               
                if(startingPos)
                    blackPlayer = new ChessAI(this, BLACK, aiOptions);
                else
                    blackPlayer = new ChessAI(this, BLACK, aiOptions, 
                            pieceArray, colorToMove, castleRights);
                
                whitePlayer = new ChessGuiController(this, game, WHITE, options,
                        "Human", blackPlayer.getPlayerName(), false);                 
                
                humanPlayer = WHITE;
                options.setCreatorColor(BLACK);
                break;
        
            case BLACKPLAYER:
                              
                if(startingPos)
                    whitePlayer = new ChessAI(this, WHITE, aiOptions);
                else
                    whitePlayer = new ChessAI(this, WHITE, aiOptions, 
                            pieceArray, colorToMove, castleRights);

                blackPlayer = new ChessGuiController(this, game, BLACK, options,
                        whitePlayer.getPlayerName(), "Human", false);                                 
                
                humanPlayer = BLACK;
                options.setCreatorColor(WHITE);
                break;
        
            case AIVSAI:
                
                whitePlayer = new TestAI(this, WHITE, aiOptions);                
                blackPlayer = new ChessAI(this, BLACK, aiOptions);
               
                humanPlayer = null;    
                observers.add(new ChessGuiController(this, game, WHITE, options,
                    whitePlayer.getPlayerName(), blackPlayer.getPlayerName(),
                        true));
        }
    }
    
}
