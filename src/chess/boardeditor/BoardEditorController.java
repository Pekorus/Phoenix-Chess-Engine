/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.boardeditor;

import chess.board.ChessColor;
import static chess.board.ChessColor.*;
import chess.board.Piece;
import chess.board.PieceType;
import static chess.board.PieceType.KING;
import static chess.board.PieceType.PAWN;
import chess.coordinate.Coordinate;
import chess.game.ChessGame;
import chess.game.ChessGameType;
import static chess.game.ChessGameType.BLACKPLAYER;
import static chess.game.ChessGameType.WHITEPLAYER;
import chess.gui.MainController;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

/**
 *
 * @author Phoenix
 */
public class BoardEditorController implements ActionListener{

    private final MainController mainControl;
    private final BoardEditorView editorView;
    private final BufferedImage[][] imageArray;

    private  final Piece[][] pieceArray = new Piece[8][8];
    private PieceType selectedPiece;
    private ChessColor selectedColor; 
    private ChessColor ownColor = WHITE;
    private int whiteKingCounter = 0, blackKingCounter = 0;
    private Piece whiteKing, blackKing;
    
    public BoardEditorController (MainController mainControl, int frameHeight, int frameWidth, ImageIcon[][] spriteArray,
            BufferedImage[][] imageArray) {
    
        this.mainControl = mainControl;
        this.editorView = new BoardEditorView(this, frameHeight, frameWidth, spriteArray);
        this.imageArray = imageArray;
    }


    public void startEditor(){
        editorView.setVisible(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        /* Piece buttons */
        for(int i=0; i<2; i++){
            for(int j=0; j<6; j++){
                if(source == editorView.pieceButtons[i][j]){
                    editorView.setCursor(imageArray[i][j]);
                    if(i==0) selectedColor = BLACK;
                    else selectedColor = WHITE;
                    selectedPiece = PieceType.values()[j];
                }                
            }
        }

        /* field on chess board is clicked */
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (source == editorView.chessBoardPanel.buttonArray[i][j]) {
                
                /* translate variables if board is rotated */    
                int a = i, b = j;
                if (ownColor == WHITE) {
                    a = 7 - i;
                    b = 7 - j;
                }                    
                Piece piece = new Piece(selectedPiece, selectedColor, 
                     new Coordinate(a, b));
                if(verifyAndAdjustPiece(piece)) setPiece(piece, a ,b);                 
                updatePlayButtonsEnabled();
                editorView.chessBoardPanel.drawBoard(pieceArray);
                }    
            }    
        }        
    
        if(source == editorView.eraseButton){
            editorView.restoreCursor();
            selectedPiece = null;
            selectedColor = null;
        }

        if(source == editorView.rotateBoard){
            ownColor = ownColor.getInverse();
            editorView.setOwnColor(ownColor);
            editorView.chessBoardPanel.drawBoard(pieceArray);
        }        
    
        if(source == editorView.playAsWhiteButton){            
            startGame(WHITEPLAYER);
        }
    
        if(source == editorView.playAsBlackButton){
            startGame(BLACKPLAYER);
        }
    }

    private void startGame(ChessGameType gameType){
            
        ChessColor colorToMove = WHITE;
        if(editorView.blackToMove.isSelected()) colorToMove = BLACK;
        
        boolean[] castleRights = {editorView.whiteSmallCastle.isSelected(),
            editorView.whiteLargeCastle.isSelected(),
            editorView.blackSmallCastle.isSelected(),
            editorView.blackLargeCastle.isSelected()};

        if(verifyPositionLegal(colorToMove, castleRights)){
                    
            editorView.setVisible(false);
            mainControl.startGameFromBoardEditor(pieceArray, gameType,
                colorToMove, castleRights);        
        }    
    }

    private boolean verifyAndAdjustPiece(Piece piece) {
        
        if(piece.getPiecetype() == PAWN){
            
            /* no pawns on first and last rank */
            if(piece.getCoord().getX() == 0 || piece.getCoord().getX() == 7)
                return false;
            
            /* increase move counter if pawn is not on initial position */
            if(piece.isColor() == WHITE && piece.getCoord().getX()!= 1)
                piece.increaseMoveCounter();
            if(piece.isColor() == BLACK && piece.getCoord().getX()!= 6)
                piece.increaseMoveCounter();
        }
    
        if(piece.getPiecetype() == KING){
            
            if(piece.isColor() == WHITE){
                /* only one king per side */                
                if(whiteKingCounter > 0) return false;          
                /* no kings on adjacent squares */ 
                if(blackKing != null && 
                        piece.getCoord().distance(blackKing.getCoord())==1)
                    return false;                
                whiteKing = piece;
                whiteKingCounter++;    
            }
            else{
                if(blackKingCounter > 0) return false;          
                if(whiteKing != null && 
                        piece.getCoord().distance(whiteKing.getCoord())==1)
                    return false;
                blackKing = piece;
                blackKingCounter++;    
            }                       
        }        
        return true;
    }

    private void setPiece(Piece piece, int x, int y) {
        
        Piece oldPiece = pieceArray[x][y];
        
        /* restore fields if king is deleted */
        if(oldPiece != null && oldPiece.getPiecetype() == KING){
            if(oldPiece.isColor() == WHITE){
                whiteKingCounter--;
                whiteKing = null;
            }
            else{
                blackKingCounter--;
                blackKing = null;
            }        
        }        
        if(piece.getPiecetype() != null) pieceArray[x][y] = piece;    
        else pieceArray[x][y] = null;
    }

    private void updatePlayButtonsEnabled() {
        if(whiteKingCounter == 1 && blackKingCounter == 1){
            editorView.playAsWhiteButton.setEnabled(true);
            editorView.playAsBlackButton.setEnabled(true);
        }
        else{
            editorView.playAsWhiteButton.setEnabled(false);
            editorView.playAsBlackButton.setEnabled(false);            
        }
    }

    /* checks if the side to move has legal moves in the position,
        otherwise it is an illegal position 
    */
    private boolean verifyPositionLegal(ChessColor colorToMove, boolean[]
             castleRights) {
        ChessGame game = new ChessGame(pieceArray, colorToMove, castleRights);
        
        return !(game.getRules().isInCheck(colorToMove.getInverse())
                || game.getRules().isStalemate());
    }
}
