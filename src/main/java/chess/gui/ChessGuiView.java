package chess.gui;

import chess.options.ChessOptions;
import chess.board.ChessColor;
import static chess.board.ChessColor.*;
import chess.board.Piece;
import static chess.board.PieceType.*;
import chess.game.ChessGame;
import chess.game.DrawType;
import chess.move.Move;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.LinkedList;
import javax.swing.*;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

/**
 *
 * Provides a GUI for a chess game. Needs the classes ChessGuiController and 
 * ChessBoardView (chess board panel) to function.
 */
public class ChessGuiView extends JPanel{
    

    private final ChessGuiController guiController;
    /* board state represented as piece array */
    Piece[][] pieceArray;
    /* Options to control gui, including sprites for painting */
    ChessOptions options;
    
    /* panels */
    final ChessBoardView chessBoardPanel;
    
    /* right side: player names, result, move display */
    private final JPanel rightSidePanel = new JPanel();
    private final JLabel resultLabel = new JLabel();
    private final JPanel displayMovesPanel = new JPanel();
    private final JScrollPane displayMovesScroll = new JScrollPane(
                                displayMovesPanel, VERTICAL_SCROLLBAR_ALWAYS, 
                                                    HORIZONTAL_SCROLLBAR_NEVER);
    
    /* dialog for piece promotion */
    final JDialog promoteDialog = new JDialog();
    /* promotion buttons to select piece */ 
    JButton queenButton;
    JButton bishopButton;
    JButton knightButton;
    JButton rookButton;
    
    /**
     * Class constructor.
     * 
     * @param guiController     controller of the gui
     * @param options           options to control gui
     * @param ownColor          color of human player
     */
    public ChessGuiView(ChessGuiController guiController, ChessOptions options,
                                                         ChessColor ownColor) {
        this.guiController = guiController;    
        this.options = options;
        this.chessBoardPanel = new ChessBoardView(guiController, options,
                                                                    ownColor);
    }
    
    public void createView(){
        
        /* create view components */
        createPromotionDialog();
        chessBoardPanel.createView();
        createRightPanel();
        createDisplayMovesScroll();
        
        this.setLayout(new GridBagLayout());
        
        /* filling main panel */
        this.add(chessBoardPanel);
        this.add(rightSidePanel);
    } 

    /**
     * Updates the view after a move was made in the game. 
     * 
     * @param game              game information that should be displayed 
     * @param observerMode      flag that shows if gui is player or observer
     */
    public void update(ChessGame game, boolean observerMode) {
        
        /* draw board */
        pieceArray = game.getBoard().getPieceArray();
        chessBoardPanel.drawBoard(pieceArray);
        /* update moves display and in case of ended game the winner */
        updateMovesDisplay(game.getMoveList());
        if (game.getWinner() != null || game.getDraw() != null) {           
            setResultLabel(game.getWinner());
            if(!observerMode)
                   showGameEndDialog(game.getWinner(), game.getDraw());
        }
    }

    private void createPromotionDialog() {

        JPanel promotePanel = new JPanel(new GridLayout(1, 4));
        promoteDialog.add(promotePanel);
        promoteDialog.setTitle("Pawn Promotion");
        promoteDialog.setSize(400, 150);
        promoteDialog.setModal(true);

        queenButton = new JButton(options.getSprite(QUEEN, WHITE));
        MainView.iconOnlyButton(queenButton);
        bishopButton = new JButton(options.getSprite(BISHOP, WHITE));
        MainView.iconOnlyButton(bishopButton);
        knightButton = new JButton(options.getSprite(KNIGHT, WHITE));
        MainView.iconOnlyButton(knightButton);
        rookButton = new JButton(options.getSprite(ROOK, WHITE));
        MainView.iconOnlyButton(rookButton);
        queenButton.addActionListener(guiController);
        bishopButton.addActionListener(guiController);
        knightButton.addActionListener(guiController);
        rookButton.addActionListener(guiController);

        promotePanel.add(queenButton);
        promotePanel.add(bishopButton);
        promotePanel.add(knightButton);
        promotePanel.add(rookButton);
    }

    /**
     * Sets button sprites of promotion dialog depending on given color.
     * 
     * @param color     color of sprites that will be used
     */
    public void setPromoteDialogColor(ChessColor color) {
        
        queenButton.setIcon(options.getSprite(QUEEN, color));
        bishopButton.setIcon(options.getSprite(BISHOP, color));
        knightButton.setIcon(options.getSprite(KNIGHT, color));
        rookButton.setIcon(options.getSprite(ROOK, color));
    }

    /**
     * Invokes a dialog to inform human player about end of game and the winner.
     * 
     * @param winner    color of player that won the game
     * @param draw      draw type if game is a draw
     */
    private void showGameEndDialog(ChessColor winner, DrawType draw) {
        
        if (winner != null) {
            JOptionPane.showMessageDialog(chessBoardPanel,
                    winner + " Player has won!", "Game ended", 
                                                  JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(chessBoardPanel,
                    "Game ended in a draw (" + draw + ")", "Game ended",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void createDisplayMovesScroll() {
        
        displayMovesPanel.setLayout(new BoxLayout(displayMovesPanel,
                BoxLayout.Y_AXIS));
    }

    private void updateMovesDisplay(LinkedList<Move> moveList) {
        
        JLabel label1, label2, label3, dummyLabel;
        int newestMove = moveList.size() - 1;
        GridBagConstraints numberConstr, move1Constr, move2Constr;

        if (newestMove == -1) return;
        /* number of moves in list is odd */
        else if (newestMove % 2 == 0) {
            JPanel triPanel = new JPanel(new GridBagLayout());
            triPanel.setMaximumSize(new Dimension(220, 20));

            label1 = new JLabel(newestMove / 2 + 1 + ".",
                                                        SwingConstants.CENTER);

            label1.setPreferredSize(new Dimension(40, 20));
            label1.setFont(new Font("Arial", Font.BOLD, 16));
            numberConstr = new GridBagConstraints();
            numberConstr.gridx = 0;
            numberConstr.gridy = newestMove / 2;
            triPanel.add(label1, numberConstr);

            label2 = new JLabel(" " + moveList.getLast().toString());
            label2.setFont(new Font("Arial", Font.BOLD, 16));
            label2.setPreferredSize(new Dimension(80, 20));
            move1Constr = new GridBagConstraints();
            move1Constr.gridx = 1;
            move1Constr.gridy = newestMove / 2;
            triPanel.add(label2, move1Constr);

            /* dummy label to be removed when black's next move is added */
            dummyLabel = new JLabel();
            dummyLabel.setPreferredSize(new Dimension(80, 20));
            move1Constr.gridx = 2;
            triPanel.add(dummyLabel, move1Constr);

            if (newestMove / 2 % 2 == 1) {
                label1.setOpaque(true);
                label1.setBackground(Color.LIGHT_GRAY);
                label2.setOpaque(true);
                label2.setBackground(Color.LIGHT_GRAY);
                dummyLabel.setOpaque(true);
                dummyLabel.setBackground(Color.LIGHT_GRAY);
            }

            displayMovesPanel.add(triPanel);
        /* number of moves in list is even */
        } else {
            JPanel lastPanel = (JPanel) displayMovesPanel.
                    getComponent(displayMovesPanel.getComponentCount() - 1);
            label3 = new JLabel(moveList.getLast().toString());
            label3.setFont(new Font("Arial", Font.BOLD, 16));
            label3.setPreferredSize(new Dimension(80, 20));
            move2Constr = new GridBagConstraints();
            move2Constr.gridx = 2;
            move2Constr.gridy = newestMove / 2;
            if (newestMove / 2 % 2 == 1) {
                label3.setOpaque(true);
                label3.setBackground(Color.LIGHT_GRAY);
            }
            lastPanel.remove(lastPanel.getComponentCount() - 1);
            lastPanel.add(label3, move2Constr);
        }
        displayMovesPanel.repaint();
    }

    private void createRightPanel() {
        rightSidePanel.setLayout(new BoxLayout(rightSidePanel, Y_AXIS));
        createDisplayMovesScroll();
        JLabel playerNames;
        playerNames = new JLabel(guiController.getWhitePlayerName()
                    + " - " + guiController.getBlackPlayerName());

        playerNames.setFont(new Font("Arial", Font.BOLD, 18));
        playerNames.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightSidePanel.add(playerNames);

        resultLabel.setText(" ");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 18));
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightSidePanel.add(resultLabel);
        rightSidePanel.add(displayMovesScroll);
        rightSidePanel.setPreferredSize(new Dimension(200, 700));
    }

    private void setResultLabel(ChessColor winner) {
        if (winner == WHITE) {
            resultLabel.setText("1   -   0");
        } else if (winner == BLACK) {
            resultLabel.setText("0   -   1");
        } else {
            resultLabel.setText("0.5   -   0.5");
        }
    }
    
}
