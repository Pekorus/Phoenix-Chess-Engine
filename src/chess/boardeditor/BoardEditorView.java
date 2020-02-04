/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.boardeditor;

import chess.board.ChessColor;
import static chess.board.ChessColor.WHITE;
import chess.gui.ChessBoardView;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * @author Phoenix
 */
public class BoardEditorView {
    
    private final JFrame mainFrame;
    private final int frameHeight;
    private final int frameWidth; 

    /* sprites */
    ImageIcon[][] spriteArray;
    
    /* JPanels */
    private final JPanel mainPanel;
    protected final ChessBoardView chessBoardPanel;
    private final JPanel buttonPanel;
    
    /* JButtons */
    protected final JButton[][] pieceButtons = new JButton[2][6];
    protected final JButton eraseButton = new JButton("Erase");
    protected final JButton playAsWhiteButton = new JButton("Play as White");
    protected final JButton playAsBlackButton = new JButton("Play as Black");
    protected final JButton rotateBoard = new JButton("rotate board");
    protected JRadioButton whiteToMove , blackToMove;
    protected JCheckBox whiteSmallCastle, blackSmallCastle, whiteLargeCastle,
            blackLargeCastle;
    
    private final BoardEditorController controller;
    
    public BoardEditorView(BoardEditorController controller, int frameHeight, int frameWidth,
                ImageIcon[][] spriteArray) {
        
        this.controller = controller;
        
        this.mainFrame = new JFrame("Board Editor");
        mainFrame.setSize(frameWidth, frameHeight);
        mainFrame.setLayout(new BorderLayout());
        this.frameHeight = frameHeight;
        this.frameWidth = frameWidth;

        this.spriteArray = spriteArray;
        
        this.mainPanel = new JPanel(new GridBagLayout());
        mainFrame.add(mainPanel);
    
        this.chessBoardPanel = new ChessBoardView(controller, WHITE);
        chessBoardPanel.createChessboard();
        chessBoardPanel.setSpriteArray(spriteArray);
        
        this.buttonPanel = new JPanel(new GridBagLayout());
        createButtonPanel();
        //GridBagConstraints constr = new GridBagConstraints();
        //constr.fill = GridBagConstraints.BOTH;
        mainPanel.add(chessBoardPanel);
        mainPanel.add(buttonPanel);
        mainFrame.pack();    
    }    

    public void setVisible(boolean visible){
        mainFrame.setVisible(visible);
    }

    private void createButtonPanel() {
        
        GridBagConstraints constr = new GridBagConstraints();
        
        /* side to move group */
        ButtonGroup colorMoveSelect = new ButtonGroup();
        whiteToMove = new JRadioButton("white to move");
        blackToMove = new JRadioButton("black to move");
        colorMoveSelect.add(whiteToMove);
        colorMoveSelect.add(blackToMove);
        whiteToMove.setSelected(true);
        JPanel radioPanel = new JPanel();
        radioPanel.add(whiteToMove);
        radioPanel.add(blackToMove);        
        constr.gridx = 0;
        constr.gridy = 0;
        constr.gridwidth = 2;
        constr.insets = new Insets(0,0,10,0);
        buttonPanel.add(radioPanel, constr);

    
        /* possible castles group */
        whiteSmallCastle = new JCheckBox("White 0-0");
        whiteLargeCastle = new JCheckBox("White 0-0-0");
        blackSmallCastle = new JCheckBox("Black 0-0");
        blackLargeCastle = new JCheckBox("Black 0-0-0");        
        JPanel castleChecks = new JPanel(new GridLayout(2,2));
        castleChecks.add(whiteSmallCastle);
        castleChecks.add(whiteLargeCastle);
        castleChecks.add(blackSmallCastle);        
        castleChecks.add(blackLargeCastle);
        constr.gridy = 1;
        constr.insets = new Insets(0,0,20,0);        
        buttonPanel.add(castleChecks, constr);        
        constr.gridwidth = 1;
        constr.insets = new Insets(0,0,0,0);        


        for(int i=0; i<2; i++){
            for(int j=0; j<6; j++){
                pieceButtons[i][j] = new JButton(spriteArray[i][j]);
                //MainView.iconOnlyButton(pieceButtons[i][j]);
                pieceButtons[i][j].setBorderPainted(false);
                pieceButtons[i][j].setContentAreaFilled(false);
                pieceButtons[i][j].setOpaque(false);
                pieceButtons[i][j].addActionListener(controller);
                
                constr.gridx = i;
                constr.gridy = j+3;
                buttonPanel.add(pieceButtons[i][j], constr);
            }
        }
        
        //MainView.iconOnlyButton(eraseButton);
        JPanel erasePanel = new JPanel();
        eraseButton.addActionListener(controller);
        erasePanel.add(eraseButton);
        rotateBoard.addActionListener(controller);
        erasePanel.add(rotateBoard);
        constr.gridx = 0;
        constr.gridy = 9;
        constr.gridwidth = 2;
        constr.insets = new Insets(0,0,20,0);   
        buttonPanel.add(erasePanel, constr);

        JPanel playPanel = new JPanel();
        playAsWhiteButton.addActionListener(controller);
        playAsWhiteButton.setEnabled(false);
        playPanel.add(playAsWhiteButton);
        playAsBlackButton.addActionListener(controller);
        playAsBlackButton.setEnabled(false);
        playPanel.add(playAsBlackButton);
        
        constr.gridx = 0;        
        constr.gridy = 10;
        buttonPanel.add(playPanel, constr);
    }

    void setCursor(BufferedImage image) {
        mainFrame.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
                    image, new Point(0,0), "custom cursor"));
        mainFrame.validate();
    }

    void restoreCursor() {
       mainFrame.setCursor(Cursor.getDefaultCursor());
    }

    public void setOwnColor(ChessColor ownColor) {
        chessBoardPanel.setOwnColor(ownColor);
    }

    
}
