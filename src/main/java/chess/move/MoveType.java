package chess.move;

/**
 *
 * Provides a set of move types for a chess game.
 */
public enum MoveType {
        NORMAL,
        TAKE,
        ENPASSANT,
        CASTLE;

    @Override
    public String toString() {
        return switch (this) {
            case NORMAL -> "-";
            case TAKE, ENPASSANT -> "x";
            default -> "";
        };
    }

        
}
