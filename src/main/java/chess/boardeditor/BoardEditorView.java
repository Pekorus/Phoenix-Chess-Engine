package chess.boardeditor;

import chess.board.ChessColor;
import static chess.board.ChessColor.WHITE;
import chess.gui.ChessBoardView;
import chess.options.ChessOptions;
import chess.gui.MainView;
import static chess.gui.MainView.dialogButton;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.WEST;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * Provides a board editor (with gui) to create custom chess positions and start 
 * them. Needs the class BoardEditorController (logic of the editor) to 
 * function.
 */
public class BoardEditorView extends JFrame {

    /* Options including sprites */
    ChessOptions options;
    ImageIcon rotateIcon;
    BufferedImage trashCanImage;
    
    /* JPanels */
    private final JPanel mainPanel;
    protected final ChessBoardView chessBoardPanel;
    private final JPanel buttonPanel;

    /* JButtons */
    protected final JButton[][] pieceButtons = new JButton[2][6];
    protected JButton eraseButton;
    protected final JButton playAsWhiteButton = new JButton("Play as White");
    protected final JButton playAsBlackButton = new JButton("Play as Black");
    protected JButton rotateBoard;
    protected JRadioButton whiteToMove, blackToMove;
    protected JCheckBox whiteSmallCastle, blackSmallCastle, whiteLargeCastle,
            blackLargeCastle;

    /* controller of the gui */
    private final BoardEditorController controller;

    /**
     * Class contructor.
     * 
     * @param controller    controller of the gui
     * @param options       options containing sprites to paint
     */
    public BoardEditorView(BoardEditorController controller,
            ChessOptions options) {

        this.controller = controller;
        this.options = options;
        this.mainPanel = new JPanel(new GridBagLayout());
        this.chessBoardPanel = new ChessBoardView(controller, options, WHITE);
        this.buttonPanel = new JPanel(new GridBagLayout());
        
    }

    void createView() {
        
        /* load icons (trash can and rotate board) */
        try {
            trashCanImage = ImageIO.read(getClass().
                                    getResource("/images/Trash_can.png"));
            rotateIcon = new ImageIcon(ImageIO.read(getClass().
                                    getResource("/images/Rotate_icon.png")));
        } catch (IOException ex) {
        }   
        eraseButton = new JButton(new ImageIcon(trashCanImage));
        rotateBoard = new JButton(rotateIcon);
        
        try {
            trashCanImage = ImageIO.read(getClass().
                                    getResource("/images/Trash_can.png"));
        } catch (IOException ex) {
        }   
        eraseButton = new JButton(new ImageIcon(trashCanImage));        
        
        chessBoardPanel.createView();       
        createButtonPanel();
        
        mainPanel.add(chessBoardPanel);
        mainPanel.add(buttonPanel);
        this.add(mainPanel);
    }

    /**
     * Creates the button panel on right side of board editor.
     */
    private void createButtonPanel() {

        GridBagConstraints constr = new GridBagConstraints();

        /* side to move group */
        ButtonGroup colorMoveSelect = new ButtonGroup();
        whiteToMove = new JRadioButton("white to move");
        blackToMove = new JRadioButton("black to move");
        whiteToMove.setFocusable(false);
        blackToMove.setFocusable(false);
        colorMoveSelect.add(whiteToMove);
        colorMoveSelect.add(blackToMove);
        whiteToMove.setSelected(true);
        JPanel radioPanel = new JPanel();
        radioPanel.add(whiteToMove);
        radioPanel.add(blackToMove);
        constr.gridx = 0;
        constr.gridy = 0;
        constr.gridwidth = 2;
        constr.insets = new Insets(0, 0, 10, 0);
        buttonPanel.add(radioPanel, constr);

        /* possible castles group */
        whiteSmallCastle = new JCheckBox("White 0-0");
        whiteLargeCastle = new JCheckBox("White 0-0-0");
        blackSmallCastle = new JCheckBox("Black 0-0");
        blackLargeCastle = new JCheckBox("Black 0-0-0");
        whiteSmallCastle.setFocusable(false);
        whiteLargeCastle.setFocusable(false);
        blackSmallCastle.setFocusable(false);
        blackLargeCastle.setFocusable(false);

        JPanel castleChecks = new JPanel(new GridLayout(2, 2));
        castleChecks.add(whiteSmallCastle);
        castleChecks.add(whiteLargeCastle);
        castleChecks.add(blackSmallCastle);
        castleChecks.add(blackLargeCastle);
        constr.gridy = 1;
        constr.insets = new Insets(0, 0, 20, 0);
        buttonPanel.add(castleChecks, constr);
        constr.insets = new Insets(0, 0, 0, 0);

        /* piece buttons */
        constr.weightx = 0.5;
        constr.gridwidth = 1;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 6; j++) {
                ImageIcon[][] spriteArray = options.getSpriteArray();
                pieceButtons[i][j] = new JButton(spriteArray[i][j]);
                MainView.iconOnlyButton(pieceButtons[i][j]);
                pieceButtons[i][j].addActionListener(controller);

                if (i == 0) {
                    constr.anchor = EAST;
                } else {
                    constr.anchor = WEST;
                }

                constr.gridx = i;
                constr.gridy = j + 3;
                buttonPanel.add(pieceButtons[i][j], constr);
            }
        }

        /* erase and rotate board buttons */
        eraseButton.addActionListener(controller);
        MainView.iconOnlyButton(eraseButton);
        
        constr.gridx = 0;
        constr.gridy = 9;
        constr.gridwidth = 1;
        constr.anchor = EAST;
        constr.insets = new Insets(0, 0, 20, 0);
        buttonPanel.add(eraseButton, constr);

        constr.gridx = 1;
        constr.anchor = WEST;
        MainView.iconOnlyButton(rotateBoard);
        rotateBoard.addActionListener(controller);
        buttonPanel.add(rotateBoard, constr);

        /* play as white and black buttons */
        JPanel playPanel = new JPanel();
        playAsWhiteButton.addActionListener(controller);
        dialogButton(playAsWhiteButton);
        playAsWhiteButton.setEnabled(false);
        playPanel.add(playAsWhiteButton);
        playAsBlackButton.addActionListener(controller);
        dialogButton(playAsBlackButton);
        playAsBlackButton.setEnabled(false);
        playPanel.add(playAsBlackButton);

        constr.anchor = CENTER;
        constr.gridx = 0;
        constr.gridy = 10;
        constr.gridwidth = 2;
        buttonPanel.add(playPanel, constr);
    }

    /**
     * Changes ths image of the cursor to that of given image. 
     * 
     * @param image    change cursor to this image
     */
    void setCursor(BufferedImage image) {
        this.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
                image, new Point(0, 0), "custom cursor"));
        this.validate();
    }

    void restoreCursor() {
        this.setCursor(Cursor.getDefaultCursor());
    }

    public void setOwnColor(ChessColor ownColor) {
        chessBoardPanel.setOwnColor(ownColor);
    }
    
}
