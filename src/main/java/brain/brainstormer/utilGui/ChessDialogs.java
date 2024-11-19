package brain.brainstormer.utilGui;

import com.github.bhlangonijr.chesslib.Piece;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;

import java.util.Optional;

public class ChessDialogs {

    // Method to show the promotion dialog
    public static Piece showPromotionDialog(boolean isWhite) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Queen", "Queen", "Rook", "Bishop", "Knight");
        dialog.setTitle("Pawn Promotion");
        dialog.setHeaderText("Choose your promotion piece:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            switch (result.get()) {
                case "Rook": return isWhite ? Piece.WHITE_ROOK : Piece.BLACK_ROOK;
                case "Bishop": return isWhite ? Piece.WHITE_BISHOP : Piece.BLACK_BISHOP;
                case "Knight": return isWhite ? Piece.WHITE_KNIGHT : Piece.BLACK_KNIGHT;
                default: return isWhite ? Piece.WHITE_QUEEN : Piece.BLACK_QUEEN;
            }
        }
        return isWhite ? Piece.WHITE_QUEEN : Piece.BLACK_QUEEN; // Default to Queen
    }

    // Method to show a checkmate dialog
    public static void showCheckmateDialog(String winner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Checkmate!");
        alert.setHeaderText("Game Over");
        alert.setContentText(winner + " wins by checkmate!");
        alert.showAndWait();
    }


}
