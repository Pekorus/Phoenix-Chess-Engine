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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;
import javax.swing.WindowConstants;

/**
 *
 * Provides the gui of a chess programm. Needs MainView (graphical part) class 
 * to function.
 */
public class MainController extends WindowAdapter implements ActionListener, 
                                                                MouseListener{

    MainView mainView;
    /* controller of the chess agme */
    GameController gameController;
    ChessGameType gameType;
    /* Options to control gui */
    ChessOptions options;
    /* options to control AI behaviour */
    AIOptions aiOptions;
    
    /**
     * Class constructor.
     */
    public MainController() {
        
        this.options = new ChessOptions();         
        /* initialize main gui */
        this.mainView = new MainView(this, options);
        mainView.setTitle("Phoenix Chess Engine");
        mainView.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainView.setResizable(false);
        mainView.setSize(760, 940);
        mainView.addCardPanel();
        
        /* initialize game */
        this.gameType = null;      
        this.aiOptions = new AIOptions();
        this.gameController = new GameController(WHITEPLAYER, options,
                                                                    aiOptions);
        handleViews();
        
        mainView.setLocationRelativeTo(null);
        mainView.setVisible(true);  
    }
       
    @Override
    public void actionPerformed(ActionEvent e) {
       
        Object source = e.getSource();
        
        /* new game button */
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
            mainView.searchDepthSlider.setValue(
                                             aiOptions.getDefaultSearchDepth());
            mainView.quietSearchDepthSlider.setValue(
                                        aiOptions.getDefaultQuietSearchDepth());            
            mainView.peterCheckBox.setSelected(false);
        }
        
        else if(source == mainView.applyChanges){
            
            aiOptions.setSearchDepth(mainView.searchDepthSlider.getValue());
            aiOptions.setQuietSearchDepth(mainView.quietSearchDepthSlider
                                                                   .getValue());
            aiOptions.setCreatorMode(mainView.peterCheckBox.isSelected());          
            options.setCreatorMode(mainView.peterCheckBox.isSelected());
            
            /* start new game */
            mainView.gameTypeDialog.setLocationRelativeTo(mainView);
            mainView.gameTypeDialog.setVisible(true);
            if(gameType != null){ 
                mainView.optionsDialog.setVisible(false);             
                restartGame();            
            }    

        }          
       
        else if(source == mainView.closeProgram){
            gameController.endGame();
            System.exit(0);
       }
    }

    public MainView getMainView() {
        return mainView;
    }

    /**
     * Starts a new chess game with regular starting position.
     */
    public void start() {
        if(gameController!=null) gameController.startGame();               
    }

    /**
     * Closes old game and starts a new one.
     */
    private void restartGame() {
        gameController.endGame();
        this.gameController = new GameController(gameType, options, aiOptions);
        gameType = null;
        handleViews();
        mainView.removeGamePanel();     
        gameController.startGame();
    }

    /**
     * Starts a chess game from a custom postion.
     *  
     * @param pieceArray    board position represented by piece array
     * @param gameType      type of game
     * @param colorToMove   color of player to move first
     * @param castleRights  castling rights in order: 0-0 white, 0-0-0 white,
     *                      0-0- black, 0-0-0 black
     */
    public void startGameFromBoardEditor(Piece[][] pieceArray, ChessGameType 
            gameType, ChessColor colorToMove, boolean[] castleRights){
        
        gameController.endGame();
        this.gameController = new GameController(gameType, options, aiOptions, 
                pieceArray, colorToMove, castleRights);
        this.gameType = null;
        handleViews();
        mainView.removeGamePanel();
        gameController.startGame();           
    }
    
    /**
     * Adds the panel of a ChessGuiView of the game controller to mainView.
     */
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
                    760, 940, mainView.getLocation(), options);
            boardEditor.startEditor();
        }
        
        else if(source == mainView.optionsMenu){
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

    @Override
    public void windowClosing(WindowEvent e) {
       gameController.endGame();    
    }

    @Override
    public void windowClosed(WindowEvent e) {
       gameController.endGame();
    }
        
}
