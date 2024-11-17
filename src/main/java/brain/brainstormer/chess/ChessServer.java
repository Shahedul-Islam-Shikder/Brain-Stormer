package brain.brainstormer.chess;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChessServer {
    private static final int PORT = 12345;
    private static List<GameRoom> rooms = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private GameRoom currentRoom;
        private String username;  // Added to identify spectators and players by username
        private boolean isPlayer;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Get username from client for chat identification
                out.println("Enter your username:");
                username = in.readLine();
                System.out.println("User connected with username: " + username);

                out.println("Enter room code or type 'new' to create a room:");
                String roomCode = in.readLine();
                System.out.println("Received room code from " + username + ": " + roomCode);

                if ("new".equalsIgnoreCase(roomCode)) {
                    // Create new game room
                    currentRoom = new GameRoom();
                    rooms.add(currentRoom);
                    out.println("Room created. Code: " + currentRoom.getRoomCode());
                    System.out.println("New room created with code: " + currentRoom.getRoomCode());
                } else {
                    // Join existing game room
                    currentRoom = getRoomByCode(roomCode);
                    if (currentRoom == null) {
                        out.println("Room not found. Closing connection.");
                        socket.close();
                        return;
                    }
                    out.println("Joined room: " + roomCode);
                    System.out.println(username + " joined room: " + roomCode);
                }

                String playerRole = currentRoom.addPlayer(socket, username); // Modified to pass username
                isPlayer = !playerRole.equals("Spectator");
                out.println(playerRole); // Send role (e.g., "White", "Black", or "Spectator")

                // Game loop for handling moves and chat
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("exit")) {
                        break;
                    }

                    if (message.startsWith("CHAT:")) {
                        // Chat message from a spectator or player
                        String chatMessage = message.substring(5); // Extract message after "CHAT:"
                        currentRoom.broadcastChat(username + ": " + chatMessage);
                    } else if (isPlayer) {
                        // Game move from a player
                        System.out.println("Received move from " + username + ": " + message);
                        currentRoom.broadcastMove(message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (currentRoom != null) {
                        currentRoom.removePlayer(socket);
                    }
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class GameRoom {
        private List<Socket> players = new ArrayList<>();
        private List<Socket> spectators = new ArrayList<>();
        private String roomCode;
        private boolean isWhiteTurn = true;

        public GameRoom() {
            this.roomCode = generateRoomCode();
        }

        public String getRoomCode() {
            return roomCode;
        }

        public synchronized String addPlayer(Socket player, String username) throws IOException {
            if (players.size() < 2) {
                players.add(player);
                String role = (players.size() == 1) ? "White" : "Black";
                sendMessage(player, "You are assigned as " + role);
                return role;
            } else {
                spectators.add(player);
                sendMessage(player, "You are a Spectator");
                return "Spectator";
            }
        }

        public synchronized void broadcastMove(String move) {
            for (Socket player : players) {
                try {
                    sendMessage(player, move);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            for (Socket spectator : spectators) {
                try {
                    sendMessage(spectator, move);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            isWhiteTurn = !isWhiteTurn; // Switch turns
        }

        public synchronized void broadcastChat(String chatMessage) {
            // Send chat message to all players and spectators
            for (Socket player : players) {
                try {
                    sendMessage(player, "CHAT: " + chatMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            for (Socket spectator : spectators) {
                try {
                    sendMessage(spectator, "CHAT: " + chatMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public synchronized void removePlayer(Socket player) {
            players.remove(player);
            spectators.remove(player);
        }

        private void sendMessage(Socket player, String message) throws IOException {
            if (player != null && !player.isClosed()) {
                PrintWriter out = new PrintWriter(player.getOutputStream(), true);
                out.println(message);
            }
        }

        private String generateRoomCode() {
            return Integer.toString((int) (Math.random() * 1000000));
        }
    }

    private static GameRoom getRoomByCode(String roomCode) {
        return rooms.stream().filter(room -> room.getRoomCode().equals(roomCode)).findFirst().orElse(null);
    }
}
