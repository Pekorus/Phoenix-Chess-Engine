package chess.board;

/**
 *
 * Provides a set of piece types for chess.
 */
public enum PieceType {
           
    /* Int values are regular materialic values of chess pieces in the analysis
       of chess positions. King value is arbitralily chosen as a high value that
       is never reached in an analysis to make it more important than anything
       else in the position.
    */ 
    KING(100000),
    QUEEN(900),
    ROOK(500),
    BISHOP(300),
    KNIGHT(300),
    PAWN(100);

    private final int materialValue;

    private PieceType(int materialValue) {
        this.materialValue = materialValue;
    }

    public int getMaterialValue() {
        return materialValue;
    }

    @Override
    public String toString() {
        
        switch (this){
            case KING:
                return "K";
            case QUEEN: 
                return "Q";
            case BISHOP:
                return "B";
            case KNIGHT:
                return "N";
            case ROOK:
                return "R";
            case PAWN:
                return " ";
        }
      return "";    
    }


}
