package chess.game;

import chess.board.Piece;
import chess.move.Move;

import java.util.ArrayList;

class ChessRulesTest {

    /**
     * Performance and speed test for move generation.
      */
    @org.junit.jupiter.api.Test
    void getAllLegalMoves() {

        /* initialize dependencies */
        ChessGame game = new ChessGame();

        //arrayListCapacityTest();
        //assert perft(5, game) == 4865609;
        assert perft(6, game) == 119060324;
    }

    int perft(int depth, ChessGame game){

        int moveCount = 0;
        ArrayList<Move> moveList = new ArrayList<>(30);

        /* generate all legal moves for position and add them to moveList */
        ArrayList<Piece> pieces = game.getBoard().getPiecesList(game.getPlayersTurn());
        for (Piece piece : pieces) {
            moveList.addAll(game.getRules().getAllLegalMoves(piece));
        }

        if(depth == 1) return moveList.size();

        for(Move move : moveList){
            game.executeMove(move, false);
            moveCount += perft(depth -1, game);
            game.unexecuteMove(move);
        }

        return moveCount;

    }

    void arrayListCapacityTest() {

        int iterationCount =  9000000;
        int insertCount = 30;

        ArrayList<Integer> list = new ArrayList<>(30);

        for (int i=0; i<iterationCount; i++) {
            for (int j = 0; j < insertCount; j++) list.add(5);
        }
    }

}