/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.gui;

import chess.game.ChessGameType;
import static chess.game.ChessGameType.*;
import chess.game.GameController;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 *
 * @author Phoenix
 */
public class MainController implements ActionListener, MenuListener {

    MainView mainView;
    GameController gameController;
    ChessGameType gameType;
    
    public MainController() throws IOException {
        this.mainView = new MainView(this, 740, 900);
        this.gameType = WHITEPLAYER;
        this.gameController = new GameController(mainView, gameType);
    }
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
       Object source = e.getSource();
       
        if(source == mainView.newGame){
           try {
               mainView.gameTypeDialog.setLocationRelativeTo(mainView.getFrame());
               mainView.gameTypeDialog.setVisible(true);
               restartGame();
           } catch (IOException ex) {
               Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
           }
        }
       
       if(source == mainView.whiteColorButton){
           gameType = WHITEPLAYER;
           mainView.gameTypeDialog.setVisible(false);
       } 
       
       if(source == mainView.blackColorButton){
           gameType = BLACKPLAYER;
           mainView.gameTypeDialog.setVisible(false);
       }       

       if(source == mainView.randomColorButton){
           //TODO: randomize
           gameType = WHITEPLAYER;
           mainView.gameTypeDialog.setVisible(false);
       }
       
       if(source == mainView.closeProgram){
           System.exit(0);
       }
    }

    public MainView getMainView() {
        return mainView;
    }

    public void start() {
        if(gameController!=null) gameController.startGame();               
    }

    @Override
    public void menuSelected(MenuEvent e) {
        Object source = e.getSource();
        
        if(source == mainView.options){
        }
        
        if(source == mainView.about){
        }        
    }

    @Override
    public void menuDeselected(MenuEvent e) {       
    }

    @Override
    public void menuCanceled(MenuEvent e) {
    }

    private void restartGame() throws IOException {
        this.gameController = new GameController(mainView, gameType);
        gameController.startGame();
        mainView.removeGamePanel();     
    }
 
    
}
