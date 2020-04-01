package chess.game;

import chess.move.Move;

/**
 *
 * Provides an interface for a chess player.
 */

public interface Player {
    
    /**
     * Game controller notifies players about a played move with this method. 
     * 
     * @param move  the move that was played last in this game
     */
    public void update(Move move);

    /**
     * Game controller requests the player's next move with this method.
     */
    public void getNextMove();

    /**
     * Game controller notifies players about the end of the game with this 
     * method. All preparations that have to be done before a game ends have
     * to be made before this method returns.
     */
    public void endGame();
}
