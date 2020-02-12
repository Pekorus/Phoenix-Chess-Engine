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
 * @author Phoenix
 */
public class ChessGuiView extends JPanel{

    private final ChessGuiController guiController;
    private final ChessColor ownColor;
    Piece[][] pieceArray;
    ImageIcon[][] spriteArray;
    
    /* panels */
    final ChessBoardView chessBoardPanel;
    
    /* right side: player names, result, move display */
    private final JPanel rightSidePanel = new JPanel();
    private final JLabel resultLabel = new JLabel();
    private final JPanel displayMovesPanel = new JPanel();
    private final JScrollPane displayMovesScroll = new JScrollPane(displayMovesPanel, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_NEVER);
    final JDialog promoteDialog = new JDialog();

    /* promotion buttons */ 
    JButton queenButton;
    JButton bishopButton;
    JButton knightButton;
    JButton rookButton;
    
    
    public ChessGuiView(ChessGuiController guiController, ChessColor ownColor) {
        this.ownColor = ownColor;
        this.guiController = guiController;    
        this.chessBoardPanel = new ChessBoardView(guiController, ownColor);
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
    
    public void update(ChessGame game, Object arg) {
        pieceArray = game.getBoard().getPieceArray();
        chessBoardPanel.drawBoard(pieceArray);
        updateMovesDisplay(game.getMoveList());
        if (game.getWinner() != null || game.getDraw() != null) {
            
            setResultLabel(game.getWinner());
            showGameEndDialog(game.getWinner(), game.getDraw());
        }
    }

    private void createPromotionDialog() {

        JPanel promotePanel = new JPanel(new GridLayout(1, 4));
        promoteDialog.add(promotePanel);
        promoteDialog.setTitle("Pawn Promotion");
        promoteDialog.setSize(400, 150);
        promoteDialog.setModal(true);

        queenButton = new JButton(getSprite(QUEEN, WHITE));
        MainView.iconOnlyButton(queenButton);
        bishopButton = new JButton(getSprite(BISHOP, WHITE));
        MainView.iconOnlyButton(bishopButton);
        knightButton = new JButton(getSprite(KNIGHT, WHITE));
        MainView.iconOnlyButton(knightButton);
        rookButton = new JButton(getSprite(ROOK, WHITE));
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

    public void setPromoteDialogColor(ChessColor color) {
        queenButton.setIcon(getSprite(QUEEN, color));
        bishopButton.setIcon(getSprite(BISHOP, color));
        knightButton.setIcon(getSprite(KNIGHT, color));
        rookButton.setIcon(getSprite(ROOK, color));
    }

    private void showGameEndDialog(ChessColor winner, DrawType draw) {
        if (winner != null) {
            JOptionPane.showMessageDialog(chessBoardPanel,
                    winner + " Player has won!", "Game ended", JOptionPane.WARNING_MESSAGE);
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

        if (newestMove == -1) ; else if (newestMove % 2 == 0) {
            JPanel triPanel = new JPanel(new GridBagLayout());
            triPanel.setMaximumSize(new Dimension(220, 20));

            label1 = new JLabel(Integer.toString(newestMove / 2 + 1) + ".", 
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

            //dummy label to be removed when black move is added
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
        if (ownColor == WHITE) {
            playerNames = new JLabel(guiController.getOwnName()
                    + " - " + guiController.getOpponentName());
        } else {
            playerNames = new JLabel(guiController.getOpponentName()
                    + " - " + guiController.getOwnName());
        }
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
    
    public void setSpriteArray(ImageIcon[][] spriteArray) {
        this.spriteArray = spriteArray;
        chessBoardPanel.setSpriteArray(spriteArray);
    }

    private ImageIcon getSprite(PieceType pieceType, ChessColor color) {
        int aux = 0;
        if (color == WHITE) aux = 1;
        
        switch (pieceType) {
            case KING:
                return (spriteArray[aux][0]);

            case QUEEN:
                return (spriteArray[aux][1]);

            case BISHOP:
                return (spriteArray[aux][3]);

            case KNIGHT:
                return (spriteArray[aux][4]);

            case ROOK:
                return (spriteArray[aux][2]);

            case PAWN:
                return (spriteArray[aux][5]);
        }
        return null;
    }
}
