/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import static java.awt.GridBagConstraints.WEST;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.border.Border;

/**
 *
 * @author Phoenix
 */
public class MainView extends JFrame{
    
    /* version number */
    private static final String VERSION = "0.9.9";
    
    //fields for main frame
    private final MainController mainControl;
    private final JPanel cardPanel;
    private final CardLayout cards;
    
    //fields for menu bar
    JMenuBar mainBar = new JMenuBar();
    JMenu gameMenu, boardEditor, options, about;
    JMenuItem newGame, closeProgram;
    
    //fields for game type dialog
    final JDialog gameTypeDialog = new JDialog();
    JButton whiteColorButton, blackColorButton, randomColorButton;
    
    //fields for options dialog
    final JDialog optionsDialog = new JDialog();
    JSlider searchDepthSlider; 
    JSlider quietSearchDepthSlider;
    JButton resetDefault;
    JButton applyChanges;
    JCheckBox peterCheckBox;
    
    //about dialog
    final JDialog aboutDialog = new JDialog();
    
    //sprites
    BufferedImage spriteSheet;
    BufferedImage[][] imageArray = new BufferedImage[2][6];
    ImageIcon[][] spriteArray = new ImageIcon[2][6];
    ImageIcon randomColorIcon;
    
    public MainView(MainController mainControl){
        this.mainControl = mainControl;
        this.cards = new CardLayout();
        this.cardPanel = new JPanel(cards);

        /* load sprite sheet and process it */
        createSprites();
        
        /* create components of frame */
        createMenuBar();
        createGameSettingDialog();
        createOptionsDialog();
        createAboutDialog();        
    }
 
        private void createMenuBar() {
            
            gameMenu = new JMenu ("Game");
            mainBar.add(gameMenu);
            
            newGame = new JMenuItem("New Game...");
            newGame.addActionListener(mainControl);
            gameMenu.add(newGame);
            
            closeProgram = new JMenuItem("Exit");
            closeProgram.addActionListener(mainControl);
            gameMenu.add(closeProgram);

            
            boardEditor = new JMenu("Board Editor");
            boardEditor.getComponent().addMouseListener(mainControl);
            mainBar.add(boardEditor);
            
            options = new JMenu("Options");
            options.getComponent().addMouseListener(mainControl);            
            mainBar.add(options);

            about = new JMenu("About");
            mainBar.add(about);
            about.getComponent().addMouseListener(mainControl);       
        
        this.setJMenuBar(mainBar);
    }

    void addToCards(JPanel panel) {
        cardPanel.add(panel);
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
        optionsDialog.setSize(450, 520);
        optionsDialog.setResizable(false);
        optionsDialog.setModal(true);        
        optionsDialog.setLayout(new GridBagLayout());

        /* Constraints for main option boxes */        
        GridBagConstraints mainConstr = new GridBagConstraints();
        mainConstr.gridx = 0;
        mainConstr.gridy = 0;
        mainConstr.fill = VERTICAL;
        mainConstr.gridwidth = 2;        
        mainConstr.insets = new Insets(0, 0, 10, 0);       
        
        Border optionsBorder = BorderFactory.createEtchedBorder();
        
        /* search Depth Panel */
        JPanel searchDepthPanel = new JPanel(new GridBagLayout());
        searchDepthPanel.setBorder(optionsBorder);
        optionsDialog.add(searchDepthPanel, mainConstr);
        GridBagConstraints constr = new GridBagConstraints();
        
        JLabel searchDepthLabel = new JLabel("Search depth", SwingConstants.CENTER);
        constr.gridx = 0;
        constr.gridy = 0;
        constr.insets = new Insets(0,10,0,0);
        searchDepthPanel.add(searchDepthLabel, constr);        
        
        searchDepthSlider = new JSlider(HORIZONTAL, 2, 12, 6);
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
        mainConstr.gridy = 1;
        optionsDialog.add(quietSearchDepthPanel, mainConstr);                
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
        constr.gridwidth = 1;
        
        /* creator mode checkbox */
        JPanel peterCheckBoxPanel = new JPanel(new GridBagLayout());
        peterCheckBoxPanel.setBorder(optionsBorder);
        peterCheckBox = new JCheckBox("Play against creator");
        peterCheckBox.setFocusable(false);
        constr.gridy = 0;
        constr.anchor = WEST;
        peterCheckBoxPanel.add(peterCheckBox, constr);
        JLabel peterTip = new JLabel("<html><center>AI will play Larsen's Opening as "
                + "white and Owen's Defence <br></center> as black (The preferred openings of the "
                + "creator)</html>");
        constr.gridy = 1;
        peterCheckBoxPanel.add(peterTip, constr);
        mainConstr.gridy = 2;
        optionsDialog.add(peterCheckBoxPanel, mainConstr);
        
        /* Buttons at end of options dialog */
        applyChanges = new JButton("Apply changes (starts new game)");
        dialogButton(applyChanges);
        applyChanges.addActionListener(mainControl);
        mainConstr.gridwidth = 1;
        mainConstr.gridy = 3;
        mainConstr.fill = GridBagConstraints.NONE;
        optionsDialog.add(applyChanges, mainConstr);
        
        resetDefault = new JButton("Reset default settings");
        resetDefault.addActionListener(mainControl);
        mainConstr.gridx = 1;
        mainConstr.anchor = GridBagConstraints.EAST;
        optionsDialog.add(resetDefault, mainConstr);
        
    }
    
    private void createAboutDialog(){

        aboutDialog.setTitle("About");
        aboutDialog.setSize(550, 600);
        aboutDialog.setResizable(false);
        aboutDialog.setModal(true);
        
        JPanel aboutDialogPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0,0,20,0);
        c.fill = VERTICAL;
        
        JPanel versionPanel = new JPanel();
        JPanel piecesCreditsPanel = new JPanel();
        customBoxedPanel(piecesCreditsPanel);
        JPanel canCreditsPanel = new JPanel();
        customBoxedPanel(canCreditsPanel);
        JPanel rotateCreditsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        customBoxedPanel(rotateCreditsPanel);
                
        JLabel versionLabel = new JLabel ("<html>Chess created by Peter Korusiewicz<br><br>"
                + "Version: "+VERSION+"<br><br>Send bug reports to peter.korusiewicz@gmail.com</html>");
        versionPanel.add(versionLabel);
        aboutDialogPanel.add(versionPanel, c); 
        
        c.insets = new Insets(0,0,10,0);
        JLabel creditsHead = new JLabel("Image Credits");
        c.gridy = 1;        
        aboutDialogPanel.add(creditsHead, c);
        
        JLabel piecesCreditsLabel = new JLabel("<html>Chess pieces<br><br> https://commons.wikimedia.org/wiki/File:Chess_Pieces_Sprite.svg "
                        + "<br>by jurgenwesterhof (adapted from work of Cburnett) "
                        + "<br><br>Shared under the Creative Commons Attribution ShareAlike 3.0 License"
                        + "<br>https://creativecommons.org/licenses/by-sa/3.0/legalcode</html>", SwingConstants.CENTER);
        piecesCreditsPanel.add(piecesCreditsLabel);
        c.gridy = 2;
        aboutDialogPanel.add(piecesCreditsPanel, c);        
        
        JLabel canCreditsLabel = new JLabel("<html>Trash can icon (board editor)<br><br> https://commons.wikimedia.org/wiki/File:Trash_Can.svg "
                        + "<br>by Andy"
                        + "<br><br>Shared from the Open Clip Art Library which released it into public domain"
                        + "<br>https://openclipart.org/</html>", SwingConstants.CENTER);
        canCreditsPanel.add(canCreditsLabel);
        c.gridy = 3;
        aboutDialogPanel.add(canCreditsPanel, c); 
        
        JLabel rotateCreditsLabel = new JLabel("<html>Rotate icon (board editor)<br><br> https://commons.wikimedia.org/wiki/File:Rotate2_svg.svg "
                        + "<br>by BenjStaw"
                        + "<br><br>Shared under CCO 1.0 Universal Public Domain Dedication"
                        + "</html>", SwingConstants.CENTER);
        rotateCreditsPanel.add(rotateCreditsLabel);
        c.gridy = 4;
        aboutDialogPanel.add(rotateCreditsPanel, c);         
        
        aboutDialog.add(aboutDialogPanel);
    }

    private void createSprites() {
    
        try {
            spriteSheet = ImageIO.read(getClass().
                    getResource("/images/Chess_pieces.png"));
        } catch (IOException ex) {
           loadError("Chess_Pieces.png");
        }        
        /* pieces are sorted like the PieceType Enum: King, Queen, Rook, Bishop,
            Knight, Pawn
        */
        /* white pieces */
        imageArray[1][0] = spriteSheet.getSubimage(5, 5, 75, 75);
        imageArray[1][1] = spriteSheet.getSubimage(88, 5, 75, 75);
        imageArray[1][3] = spriteSheet.getSubimage(171, 5, 75, 75);
        imageArray[1][4] = spriteSheet.getSubimage(254, 5, 75, 75);
        imageArray[1][2] = spriteSheet.getSubimage(338, 5, 75, 75);
        imageArray[1][5] = spriteSheet.getSubimage(420, 5, 75, 75);

        /* black pieces */
        imageArray[0][0] = spriteSheet.getSubimage(5, 89, 75, 75);
        imageArray[0][1] = spriteSheet.getSubimage(88, 89, 75, 75);
        imageArray[0][3] = spriteSheet.getSubimage(171, 89, 75, 75);
        imageArray[0][4] = spriteSheet.getSubimage(254, 89, 75, 75);
        imageArray[0][2] = spriteSheet.getSubimage(338, 89, 75, 75);
        imageArray[0][5] = spriteSheet.getSubimage(420, 89, 75, 75);

        for(int i=0; i<2; i++){
            for(int j=0; j<6; j++){
                spriteArray[i][j] = new ImageIcon(imageArray[i][j]);
            }
        }     
        
        try {
        randomColorIcon =new ImageIcon(ImageIO.read(getClass().
                                    getResource("/images/random_icon.png")));
        } catch (IOException ex) {
           loadError("random_icon.png");
        }
    }

    private void loadError(String string) {
        JOptionPane.showMessageDialog(this, "Could not load data. "
                +string+" seems to be missing. Game will be shut down.", 
                "Loading Failure", JOptionPane.ERROR_MESSAGE);
    
        System.exit(1);
    }

    void doPreparations(ChessGuiView chessPanel) {
        chessPanel.setSpriteArray(spriteArray);
        chessPanel.createView();
    }

    /* Sets the look of a JButton consistently for several parts of the gui */ 
    public static void iconOnlyButton(JButton button) {
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusable(false);
        button.setOpaque(false);
    }

    /* Sets the look of a JButton consistently for several parts of the gui */
    public static void dialogButton(JButton button) {
        button.setFocusable(false);
    }

    public static void customBoxedPanel(JPanel panel){
        panel.setBorder(BorderFactory.createEtchedBorder());
    }
    
    void addCardPanel() {
        this.add(cardPanel);
    }
}
