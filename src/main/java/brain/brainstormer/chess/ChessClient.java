package brain.brainstormer.chess;

import brain.brainstormer.utilGui.AlertUtil;
import brain.brainstormer.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.function.Consumer;

public class ChessClient {

    private WebSocketClient client;
    private final Gson gson;
    private final String serverUrl;

    public ChessClient(String serverAddress) {
        this.gson = new Gson();
        this.serverUrl = serverAddress;
    }

    public void connect(String roomId, String username, Consumer<JsonObject> onMessage, Runnable onOpen, Runnable onClose, Consumer<Exception> onError) {
        try {
            client = new WebSocketClient(new URI(serverUrl)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {


                    // Send the join-chess message immediately upon connection
                    JsonObject joinMessage = new JsonObject();
                    joinMessage.addProperty("type", "chess-game");
                    joinMessage.addProperty("roomId", roomId);

                    JsonObject payload = new JsonObject();
                    payload.addProperty("action", "join-chess");
                    payload.addProperty("username", username);
                    joinMessage.add("payload", payload);

                    sendMessage(joinMessage); // Send the join message


                    if (onOpen != null) onOpen.run();
                }

                @Override
                public void onMessage(String message) {

                    if (onMessage != null) {
                        JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
                        onMessage.accept(jsonMessage);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    if (onClose != null) {
                        Platform.runLater(() -> {
                            // Execute UI updates on the JavaFX application thread
                            onClose.run(); // Callback if needed
                        });
                    }
                    // Handle additional cleanup if necessary
                }

                @Override
                public void onError(Exception ex) {

                    if (onError != null) {
                        Platform.runLater(() -> {
                            onError.accept(ex); // This ensures that the error is handled on the FX thread
                        });
                    }
                }

            };
            client.connectBlocking(); // Wait until the connection is established
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("WebSocket Error", "Failed to connect to the WebSocket server.");
        }
    }

    // Send a JSON message to the WebSocket server
    public void sendMessage(JsonObject message) {
        if (client != null && client.isOpen()) {
            client.send(gson.toJson(message));
        } else {
            AlertUtil.showError("WebSocket Error", "WebSocket connection is not open.");
        }
    }

    // Send a move to the server
    public void sendMove(String roomId, JsonObject moveData) {
        JsonObject moveMessage = new JsonObject();
        moveMessage.addProperty("type", "chess-game");
        moveMessage.addProperty("roomId", roomId);

        JsonObject payload = new JsonObject();
        payload.addProperty("action", "move");
        payload.add("moveData", moveData);
        moveMessage.add("payload", payload);



        sendMessage(moveMessage);
    }

    // Send a chat message
    public void sendChat(String roomId, String chatMessage) {
        JsonObject message = new JsonObject();
        message.addProperty("type", "chess-game");
        message.addProperty("roomId", roomId);

        JsonObject payload = new JsonObject();

        payload.addProperty("action", "chat");
        payload.addProperty("username", SessionManager.getInstance().getUsername()); // Add username as a string
        payload.addProperty("text", chatMessage); // Add chat message as a string

        message.add("payload", payload); // Add the payload object

        sendMessage(message); // Send the message to the server

    }


    // Disconnect the WebSocket client
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
