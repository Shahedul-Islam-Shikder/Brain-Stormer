package brain.brainstormer.controller;

import brain.brainstormer.chess.ChessClient;
import brain.brainstormer.chess.ChessLogic;
import brain.brainstormer.utilGui.ChessDialogs;
import brain.brainstormer.utils.Chessutils;
import brain.brainstormer.utils.SessionManager;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChessController {

    @FXML
    private GridPane chessBoard;

    @FXML
    private Label roomCodeLabel; // Label to display the room code

    private ChessLogic chessGame;
    private Map<Square, StackPane> squareMap;
    private Square selectedSquare = null;

    private ChessClient chessClient;
    private boolean isMyTurn = false;
    private String playerRole;

    private final Gson gson = new Gson(); // For JSON message handling



    public ChessController() {
        chessGame = new ChessLogic();
        squareMap = new HashMap<>();
    }

    @FXML

    private Label whitePlayerName, blackPlayerName;

    @FXML
    public void initialize() {
        setupChessBoardUI();
        refreshBoard();

        // Initialize and start client to connect to server
        chessClient = new ChessClient("localhost", 12345);

        String roomCode = Chessutils.roomCode;
        String username = SessionManager.getInstance().getUsername();

        try {
            JsonObject startMessage = new JsonObject();
            startMessage.addProperty("username", username);
            startMessage.addProperty("roomCode", roomCode.isEmpty() ? "new" : roomCode);

            // Send the start message (username and room code)
            chessClient.start(startMessage);

            // If hosting, receive and set the generated room code
            if (roomCode.isEmpty()) {
                JsonObject roomCodeMessage = chessClient.receiveMessage();
                if (roomCodeMessage != null && roomCodeMessage.has("data")) {
                    roomCode = roomCodeMessage.get("data").getAsString();
                    System.out.println("Hosting room. Code: " + roomCode);
                } else {
                    throw new IllegalStateException("Failed to receive room code from server.");
                }
            } else {
                System.out.println("Joined room with code: " + roomCode);
            }

            roomCodeLabel.setText(roomCode);

            // Receive role and determine turn or spectator status
            JsonObject roleMessage = chessClient.receiveMessage();
            if (roleMessage != null && roleMessage.has("data")) {
                playerRole = roleMessage.get("data").getAsString();
                System.out.println("Player role received: " + playerRole);

                if ("White".equals(playerRole)) {
                    isMyTurn = true; // White starts the game
                } else if ("Black".equals(playerRole)) {
                    isMyTurn = false; // Black goes second
                } else if ("Spectator".equals(playerRole)) {
                    isMyTurn = false; // Spectators don't play
                    System.out.println("You are a spectator. Watching the game.");
                }
            } else {
                throw new IllegalStateException("Failed to receive player role from server.");
            }

            // Start a new thread to listen for server messages
            Thread serverListenerThread = new Thread(this::listenForServerMessages);
            serverListenerThread.setDaemon(true);
            serverListenerThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void handleSquareClick(Square square) {
        if (!isMyTurn) return; // Ensure it's the player's turn

        Piece piece = chessGame.getBoard().getPiece(square);

        if (selectedSquare == null) {
            handlePieceSelection(square, piece);
        } else {
            handleMove(square, piece);
        }
    }

    private void handlePieceSelection(Square square, Piece piece) {
        if (piece == null || piece == Piece.NONE) return; // Empty square, ignore

        boolean isWhitePiece = piece.getPieceSide() == Side.WHITE;
        if ((playerRole.equals("White") && !isWhitePiece) || (playerRole.equals("Black") && isWhitePiece)) {
            return; // Block move if piece does not belong to the player
        }

        selectedSquare = square;
        clearHighlights();
        highlightLegalMoves(selectedSquare);
    }

    private void handleMove(Square targetSquare, Piece targetPiece) {
        Piece promotionPiece = null;

        if (chessGame.isPromotionMove(selectedSquare, targetSquare)) {
            promotionPiece = ChessDialogs.showPromotionDialog(playerRole.equals("White"));
        }

        if (chessGame.makeMove(selectedSquare, targetSquare, promotionPiece)) {
            refreshBoard();
            sendMoveToServer(selectedSquare, targetSquare, promotionPiece);
            isMyTurn = false;
        }

        clearHighlights();
        selectedSquare = null;
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
    private void sendMoveToServer(Square from, Square to, Piece promotionPiece) {
        JsonObject moveData = new JsonObject();
        moveData.addProperty("from", from.toString());
        moveData.addProperty("to", to.toString());
        if (promotionPiece != null) {
            moveData.addProperty("promotion", promotionPiece.value());
        }
        moveData.addProperty("username", SessionManager.getInstance().getUsername());

        JsonObject moveMessage = createMessage("move", moveData);
        chessClient.sendMove(moveMessage.toString());
    }




    private void listenForServerMessages() {
        try {
            while (true) {
                JsonObject serverMessage = chessClient.receiveMessage();
                if (serverMessage == null) break; // Stop if no message is received

                Platform.runLater(() -> processServerMessage(serverMessage));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error in server listener thread.");
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

    private void processServerMessage(JsonObject message) {
        try {
            String type = message.get("type").getAsString();

            if (message.has("data")) {
                if (message.get("data").isJsonObject()) {
                    JsonObject data = message.getAsJsonObject("data");
                    switch (type) {
                        case "move":
                            processMove(data);
                            break;
                        case "chat":
                            displayChatMessage(data);
                            break;
                        default:
                            System.err.println("Unknown message type with object data: " + type);
                            break; // Ensure no fall-through
                    }
                } else if (message.get("data").isJsonPrimitive()) {
                    String data = message.get("data").getAsString();
                    switch (type) {
                        case "roomCode":
                            System.out.println("Room code received: " + data);
                            break;
                        case "status":
                            System.out.println("Status message: " + data);
                            break;
                        case "role":
                            System.out.println("Player role received: " + data);
                            playerRole = data;
                            isMyTurn = "White".equals(playerRole);
                            break;
                        case "usernames":
                            // Parse usernames and update the GUI
                            List<String> usernames = gson.fromJson(data, List.class);
                            System.out.println("users !");
                            updatePlayerLabels(usernames);
                            break;
                        default:
                            System.err.println("Unknown message type with primitive data: " + type);
                            break;
                    }
                }
            } else {
                System.err.println("Invalid message: Missing 'data' field.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error processing server message: " + message);
        }
    }

    // New method to update player labels
    private void updatePlayerLabels(List<String> usernames) {
        Platform.runLater(() -> {
            if (usernames.size() > 0) {
                whitePlayerName.setText("White: " + usernames.get(0));
            }
            if (usernames.size() > 1) {
                blackPlayerName.setText("Black: " + usernames.get(1));
            }
        });
    }




    private void processMove(JsonObject data) {
        Square from = Square.valueOf(data.get("from").getAsString());
        Square to = Square.valueOf(data.get("to").getAsString());
        Piece promotionPiece = data.has("promotion") ?
                Piece.fromValue(data.get("promotion").getAsString()) : null;

        chessGame.makeMove(from, to, promotionPiece);
        refreshBoard();
        isMyTurn = true;
    }

    private JsonObject createMessage(String type, JsonObject data) {
        JsonObject message = new JsonObject();
        message.addProperty("type", type);
        message.add("data", data);
        return message;
    }

    private void displayChatMessage(JsonObject data) {
        System.out.println("Chat: " + data.get("message").getAsString());
    }
}
