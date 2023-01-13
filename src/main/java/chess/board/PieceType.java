package chess.board;

/**
 *
 * Provides a set of piece types for chess.
 */
public enum PieceType {
           
    /* Int values are regular material values of chess pieces in the analysis
       of chess positions. King value is arbitrarily chosen as a high value that
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

    PieceType(int materialValue) {
        this.materialValue = materialValue;
    }

    public int getMaterialValue() {
        return materialValue;
    }

    @Override
    public String toString() {

        return switch (this) {
            case KING -> "K";
            case QUEEN -> "Q";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case ROOK -> "R";
            case PAWN -> " ";
        };
    }


}
