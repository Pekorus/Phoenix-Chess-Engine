/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.game;

/**
 *
 * @author Phoenix
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
