/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.game;

import chess.board.Board;
import chess.board.ChessColor;
import static chess.board.ChessColor.*;
import chess.move.Move;
import java.util.LinkedList;

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
    private ChessRules rules;
    private final LinkedList<Move> moveList= new LinkedList<>();
    
    public ChessGame(Player playerWhite, Player playerBlack) {
        this.playerWhite = playerWhite;
        this.playerBlack = playerBlack;
        this.board = new Board(); 
        this.checkMate = false;
        this.winner = null;
        this.rules = new ChessRules();
        this.playersTurn = WHITE;
    }    

    public boolean nextMove(Move move){
        //class game rules
        if(!rules.validateMove(move, this)) return false;
        board.executeMove(move);
        moveList.add(move);
        this.nextPlayer();
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

    private void nextPlayer() {
        if(this.playersTurn== WHITE) this.playersTurn= BLACK;
        else this.playersTurn= WHITE;
    }

    ChessColor getPlayersTurn() {
        return playersTurn;
    }    

    Move getLastMove() {
        return moveList.getLast();
    }
}
