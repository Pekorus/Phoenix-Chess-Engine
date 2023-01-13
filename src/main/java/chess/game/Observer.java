package chess.game;

import chess.move.Move;

/**
 *
 * Provides an Interface for an observer of a chess game.
 */
public interface Observer {
    
    /**
     * Game controller notifies players about a played move with this method. 
     * 
     * @param move  the move that was played last in this game, is null at start
     *              of game
     */
    void update(Move move);

    /**
     * Game controller notifies players about the end of the game with this 
     * method. All preparations that have to be done before a game ends have
     * to be made before this method returns.
     * 
     * @param result    result of the game (white/ black won, draw or unfinished
     */
    void endGame(ChessGameEndType result);
    
}
