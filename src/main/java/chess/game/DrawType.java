package chess.game;

/**
 *
 * Provides a set of draw types that are possible in chess.
 */
public enum DrawType {
    TECHNICAL, STALEMATE, FIFTYTURNS, THREEFOLD;

    @Override
    public String toString() {
        return switch (this) {
            case TECHNICAL -> "technical draw";
            case STALEMATE -> "stalemate";
            case FIFTYTURNS -> "Fifty-move rule";
            case THREEFOLD -> "threefold repetition";
        };
    }


}
