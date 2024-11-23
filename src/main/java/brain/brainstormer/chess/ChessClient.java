package brain.brainstormer.chess;

import java.io.*;
import java.net.*;

public class ChessClient {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

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

    public void start(String roomCode) {
        try {
            out.println(roomCode); // Send the room code or "new" to create a room
            System.out.println("Server: " + in.readLine()); // Confirmation of room
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMoveToServer(String move) throws IOException {
        out.println(move); // Send the move string directly to the server
    }

    public String receiveMoveFromServer() throws IOException {
        return in.readLine();
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
