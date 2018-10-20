/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.game;

import chess.board.Board;
import chess.board.ChessColor;
import chess.move.Move;
import java.io.IOException;

/**
 *
 * @author Phoenix
 */
public class ChessGUI implements Player{
    
    private final ChessBoardFrame mainFrame;
    private final ChessColor ownColor;
    
    public ChessGUI(GameController controller, ChessColor ownColor) throws IOException {
        this.mainFrame = new ChessBoardFrame(controller, ownColor);
        mainFrame.setVisible(true);
        this.ownColor = ownColor;
    }
    

    @Override
    public void update(ChessGame game, Object arg){
        mainFrame.update(game, null);
    }

    @Override
    public Move nextMove(Board board) {
        //TODO:
    return mainFrame.getNextMove();
        
    }
}
