package brain.brainstormer.chess;


import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;

import java.util.List;
import java.util.Stack;

public class ChessLogic {
    private Board board;
    private Stack<Move> moveStack;

    public ChessLogic() {
        this.board = new Board(); // Initialize the board
        this.moveStack = new Stack<>(); // Initialize the move stack
    }
    public boolean isPromotionMove(Square from, Square to) {
        Piece movingPiece = board.getPiece(from);
        // Check for null piece
        if (movingPiece == null) {
            return false;
        }

        // Get the ranks of the starting and destination squares
        Rank fromRank = from.getRank();
        Rank toRank = to.getRank();


        if(movingPiece == Piece.WHITE_PAWN && fromRank == Rank.RANK_7 && toRank == Rank.RANK_8) {
            return true;
        }

        if (movingPiece == Piece.BLACK_PAWN && fromRank == Rank.RANK_2 && toRank == Rank.RANK_1) {
            return true;
        }

        return false;
    }



    // Primary makeMove method with promotion piece
    public boolean makeMove(Square from, Square to, Piece promotionPiece) {
        Move move;

        // Use the constructor with the promotion piece if provided
        if (promotionPiece != null) {
            move = new Move(from, to, promotionPiece);
        } else {
            move = new Move(from, to);
        }

        try {
            List<Move> legalMoves = MoveGenerator.generateLegalMoves(board);
            if (legalMoves.contains(move)) {
                board.doMove(move);
                moveStack.push(move);
                return true;
            }
        } catch (MoveGeneratorException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Overloaded makeMove method without promotion piece
    public boolean makeMove(Square from, Square to) {
        return makeMove(from, to, null); // Call the primary method with no promotion
    }

    public boolean isCheckmate() {
        return board.isMated();
    }
    public boolean isStalemate() {
        return board.isStaleMate();
    }

    // Get winner based on checkmate
    public Side getWinner() {
        if (isCheckmate()) {
            // If the opponent's king is in checkmate, the winner is the side that is not in checkmate
            return board.getSideToMove() == Side.WHITE ? Side.BLACK : Side.WHITE;
        }
        return null; // No winner if no checkmate
    }

    public Square getCheckedKingSquare() {
        if (board.isKingAttacked()) {
            // Get the side currently in check
            Side currentSide = board.getSideToMove();
            // Return the square of the king in check
            return board.getKingSquare(currentSide);
        }
        return null; // No king is in check
    }





    public Board getBoard() {
        return board;
    }
}
