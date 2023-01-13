package chess.gui;

import chess.options.ChessOptions;
import static chess.board.ChessColor.*;
import static chess.board.PieceType.KING;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import static java.awt.GridBagConstraints.WEST;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.IOException;
import java.text.NumberFormat;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import static javax.swing.SwingConstants.HORIZONTAL;
import static javax.swing.SwingConstants.VERTICAL;
import javax.swing.border.Border;

/**
 *
 * Provides the gui of a chess program. Needs MainController class (logic part)
 * to function.
 */
public class MainView extends JFrame{
    
    private static final String VERSION = "1.03";
    
    /* fields for main frame */
    private final MainController mainControl;
    private final JPanel cardPanel;
    private final CardLayout cards;
    private final ChessOptions options;
    
    /* menu bar */
    JMenuBar mainBar = new JMenuBar();
    JMenu gameMenu, boardEditor, optionsMenu, about;
    JMenuItem newGame, closeProgram, aiBattle, aiMatch;
    
    /* game type dialog */
    final JDialog gameTypeDialog = new JDialog();
    JButton whiteColorButton, blackColorButton, randomColorButton;
    
    /* options dialog */
    final JDialog optionsDialog = new JDialog();
    JSlider searchDepthSlider; 
    JSlider quietSearchDepthSlider;
    JButton resetDefault, applyChanges;
    JCheckBox peterCheckBox;
    JFormattedTextField timeField; 
    JRadioButton depthCalc, timeCalc;
    
    /* about dialog */
    final JDialog aboutDialog = new JDialog();
    
    /**
     * Class constructor.
     * 
     * @param mainControl   controller of the gui 
     * @param options       options to control gui, including sprites
     */
    public MainView(MainController mainControl, ChessOptions options){
        
        this.mainControl = mainControl;
        this.cards = new CardLayout();
        this.cardPanel = new JPanel(cards);
        
        this.options = options;
        
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
            
            aiBattle = new JMenuItem("AI vs AI");
            aiBattle.addActionListener(mainControl);
            gameMenu.add(aiBattle);
            
            aiMatch = new JMenuItem("AI match (Beta)");
            aiMatch.addActionListener(mainControl);
            gameMenu.add(aiMatch);
            
            closeProgram = new JMenuItem("Exit");
            closeProgram.addActionListener(mainControl);
            gameMenu.add(closeProgram);

            
            boardEditor = new JMenu("Board Editor");
            boardEditor.getComponent().addMouseListener(mainControl);
            mainBar.add(boardEditor);
            
            optionsMenu = new JMenu("Options");
            optionsMenu.getComponent().addMouseListener(mainControl);            
            mainBar.add(optionsMenu);

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
        
        JLabel chooseColor = new JLabel ("<html><br/>Choose color:<br/></html>",
                                SwingConstants.CENTER);
        gameTypePanel.add(chooseColor, BorderLayout.NORTH);
        
        JPanel colorChoiceButtons = new JPanel(new GridLayout(1, 3));
        gameTypePanel.add(colorChoiceButtons, BorderLayout.CENTER);

        whiteColorButton = new JButton(options.getSprite(KING, WHITE));
        iconOnlyButton(whiteColorButton);
        whiteColorButton.addActionListener(mainControl);
        
        blackColorButton = new JButton(options.getSprite(KING, BLACK));
        iconOnlyButton(blackColorButton);
        blackColorButton.addActionListener(mainControl);
        
        randomColorButton = new JButton(options.getRandomColorIcon());
        iconOnlyButton(randomColorButton);
        randomColorButton.addActionListener(mainControl);

        colorChoiceButtons.add(whiteColorButton);
        colorChoiceButtons.add(blackColorButton);
        colorChoiceButtons.add(randomColorButton);
    
        gameTypeDialog.add(gameTypePanel);
    }

    private void createOptionsDialog(){
        
        optionsDialog.setTitle("AI Options");
        optionsDialog.setSize(600, 700);
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
        
        GridBagConstraints constr = new GridBagConstraints();        
        Border optionsBorder = BorderFactory.createEtchedBorder();
        
        /* panel to choose type of restriction for move calculation */
        JPanel moveCalcPanel = new JPanel(new GridBagLayout());
        moveCalcPanel.setBorder(optionsBorder);
        optionsDialog.add(moveCalcPanel, mainConstr);
        
        mainConstr.insets = new Insets(10,10,20,10);
        moveCalcPanel.add(new JLabel("<html>AI move calculation can be "
                + "restricted in two ways: fixed depth or fixed time.<br><br> "
                + "Restrict by depth:<br> Move will be calculated fully for "
                + "that depth. This can take a long time in extreme cases, "
                + "<br>depending on processor power and complexity of"
                + " position.<br><br> Restrict by time:<br> Given time is "
                + "considered as maximum time to execute a move."
                + " Displayed time in analytics <br>is the actual time "
                + "needed to calculate the move, all additional time went to "
                + "calculate one<br>ply deeper but it was not finished in max "
                + "time. </html>"), mainConstr);
        mainConstr.insets = new Insets(0, 0, 10, 0);
        
        ButtonGroup group = new ButtonGroup();
        timeCalc = new JRadioButton("Restrict by time");
        group.add(timeCalc);
        timeCalc.setFocusable(false);
        constr.gridx = 1;
        constr.gridy = 1;
        moveCalcPanel.add(timeCalc, constr);
        
        depthCalc = new JRadioButton("Restrict by depth");
        group.add(depthCalc);
        depthCalc.setFocusable(false);
        constr.gridx = 0;
        constr.gridy = 1;
        moveCalcPanel.add(depthCalc, constr);        
        
        
        /* search Depth Panel */
        JPanel searchDepthPanel = new JPanel(new GridBagLayout());        
        constr.gridx = 0;
        constr.gridy = 2;
        moveCalcPanel.add(searchDepthPanel, constr);
        
        JLabel searchDepthLabel = new JLabel("Search depth");
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

        JLabel searchDepthTip = new JLabel("<html><center>Controls the regular "
                + "search depth.</center><br> <font color='red'>Warning:</font>"
                + " Processing power and memory needed grow <br> exponentially "
                + "with search depth. Increase in moderation!</html>", 
                SwingConstants.CENTER);
        constr.gridx = 0;
        constr.gridy = 1;
        constr.gridwidth = 2;
        searchDepthPanel.add(searchDepthTip, constr);
        constr.gridwidth = 1;         
        
        
        /* time restriction panel */
        JPanel timePanel = new JPanel (new GridBagLayout());
        constr.gridx = 1;
        constr.gridy = 2;        
        moveCalcPanel.add(timePanel, constr);

        constr.gridx = 0;
        constr.gridy = 0;
        constr.insets = new Insets(0,10,0,0);
        JPanel timeInput = new JPanel(new GridBagLayout());
        timePanel.add(timeInput, constr);
        
        constr.insets = new Insets(0,10,10,0);
        constr.gridwidth = 2;
        timeInput.add(new JLabel("Calculation time: "), constr);
        constr.gridwidth = 1;
        constr.gridy = 1;
        timeField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        timeField.setValue(6);
        timeField.setColumns(3);
        timeInput.add(timeField, constr);
        constr.gridx = 1;
        timeInput.add(new JLabel(" seconds"), constr);
        
        
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
        
        
        /* depth slider */
        quietSearchDepthSlider = new JSlider(HORIZONTAL, 0, 30, 20);
        quietSearchDepthSlider.setMajorTickSpacing(5);
        quietSearchDepthSlider.setSnapToTicks(true);
        quietSearchDepthSlider.setPaintTicks(true);
        quietSearchDepthSlider.setPaintLabels(true);        
        constr.gridx = 1;
        constr.gridy = 0;
        constr.insets = new Insets(10,10,10,10);        
        quietSearchDepthPanel.add(quietSearchDepthSlider, constr);        

        JLabel quietSearchDepthTip = new JLabel("<html><center>Controls "
                + "quiescence search depth at end of regular search.<br>"
                + "</center> Maximal search depth = regular depth + quiescence "
                + "depth</html>", SwingConstants.CENTER);
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
        JLabel peterTip = new JLabel("<html><center>AI will play Larsen's "
                + "Opening as white and Owen's Defence <br></center> as black "
                + "(The preferred openings of the creator)</html>");
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
        aboutDialog.setSize(600, 550);
        aboutDialog.setResizable(false);
        aboutDialog.setModal(true);
        
        JPanel aboutDialogPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        JPanel versionPanel = new JPanel();
        JPanel piecesCreditsPanel = customBoxedPanel();
        JPanel canCreditsPanel = customBoxedPanel();
        JPanel rotateCreditsPanel = customBoxedPanel();
        JPanel fastUtilsPanel = customBoxedPanel();
        
        c.insets = new Insets(0,0,20,0);
        c.fill = VERTICAL;        
        c.gridwidth = 2;
        versionPanel.add(new JLabel ("<html>Chess created by Peter "
                + "Korusiewicz<br><br>Version: "+VERSION+"<br><br>Send bug "
                        + "reports to peter.korusiewicz@gmail.com</html>"));
        aboutDialogPanel.add(versionPanel, c); 
        
        c.gridwidth = 1;
        c.insets = new Insets(0,0,10,0);
        JLabel creditsHead = new JLabel("Image credits");
        c.gridy = 1;        
        aboutDialogPanel.add(creditsHead, c);
        
        piecesCreditsPanel.add(new JLabel("<html>Chess pieces<br><br> https://c"
                + "ommons.wikimedia.org/wiki/File:Chess_Pieces_Sprite.svg "
                + "<br>by jurgenwesterhof (adapted from work of Cburnett) "
                + "<br><br>Shared under the Creative Commons Attribution ShareA"
                + "like 3.0 License<br>https://creativecommons.org/licenses/by-"
                + "sa/3.0/legalcode</html>"));
        c.gridy = 2;
        aboutDialogPanel.add(piecesCreditsPanel, c);        
        
        ImageIcon trashCanImage;
        ImageIcon rotateIcon;
        /* load icons (trash can and rotate board) */
        try {
            trashCanImage = new ImageIcon(ImageIO.read(getClass().
                                    getResource("/Trash_can.png")));
            rotateIcon = new ImageIcon(ImageIO.read(getClass().
                                    getResource("/Rotate_icon.png")));

            canCreditsPanel.add(new JLabel(trashCanImage));
            canCreditsPanel.add(new JLabel("<html>"
                + "https://commons.wikimedia.org/wiki/File:Trash_Can.svg "
                + "<br>by Andy<br><br>Shared from the Open Clip Art Library "
                + "which released it into public domain<br>https://openclipart."
                + "org/</html>"));
            c.gridy = 3;
            aboutDialogPanel.add(canCreditsPanel, c); 
        
            rotateCreditsPanel.add(new JLabel(rotateIcon));
            rotateCreditsPanel.add(new JLabel("<html>"
                + "https://commons.wikimedia.org/wiki/File:Rotate2_svg.svg"
                + " <br>by BenjStaw<br><br>Shared under CCO 1.0 Universal Publi"
                + "c Domain Dedication</html>"));
            c.gridy = 4;
            aboutDialogPanel.add(rotateCreditsPanel, c);         
        } catch (IOException ex) {
        }
        
        aboutDialog.add(aboutDialogPanel);
    }

    void doPreparations(ChessGuiView chessPanel) {
        this.addWindowListener(mainControl);
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

    /**
     * Creates a boxed panel with FlowLayout.LEFT for credits of about page
     * 
     * @return  custom boxed panel
     */
    private JPanel customBoxedPanel(){
        JPanel boxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        boxPanel.setBorder(BorderFactory.createEtchedBorder());
        return boxPanel;
    }
    
    void addCardPanel() {
        this.add(cardPanel);
    }

}
