/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.gui;

import chess.board.ChessColor;
import chess.board.Piece;
import chess.boardeditor.BoardEditorController;
import chess.game.ChessGameType;
import static chess.game.ChessGameType.*;
import chess.game.GameController;
import chess.options.AIOptions;
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
    AIOptions aiOptions;
    
    public MainController() {
        this.mainView = new MainView(this, 760, 940);
        this.gameType = null;
        this.aiOptions = new AIOptions();
        this.gameController = new GameController(WHITEPLAYER, aiOptions);
        handleViews();
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
       
        else if(source == mainView.whiteColorButton){
           gameType = WHITEPLAYER;
           mainView.gameTypeDialog.setVisible(false);
       } 
       
        else if(source == mainView.blackColorButton){
           gameType = BLACKPLAYER;
           mainView.gameTypeDialog.setVisible(false);
        }       

        else if(source == mainView.randomColorButton){
           Random rand = new Random();
           int aux = rand.nextInt(2);
           gameType = WHITEPLAYER;
           if(aux==0) gameType = BLACKPLAYER;
           mainView.gameTypeDialog.setVisible(false);
        }

        else if(source == mainView.boardEditor){
            BoardEditorController boardEditor = new BoardEditorController(this, 760, 940, 
                    mainView.spriteArray, mainView.imageArray);
            boardEditor.startEditor();
        }
        
        else if(source == mainView.options){
            mainView.optionsDialog.setLocationRelativeTo(mainView.getFrame());
            mainView.optionsDialog.setVisible(true);
        }

        else if(source == mainView.resetDefault){
            mainView.searchDepthSlider.setValue(aiOptions.getDefaultSearchDepth());
            mainView.quietSearchDepthSlider.setValue(aiOptions.getDefaultQuietSearchDepth());            
        }
        
        else if(source == mainView.applyChanges){
            aiOptions.setSearchDepth(mainView.searchDepthSlider.getValue());
            aiOptions.setQuietSearchDepth(mainView.quietSearchDepthSlider.getValue());
            mainView.optionsDialog.setVisible(false);           
        }
        
        else if(source == mainView.about){
        mainView.aboutDialog.setLocationRelativeTo(mainView.getFrame());
        mainView.aboutDialog.setVisible(true);
        }             
       
        else if(source == mainView.closeProgram){
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
        gameController.endGame();
        this.gameController = new GameController(gameType, aiOptions);
        gameType = null;
        handleViews();
        gameController.startGame();
        mainView.removeGamePanel();     
    }

    /* castle rights from 0 to 3: white small castle, white large castle, black
        small castle, black large castle.
    */ 
    public void startGameFromBoardEditor(Piece[][] pieceArray, ChessGameType gameType,
            ChessColor colorToMove, boolean[] castleRights){
        gameController.endGame();
        this.gameController = new GameController(gameType, aiOptions, pieceArray, 
                colorToMove, castleRights);
        this.gameType = null;
        handleViews();
        gameController.startGame();
        mainView.removeGamePanel();           
    }
    
    private void handleViews(){
        ChessGuiView chessPanel = gameController.getView();
        mainView.add(chessPanel.getMainPanel());
        mainView.doPreparations(chessPanel);
        mainView.setVisible(true);  
    }
        
}
