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
import chess.game.DrawType;
import chess.move.Move;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

/**
 *
 * @author Phoenix
 */
public class ChessGuiView extends JFrame {

    private final ChessGuiController guiController;
    private final ChessColor ownColor;
    Piece[][] pieceArray;
    ImageIcon[][] spriteArray;
    
    //frames, panels, dialogs
    private JFrame mainFrame;
    private final JPanel mainPanel = new JPanel(new GridBagLayout());
    private final JPanel chessBoardPanel = new JPanel(new GridBagLayout());
    
    //right side
    private final JPanel rightSidePanel = new JPanel();
    private final JLabel resultLabel = new JLabel();
    private final JPanel displayMovesPanel = new JPanel();
    private final JScrollPane displayMovesScroll = new JScrollPane(displayMovesPanel, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_NEVER);
    final JDialog promoteDialog = new JDialog();

    //buttons
    final JButton[][] buttonArray = new JButton[8][8];
    JButton queenButton;
    JButton bishopButton;
    JButton knightButton;
    JButton rookButton;

    //colors to be used for painting chess squares
    private final Color lightColor = Color.getHSBColor(0.52175f, 0.4f, 0.9f);
    private final Color darkColor = Color.getHSBColor(0.52175f, 0.4f, 0.6f);
    
    public ChessGuiView(ChessGuiController guiController, ChessColor ownColor) {
        this.ownColor = ownColor;
        this.guiController = guiController;    
    }
    
    public void createView(){
        
        //create view components
        createPromotionDialog();
        createChessboardPanel();
        createRightPanel();
        createDisplayMovesScroll();
        
        //filling main panel
        GridBagConstraints c = new GridBagConstraints();
        GridBagConstraints r = new GridBagConstraints();
        
        //c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.2;
        //c.weighty = 0.6;
        mainPanel.add(chessBoardPanel, c);
        
        r.gridx = 1;
        r.gridy = 0;        
        r.weightx = 0.8;
        r.weighty = 1;
        r.anchor = GridBagConstraints.LINE_END;
        r.fill = GridBagConstraints.BOTH;
        mainPanel.add(rightSidePanel, r);
    } 
    
    public void update(ChessGame game, Object arg) {
        pieceArray = game.getBoard().getPieceArray();
        this.drawBoard(pieceArray);
        updateMovesDisplay(game.getMoveList());
        if (game.getWinner() != null || game.getDraw() != null) {
            
            setResultLabel(game.getWinner());
            showGameEndDialog(game.getWinner(), game.getDraw());
        }
    }

    public void drawBoard(Piece[][] pieces) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int a = i, b = j;
                if (ownColor == WHITE) {
                    a = 7 - i;
                    b = 7 - j;
                }
                if (pieces[i][j] == null) {
                    buttonArray[a][b].setIcon(null);
                } else {
                    ImageIcon sprite = getSprite(pieces[i][j].getPiecetype(),
                            pieces[i][j].isColor());
                    buttonArray[a][b].setIcon(sprite);
                }
            }
        }
    }

    private void createPromotionDialog() {

        JPanel promotePanel = new JPanel(new GridLayout(1, 4));
        promoteDialog.add(promotePanel);
        promoteDialog.setTitle("Pawn Promotion");
        promoteDialog.setSize(400, 150);
        promoteDialog.setModal(true);

        queenButton = new JButton(getSprite(QUEEN, WHITE));
        iconOnlyButton(queenButton);
        bishopButton = new JButton(getSprite(BISHOP, WHITE));
        iconOnlyButton(bishopButton);
        knightButton = new JButton(getSprite(KNIGHT, WHITE));
        iconOnlyButton(knightButton);
        rookButton = new JButton(getSprite(ROOK, WHITE));
        iconOnlyButton(rookButton);
        queenButton.addActionListener(guiController);
        bishopButton.addActionListener(guiController);
        knightButton.addActionListener(guiController);
        rookButton.addActionListener(guiController);

        promotePanel.add(queenButton);
        promotePanel.add(bishopButton);
        promotePanel.add(knightButton);
        promotePanel.add(rookButton);
    }

    static void iconOnlyButton(JButton button) {
        button.setBorder(null);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
    }

    public void paintFieldColor(Coordinate coord) {
        if(coord==null) return;
        int x = coord.getX();
        int y = coord.getY();
        if ((x + y) % 2 == 0) {
            buttonArray[x][y].setBackground(lightColor);
        } else {
            buttonArray[x][y].setBackground(darkColor);
        }        
    }

    public void restoreFieldColor(Coordinate coord) {
        if(coord==null) return;
        int x = coord.getX();
        int y = coord.getY();
        if ((x + y) % 2 == 0) {
            buttonArray[x][y].setBackground(Color.white);
        } else {
            buttonArray[x][y].setBackground(Color.gray);
        }
    }

    public void setPromoteDialogColor(ChessColor color) {
        queenButton.setIcon(getSprite(QUEEN, color));
        bishopButton.setIcon(getSprite(BISHOP, color));
        knightButton.setIcon(getSprite(KNIGHT, color));
        rookButton.setIcon(getSprite(ROOK, color));
    }

    private void createChessboardPanel() {
        
        /* wrap every JButton with a JPanel to allow size increase by 
            flow Layout */
        JPanel boardPanel = new JPanel(new GridLayout(8, 8));
        JPanel[][] panelArray = new JPanel[8][8];
        
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                buttonArray[i][j] = new JButton();
                buttonArray[i][j].setBorder(null);
                buttonArray[i][j].setPreferredSize(new Dimension(80,80));

                if ((i + j) % 2 == 0) {
                    buttonArray[i][j].setBackground(Color.white);                
                } else {
                    buttonArray[i][j].setBackground(Color.gray);
                }
                buttonArray[i][j].addActionListener(guiController);
                
                panelArray[i][j] = new JPanel();                               
                panelArray[i][j].add(buttonArray[i][j]);
                boardPanel.add(panelArray[i][j]);
            }
        }
        
        JPanel downCoordAxis = new JPanel(new GridLayout(1,8));
        downCoordAxis.setBackground(Color.orange);
        String[] downCoord = {"A", "B", "C", "D", "E", "F", "G", "H"};
        List<String> downCoordList = Arrays.asList(downCoord);
        
        JPanel rightCoordAxis = new JPanel(new GridLayout(8,1));
        rightCoordAxis.setBackground(Color.orange);        
        String[] rightCoord = {"8", "7", "6", "5", "4", "3", "2", "1"};        
        List<String> rightCoordList = Arrays.asList(rightCoord);
        
        if(ownColor==BLACK){
            Collections.reverse(downCoordList);
            Collections.reverse(rightCoordList);
        }
        
        JLabel auxLabel;
        
        for(int i=0; i<8; i++){
                auxLabel = new JLabel(downCoordList.get(i), SwingConstants.CENTER);
                downCoordAxis.add(auxLabel);
                auxLabel = new JLabel("  "+rightCoordList.get(i)+"  ", SwingConstants.CENTER);        
                rightCoordAxis.add(auxLabel);              
        }        
        
        GridBagConstraints c = new GridBagConstraints();
        
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;        
        chessBoardPanel.add(boardPanel, c);
        
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        chessBoardPanel.add(downCoordAxis, c);
        
        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.VERTICAL;        
        chessBoardPanel.add(rightCoordAxis, c);
    
        c.gridx = 1;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        JPanel extra = new JPanel();
        extra.setBackground(Color.ORANGE);
        chessBoardPanel.add(extra, c);
    }

    private void showGameEndDialog(ChessColor winner, DrawType draw) {
        if (winner != null) {
            JOptionPane.showMessageDialog(mainFrame,
                    winner + " Player has won!", "Game ended", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(mainFrame,
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
            /*if(newestMove/2%2==0){
                label1.setOpaque(true);
                label1.setBackground(Color.LIGHT_GRAY);
            }*/
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
        rightSidePanel.setPreferredSize(new Dimension(200, mainFrame.getHeight()));
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

    void promoteDialogSetLocation() {
        promoteDialog.setLocationRelativeTo(mainFrame);
    }

    public void setMainFrame(JFrame mainFrame){
        this.mainFrame = mainFrame;
    }
    
    public void setSpriteArray(ImageIcon[][] spriteArray) {
        this.spriteArray = spriteArray;
    }
    
    public JPanel getMainPanel(){
        return mainPanel;
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
                return (spriteArray[aux][2]);

            case KNIGHT:
                return (spriteArray[aux][3]);

            case ROOK:
                return (spriteArray[aux][4]);

            case PAWN:
                return (spriteArray[aux][5]);
        }
        return null;
    }
}
