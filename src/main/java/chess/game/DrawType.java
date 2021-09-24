package chess.game;

/**
 *
 * Provides a set of draw types that are possible in chess.
 */
public enum DrawType {
    TECHNICAL, STALEMATE, FIFTYTURNS, THREEFOLD;

    @Override
    public String toString() {
        switch(this){
            case TECHNICAL:
                return "technical draw";
            case STALEMATE:
                return "stalemate";
            case FIFTYTURNS:
                return "Fifty-move rule";
            case THREEFOLD:
                return "threefold repetition";
        }       
        return "";
    }


}
