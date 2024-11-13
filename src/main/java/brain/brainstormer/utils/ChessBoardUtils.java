package brain.brainstormer.utils;

import com.github.bhlangonijr.chesslib.Square;

public class ChessBoardUtils {

    public static boolean isLightSquare(Square square) {
        // Use the square name (e.g., "A1", "B2") to determine color
        char file = square.name().charAt(0); // The letter (A, B, ...)
        char rank = square.name().charAt(1); // The number (1, 2, ...)

        // Check if file (column) and rank (row) are both even or both odd
        return (file + rank) % 2 == 0;
    }
}
