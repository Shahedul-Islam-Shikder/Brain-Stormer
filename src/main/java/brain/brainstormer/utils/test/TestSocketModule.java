package brain.brainstormer.utils.test;

import brain.brainstormer.socket.Socket;

public class TestSocketModule {
    public static void main(String[] args) {
        String serverUrl = "ws://localhost:8000";

        Socket socket = new Socket(serverUrl);

        socket.connect(
                // On message received
                (message) -> {
                    System.out.println("Server: " + message);

                    // Parse the message to check its type
                    if (message.contains("\"type\":\"refresh\"")) {
                        System.out.println("Received refresh message: " + message);
                    } else {
                        System.out.println("Received unexpected message: " + message);
                    }
                },

                // On connection open
                () -> {
                    System.out.println("WebSocket connection established!");
                    // Send a join message
                    socket.sendMessage("{\"type\": \"join\", \"roomId\": \"template123\"}");

                    // Send an update message after joining
                    socket.sendMessage("{\"type\": \"update\", \"roomId\": \"template123\", \"payload\": {\"message\": \"Hello from Java client!\"}}");
                },

                // On connection close
                () -> System.out.println("WebSocket connection closed."),

                // On error
                (error) -> System.err.println("WebSocket error: " + error.getMessage())
        );

        // Keep the connection open for testing
        try {
            Thread.sleep(10000); // Keep the connection alive for 10 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Disconnect after testing
        socket.disconnect();
    }
}
