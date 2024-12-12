package brain.brainstormer.chess;

import brain.brainstormer.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
                    System.out.println("Connected to WebSocket server.");

                    // Send the join-chess message immediately upon connection
                    JsonObject joinMessage = new JsonObject();
                    joinMessage.addProperty("type", "chess-game");
                    joinMessage.addProperty("roomId", roomId);

                    JsonObject payload = new JsonObject();
                    payload.addProperty("action", "join-chess");
                    payload.addProperty("username", username);
                    joinMessage.add("payload", payload);

                    sendMessage(joinMessage); // Send the join message
                    System.out.println("ChessClient:-> Sent join-chess message for roomId: " + roomId);

                    if (onOpen != null) onOpen.run();
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("Message received: " + message);
                    if (onMessage != null) {
                        JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
                        onMessage.accept(jsonMessage);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Disconnected from WebSocket server. Reason: " + reason);
                    if (onClose != null) onClose.run();
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("WebSocket error: " + ex.getMessage());
                    if (onError != null) onError.accept(ex);
                }
            };
            client.connectBlocking(); // Wait until the connection is established
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to connect to WebSocket server.");
        }
    }

    // Send a JSON message to the WebSocket server
    public void sendMessage(JsonObject message) {
        if (client != null && client.isOpen()) {
            client.send(gson.toJson(message));
        } else {
            System.err.println("WebSocket is not connected.");
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

        System.out.println("ChessClient:-> Sending move message: " + moveMessage);

        sendMessage(moveMessage);
    }

    // Send a chat message
    public void sendChat(String roomId, String chatMessage) {
        JsonObject chatData = new JsonObject();
        chatData.addProperty("message", chatMessage);

        JsonObject message = new JsonObject();
        message.addProperty("type", "chess-game");
        message.addProperty("roomId", roomId);

        JsonObject payload = new JsonObject();
        payload.addProperty("action", "chat");
        payload.add("data", chatData);
        message.add("payload", payload);

        sendMessage(message);
    }

    // Disconnect the WebSocket client
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
