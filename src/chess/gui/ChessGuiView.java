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
import chess.move.Move;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import javax.swing.*;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

/**
 *
 * @author Phoenix
 */
public class ChessGuiView {

    private final ChessGuiController guiController;
    private final ChessColor ownColor;
    Piece[][] pieceArray;

    //frames, panels, dialogs
    private final JFrame chessBoardFrame = new JFrame("Schaaach");
    private final int frameHeight = 700;
    private final int frameWidth = 900;
    private final JPanel borderPanel = new JPanel(new BorderLayout());
    private final JPanel chessBoardPanel = new JPanel(new GridLayout(8, 8));   
    private final JPanel downPanel= new JPanel();
    //right side
    private final JPanel rightSidePanel = new JPanel();
    private final JLabel resultLabel = new JLabel();
    private final JPanel displayMovesPanel= new JPanel();
    private final JScrollPane displayMovesScroll = new JScrollPane(displayMovesPanel, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_NEVER);
    private final JMenuBar mainBar = new JMenuBar();
    final JDialog promoteDialog = new JDialog();
    
    //buttons
    final JButton[][] buttonArray = new JButton[8][8];
    JButton queenButton;
    JButton bishopButton;
    JButton knightButton;
    JButton rookButton;

    //sprites
    BufferedImage spriteSheet;
    ImageIcon[][] spriteArray = new ImageIcon[2][6];

    public ChessGuiView(ChessGuiController guiController, ChessColor ownColor)
            throws IOException {
        this.ownColor = ownColor;
        this.guiController = guiController;

        //create view components
        createMainFrame();        
        createMenuBar();
        createPromotionDialog();
        createChessboardPanel();
        createRightPanel();
        createDisplayMovesScroll();
        
        //load sprite sheet and process it
        spriteSheet = ImageIO.read(getClass().
                getResource("/images/Chess_pieces.png"));
        createSpriteArray();

        promoteDialog.setLocationRelativeTo(chessBoardFrame);
        //border panel
        borderPanel.add(chessBoardPanel, BorderLayout.CENTER);
        borderPanel.add(mainBar, BorderLayout.PAGE_START);
        borderPanel.add(rightSidePanel, BorderLayout.LINE_END);
        borderPanel.add(downPanel, BorderLayout.PAGE_END);
        chessBoardFrame.add(borderPanel);
        //pack();
    }

    public void update(ChessGame game, Object arg) {
        pieceArray = game.getBoard().getPieceArray();
        this.drawBoard(pieceArray);
        updateMovesDisplay(game.getMoveList());
        if(game.getWinner()!= null){
            setResultLabel(game.getWinner());
            showGameEndDialog(game.getWinner());
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

    private ImageIcon getSprite(PieceType pieceType, ChessColor color) {
        int aux = 0;
        if (color == WHITE) {
            aux = 1;
        }

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

    private void createSpriteArray() {
        BufferedImage whiteKing = spriteSheet.getSubimage(5, 5, 75, 75);
        BufferedImage whiteQueen = spriteSheet.getSubimage(88, 5, 75, 75);
        BufferedImage whiteBishop = spriteSheet.getSubimage(171, 5, 75, 75);
        BufferedImage whiteKnight = spriteSheet.getSubimage(254, 5, 75, 75);
        BufferedImage whiteRook = spriteSheet.getSubimage(338, 5, 75, 75);
        BufferedImage whitePawn = spriteSheet.getSubimage(420, 5, 75, 75);

        BufferedImage blackKing = spriteSheet.getSubimage(5, 89, 75, 75);
        BufferedImage blackQueen = spriteSheet.getSubimage(88, 89, 75, 75);
        BufferedImage blackBishop = spriteSheet.getSubimage(171, 89, 75, 75);
        BufferedImage blackKnight = spriteSheet.getSubimage(254, 89, 75, 75);
        BufferedImage blackRook = spriteSheet.getSubimage(338, 89, 75, 75);
        BufferedImage blackPawn = spriteSheet.getSubimage(420, 89, 75, 75);

        spriteArray[0][0] = new ImageIcon(blackKing);
        spriteArray[0][1] = new ImageIcon(blackQueen);
        spriteArray[0][2] = new ImageIcon(blackBishop);
        spriteArray[0][3] = new ImageIcon(blackKnight);
        spriteArray[0][4] = new ImageIcon(blackRook);
        spriteArray[0][5] = new ImageIcon(blackPawn);
        spriteArray[1][0] = new ImageIcon(whiteKing);
        spriteArray[1][1] = new ImageIcon(whiteQueen);
        spriteArray[1][2] = new ImageIcon(whiteBishop);
        spriteArray[1][3] = new ImageIcon(whiteKnight);
        spriteArray[1][4] = new ImageIcon(whiteRook);
        spriteArray[1][5] = new ImageIcon(whitePawn);
    }

    private void createMenuBar() {
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGame = new JMenuItem("New Game");
        gameMenu.add(newGame);
        mainBar.add(gameMenu);
    }

    public void setVisible(Boolean b) {
        chessBoardFrame.setVisible(b);
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

    private void iconOnlyButton(JButton button) {
        button.setBorder(null);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
    }

    public void paintFieldColor(Coordinate coord) {
        buttonArray[coord.getX()][coord.getY()].setBackground(Color.cyan);
    }

    public void restoreFieldColor(Coordinate coord) {
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

    private void createMainFrame() {
        chessBoardFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        chessBoardFrame.setResizable(false);
        chessBoardFrame.setSize(frameWidth, frameHeight);
        chessBoardFrame.setLocationRelativeTo(null);
    }

    private void createChessboardPanel() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                buttonArray[i][j] = new JButton();
                buttonArray[i][j].setBorder(null);
                if ((i + j) % 2 == 0) {
                    buttonArray[i][j].setBackground(Color.white);
                } else {
                    buttonArray[i][j].setBackground(Color.gray);
                }
                buttonArray[i][j].addActionListener(guiController);
                chessBoardPanel.add(buttonArray[i][j]);
            }
        }
        chessBoardPanel.setPreferredSize(new Dimension(650,650));
    }

    private void showGameEndDialog(ChessColor winner) {
        JOptionPane.showMessageDialog(chessBoardFrame, winner+" Player has won!", "Game ended",
                JOptionPane.WARNING_MESSAGE);
    }

    private void createDisplayMovesScroll() {
        displayMovesPanel.setLayout(new BoxLayout(displayMovesPanel, 
                                                            BoxLayout.Y_AXIS));       
        displayMovesScroll.setPreferredSize(new Dimension(200, 300));
    }

    private void updateMovesDisplay(LinkedList<Move> moveList) {
        JLabel label1, label2, label3;
        int newestMove = moveList.size()-1;
        
        if(newestMove==-1) ; 
        else if(newestMove%2==0){
                JPanel triPanel= new JPanel(new GridLayout(1,3));
                triPanel.setMaximumSize(new Dimension(200, 20));
                label1 = new JLabel((newestMove/2+1)+".");
                label1.setBackground(Color.red);
                label1.setMaximumSize(new Dimension(20, 20));
                label1.setAlignmentY(Component.RIGHT_ALIGNMENT);
                label1.setFont(new Font("Arial", Font.PLAIN, 16));
                label2 = new JLabel(moveList.getLast().toString());
                label2.setFont(new Font("Arial", Font.PLAIN, 16));                
                label2.setMaximumSize(new Dimension(90, 20));
                label2.setAlignmentY(Component.LEFT_ALIGNMENT);                
                triPanel.add(label1);
                triPanel.add(label2);
                displayMovesPanel.add(triPanel);   
            }
            else{
                JPanel lastPanel = (JPanel)displayMovesPanel.
                            getComponent(displayMovesPanel.getComponentCount()-1);
                label3 = new JLabel(moveList.getLast().toString());
                label3.setFont(new Font("Arial", Font.PLAIN, 16));
                lastPanel.add(label3);  
            }
        displayMovesPanel.repaint();
    }

    private void createRightPanel() {
        rightSidePanel.setLayout(new BoxLayout(rightSidePanel, Y_AXIS));
        createDisplayMovesScroll();
        JLabel playerNames;
        if(ownColor==WHITE)playerNames = new JLabel(guiController.getOwnName()+
                                         " - "+guiController.getOpponentName());
        else playerNames = new JLabel(guiController.getOpponentName()+
                                         " - "+guiController.getOwnName());
        playerNames.setFont(new Font("Arial", Font.BOLD, 18));        
        playerNames.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightSidePanel.add(playerNames);        
        
        resultLabel.setText(" ");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 18)); 
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightSidePanel.add(resultLabel);
        rightSidePanel.add(displayMovesScroll);
        rightSidePanel.setPreferredSize(new Dimension(200, frameHeight));        
    }

    private void setResultLabel(ChessColor winner) {
        if(winner==WHITE) resultLabel.setText("1   -   0");
        else resultLabel.setText("0   -   1");
    }
}
