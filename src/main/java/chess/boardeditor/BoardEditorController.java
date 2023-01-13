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
import chess.options.ChessOptions;
import chess.gui.MainController;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 *
 * Provides a board editor (with gui) to create custom chess positions and start 
 * them. Needs the class BoardEditorView (graphical part of the editor) to 
 * function.
 */
public class BoardEditorController implements ActionListener{

    /* main controller of the program */
    private final MainController mainControl;
    /* graphical interface of the editor */
    private final BoardEditorView editorView;
    /* needed images provided by the main program */
    private final BufferedImage[][] imageArray;

    /* piece positions on the board represented by an array */
    private  final Piece[][] pieceArray = new Piece[8][8];
    /* type of currently by user selected piece */
    private PieceType selectedPiece;
    /* color of currently by user selected piece */
    private ChessColor selectedColor; 
    private ChessColor ownColor = WHITE;
    /* number of kings the user set onto the board (only one per color allowed)
    */ 
    private int whiteKingCounter = 0, blackKingCounter = 0;
    /* positions of kings to prevent two kings side by side on the board */
    private Piece whiteKing, blackKing;
    
    /**
     * Class constructor.
     * 
     * @param mainControl   main controller of the program
     * @param frameHeight   height of board editor frame
     * @param frameWidth    width of board editor frame
     * @param location      location of the main frame of program
     * @param options       options including sprites to paint
     */
    public BoardEditorController (MainController mainControl, int frameHeight, 
             int frameWidth, Point location, ChessOptions options) {
    
        this.mainControl = mainControl;
        this.editorView = new BoardEditorView(this, options);
        editorView.setTitle("Board Editor");
        editorView.setSize(frameWidth, frameHeight);
        editorView.setResizable(false);        
        editorView.setLocation(location);
        editorView.createView();
        
        this.imageArray = options.getImageArray();
    }

    public void startEditor(){
        editorView.pack();
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
                
                /* translate variables if board is rotated (human is white 
                    player) */    
                int a = i, b = j;
                if (ownColor == WHITE) {
                    a = 7 - i;
                    b = 7 - j;
                }                    
                Piece piece = new Piece(selectedPiece, selectedColor, 
                     new Coordinate(a, b), 0);
                if(verifyAndAdjustPiece(piece)) setPiece(piece, a ,b);                 
                updatePlayButtonsEnabled();
                editorView.chessBoardPanel.drawBoard(pieceArray);
                }    
            }    
        }        
    
        if(source == editorView.eraseButton){
            editorView.setCursor(editorView.trashCanImage);
            selectedPiece = null;
            selectedColor = null;
        }

        if(source == editorView.rotateBoard){
            ownColor = ownColor.getInverse();
            editorView.setOwnColor(ownColor);
            editorView.chessBoardPanel.rotateAndDrawBoard(pieceArray);
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

    /**
     * Verifies if a set piece by the user is legal on that chess square (kings
     * and pawns). Does not verify if a player is in check, but not to move
     * (which is an illegal position).
     * 
     * @param piece     piece to be verified
     * @return          
     */
    private boolean verifyAndAdjustPiece(Piece piece) {
        
        if(piece.getType() == PAWN){
            
            /* no pawns on first and last rank */
            if(piece.getCoord().getX() == 0 || piece.getCoord().getX() == 7)
                return false;
            
            /* increase move counter if pawn is not on initial position */
            if(piece.getColor() == WHITE && piece.getCoord().getX()!= 1)
                piece.increaseMoveCounter();
            if(piece.getColor() == BLACK && piece.getCoord().getX()!= 6)
                piece.increaseMoveCounter();
        }
    
        if(piece.getType() == KING){
            
            if(piece.getColor() == WHITE){
                /* only one king per side (same coord is allowed to allow 
                removing of king with the same king selected by button */                
                if(whiteKing!= null && !piece.getCoord().equals(whiteKing.
                        getCoord())) return false;          
                /* no kings on adjacent squares */
                return blackKing == null ||
                        piece.getCoord().distance(blackKing.getCoord()) != 1;
            }
            else{
                if(blackKing!= null && !piece.getCoord()
                        .equals(blackKing.getCoord())) return false;
                return whiteKing == null ||
                        piece.getCoord().distance(whiteKing.getCoord()) != 1;
            }                       
        }        
        return true;
    }

    private void setPiece(Piece piece, int x, int y) {
        
        Piece oldPiece = pieceArray[x][y];

        if (piece.getType() == KING) {
            if (piece.getColor() == WHITE) {
                whiteKing = piece;
                whiteKingCounter++;
            } else {
                blackKing = piece;
                blackKingCounter++;
            }
        }
        
        if (oldPiece != null) {
            /* restore fields if king is deleted */
            if (oldPiece.getType() == KING) {
                if (oldPiece.getColor() == WHITE) {
                    whiteKingCounter--;
                    whiteKing = null;
                } else {
                    blackKingCounter--;
                    blackKing = null;
                }
            }

            /* if the same piece is selected, remove the piece from the board */
            if (piece.getType() == oldPiece.getType()
                    && piece.getColor() == oldPiece.getColor()) {
                /* if king is replaced, another correction is needed (until this
                    point king counter was added and then subtracted by 1
                */
                if(piece.getType() == KING){
                    if(piece.getColor() == WHITE) whiteKingCounter--;
                    else blackKingCounter--;
                }
                
                pieceArray[x][y] = null;
                return;
            }
        }
        
        if(piece.getType() != null) pieceArray[x][y] = piece;    
        else pieceArray[x][y] = null;
    }

    /**
     * Updates the play buttons to be clickable / not clickable.
     */
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

    /**
     * Verifies if the side to move has legal moves in the position,
        otherwise it is an illegal position.
     * 
     * @param colorToMove   color to move and be checked
     * @param castleRights  castling rights in order 0-0 white, 0-0-0 white, 
     *                      0-0 black, 0-0-0 black
     * @return
     */
    private boolean verifyPositionLegal(ChessColor colorToMove, boolean[]
             castleRights) {
        ChessGame game = new ChessGame(pieceArray, colorToMove, castleRights);
        
        return !(game.getRules().isInCheck(colorToMove.getInverse())
                || game.getRules().isStalemate());
    }
}
