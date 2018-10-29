/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.game;

import chess.board.Board;
import chess.board.ChessColor;
import static chess.board.ChessColor.*;
import static chess.board.PieceType.PAWN;
import chess.move.Move;
import static chess.move.MoveType.TAKE;
import java.util.LinkedList;

/**
 *
 * @author Phoenix
 */
public class ChessGame{
    
    private final Board board;
    private ChessColor playersTurn;
    private ChessColor winner;
    private DrawType draw;
    private int drawTurnTimer;
    private final ChessRules rules;
    private final LinkedList<Move> moveList= new LinkedList<>();
    
    public ChessGame() {
        this.board = new Board(); 
        this.winner = null;
        this.draw = null;
        this.drawTurnTimer=0;
        this.rules = new ChessRules(this);
        this.playersTurn = WHITE;
    }    

    public boolean nextMove(Move move){
        //class game rules
        if(!rules.validateMove(move, this)) return false;
        board.executeMove(move);
        moveList.add(move);
        drawTurnTimer++;
        if(move.getMoveType()==TAKE || move.getPiece().getPiecetype()==PAWN)
            drawTurnTimer=0;
        this.nextPlayer();        
        return true;
    }
    
    public Board getBoard() {
        return board;
    }

    public boolean isCheckmate() {
        if(rules.isCheckmate(playersTurn)){
            winner= playersTurn.getInverse();
            return true;
        }
        return false;
    }

    public boolean isDraw() {
        draw= rules.isDraw();
        return draw!=null;       
    }

    public ChessColor getWinner() {
        return winner;
    }

    public DrawType getDraw() {
        return draw;
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

    public LinkedList<Move> getMoveList() {
        return moveList;
    }

    int getDrawTurnTimer() {
        return drawTurnTimer;
    }

    public void resetDrawTurnTimer() {
        this.drawTurnTimer = 0;
    }
    
    public void increaseDrawTurnTimer() {
        drawTurnTimer++;
    }
    
    public void decreaseDrawTurnTimer(){
        drawTurnTimer--;
    }

    public ChessRules getRules(){
        return rules;
    }
}
