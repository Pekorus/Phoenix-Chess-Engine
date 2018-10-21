/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.gui;

import chess.board.ChessColor;
import static chess.board.ChessColor.*;
import chess.board.Piece;
import chess.board.PieceType;
import static chess.board.PieceType.*;
import chess.coordinate.Coordinate;
import chess.game.ChessGame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 *
 * @author Phoenix
 */
public class ChessGuiView {

    private final ChessGuiController guiController;
    private final ChessColor ownColor;
    Piece[][] pieceArray;

    //frames, panels, dialogs
    private final JFrame chessBoardFrame = new JFrame("Totally not Gergen's chess");
    private final JPanel boarderPanel = new JPanel(new BorderLayout());
    private final JPanel chessBoardPanel = new JPanel(new GridLayout(8, 8));
    private final JMenuBar mainBar = new JMenuBar();
    final JDialog promoteDialog = new JDialog();

    //buttons
    final JButton[][] buttonArray = new JButton[8][8];
    JButton queenButton;
    JButton bishopButton;
    JButton knightButton;
    JButton rookButton;

    //sprites
    BufferedImage spriteSheet;
    ImageIcon[][] spriteArray = new ImageIcon[2][6];

    public ChessGuiView(ChessGuiController guiController, ChessColor ownColor)
            throws IOException {
        this.ownColor = ownColor;
        this.guiController = guiController;

        createMainFrame();

        //load sprite sheet and process it
        spriteSheet = ImageIO.read(getClass().
                getResource("/images/Chess_pieces.png"));
        createSpriteArray();

        //creating view components
        createMenuBar();
        createPromotionDialog();
        createChessboardPanel();

        promoteDialog.setLocationRelativeTo(chessBoardFrame);
        //boarder panel
        boarderPanel.add(chessBoardPanel, BorderLayout.CENTER);
        boarderPanel.add(mainBar, BorderLayout.PAGE_START);
        //boarderPanel.add(jLabel, BorderLayout.PAGE_END);

        //chessBoardPanel.setSize(400,400);
        //chessBoardPanel.setLayout(new GridLayout(8,8));       
        chessBoardFrame.getContentPane().add(boarderPanel);
        //pack();
    }

    public void update(ChessGame game, Object arg) {
        //TODO: unschöne lösung, geben das PieceArray raus an viewer, könnte
        //verändert werden
        pieceArray = game.getBoard().getPieceArray();
        this.drawBoard(pieceArray);
    }

    public void drawBoard(Piece[][] pieces) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int a = i, b = j;
                if (ownColor == WHITE) {
                    a = 7 - i;
                    b = 7 - j;
                }
                if (pieces[i][j] == null) {
                    buttonArray[a][b].setIcon(null);
                } else {
                    ImageIcon sprite = getSprite(pieces[i][j].getPiecetype(),
                            pieces[i][j].isColor());
                    buttonArray[a][b].setIcon(sprite);
                }
            }
        }
    }

    private ImageIcon getSprite(PieceType pieceType, ChessColor color) {
        int aux = 0;
        if (color == WHITE) {
            aux = 1;
        }

        switch (pieceType) {
            case KING:
                return (spriteArray[aux][0]);

            case QUEEN:
                return (spriteArray[aux][1]);

            case BISHOP:
                return (spriteArray[aux][2]);

            case KNIGHT:
                return (spriteArray[aux][3]);

            case ROOK:
                return (spriteArray[aux][4]);

            case PAWN:
                return (spriteArray[aux][5]);
        }
        return null;
    }

    private void createSpriteArray() {
        BufferedImage whiteKing = spriteSheet.getSubimage(5, 5, 75, 75);
        BufferedImage whiteQueen = spriteSheet.getSubimage(88, 5, 75, 75);
        BufferedImage whiteBishop = spriteSheet.getSubimage(171, 5, 75, 75);
        BufferedImage whiteKnight = spriteSheet.getSubimage(254, 5, 75, 75);
        BufferedImage whiteRook = spriteSheet.getSubimage(338, 5, 75, 75);
        BufferedImage whitePawn = spriteSheet.getSubimage(420, 5, 75, 75);

        BufferedImage blackKing = spriteSheet.getSubimage(5, 89, 75, 75);
        BufferedImage blackQueen = spriteSheet.getSubimage(88, 89, 75, 75);
        BufferedImage blackBishop = spriteSheet.getSubimage(171, 89, 75, 75);
        BufferedImage blackKnight = spriteSheet.getSubimage(254, 89, 75, 75);
        BufferedImage blackRook = spriteSheet.getSubimage(338, 89, 75, 75);
        BufferedImage blackPawn = spriteSheet.getSubimage(420, 89, 75, 75);

        spriteArray[0][0] = new ImageIcon(blackKing);
        spriteArray[0][1] = new ImageIcon(blackQueen);
        spriteArray[0][2] = new ImageIcon(blackBishop);
        spriteArray[0][3] = new ImageIcon(blackKnight);
        spriteArray[0][4] = new ImageIcon(blackRook);
        spriteArray[0][5] = new ImageIcon(blackPawn);
        spriteArray[1][0] = new ImageIcon(whiteKing);
        spriteArray[1][1] = new ImageIcon(whiteQueen);
        spriteArray[1][2] = new ImageIcon(whiteBishop);
        spriteArray[1][3] = new ImageIcon(whiteKnight);
        spriteArray[1][4] = new ImageIcon(whiteRook);
        spriteArray[1][5] = new ImageIcon(whitePawn);
    }

    private void createMenuBar() {
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGame = new JMenuItem("New Game");
        gameMenu.add(newGame);
        mainBar.add(gameMenu);
    }

    public void setVisible(Boolean b) {
        chessBoardFrame.setVisible(b);
    }

    private void createPromotionDialog() {
        JPanel promotePanel = new JPanel(new GridLayout(1, 4));
        promoteDialog.add(promotePanel);
        promoteDialog.setTitle("Pawn Promotion");
        promoteDialog.setSize(400, 150);
        promoteDialog.setModal(true);

        queenButton = new JButton(getSprite(QUEEN, WHITE));
        iconOnlyButton(queenButton);
        bishopButton = new JButton(getSprite(BISHOP, WHITE));
        iconOnlyButton(bishopButton);
        knightButton = new JButton(getSprite(KNIGHT, WHITE));
        iconOnlyButton(knightButton);
        rookButton = new JButton(getSprite(ROOK, WHITE));
        iconOnlyButton(rookButton);
        queenButton.addActionListener(guiController);
        bishopButton.addActionListener(guiController);
        knightButton.addActionListener(guiController);
        rookButton.addActionListener(guiController);

        promotePanel.add(queenButton);
        promotePanel.add(bishopButton);
        promotePanel.add(knightButton);
        promotePanel.add(rookButton);
    }

    private void iconOnlyButton(JButton button) {
        button.setBorder(null);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
    }

    public void paintFieldColor(Coordinate coord) {
        buttonArray[coord.getX()][coord.getY()].setBackground(Color.cyan);
    }

    public void restoreFieldColor(Coordinate coord) {
        int x = coord.getX();
        int y = coord.getY();
        if ((x + y) % 2 == 0) {
            buttonArray[x][y].setBackground(Color.white);
        } else {
            buttonArray[x][y].setBackground(Color.gray);
        }
    }

    public void setPromoteDialogColor(ChessColor color) {
        queenButton.setIcon(getSprite(QUEEN, color));
        bishopButton.setIcon(getSprite(BISHOP, color));
        knightButton.setIcon(getSprite(KNIGHT, color));
        rookButton.setIcon(getSprite(ROOK, color));
    }

    private void createMainFrame() {
        chessBoardFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        chessBoardFrame.setResizable(false);
        chessBoardFrame.setSize(700, 720);
        chessBoardFrame.setLocationRelativeTo(null);
    }

    private void createChessboardPanel() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                buttonArray[i][j] = new JButton();
                buttonArray[i][j].setBorder(null);
                if ((i + j) % 2 == 0) {
                    buttonArray[i][j].setBackground(Color.white);
                } else {
                    buttonArray[i][j].setBackground(Color.gray);
                }
                buttonArray[i][j].addActionListener(guiController);
                chessBoardPanel.add(buttonArray[i][j]);
            }
        }
    }
}
