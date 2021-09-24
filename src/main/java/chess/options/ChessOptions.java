package chess.options;

import chess.board.ChessColor;
import static chess.board.ChessColor.WHITE;
import chess.board.PieceType;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * Provides a set of options for a chess gui.
 */
public class ChessOptions {
    
    /* piece images to be used */
    private final BufferedImage[][] imageArray = new BufferedImage[2][6];
    /* piece sprites to be used */
    private final ImageIcon[][] spriteArray = new ImageIcon[2][6];;     
    /* random color icon */
    private ImageIcon randomColorIcon;
    /* creator sprites for creator mode */
    private ImageIcon creatorWhiteImage;
    private ImageIcon creatorBlackImage;
    /* stores if game played is in creator mode or not */
    private boolean creatorMode;
    /* stores color of pieces of creator in creator mode */
    private ChessColor  creatorColor;
    
    public ChessOptions() {
        
        loadSprites();
        this.creatorMode = false;
        this.creatorColor = null;
    }

    public ImageIcon[][] getSpriteArray() {
        return spriteArray;
    }

    public ImageIcon getCreatorImage() {
        return creatorWhiteImage;
    }

    public ImageIcon getRandomColorIcon() {
        return randomColorIcon;
    }    

    public BufferedImage[][] getImageArray() {
        return imageArray;
    }    
    
    public boolean isCreatorMode() {
        return creatorMode;
    }

    public void setCreatorMode(boolean creatorMode) {
        this.creatorMode = creatorMode;
    }

    public void setCreatorColor(ChessColor creatorColor) {
        this.creatorColor = creatorColor;
    }
    
    /**
     * Loads sprites and sets imageArray, spriteArray and randomColorIcon.
     */
    private void loadSprites() {
        
        try {        
            randomColorIcon = new ImageIcon(ImageIO.read(getClass().
                                getResource("/Random_color_icon.png")));
            
            creatorWhiteImage = new ImageIcon(ImageIO.read(getClass().
                                getResource("/CreatorWhiteCrown.png"))); 
            creatorBlackImage = new ImageIcon(ImageIO.read(getClass().
                                getResource("/CreatorBlackCrown.png")));
            
            BufferedImage spriteSheet = ImageIO.read(getClass().
                    getResource("/Chess_pieces.png"));
        
            /* Pieces are sorted like the PieceType Enum: King, Queen, Rook, 
               Bishop, Knight, Pawn
            */
            /* white pieces */
            imageArray[1][0] = spriteSheet.getSubimage(5, 5, 75, 75);
            imageArray[1][1] = spriteSheet.getSubimage(88, 5, 75, 75);
            imageArray[1][3] = spriteSheet.getSubimage(171, 5, 75, 75);
            imageArray[1][4] = spriteSheet.getSubimage(254, 5, 75, 75);
            imageArray[1][2] = spriteSheet.getSubimage(338, 5, 75, 75);
            imageArray[1][5] = spriteSheet.getSubimage(420, 5, 75, 75);
        
            /* black pieces */
            imageArray[0][0] = spriteSheet.getSubimage(5, 89, 75, 75);
            imageArray[0][1] = spriteSheet.getSubimage(88, 89, 75, 75);
            imageArray[0][3] = spriteSheet.getSubimage(171, 89, 75, 75);
            imageArray[0][4] = spriteSheet.getSubimage(254, 89, 75, 75);
            imageArray[0][2] = spriteSheet.getSubimage(338, 89, 75, 75);
            imageArray[0][5] = spriteSheet.getSubimage(420, 89, 75, 75);

            for(int i=0; i<2; i++){
                for(int j=0; j<6; j++){
                    spriteArray[i][j] = new ImageIcon(imageArray[i][j]);
                }
            }     
                
        } catch (IOException ex) {
        }        
     
    }
    
    /**
     * Gets sprite icon for given piece type and color.
     * 
     * @param pieceType     piece type of sprite
     * @param color         color of sprite
     * @return              sprite of specified piece type and color
     */
    public ImageIcon getSprite(PieceType pieceType, ChessColor color) {
        
        int aux = 0;
        if (color == WHITE) aux = 1;
        
        switch (pieceType) {
            case KING:
                
                if(creatorMode && color == creatorColor) 
                    if(color == WHITE) return creatorWhiteImage; 
                    else return creatorBlackImage;
                
                return (spriteArray[aux][0]);

            case QUEEN:
                return (spriteArray[aux][1]);

            case BISHOP:
                return (spriteArray[aux][3]);

            case KNIGHT:
                return (spriteArray[aux][4]);

            case ROOK:
                return (spriteArray[aux][2]);

            case PAWN:
                return (spriteArray[aux][5]);
        }
        return null;
    }
    
}
