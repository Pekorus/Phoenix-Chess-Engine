/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.gui;

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
    
    private final JFrame mainFrame;
    private final int frameHeight;
    private final int frameWidth;
    private final JMenuBar mainBar = new JMenuBar();
    
    public MainView(int frameHeight, int frameWidth) {
        mainFrame = new JFrame("Schaaach");
        this.frameHeight = frameHeight;
        this.frameWidth = frameWidth;
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setResizable(false);
        mainFrame.setSize(frameWidth, frameHeight);
        mainFrame.setLocationRelativeTo(null);
    
        createMenuBar();
        mainFrame.setVisible(true);
    }
 
        private void createMenuBar() {
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGame = new JMenuItem("New Game");
        gameMenu.add(newGame);
        mainBar.add(gameMenu);
    
        mainFrame.setJMenuBar(mainBar);
    }

    void add(JPanel panel) {
        mainFrame.add(panel);
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
}
