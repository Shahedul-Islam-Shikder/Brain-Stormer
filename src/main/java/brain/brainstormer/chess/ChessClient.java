package brain.brainstormer.chess;

import java.io.*;
import java.net.*;

public class ChessClient {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerRole;

    public ChessClient(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to connect to server. Make sure the server is running.");
        }
    }

    public void start() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            // Set username for chat identification
            System.out.print("Enter your username: ");
            String username = reader.readLine();
            out.println(username);  // Send username to server

            // Room Code and Mode Setup
            System.out.print("Enter room code or type 'new' to create a room: ");
            String roomCode = reader.readLine();
            out.println(roomCode);  // Send room code to server

            System.out.println(in.readLine()); // Room creation/join confirmation
            playerRole = in.readLine(); // Receive role (White, Black, or Spectator)
            System.out.println("You are playing as: " + playerRole);

            // Start a thread to listen for incoming messages
            new Thread(this::listenForMessages).start();

            // Handle user input for moves or chat
            while (true) {
                System.out.print("Type your move or chat message: ");
                String message = reader.readLine();

                // If the message starts with "CHAT:", send it as a chat message
                if (message.startsWith("CHAT:")) {
                    out.println(message);
                } else if (playerRole.equals("White") || playerRole.equals("Black")) {
                    // If the user is a player, send it as a move
                    out.println(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("CHAT:")) {
                    // Display chat message
                    System.out.println(message.substring(5)); // Remove "CHAT:" prefix and display
                } else {
                    // Display move or system message
                    System.out.println("Server: " + message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPlayerRole() {
        return playerRole;
    }

    public void close() throws IOException {
        socket.close();
    }
}
