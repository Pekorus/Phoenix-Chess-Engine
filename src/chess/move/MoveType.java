/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.move;

/**
 *
 * @author Phoenix
 */
public enum MoveType {
        NORMAL,
        TAKE,
        ENPASSANT,
        CASTLE;

    //TODO: vereinfachen
    @Override
    public String toString() {
        switch (this){
            case NORMAL:
                return "-";
            case TAKE:
                return "x";
        }
    return "";    
    }

        
}
