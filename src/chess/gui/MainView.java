/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.gui;

import static chess.gui.ChessGuiView.iconOnlyButton;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 *
 * @author Phoenix
 */
public class MainView{
    
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
    
    public MainView(MainController mainControl, int frameHeight, int frameWidth) {
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
        
        //gameTypeDialog.setLocationRelativeTo(mainFrame);
        
        JPanel gameTypePanel = new JPanel(new GridLayout(1, 3));
        gameTypeDialog.add(gameTypePanel);
        gameTypeDialog.setTitle("Game Settings");
        gameTypeDialog.setSize(400, 150);
        gameTypeDialog.setModal(true);
        
        //TODO: add icons
        whiteColorButton = new JButton("White");
        iconOnlyButton(whiteColorButton);
        blackColorButton = new JButton("Black");
        iconOnlyButton(blackColorButton);
        randomColorButton = new JButton("Random");
        iconOnlyButton(randomColorButton);
        whiteColorButton.addActionListener(mainControl);
        blackColorButton.addActionListener(mainControl);
        randomColorButton.addActionListener(mainControl);

        gameTypePanel.add(whiteColorButton);
        gameTypePanel.add(blackColorButton);
        gameTypePanel.add(randomColorButton);
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
        
        JLabel versionLabel = new JLabel ("<html>Chess made by Phoenix<br/>"
                + "Version: 0.5.0<br/>Send bug reports to deimama@deiemail.com"
                + "</html>", SwingConstants.CENTER);
        aboutDialog.add(versionLabel); 
    }
}
