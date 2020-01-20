/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.gui;

import static chess.gui.ChessGuiView.iconOnlyButton;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
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
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import static javax.swing.SwingConstants.HORIZONTAL;
import static javax.swing.SwingConstants.VERTICAL;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

/**
 *
 * @author Phoenix
 */
public class MainView{
    
    //string to update version number
    private static final String VERSION = "0.9.0";
    
    //fields for main frame
    private final MainController mainControl;
    private final JFrame mainFrame;
    private final int frameHeight;
    private final int frameWidth;
    private final JPanel cardPanel;
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
    JSlider searchDepthSlider; 
    JSlider quietSearchDepthSlider;
    JButton resetDefault;
    JButton applyChanges;
    
    //about dialog
    final JDialog aboutDialog = new JDialog();
    
    //sprites
    BufferedImage spriteSheet;
    ImageIcon[][] spriteArray = new ImageIcon[2][6];
    ImageIcon randomColorIcon;
    
    public MainView(MainController mainControl, int frameHeight, int frameWidth){
        this.mainControl = mainControl;
        this.mainFrame = new JFrame("Schaaach");
        this.frameHeight = frameHeight;
        this.frameWidth = frameWidth;
        this.cards = new CardLayout();
        this.cardPanel = new JPanel(cards);
       
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setResizable(false);
        mainFrame.setSize(frameWidth, frameHeight);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.add(cardPanel);

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
        cardPanel.add(panel);
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
        cards.next(cardPanel);
        cardPanel.remove(0);
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

        whiteColorButton = new JButton(spriteArray[1][0]);
        iconOnlyButton(whiteColorButton);
        whiteColorButton.addActionListener(mainControl);
        
        blackColorButton = new JButton(spriteArray[0][0]);
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
        optionsDialog.setSize(400, 450);
        optionsDialog.setModal(true);        
        optionsDialog.setLayout(new FlowLayout(VERTICAL));
        
        Border optionsBorder = BorderFactory.createEtchedBorder();
        
        /* search Depth Panel */
        JPanel searchDepthPanel = new JPanel(new GridBagLayout());
        searchDepthPanel.setBorder(optionsBorder);
        optionsDialog.add(searchDepthPanel);
        GridBagConstraints constr = new GridBagConstraints();
        
        JLabel searchDepthLabel = new JLabel("Search depth", SwingConstants.CENTER);
        constr.gridx = 0;
        constr.gridy = 0;
        constr.insets = new Insets(0,10,0,0);
        searchDepthPanel.add(searchDepthLabel, constr);        
        
        searchDepthSlider = new JSlider(HORIZONTAL, 1, 10, 5);
        searchDepthSlider.setMajorTickSpacing(1);
        searchDepthSlider.setPaintTicks(true);
        searchDepthSlider.setPaintLabels(true);        
        constr.gridx = 1;
        constr.gridy = 0;
        constr.insets = new Insets(10,10,10,10);        
        searchDepthPanel.add(searchDepthSlider, constr);

        JLabel searchDepthTip = new JLabel("<html><center>Controls the regular search "
                + "depth.</center><br> <font color='red'>Warning:</font> Processing power and "
                + "memory needed grow <br> exponentially with search depth. "
                + "Increase in moderation!</html>", SwingConstants.CENTER);
        constr.gridx = 0;
        constr.gridy = 1;
        constr.gridwidth = 2;
        searchDepthPanel.add(searchDepthTip, constr);
        constr.gridwidth = 1;
        
        /* quiescence search depth panel */
        JPanel quietSearchDepthPanel = new JPanel(new GridBagLayout());        
        quietSearchDepthPanel.setBorder(optionsBorder);
        optionsDialog.add(quietSearchDepthPanel);                
        JLabel quietSearchDepthLabel = new JLabel("Quiescence search depth", 
                SwingConstants.CENTER);
        constr.gridx = 0;
        constr.gridy = 0;
        constr.insets = new Insets(0,10,0,0);
        quietSearchDepthPanel.add(quietSearchDepthLabel, constr);        
        
        quietSearchDepthSlider = new JSlider(HORIZONTAL, 0, 30, 20);
        quietSearchDepthSlider.setMajorTickSpacing(5);
        quietSearchDepthSlider.setSnapToTicks(true);
        quietSearchDepthSlider.setPaintTicks(true);
        quietSearchDepthSlider.setPaintLabels(true);        
        constr.gridx = 1;
        constr.gridy = 0;
        constr.insets = new Insets(10,10,10,10);        
        quietSearchDepthPanel.add(quietSearchDepthSlider, constr);        

        JLabel quietSearchDepthTip = new JLabel("<html><center>Controls quiescence search "
                + "depth at end of regular search.<br></center> Maximal search depth ="
                + " regular depth + quiescence depth</html>", 
                SwingConstants.CENTER);
        constr.gridx = 0;
        constr.gridy = 1;
        constr.gridwidth = 2;
        quietSearchDepthPanel.add(quietSearchDepthTip, constr);

        /* Buttons at end of options dialog */
        applyChanges = new JButton("Apply changes");
        applyChanges.addActionListener(mainControl);
        optionsDialog.add(applyChanges);
        
        resetDefault = new JButton("Reset default settings");
        resetDefault.addActionListener(mainControl);
        optionsDialog.add(resetDefault);
        
    }
    
    private void createAboutDialog(){

        aboutDialog.setTitle("About");
        aboutDialog.setSize(400, 150);
        aboutDialog.setModal(true);
        
        JLabel versionLabel = new JLabel ("<html>Chess made by Peter Korusiewicz<br/>"
                + "Version: "+VERSION+"<br/>Send bug reports to peter.korusiewicz@gmail.com"
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

    private void loadError(String string) {
        JOptionPane.showMessageDialog(mainFrame, "Could not load data. "
                +string+" seems to be missing. Game will be shut down.", 
                "Loading Failure", JOptionPane.ERROR_MESSAGE);
    
        System.exit(1);
    }

    void doPreparations(ChessGuiView chessPanel) {
        chessPanel.setSpriteArray(spriteArray);
        chessPanel.setMainFrame(mainFrame);
        chessPanel.createView();
    }
}
