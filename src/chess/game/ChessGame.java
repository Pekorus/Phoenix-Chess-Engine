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
    private final LinkedList<Long> recentPositions;
    
    public ChessGame() {
        this.board = new Board(); 
        this.winner = null;
        this.draw = null;
        this.drawTurnTimer=0;
        this.rules = new ChessRules(this);
        this.playersTurn = WHITE;
        this.recentPositions = new LinkedList<>();
        recentPositions.add(board.getHashValue());
    }    

    public boolean executeMove(Move move, boolean validationMode){
        if(validationMode && !rules.validateMove(move, this)) return false;
        board.executeMove(move);
        moveList.add(move);
        recentPositions.add(board.getHashValue());
        if(recentPositions.size()>20) recentPositions.removeFirst();
        drawTurnTimer++;
        if(move.getMoveType()==TAKE || move.getPieceType()==PAWN)
            drawTurnTimer=0;
        this.nextPlayer();        
        return true;
    }
    
    public void unexecuteMove(Move move){
        board.unexecuteMove(move);
        moveList.remove(move);
        recentPositions.removeLast();
        drawTurnTimer--;
        //TODO: draw turn timer reset
        if(move.getMoveType()==TAKE || move.getPieceType()==PAWN)
            drawTurnTimer=0;
        this.nextPlayer();         
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

    public boolean setDraw() {
        draw= rules.isDraw(true);
        return draw!=null;       
    }
    
    public boolean isDraw(boolean mode){
        return rules.isDraw(mode)!= null;
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

    public ChessColor getPlayersTurn() {
        return playersTurn;
    }    

    public Move getLastMove() {
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

    public LinkedList<Long> getRecentPositions() {
        return recentPositions;
    }

    public boolean isStalemate() {
        return rules.isStalemate();
    }

    
}
