/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.board;

import chess.coordinate.Coordinate;

/**
 *
 * @author Phoenix
 */
public class Move {
    
    private final Piece piece;
    private final Coordinate coordFrom;
    private final Coordinate coordTo;
    private final MoveType moveType;
    private final Piece optionalPiece;
    private final Coordinate optPieceCoord;
    private final PieceType promoteTo;
    
    public Move(Piece piece, Coordinate coordinate, MoveType moveType) {
        this.piece = piece;
        if(piece!=null) this.coordFrom = piece.getCoordinate();
            else coordFrom =null;
        this.coordTo = coordinate;
        this.moveType = moveType;
        this.optionalPiece = null;
        this.optPieceCoord = null;
        this.promoteTo = null;
    }

    public Move(Piece piece, Coordinate coordinate, 
                MoveType moveType, Piece optionalPiece, PieceType promoteTo) {
        this.piece = piece;
        if(piece!=null)this.coordFrom= piece.getCoordinate();
        else this.coordFrom = null;
        this.coordTo = coordinate;
        this.moveType = moveType;
        this.optionalPiece = optionalPiece;
        if(optionalPiece!=null) 
            this.optPieceCoord = optionalPiece.getCoordinate();
        else this.optPieceCoord = null;
        this.promoteTo=promoteTo;
    }
    
    public Piece getPiece() {
        return piece;
    }

    public Coordinate getCoordFrom() {
        return coordFrom;
    }

    public Coordinate getCoordTo() {
        return coordTo;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public Piece getOptionalPiece() {
        return optionalPiece;
    }
    
    public Coordinate getOptionalPieceCoord(){
        return optPieceCoord;
    }   

    public Coordinate getOptPieceCoord() {
        return optPieceCoord;
    }

    public PieceType getPromoteTo() {
        return promoteTo;
    }

    
}
