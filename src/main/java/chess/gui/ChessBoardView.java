package chess.gui;

import chess.options.ChessOptions;
import chess.board.ChessColor;
import static chess.board.ChessColor.*;
import chess.board.Piece;
import chess.coordinate.Coordinate;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 *
 * Provides the GUI of a chess board. Needs a controller that implements 
 * ActionListener to function.
 */
public class ChessBoardView extends JPanel{

    /* controller that implements gui logic */
    private final ActionListener controller;
    private ChessColor ownColor;
    
    /* coordinate axes of chess board */
    private final JPanel downCoordAxis = new JPanel(new GridLayout(1,8));
    private final JPanel rightCoordAxis = new JPanel(new GridLayout(8,1));;
    
    /* Options to control gui, including sprites for painting */
    ChessOptions options;

    /* buttons */
    final public JButton[][] buttonArray = new JButton[8][8];
    
    /* colors of chess board squares */
    private final Color lightColor = Color.getHSBColor(0.52175f, 0.4f, 0.9f);
    private final Color darkColor = Color.getHSBColor(0.52175f, 0.4f, 0.6f);
    
    /**
     * Class contructor.
     * 
     * @param controller    controller that implements button logic
     * @param options       options to control gui
     * @param ownColor      color of the player (this color is displayed at 
     *                      bottom) 
     */
    public ChessBoardView(ActionListener controller, ChessOptions options,
                                                        ChessColor ownColor) {

        this.controller = controller;
        this.ownColor = ownColor;
        this.options = options;
    }

    public void createView(){
        
        this.setLayout(new GridBagLayout());        
        createChessboard();
    
    }
    
    public void createChessboard() {
        
        /* wrap every JButton with a JPanel to allow size increase by 
            flow Layout */
        JPanel boardPanel = new JPanel(new GridLayout(8, 8));
        JPanel[][] panelArray = new JPanel[8][8];
        
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                buttonArray[i][j] = new JButton();
                buttonArray[i][j].setBorderPainted(false);
                buttonArray[i][j].setFocusable(false);
                buttonArray[i][j].setPreferredSize(new Dimension(90,90));

                if ((i + j) % 2 == 0) {
                    buttonArray[i][j].setBackground(Color.white);                
                } else {
                    buttonArray[i][j].setBackground(Color.gray);
                }
                buttonArray[i][j].addActionListener(controller);
                
                panelArray[i][j] = new JPanel();                               
                panelArray[i][j].add(buttonArray[i][j]);
                boardPanel.add(buttonArray[i][j]);
            }
        }
        
        initializeCoordAxes(); 
        
        GridBagConstraints c = new GridBagConstraints();
        
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;        
        this.add(boardPanel, c);
        
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(downCoordAxis, c);
        
        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.VERTICAL;        
        this.add(rightCoordAxis, c);
    
        c.gridx = 1;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        JPanel extra = new JPanel();
        extra.setBackground(Color.ORANGE);
        this.add(extra, c);
    }    

    /**
     * Updates the board with given piece array.
     * 
     * @param pieces    board position to be drawn
     */
    public void drawBoard(Piece[][] pieces) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                
                int a = i, b = j;
                /* translate coordinates if board is rotated */
                if (ownColor == WHITE) {
                    a = 7 - i;
                    b = 7 - j;
                }
                if (pieces[i][j] == null) {
                    buttonArray[a][b].setIcon(null);
                } else {
                    ImageIcon sprite = options.getSprite(pieces[i][j].getType(),
                            pieces[i][j].isColor());
                    buttonArray[a][b].setIcon(sprite);
                }
            }
        }
    }

    /**
     * Paints given coordinate with corresponding color stated in constants
     * lightColor and darkColor.
     * 
     * @param coord     coordinate on chess board to be painted 
     */
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

    /**
     * Restores the color of square with given coordinate.
     * 
     * @param coord     coordinate to be restored
     */
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

    public void setOwnColor(ChessColor ownColor) {
        this.ownColor = ownColor;
    }    

    /**
     * Builds coordinate axes of a chess board.
     */
    public void initializeCoordAxes() {
        
        downCoordAxis.removeAll();
        downCoordAxis.setBackground(Color.orange);
        String[] downCoord = {"A", "B", "C", "D", "E", "F", "G", "H"};
        List<String> downCoordList = Arrays.asList(downCoord);
        
        rightCoordAxis.removeAll();
        rightCoordAxis.setBackground(Color.orange);        
        String[] rightCoord = {"8", "7", "6", "5", "4", "3", "2", "1"};        
        List<String> rightCoordList = Arrays.asList(rightCoord);
        
        /* mirror letters and integers if own color is black */
        if(ownColor == BLACK){
            Collections.reverse(downCoordList);
            Collections.reverse(rightCoordList);
        }
        
        JLabel auxLabel;
        
        for(int i=0; i<8; i++){
                auxLabel = new JLabel(downCoordList.get(i), 
                                                        SwingConstants.CENTER);
                downCoordAxis.add(auxLabel);
                auxLabel = new JLabel("  "+rightCoordList.get(i)+"  ", 
                                                        SwingConstants.CENTER);        
                rightCoordAxis.add(auxLabel);              
        }     
    }

    /**
     * Updates chess board including coordinate axes after rotation.
     * 
     * @param pieceArray    rotated board position represented by piece array
     */
    public void rotateAndDrawBoard(Piece[][] pieceArray) {
        initializeCoordAxes();
        this.revalidate();
        this.repaint();
        drawBoard(pieceArray);
    }
    
}
