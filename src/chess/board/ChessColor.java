/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.board;

/**
 *
 * @author Phoenix
 */
public enum ChessColor {
    WHITE,
    BLACK;

    public ChessColor getOppositeColor() {
        if (this == WHITE) {
            return BLACK;
        }
        return WHITE;
    }
    
    @Override
    public String toString(){
        if(this==WHITE) return "White";
        return "Black";
    }
}
