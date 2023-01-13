package chess.gui;

import chess.options.ChessOptions;
import chess.board.ChessColor;
import static chess.board.ChessColor.*;
import chess.board.Piece;
import chess.board.PieceType;
import static chess.board.PieceType.*;
import chess.coordinate.Coordinate;
import chess.game.ChessGame;
import chess.game.ChessGameEndType;
import chess.game.GameController;
import chess.game.Observer;
import chess.game.Player;
import chess.move.Move;
import chess.move.MoveType;
import static chess.move.MoveType.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * Provides a GUI for a chess game. Needs the classes ChessGuiView and 
 * ChessBoardView (graphical parts) to function.
 */
public class ChessGuiController implements ActionListener, Player, Observer {

    /* graphical part of the gui */
    private final ChessGuiView view;
    /* controller of the game */
    private final GameController gameControl;
    private final ChessGame game;
    private final ChessColor ownColor;
    /* player names */
    private final String whitePlayerName;
    private final String blackPlayerName;
    
    /* stores if gui is in observer mode or can be interacted with as player */
    private final boolean observerMode;
    /* stores human players choice for next promotion */
    private PieceType nextPromotion = null;
    /* stores the next move that human player wants to make */
    private Move nextMove = null;
    /* fields to store user pressed coordinates and which fields are currently
       highlighted by being painted
    */ 
    private Coordinate pressedCoord1 = null, pressedCoord2 = null;
    private Coordinate paintedCoord1, paintedCoord2;
    private Coordinate paintedOppCoord1, paintedOppCoord2;
    
    /**
     * Class constructor.
     * 
     * @param gameControl   controller of the game
     * @param game          current game
     * @param ownColor      color controlled by human player 
     * @param options       options to control gui
     * @param whitePlayerName    name of human player
     * @param blackPlayerName  name of opponents name
     * @param observerMode  determines if gui only observes a game or plays
     */
    public ChessGuiController(GameController gameControl, ChessGame game,
            ChessColor ownColor, ChessOptions options, String whitePlayerName, 
            String blackPlayerName, boolean observerMode) {
        
        this.gameControl = gameControl;
        this.game = game;
        this.ownColor = ownColor;
        this.whitePlayerName = whitePlayerName;
        this.blackPlayerName = blackPlayerName;
        this.view = new ChessGuiView(this, options, ownColor);
        this.observerMode = observerMode;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        if(observerMode) return;
        
        Object source = e.getSource();
        
        /* promotion dialog */
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
        
        /* pressed a square of chess board */
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (source == view.chessBoardPanel.buttonArray[i][j]) {
                    int a = i, b = j;
                    
                    /* translate coordinates if playing white (rotated board) */
                    if (ownColor == WHITE) {
                        a = 7 - i;
                        b = 7 - j;
                    }
                    
                    /* Reset squares if two are already highlighted */
                    if (pressedCoord1 != null && pressedCoord2 != null) {
                        nextMove = null;
                        restoreFields();
                    }
                    Piece clickedPiece = view.pieceArray[a][b];
                    /* first square pressed and there is own piece on it
                        => highlight square and store it 
                    */
                    if (pressedCoord1 == null && clickedPiece != null 
                                && clickedPiece.getColor() ==ownColor) {
                        pressedCoord1 = new Coordinate(a, b);
                        paintedCoord1 = new Coordinate(i, j);
                        view.chessBoardPanel.paintFieldColor(paintedCoord1);
                    /* one square already stored and this square has own
                        piece on it
                        => store as first square and highlight it
                    */
                    } else if (pressedCoord1 != null && pressedCoord2 == null 
                                     && clickedPiece!= null && 
                                           clickedPiece.getColor()== ownColor) {
                        view.chessBoardPanel.restoreFieldColor(paintedCoord1);
                        pressedCoord1 = new Coordinate(a, b);
                        paintedCoord1 = new Coordinate(i, j);
                        view.chessBoardPanel.paintFieldColor(paintedCoord1);                                      
                    /* one square already stored and this square hasn't own
                        piece on it
                        => highlight and store second square and send move to
                        game controller for validation
                    */
                    } else if(pressedCoord1 != null && pressedCoord2 == null) {
                        pressedCoord2 = new Coordinate(a, b);
                        paintedCoord2 = new Coordinate(i, j);
                        view.chessBoardPanel.paintFieldColor(paintedCoord2);
                        createMove(pressedCoord1, pressedCoord2);
                        gameControl.nextMove(ownColor, nextMove);
                    }
                }
            }
        }
    }

    /**
     * Creates a move and stores it in field nextMove. Legality of move is not
     * tested.
     * 
     * @param coordFrom     square of the piece to move
     * @param coordTo       square to move to
     */
    private void createMove(Coordinate coordFrom, Coordinate coordTo) {
        
        /* retrieve pieces from piece array */
        Piece piece1 = view.pieceArray[coordFrom.getX()][coordFrom.getY()];
        Piece piece2 = view.pieceArray[coordTo.getX()][coordTo.getY()];

        /* no piece to move or second piece is own piece */
        if (piece1 == null) {
            return;
        }
        
        MoveType type;
        
        /* determine type of move */
        
        /* capture */
        if (piece2 != null) type = TAKE;           
        /* castling */
        else if (piece1.getType() == KING && coordFrom.distance(coordTo) == 2)
                                                                type = CASTLE;
        /* en passant */
        else if (piece1.getType() == PAWN && 
                                coordFrom.diagonalLineDir(coordTo) != null)
                                                            type = ENPASSANT;
        /* regular move */
        else type = NORMAL;
        
        /* type determined, determine if move is a pawn promotion or not */
        if (piece1.getType() == PAWN && piece1.getColor()==ownColor
                    && pawnPromotionValid()) {
                
            view.setPromoteDialogColor(piece1.getColor());
            view.promoteDialog.setLocationRelativeTo(view);
            view.promoteDialog.setVisible(true);
            nextMove = new Move(PAWN, coordFrom, coordTo, type, nextPromotion);
        }
        /* not a pawn promotion */
        else nextMove = new Move(piece1.getType(), coordFrom, coordTo, type);
        
    }

    @Override
    public void update(Move move) {
          
        if(move!=null)  highlightMove(move);       
        view.update(game, observerMode);        
    }

    /**
     * Verifies if human player wants to make a pawn promotion move.
     * 
     * @return 
     */
    private boolean pawnPromotionValid() {
        if(ownColor==WHITE && pressedCoord2.getX() == 7 && 
                                                      pressedCoord1.getX()==6)
            return true;

        else return ownColor == BLACK && pressedCoord2.getX() == 0
                && pressedCoord1.getX() == 1;
    }

    public String getWhitePlayerName() {
        return whitePlayerName;
    }

    public String getBlackPlayerName() {
        return blackPlayerName;
    }

    @Override
    public void getNextMove() {
    }

    public ChessGuiView getView() {
        return view;
    }
    
    /**
     * Restores all highlighted squares to default colors and resets all fields
     * that store pressed squares.
     */
    private void restoreFields() {        
        view.chessBoardPanel.restoreFieldColor(paintedCoord1);
        view.chessBoardPanel.restoreFieldColor(paintedCoord2);
        view.chessBoardPanel.restoreFieldColor(paintedOppCoord1);
        view.chessBoardPanel.restoreFieldColor(paintedOppCoord2);        
        paintedCoord1 = null;
        paintedCoord2 = null;
        pressedCoord1 = null;
        pressedCoord2 = null;        
        paintedOppCoord1 = null;
        paintedOppCoord2 = null;
    }

    /**
     * Resets all previous highlighted squares and highlights squares of given
     * move.
     * 
     * @param move  move to be highlighted
     */
    private void highlightMove(Move move) {
        Coordinate auxCoord1 = move.getCoordFrom(); 
        Coordinate auxCoord2 = move.getCoordTo();
        /* translate coordinates if board is rotated (playing as white) */
        if(ownColor==WHITE){
            auxCoord1 = auxCoord1.pointSymmCoordinate();
            auxCoord2 = auxCoord2.pointSymmCoordinate();
        }
        restoreFields();
        view.chessBoardPanel.paintFieldColor(auxCoord1);
        view.chessBoardPanel.paintFieldColor(auxCoord2);            
        paintedOppCoord1 = auxCoord1;
        paintedOppCoord2 = auxCoord2;
    }

    @Override
    public void endGame(ChessGameEndType result) {        
    }

    @Override
    public String getPlayerName() {
        return "Human";
    }

}
