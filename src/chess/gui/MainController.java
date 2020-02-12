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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Random;
import javax.swing.WindowConstants;

/**
 *
 * @author Phoenix
 */
public class MainController implements ActionListener, MouseListener{

    MainView mainView;
    GameController gameController;
    ChessGameType gameType;
    AIOptions aiOptions;
    
    public MainController() {
        
        this.mainView = new MainView(this);
        mainView.setTitle("Schaaach");
        mainView.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainView.setResizable(false);
        mainView.setSize(760, 940);
        mainView.addCardPanel();
        
        this.gameType = null;
        this.aiOptions = new AIOptions();
        this.gameController = new GameController(WHITEPLAYER, aiOptions);
        handleViews();
        
        mainView.setLocationRelativeTo(null);
        mainView.setVisible(true);  
    }
       
    @Override
    public void actionPerformed(ActionEvent e) {
       Object source = e.getSource();
       
        if(source == mainView.newGame){
               mainView.gameTypeDialog.setLocationRelativeTo(mainView);
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

        else if(source == mainView.resetDefault){
            mainView.searchDepthSlider.setValue(aiOptions.getDefaultSearchDepth());
            mainView.quietSearchDepthSlider.setValue(aiOptions.getDefaultQuietSearchDepth());            
            mainView.peterCheckBox.setSelected(false);
        }
        
        else if(source == mainView.applyChanges){
            
            aiOptions.setSearchDepth(mainView.searchDepthSlider.getValue());
            aiOptions.setQuietSearchDepth(mainView.quietSearchDepthSlider.getValue());
            aiOptions.setPeterMode(mainView.peterCheckBox.isSelected());          
            
            /* start new game */
            mainView.gameTypeDialog.setLocationRelativeTo(mainView);
            mainView.gameTypeDialog.setVisible(true);
            if(gameType != null){ 
                mainView.optionsDialog.setVisible(false);             
                restartGame();            
            }    

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
        mainView.removeGamePanel();     
        gameController.startGame();
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
        mainView.removeGamePanel();
        gameController.startGame();           
    }
    
    private void handleViews(){
        ChessGuiView chessPanel = gameController.getView();
        mainView.addToCards(chessPanel);
        mainView.doPreparations(chessPanel);
        mainView.pack(); 
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        
        Object source = e.getSource();
        
        if(source == mainView.boardEditor){
            BoardEditorController boardEditor = new BoardEditorController(this, 
                    760, 940, mainView.getLocation(),
                    mainView.spriteArray, mainView.imageArray);
            boardEditor.startEditor();
        }
        
        else if(source == mainView.options){
            mainView.optionsDialog.setLocationRelativeTo(mainView);
            mainView.optionsDialog.setVisible(true);
        }
    
        else if(source == mainView.about){
            mainView.aboutDialog.setLocationRelativeTo(mainView);
            mainView.aboutDialog.setVisible(true);
        }         
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
       
    }

    @Override
    public void mouseExited(MouseEvent e) {
        
    }
        
}
