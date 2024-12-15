package brain.brainstormer.controller;

import brain.brainstormer.chess.ChessClient;
import brain.brainstormer.chess.ChessLogic;
import brain.brainstormer.utilGui.AlertUtil;
import brain.brainstormer.utilGui.ChessDialogs;
import brain.brainstormer.utils.Chessutils;
import brain.brainstormer.utils.EnvUtil;
import brain.brainstormer.config.SessionManager;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChessController {

    @FXML
    private GridPane chessBoard;

    @FXML
    private Label roomCodeLabel;

    @FXML
    private Label whitePlayerName, blackPlayerName;

    @FXML
    private Circle activeBlack, activeWhite;

    @FXML
    private TextField chatMessage;

    @FXML
    private Button sendChat;

    @FXML
    private VBox chatVBox;

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

    public void initialize() {
        setupChessBoardUI();
        refreshBoard();

        // Initialize the WebSocket client
        String serverUrl = EnvUtil.getEnv("SERVER_URL");
        chessClient = new ChessClient(serverUrl);

        String roomCode = Chessutils.roomCode.isEmpty() ? "new" : Chessutils.roomCode;
        String username = SessionManager.getInstance().getUsername();

        // Connect to the WebSocket server
        chessClient.connect(
                roomCode,
                username,
                this::processServerMessage, // Callback for incoming messages
                () -> {

                    Platform.runLater(() -> {
                        // Update room code label after connection is established
                        if (roomCode.equals("new")) {
                            roomCodeLabel.setText("Waiting...");
                        } else {
                            roomCodeLabel.setText(roomCode);
                        }
                    });
                },
                () -> AlertUtil.showError("Connection Error", "Failed to connect to the WebSocket server."),
                Throwable::printStackTrace
        );

        sendChat.setOnAction(event -> sendChatMessage());
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
        if ("Spectator".equals(playerRole)) {
            System.out.println("Spectator interaction ignored.");
            return;
        }

        if (!isMyTurn) return;

        Piece piece = chessGame.getBoard().getPiece(square);

        if (selectedSquare == null) {
            handlePieceSelection(square, piece);
        } else {
            handleMove(square, piece);
        }
    }

    private void handlePieceSelection(Square square, Piece piece) {
        if (piece == null || piece == Piece.NONE) return;

        boolean isWhitePiece = piece.getPieceSide() == Side.WHITE;
        if ((playerRole.equals("White") && !isWhitePiece) || (playerRole.equals("Black") && isWhitePiece)) {
            return;
        }

        selectedSquare = square;
        clearHighlights();
        highlightLegalMoves(selectedSquare);
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

    private void sendMoveToServer(Square from, Square to, Piece promotionPiece) {
        JsonObject moveData = new JsonObject();
        moveData.addProperty("from", from.toString());
        moveData.addProperty("to", to.toString());
        moveData.addProperty("fen", chessGame.getBoard().getFen());
        if (promotionPiece != null) {
            moveData.addProperty("promotion", promotionPiece.value());
        }

        chessClient.sendMove(Chessutils.roomCode, moveData);
        endGame();
    }



    private void sendChatMessage() {
        chessClient.sendChat(Chessutils.roomCode, chatMessage.getText());
        chatMessage.setText("");
    }

    private void processServerMessage(JsonObject message) {
        Platform.runLater(() -> {
            try {
                // Since the type is always "chess-game", we'll just proceed with that
                JsonObject payload = message.getAsJsonObject("payload");
                String action = payload.get("action").getAsString();  // Get action from the payload

                switch (action) {
                    case "move":
                        processMove(payload);
                        break;
                    case "players":
                        updatePlayerLabels(payload.getAsJsonArray("payload"));
                        break;
                    case "joined":
                    case "room-update":
                        handleChessGameMessage(payload);
                        break;
                    case "chat":

                        displayChatMessage(payload);
                        break;
                    default:
                        System.out.println("Unknown action received from the server.");
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void handleChessGameMessage(JsonObject payload) {



        // Extract action from the payload
        String action = payload.get("action").getAsString();

        switch (action) {
            case "joined":

                String role = payload.get("role").getAsString();
                String roomId = payload.get("roomId").getAsString();
                String username = payload.get("username").getAsString();
                JsonObject state = payload.getAsJsonObject("state"); // Extract room state

                Chessutils.roomCode = roomId;

                String fen = state.get("fen").getAsString();
                System.out.println("FEN: " + fen);
                chessGame.getBoard().loadFromFen(fen); // Load board state from FEN
                refreshBoard();

                Platform.runLater(() -> {
                    roomCodeLabel.setText(roomId);

                    // Update player labels based on the room state
                    whitePlayerName.setText("White: " + state.get("white").getAsString());
                    blackPlayerName.setText("Black: " + state.get("black").getAsString());

                    // Update playerRole and determine if it's this player's turn
                    playerRole = role;
                    setIsMyTurn();
                    updateActivePlayerIndicator();


                    // Load and display the chat history
                    JsonArray chatHistory = payload.getAsJsonArray("chatHistory"); // Extract chat history
                    for (JsonElement chatElement : chatHistory) {
                        JsonObject chat = chatElement.getAsJsonObject();
                        displayChatMessage(chat); // Display each chat message using your method
                    }

                });
                break;

            case "room-update":
                JsonObject updatedState = payload.getAsJsonObject("state"); // Extract updated room state
                Platform.runLater(() -> {
                    whitePlayerName.setText("White: " + updatedState.get("white").getAsString());
                    blackPlayerName.setText("Black: " + updatedState.get("black").getAsString());
                    updateActivePlayerIndicator();
                });
                break;

            default:
                AlertUtil.showError("Unknown Action", "Unknown action received from the server.");
                break;
        }
    }














    private void processMove(JsonObject data) {


        // Extract 'moveData' from the payload
        JsonObject moveData = data.getAsJsonObject("moveData");

        // Check if moveData is not null to avoid NullPointerException
        if (moveData != null) {
            // Extract the 'from' and 'to' squares
            String fromStr = moveData.get("from").getAsString();
            String toStr = moveData.get("to").getAsString();

            // Convert to Square enum values
            Square from = Square.valueOf(fromStr);
            Square to = Square.valueOf(toStr);

            // Extract the promotion piece if present
            Piece promotionPiece = moveData.has("promotion")
                    ? Piece.fromValue(moveData.get("promotion").getAsString())
                    : null;

            // Make the move on the chess game
            chessGame.makeMove(from, to, promotionPiece);
            refreshBoard();
            highlightCheckedKing();
            endGame();

            // Update isMyTurn based on the FEN
            setIsMyTurn();
            updateActivePlayerIndicator();

        } else {
            AlertUtil.showError("Invalid Move", "Invalid move data received from the server.");
        }
    }

    private void setIsMyTurn(){

        String fen = chessGame.getBoard().getFen();
        if ("White".equals(playerRole)) {
            isMyTurn = fen.contains("w"); // White's turn if FEN has 'w'
        } else if ("Black".equals(playerRole)) {
            isMyTurn = fen.contains("b"); // Black's turn if FEN has 'b'
        } else {
            isMyTurn = false; // Spectators never have a turn
        }


    }

    private boolean getWhitesTurn(){
        String fen = chessGame.getBoard().getFen();

         return fen.contains("w");


    }



    private void updatePlayerLabels(JsonArray players) {
        Platform.runLater(() -> {
            if (players.size() > 0) {
                whitePlayerName.setText("White: " + players.get(0).getAsString());

            }
            if (players.size() > 1) {
                blackPlayerName.setText("Black: " + players.get(1).getAsString());

            }
        });
    }


    private void updateActivePlayerIndicator() {
        Platform.runLater(() -> {
            activeBlack.setVisible(false);
            activeWhite.setVisible(false);

            if(getWhitesTurn()){
                activeWhite.setVisible(true);
            } else {
                activeBlack.setVisible(true);
            }


        });
    }

    private void displayChatMessage(JsonObject chat) {


        // Check if chat contains the 'chatMessage' field (single message)
        if (chat.has("chatMessage")) {
            JsonObject chatMessage = chat.getAsJsonObject("chatMessage");
            String message = chatMessage.get("text").getAsString();
            String user = chatMessage.get("user").getAsString();

            // Format the chat message
            String formattedMessage = user + ": " + message;

            // Create a label to display the chat message
            Label messageLabel = new Label(formattedMessage);
            messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5px;");

            // Add the label to the VBox
            Platform.runLater(() -> chatVBox.getChildren().add(messageLabel));

        } else {
            // Handle case where it's part of chatHistory (loaded messages)
            String message = chat.get("text").getAsString();
            String user = chat.get("user").getAsString();

            // Format the chat message
            String formattedMessage = user + ": " + message;

            // Create a label to display the chat message
            Label messageLabel = new Label(formattedMessage);
            messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5px;");

            // Add the label to the VBox
            Platform.runLater(() -> chatVBox.getChildren().add(messageLabel));
        }
    }

    private void endGame() {
        if (chessGame.isCheckmate()) {
            Side winnerSide = chessGame.getBoard().getSideToMove() == Side.WHITE ? Side.BLACK : Side.WHITE; // Opponent is the winner
            String winner = winnerSide == Side.WHITE ? "White" : "Black";

            AlertUtil.showWarning("Game Over", winner + " wins by Checkmate!");

            Stage stage = (Stage) roomCodeLabel.getScene().getWindow(); // Use an existing node from your scene
            stage.close(); // Close only the current window (stage)
        } else if (chessGame.isStalemate()) {
            System.out.println("Stalemate! Game Over.");
            AlertUtil.showWarning("Game Over", "It's a Stalemate!");


            Stage stage = (Stage) roomCodeLabel.getScene().getWindow(); // Use an existing node from your scene
            stage.close(); // Close only the current window (stage)
        }
    }





}
