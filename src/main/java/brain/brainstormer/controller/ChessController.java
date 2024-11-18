package brain.brainstormer.controller;

import brain.brainstormer.chess.ChessClient;
import brain.brainstormer.chess.ChessLogic;
import brain.brainstormer.utilGui.Dialogs;
import brain.brainstormer.utils.Chessutils;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChessController {

    @FXML
    private GridPane chessBoard;



    @FXML
    private Label roomCodeLabel; // Label to display the room code
    @FXML
    private Label waitingLabel;






    private ChessLogic chessGame;
    private Map<Square, StackPane> squareMap;
    private Square selectedSquare = null;

    private ChessClient chessClient;
    private boolean isMyTurn = false;
    private String playerRole;

    public ChessController() {
        chessGame = new ChessLogic();
        squareMap = new HashMap<>();
    }

    @FXML
    public void initialize() {
        setupChessBoardUI();
        refreshBoard();

        // Initialize and start client to connect to server
        chessClient = new ChessClient("localhost", 12345);
        String roomCode = Chessutils.roomCode;
        if (roomCode.isEmpty()) {
            // Host a new game
            chessClient.start("new");
            try {
                roomCode = chessClient.receiveMoveFromServer(); // Receives the generated room code
                System.out.println("Hosting room. Code: " + roomCode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Join an existing room with code
            chessClient.start(roomCode);
            System.out.println("Joined room with code: " + roomCode);
        }

        roomCodeLabel.setText("Room Code: " + roomCode);

        // Receive role and assign turn accordingly
        try {
            playerRole = chessClient.receiveMoveFromServer();
            System.out.println("Player role received: " + playerRole);

            // Set turn based on player role
            isMyTurn = "White".equals(playerRole); // White starts the game
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Start a new thread to listen for moves from the server
        Thread serverListenerThread = new Thread(this::listenForServerMoves);
        serverListenerThread.setDaemon(true);
        serverListenerThread.start();
    }

    private void setupChessBoardUI() {
        final int cellSize = 60;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Square square = Square.valueOf((char) ('A' + col) + String.valueOf(8 - row));
                StackPane cell = new StackPane();
                cell.setPrefSize(cellSize, cellSize);

                String color = ((row + col) % 2 == 0) ? "#f0d9b5" : "#b58863";
                cell.setStyle("-fx-background-color: " + color + ";");

                squareMap.put(square, cell);
                cell.setOnMouseClicked(event -> handleSquareClick(square));
                chessBoard.add(cell, col, row);
            }
        }
    }

    private void refreshBoard() {
        clearHighlights();
        highlightCheckedKing();

        for (Square square : Square.values()) {
            if (square == Square.NONE) continue;
            waitingLabel.setText("");
            StackPane cell = squareMap.get(square);
            if (cell != null) {
                cell.getChildren().clear();
                String color = Chessutils.isLightSquare(square) ? "#f0d9b5" : "#b58863";
                cell.setStyle("-fx-background-color: " + color + ";");
            }
        }

        for (Square square : Square.values()) {
            if (square == Square.NONE) continue;

            Piece piece = chessGame.getBoard().getPiece(square);
            if (piece != null && !piece.toString().equals("NONE")) {
                ImageView pieceImage = new ImageView(new Image(
                        getClass().getResourceAsStream("/brain/brainstormer/images/" + piece.value().toLowerCase() + ".png")));
                pieceImage.setFitWidth(60);
                pieceImage.setFitHeight(60);
                squareMap.get(square).getChildren().add(pieceImage);
            }
        }
    }

    private void clearHighlights() {
        Square checkedKingSquare = chessGame.getCheckedKingSquare();

        for (Square square : Square.values()) {
            if (square == Square.NONE) continue;

            String color = square.equals(checkedKingSquare) ? "red" : (Chessutils.isLightSquare(square) ? "#f0d9b5" : "#b58863");
            StackPane cell = squareMap.get(square);
            if (cell != null) {
                cell.setStyle("-fx-background-color: " + color + ";");
                cell.getChildren().removeIf(node -> node instanceof Circle);
            }
        }
    }

    private void highlightCheckedKing() {
        Square checkedKingSquare = chessGame.getCheckedKingSquare();

        // Highlight the king's square in red if it's in check
        if (checkedKingSquare != null && checkedKingSquare != Square.NONE) {
            StackPane kingCell = squareMap.get(checkedKingSquare);
            if (kingCell != null) {
                kingCell.setStyle("-fx-background-color: red;");
            }
        }
    }

    private void highlightLegalMoves(Square from) {
        try {
            // Get all legal moves from the current board position
            List<Move> legalMoves = MoveGenerator.generateLegalMoves(chessGame.getBoard());

            // Highlight only moves starting from the selected square
            for (Move move : legalMoves) {
                if (move.getFrom().equals(from)) {
                    Square targetSquare = move.getTo();
                    StackPane targetCell = squareMap.get(targetSquare);
                    if (targetCell != null) {
                        // Create a small dot and add it to the target cell
                        Circle dot = new Circle(10); // Adjust radius as desired for dot size
                        dot.setStyle("-fx-fill: rgba(0, 0, 0, 0.5);"); // Semi-transparent black dot
                        targetCell.getChildren().add(dot);
                    }
                }
            }
        } catch (MoveGeneratorException e) {
            e.printStackTrace();
        }
    }

    private void handleSquareClick(Square square) {
        if (!isMyTurn || "Spectator".equals(playerRole)) return; // Prevent move if it's not player's turn or if the player is a spectator

        Piece piece = chessGame.getBoard().getPiece(square);

        if (selectedSquare == null) {
            handlePieceSelection(square, piece);
        } else {
            handleMove(square, piece);
        }
    }


    // Method to handle piece selection
    private void handlePieceSelection(Square square, Piece piece) {
        if (piece == null || piece == Piece.NONE) return; // Empty square, ignore

        // Determine if the piece belongs to the player
        boolean isWhitePiece = piece.getPieceSide() == Side.WHITE;
        if ((playerRole.equals("White") && !isWhitePiece) || (playerRole.equals("Black") && isWhitePiece)) {
            System.out.println(playerRole+"fdjfldskjfj");
            return; // Block move if piece does not belong to the player
        }

        // Select square and highlight moves if valid
        selectedSquare = square;
        clearHighlights();
        highlightLegalMoves(selectedSquare);
    }

    // Method to handle the move
    private void handleMove(Square targetSquare, Piece targetPiece) {
        Piece promotionPiece = null;

        // Check if the move is a promotion move
        if (chessGame.isPromotionMove(selectedSquare, targetSquare)) {
            promotionPiece = Dialogs.showPromotionDialog(playerRole.equals("White"));
        }

        // Attempt move and update server
        if (chessGame.makeMove(selectedSquare, targetSquare, promotionPiece)) {
            refreshBoard();
            sendMoveToServer(selectedSquare, targetSquare, promotionPiece);
            isMyTurn = false; // End turn after a move
        }

        // Reset selection
        clearHighlights();
        selectedSquare = null;
    }

    // Method to send the move to the server, with promotion if applicable
    private void sendMoveToServer(Square from, Square to, Piece promotionPiece) {
        try {
            String moveMessage = from + " " + to;
            if (promotionPiece != null) {
                moveMessage += " " + promotionPiece.value(); // Append promotion piece type
            }
            chessClient.sendMoveToServer(moveMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    private void listenForServerMoves() {
        while (true) {
            try {
                String serverMove = chessClient.receiveMoveFromServer();
                if (serverMove == null) break;

                if (serverMove.equals("Waiting for another player...") || serverMove.equals("Game start!")) {
                    System.out.println("Server status: " + serverMove);
                    continue;
                }

                // Only players process moves; spectators observe
                if (!"Spectator".equals(playerRole)) {
                    Move move = parseMove(serverMove);
                    if (move != null) {
                        Platform.runLater(() -> {
                            chessGame.makeMove(move.getFrom(), move.getTo());
                            refreshBoard();
                            isMyTurn = true;
                        });
                    } else {
                        System.err.println("Failed to parse move from server: " + serverMove);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }



    private Move parseMove(String moveStr) {
        try {
            String[] parts = moveStr.split(" ");
            if (parts.length == 2 || parts.length == 3) { // Adjust to 3 for promotion
                Square from = Square.valueOf(parts[0].toUpperCase());
                Square to = Square.valueOf(parts[1].toUpperCase());
                Move move = new Move(from, to);

                if (parts.length == 3) {
                    Piece promotionPiece = Piece.fromValue(parts[2].toUpperCase());
                    chessGame.makeMove(from, to, promotionPiece);
                } else {
                    chessGame.makeMove(from, to);
                }
                return move;
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid move format received: " + moveStr);
        }
        return null;
    }

}
