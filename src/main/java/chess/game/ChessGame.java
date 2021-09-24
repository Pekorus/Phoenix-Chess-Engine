package chess.game;

import chess.board.Board;
import chess.board.ChessColor;
import static chess.board.ChessColor.*;
import chess.board.Piece;
import static chess.board.PieceType.PAWN;
import chess.move.Move;
import static chess.move.MoveType.TAKE;
import java.util.LinkedList;

/**
 *
 * Provides a game of chess.
 */
public class ChessGame{
    
    /* chess board */
    private final Board board;
    /* player to move */
    private ChessColor playersTurn;
    /* winner of the game */
    private ChessColor winner;
    /* if the game is a draw, which kind of draw is it */
    private DrawType draw;
    /* counter to verify 50 turn draw rule */
    private int drawTurnTimer;
    /* rules class to be used for the game */
    private final ChessRules rules;
    /* list of all past moves */
    private final LinkedList<Move> moveList= new LinkedList<>();
    /* list of past positions, stored as zobrist hash vlue */
    private final LinkedList<Long> recentPositions;
    
    /**
     * Class constructor for a chess game from regular starting position.
     */
    public ChessGame() {
        
        this.board = new Board(); 
        this.winner = null;
        this.draw = null;
        this.drawTurnTimer = 0;
        this.rules = new ChessRules(this);
        this.playersTurn = WHITE;
        this.recentPositions = new LinkedList<>();
        recentPositions.add(board.getHashValue());
    }    

    /**
     * Class constructor for a chess game from custom position.
     * 
     * @param pieceArray        board position represented by a piece array
     * @param colorToMove       color to move first
     * @param castleRights  ´   castling rights, in order 0-0 white, 0-0-0 white
     *                          0-0 black, 0-0-0 black
     */
    public ChessGame(Piece[][] pieceArray, ChessColor colorToMove, 
                                                    boolean[] castleRights) {
        
        this.board = new Board(pieceArray, colorToMove, castleRights);
        this.winner = null;
        this.draw = null;
        this.drawTurnTimer = 0;
        this.rules = new ChessRules(this);
        this.playersTurn = colorToMove;
        this.recentPositions = new LinkedList<>();
        recentPositions.add(board.getHashValue());    
    }
    
    /**
     * Executes given move in this game. validationMode controls if the move is
     * verified as legal first. Returns true if move was executed, false
     * otherwise.
     * 
     * @param move              move to be executed
     * @param validationMode    controls if move is first validated as legal
     * @return                  move was executed or not
     */
    public boolean executeMove(Move move, boolean validationMode){
        
        if(validationMode && !rules.validateMove(move)) return false;
        board.executeMove(move);
        moveList.add(move);
        recentPositions.add(board.getHashValue());
        drawTurnTimer++;
        /* capture or pawn move resets the counter according to the Fifty-move
            draw rule in chess
        */
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

    /**
     * Verifies if player of given color is in check.
     * 
     * @param color     color of player to be checked
     * @return 
     */
    public boolean isInCheck(ChessColor color) {
        return rules.isInCheck(color);
    }
    
    /**
     * Verifies if player of given color is in check mate.
     * 
     * @return 
     */
    public boolean isCheckmate() {
        if(rules.isCheckmate(playersTurn)){
            winner= playersTurn.getInverse();
            return true;
        }
        return false;
    }

    /**
     * Verifies if game is a draw and sets the draw field with the type of draw. 
     * 
     * @return  
     */
    public boolean setDraw() {
        
        draw= rules.isDraw(true);
        return draw!=null;       
    }
    
    /**
     * Verifies if game is a draw, mode controls if stalemate is also verified.
     * False mode is used in AI to improve performance.
     * 
     * @param mode
     * @return 
     */
    public boolean isDraw(boolean mode){
        return rules.isDraw(mode)!= null;
    }
    
    public ChessColor getWinner() {
        return winner;
    }

    public DrawType getDraw() {
        return draw;
    }    
    
    /**
     * Sets field playersTurn with next player.
     */
    private void nextPlayer() {
        if(this.playersTurn== WHITE) this.playersTurn= BLACK;
        else this.playersTurn= WHITE;
    }

    public ChessColor getPlayersTurn() {
        return playersTurn;
    }    

    public Move getLastMove() {
        if(!moveList.isEmpty()) return moveList.getLast();
        return null;
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

    /**
     * Represents unexecution of a null move (side to move
     * just passes) for null move search .
     */
    public void unexecuteNullMove() {
        playersTurn = playersTurn.getInverse();
        board.unexecuteNullMove();
    }

    /**
     * Represents a null move (side to move just passes) for null move search.
     */
    public void executeNullMove() {
       playersTurn = playersTurn.getInverse();
       board.executeNullMove();
    }
    
}
