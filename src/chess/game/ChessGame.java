/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.game;

import chess.board.Board;
import chess.board.ChessColor;
import chess.board.Move;

/**
 *
 * @author Phoenix
 */
public class ChessGame{
    
    private final Board board;
    private final Player playerWhite, playerBlack; 
    private boolean checkMate;
    private ChessColor playersTurn;
    private ChessColor winner;
    
    
    public ChessGame(Player playerWhite, Player playerBlack) {
        this.playerWhite = playerWhite;
        this.playerBlack = playerBlack;
        this.board = new Board(); 
        this.checkMate = false;
        this.winner = null;
    }    
    
/*    public void startGame(){        
        while(!checkMate){
        this.handlePlayerMove(playerWhite);
        //checkMate = board.isCheckmate();
        if(!checkMate) this.handlePlayerMove(playerBlack);
        //checkMate = board.isCheckmate();
        }
        if(board.getPlayersTurn()==WHITE) winner = BLACK;
        else winner = WHITE;
        notifyObservers();
    }*/

    /*private void handlePlayerMove(Player player) {
        nextMove = player.nextMove(board);
        while(!board.validateMove(nextMove)){
            nextMove = player.nextMove(board);
        }
        board.executeMove(nextMove);
        this.notifyObservers();
    }*/

    public boolean nextMove(Move move){
        //class game rules
        if(!board.validateMove(move)) return false;
        board.executeMove(move);
        return true;
    }
    
    public Board getBoard() {
        return board;
    }

    public Player getPlayerWhite() {
        return playerWhite;
    }

    public Player getPlayerBlack() {
        return playerBlack;
    }

    public boolean isCheckMate() {
        return checkMate;
    }

    public ChessColor getWinner() {
        return winner;
    }
}
