/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.gui;

import chess.board.ChessColor;
import static chess.board.ChessColor.*;
import chess.board.Piece;
import chess.board.PieceType;
import static chess.board.PieceType.*;
import chess.coordinate.Coordinate;
import chess.game.ChessGame;
import chess.game.GameController;
import chess.game.Player;
import chess.move.Move;
import static chess.move.MoveType.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 *
 * @author Phoenix
 */
public class ChessGuiController implements ActionListener, Player {

    private final ChessGuiView view;
    private final GameController gameControl;
    private final ChessColor ownColor;
    private final String ownName;
    private final String opponentName;
    
    private PieceType nextPromotion = null;
    private Move nextMove = null;
    private Coordinate pressedCoord1 = null, pressedCoord2 = null;
    private Coordinate paintedCoord1, paintedCoord2;

    public ChessGuiController(GameController gameControl,MainView mainFrame, 
            ChessColor ownColor, 
            String playerName, String opponentName) throws IOException {
        this.gameControl = gameControl;
        this.ownColor = ownColor;
        this.ownName = playerName;
        this.opponentName = opponentName;
        this.view = new ChessGuiView(mainFrame, this, ownColor);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == view.queenButton) {
            nextPromotion = QUEEN;
            view.promoteDialog.setVisible(false);
        }
        if (source == view.bishopButton) {
            nextPromotion = BISHOP;
            view.promoteDialog.setVisible(false);
        }
        if (source == view.knightButton) {
            nextPromotion = KNIGHT;
            view.promoteDialog.setVisible(false);
        }
        if (source == view.rookButton) {
            nextPromotion = ROOK;
            view.promoteDialog.setVisible(false);
        }
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (source == view.buttonArray[i][j]) {
                    int a = i, b = j;
                    
                    //translate coordinates if playing white(rotated board)
                    if (ownColor == WHITE) {
                        a = 7 - i;
                        b = 7 - j;
                    }
                    
                    //Reset after move input
                    if (pressedCoord1 != null && pressedCoord2 != null) {
                        nextMove = null;
                        view.restoreFieldColor(paintedCoord1);
                        view.restoreFieldColor(paintedCoord2);
                        pressedCoord1 = null;
                        pressedCoord2 = null;
                        paintedCoord1 = null;
                        paintedCoord2 = null;
                    }
                    Piece clickedPiece = view.pieceArray[a][b];
                    if (pressedCoord1 == null && clickedPiece != null 
                                && clickedPiece.isColor() ==ownColor) {
                        pressedCoord1 = new Coordinate(a, b);
                        paintedCoord1 = new Coordinate(i, j);
                        view.paintFieldColor(paintedCoord1);
                    } else if (pressedCoord1 != null && pressedCoord2 ==null && 
                            clickedPiece!= null&&clickedPiece.isColor()==ownColor) {
                        view.restoreFieldColor(paintedCoord1);
                        pressedCoord1 = new Coordinate(a, b);
                        paintedCoord1 = new Coordinate(i, j);
                        view.paintFieldColor(paintedCoord1);
                        gameControl.nextMove(ownColor, nextMove);                                        
                    } else if(pressedCoord1 != null && pressedCoord2 == null) {
                        pressedCoord2 = new Coordinate(a, b);
                        paintedCoord2 = new Coordinate(i, j);
                        view.paintFieldColor(paintedCoord2);
                        createMove();
                        gameControl.nextMove(ownColor, nextMove);
                    }
                }
            }
        }
    }

    private void createMove() {
        Piece piece1 = view.pieceArray[pressedCoord1.getX()][pressedCoord1.getY()];
        Piece piece2 = view.pieceArray[pressedCoord2.getX()][pressedCoord2.getY()];

        if (piece1 == null) {
            return;
        }
        //TAKE
        if (piece2 != null) {
            //Pawn promotion
            if (piece1.getPiecetype() == PAWN && piece1.isColor()==ownColor
                    && pawnPromotionValid()){
                view.setPromoteDialogColor(piece1.isColor());
                view.promoteDialog.setVisible(true);
                nextMove = new Move(piece1.getPiecetype(), pressedCoord1, pressedCoord2, TAKE, nextPromotion);
            } //usual taking
            else {
                nextMove = new Move(piece1.getPiecetype(), pressedCoord1,
                        pressedCoord2, TAKE, null);
            }
        } //CASTLE
        else if (piece1.getPiecetype() == KING
                && pressedCoord1.distance(pressedCoord2) == 2) {
            nextMove = new Move(piece1.getPiecetype(), pressedCoord1, pressedCoord2, CASTLE);
        } /* ENPASSANT */ 
        else if (piece1.getPiecetype() == PAWN
                && pressedCoord1.diagonalLineDir(pressedCoord2) != null) {
            nextMove = new Move(piece1.getPiecetype(), pressedCoord1, pressedCoord2, ENPASSANT, null);
        } //NORMAL
        else {
            //pawn promotion
            if (piece1.getPiecetype() == PAWN && piece1.isColor()==ownColor
                    && pawnPromotionValid()) {
                view.setPromoteDialogColor(piece1.isColor());
                view.promoteDialog.setVisible(true);
                nextMove = new Move(piece1.getPiecetype(), pressedCoord1, pressedCoord2,
                        NORMAL, nextPromotion);
            } //usual move
            else {
                nextMove = new Move(piece1.getPiecetype(), pressedCoord1, pressedCoord2, NORMAL);
            }
        }
    }

    @Override
    public void update(ChessGame game, Move move, Object arg) {
        //TODO: eigenes board, nicht das von game benutzen
        view.update(game, arg);
    }

    private boolean pawnPromotionValid() {
        if(ownColor==WHITE && pressedCoord2.getX() == 7 && 
                                                      pressedCoord1.getX()==6)
            return true;
        else if(ownColor==BLACK && pressedCoord2.getX() == 0 
                && pressedCoord1.getX()== 1)
            return true;
    return false;    
    }

    public String getOwnName() {
        return ownName;
    }

    public String getOpponentName() {
        return opponentName;
    }

    @Override
    public void getNextMove() {
    }

}
