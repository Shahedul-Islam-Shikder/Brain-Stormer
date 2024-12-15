package brain.brainstormer.socket;

import brain.brainstormer.controller.TemplateController;
import brain.brainstormer.utils.SceneSwitcher;
import brain.brainstormer.utils.TemplateData;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.function.Consumer;

public class Socket {
    private WebSocketClient client;
    private final String serverUrl;

    public Socket(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    // Initialize and connect to the server
    public void connect(Consumer<String> onMessage, Runnable onOpen, Runnable onClose, Consumer<Exception> onError) {
        try {
            client = new WebSocketClient(new URI(serverUrl)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Connected to WebSocket server.");
                    if (onOpen != null) onOpen.run();
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("Message received: " + message);
                    if (onMessage != null) onMessage.accept(message);
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
            client.connectBlocking(); // Ensure connection is established before proceeding
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Send a message to the server
    public void sendMessage(String message) {
        if (client != null && client.isOpen()) {
            client.send(message);
        } else {
            System.err.println("WebSocket is not connected.");
        }
    }

    // Close the connection
    public void disconnect() {
        if (client != null) {
            client.close();
        }
    }

    public static void sendWebSocketUpdate() {
        String templateId = TemplateData.getInstance().getCurrentTemplateId();
        TemplateController controller = SceneSwitcher.getCurrentController(TemplateController.class);
        if (controller != null && controller.getSocket() != null) {
            controller.getSocket().sendMessage("{\"type\": \"update\", \"roomId\": \"" + templateId + "\"}");
        }
    }
}
