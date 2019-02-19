/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.gui;

import java.awt.CardLayout;
import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 *
 * @author Phoenix
 */
public class MainView{
    
    private final MainController mainControl;
    private final JFrame mainFrame;
    private final int frameHeight;
    private final int frameWidth;
    private JPanel mainPanel;
    private final CardLayout cards;
    
    JMenuBar mainBar = new JMenuBar();
    JMenu gameMenu, options, about;
    JMenuItem newGame, closeProgram;
    
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
        mainFrame.setVisible(true);
    }
 
        private void createMenuBar() {
        gameMenu = new JMenu("Game");
            newGame = new JMenuItem("New Game");
            newGame.addActionListener(mainControl);
            gameMenu.add(newGame);
            
            closeProgram = new JMenuItem("Exit");
            closeProgram.addActionListener(mainControl);
            gameMenu.add(closeProgram);
        mainBar.add(gameMenu);
    
        options = new JMenu("Options");
        mainBar.add(options);
        options.addMenuListener(mainControl);
        
        about = new JMenu("About");
        mainBar.add(about);
        about.addMenuListener(mainControl);
        
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

    int getWidth()  {
        return frameWidth;
    }

    void removeGamePanel() {
        cards.next(mainPanel);
        //mainFrame.remove(mainPanel);
        //mainFrame.add(mainPanel);
        //cards.last(mainPanel);
        refresh();
        //mainFrame.getContentPane().removeAll();
        //mainFrame.getGlassPane().repaint();
    }

    void refresh() {
        //mainFrame.pack();
        //mainFrame.revalidate();
        //mainFrame.repaint();
    }
}
