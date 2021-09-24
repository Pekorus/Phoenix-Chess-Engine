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
        switch (this){
            case NORMAL:
                return "-";
            case TAKE:
            case ENPASSANT:    
                return "x";
        }       
    return "";    
    }

        
}
