package chess.board;

import chess.coordinate.Direction;

/**
 *
 * Provides the set of colors for a chess game. Integer value represents the
 * rank a pawn of that color must reach to promote. frontDir represents the 
 * front of this colors army (i.e. direction the pawns can move to). pawnCapture
 * 1 and 2 store the directions that pawns of this color can capture to.
 * The chess board must have white pieces on ranks 0 and 1, the black pieces on 
 * 6 and 7 for this information to be accurate.
 */
public enum ChessColor {
    
    WHITE (7, Direction.S, Direction.SW, Direction.SE),
    BLACK (0, Direction.N, Direction.NW, Direction.NE); 

    private final int promotionRank;
    private final Direction frontDir;
    private final Direction pawnCapture1, pawnCapture2;
    
    private ChessColor(int promotionRank, Direction frontDir, 
            Direction pawnCapture1, Direction pawnCapture2) {
        
        this.promotionRank = promotionRank;
        this.frontDir = frontDir;
        this.pawnCapture1 = pawnCapture1;
        this.pawnCapture2 = pawnCapture2;
    }
    
    public ChessColor getInverse() {
        if (this == WHITE) {
            return BLACK;
        }
        return WHITE;
    }

    public int getPromotionRank(){        
        return promotionRank;
    }

    public Direction getFrontDir(){        
        return frontDir;
    }

    public Direction getPawnCapture1() {
        return pawnCapture1;
    }

    public Direction getPawnCapture2() {
        return pawnCapture2;
    }


    
    @Override
    public String toString(){
        if(this==WHITE) return "White";
        return "Black";
    }
}
