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
public enum PieceType {
           
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
