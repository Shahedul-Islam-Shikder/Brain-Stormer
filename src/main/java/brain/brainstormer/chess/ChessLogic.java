package brain.brainstormer.chess;


import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
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

    // Method to check if a move is a pawn promotion
    public boolean isPromotionMove(Square from, Square to) {
        Piece movingPiece = board.getPiece(from);
        String toSquareName = to.name();
        char toRankChar = toSquareName.charAt(1);

        // Check if the moving piece is a pawn and it's moving to the last rank
        return (movingPiece == Piece.WHITE_PAWN && toRankChar == '8') ||
                (movingPiece == Piece.BLACK_PAWN && toRankChar == '1');
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
