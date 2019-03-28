/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.gui;

import chess.board.ChessColor;
import static chess.board.ChessColor.*;
import chess.board.PieceType;
import static chess.board.PieceType.KING;
import static chess.gui.ChessGuiView.iconOnlyButton;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 *
 * @author Phoenix
 */
public class MainView{
    
    private static final String version = "0.5.9";
    
    //fields for main frame
    private final MainController mainControl;
    private final JFrame mainFrame;
    private final int frameHeight;
    private final int frameWidth;
    private final JPanel mainPanel;
    private final CardLayout cards;
    
    //fields for menu bar
    JMenuBar mainBar = new JMenuBar();
    JMenu gameMenu;
    JMenuItem newGame, closeProgram, options, about;
    
    //fields for game type dialog
    final JDialog gameTypeDialog = new JDialog();
    JButton whiteColorButton, blackColorButton, randomColorButton;
    
    //fields for options dialog
    final JDialog optionsDialog = new JDialog();
    
    //about dialog
    final JDialog aboutDialog = new JDialog();
    
    //sprites
    BufferedImage spriteSheet;
    ImageIcon[][] spriteArray = new ImageIcon[2][6];
    ImageIcon randomColorIcon;
    
    public MainView(MainController mainControl, int frameHeight, int frameWidth) throws IOException {
        this.mainControl = mainControl;
        this.mainFrame = new JFrame("Schaaach");
        this.frameHeight = frameHeight;
        this.frameWidth = frameWidth;
        this.cards = new CardLayout();
        this.mainPanel = new JPanel(cards);
        
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setResizable(false);
        mainFrame.setSize(frameWidth, frameHeight);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.add(mainPanel);

        //load sprite sheet and process it
        createSprites();
        
        //create components of frame
        createMenuBar();
        createGameSettingDialog();
        createOptionsDialog();
        createAboutDialog();
        
        mainFrame.setVisible(true);
    }
 
        private void createMenuBar() {
        gameMenu = new JMenu("Game");
            newGame = new JMenuItem("New Game...");
            newGame.addActionListener(mainControl);
            gameMenu.add(newGame);
            
            options = new JMenuItem("Options");
            gameMenu.add(options);
            options.addActionListener(mainControl);            

            about = new JMenuItem("About");
            gameMenu.add(about);
            about.addActionListener(mainControl);       

            closeProgram = new JMenuItem("Exit");
            closeProgram.addActionListener(mainControl);
            gameMenu.add(closeProgram);
        mainBar.add(gameMenu);
        
        mainFrame.setJMenuBar(mainBar);
    }

    void add(JPanel panel) {
        mainPanel.add(panel);
    }

    void setVisible(boolean b) {
        mainFrame.setVisible(b);
    }    

    Component getFrame() {
        return mainFrame;
    }

    int getHeight() {
        return frameHeight;
    }

    int getWidth() {
        return frameWidth;
    }

    void removeGamePanel() {
        cards.next(mainPanel);
        mainPanel.remove(0);
    }

    private void createGameSettingDialog() {
        
        gameTypeDialog.setTitle("Game Settings");
        gameTypeDialog.setSize(400, 150);
        gameTypeDialog.setModal(true);
                
        JPanel gameTypePanel = new JPanel(new BorderLayout());
        
        JLabel chooseColor = new JLabel ("<html> <br/>Choose color:<br/> </html>", SwingConstants.CENTER);
        gameTypePanel.add(chooseColor, BorderLayout.NORTH);
        
        JPanel colorChoiceButtons = new JPanel(new GridLayout(1, 3));
        gameTypePanel.add(colorChoiceButtons, BorderLayout.CENTER);

        whiteColorButton = new JButton(getSprite(KING,WHITE));
        iconOnlyButton(whiteColorButton);
        whiteColorButton.addActionListener(mainControl);
        
        blackColorButton = new JButton(getSprite(KING,BLACK));
        iconOnlyButton(blackColorButton);
        blackColorButton.addActionListener(mainControl);
        
        randomColorButton = new JButton(randomColorIcon);
        iconOnlyButton(randomColorButton);
        randomColorButton.addActionListener(mainControl);

        colorChoiceButtons.add(whiteColorButton);
        colorChoiceButtons.add(blackColorButton);
        colorChoiceButtons.add(randomColorButton);
    
        gameTypeDialog.add(gameTypePanel);
    }

    private void createOptionsDialog(){
        
        optionsDialog.setTitle("Game Options");
        optionsDialog.setSize(400, 150);
        optionsDialog.setModal(true);        
    
        JLabel optionsLabel = new JLabel("not supported yet", SwingConstants.CENTER);
        optionsDialog.add(optionsLabel);
    }
    
    private void createAboutDialog(){

        aboutDialog.setTitle("About");
        aboutDialog.setSize(400, 150);
        aboutDialog.setModal(true);
        
        JLabel versionLabel = new JLabel ("<html>Chess made by Peter Korusiewicz<br/>"
                + "Version: "+version+"<br/>Send bug reports to peter.korusiewicz@gmail.com"
                + "</html>", SwingConstants.CENTER);
        aboutDialog.add(versionLabel); 
    }

    private void createSprites() {
    
        try {
            spriteSheet = ImageIO.read(getClass().
                    getResource("/images/Chess_pieces.png"));
        } catch (IOException ex) {
           loadError("Chess_Pieces.png");
        }        
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

        try {
        randomColorIcon =new ImageIcon(ImageIO.read(getClass().
                                    getResource("/images/random_icon.png")));
        } catch (IOException ex) {
           loadError("random_icon.png");
        }
    }

    public ImageIcon getSprite(PieceType pieceType, ChessColor color) {
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

    private void loadError(String string) {
        JOptionPane.showMessageDialog(mainFrame, "Could not load data. "
                +string+" seems to be missing. Game will be shut down.", 
                "Loading Failure", JOptionPane.ERROR_MESSAGE);
    
        System.exit(1);
    }
}
