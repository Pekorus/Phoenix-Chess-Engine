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
import java.util.Random;

/**
 *
 * @author Phoenix
 */
public class MainController implements ActionListener{

    MainView mainView;
    GameController gameController;
    ChessGameType gameType;
    
    public MainController() {
        this.mainView = new MainView(this, 740, 900);
        this.gameType = null;
        this.gameController = new GameController(mainView, WHITEPLAYER);
    }
       
    @Override
    public void actionPerformed(ActionEvent e) {
       Object source = e.getSource();
       
        if(source == mainView.newGame){
               mainView.gameTypeDialog.setLocationRelativeTo(mainView.getFrame());
               mainView.gameTypeDialog.setVisible(true);
               if(gameType != null){
                   restartGame();
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
           Random rand = new Random();
           int aux = rand.nextInt(2);
           gameType = WHITEPLAYER;
           if(aux==0) gameType = BLACKPLAYER;
           mainView.gameTypeDialog.setVisible(false);
       }

        if(source == mainView.options){
        mainView.optionsDialog.setLocationRelativeTo(mainView.getFrame());
        mainView.optionsDialog.setVisible(true);
        }
        
        if(source == mainView.about){
        mainView.aboutDialog.setLocationRelativeTo(mainView.getFrame());
        mainView.aboutDialog.setVisible(true);
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

    private void restartGame() {
        this.gameController = new GameController(mainView, gameType);
        gameType = null;
        gameController.startGame();
        mainView.removeGamePanel();     
    }
 
    
}
