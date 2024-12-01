package brain.brainstormer.chess;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChessServer {
    private static final int PORT = 12345;
    private static List<GameRoom> rooms = new ArrayList<>();
    private static final Gson gson = new Gson();

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

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Read the JSON object from the client
                String messageJson = in.readLine();
                JsonObject startMessage = gson.fromJson(messageJson, JsonObject.class);

                String username = startMessage.get("username").getAsString();
                String roomCode = startMessage.get("roomCode").getAsString();

                System.out.println("Received username: " + username);
                System.out.println("Received room code from client: " + roomCode);

                // Find or create the game room
                currentRoom = getRoomByCode(roomCode);
                if (currentRoom == null && "new".equalsIgnoreCase(roomCode)) {
                    currentRoom = new GameRoom();
                    rooms.add(currentRoom);
                    out.println(gson.toJson(createMessage("roomCode", currentRoom.getRoomCode())));
                } else if (currentRoom == null) {
                    out.println(gson.toJson(createMessage("error", "Room not found. Closing connection.")));
                    socket.close();
                    return;
                }

                // Add the client to the room as a player or spectator
                String role = currentRoom.addPlayer(socket, username);
                System.out.println("User " + username + " assigned role: " + role);

                if ("Spectator".equals(role)) {
                    currentRoom.broadcastToSpectators(createMessage("status", username + " joined as a spectator.").toString());
                    currentRoom.sendBoardState(socket); // Send the current board state to the spectator
                }


                // Listen for client messages
                while ((messageJson = in.readLine()) != null) {
                    System.out.println("Received message: " + messageJson);
                    processMessage(messageJson);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }

        private void processMessage(String messageJson) {
            try {
                JsonObject message = gson.fromJson(messageJson, JsonObject.class);
                String type = message.get("type").getAsString();
                JsonObject data = message.getAsJsonObject("data");

                switch (type) {
                    case "move":
                        handleMove(data);
                        break;
                    case "chat":
                        handleChat(data);
                        break;
                    default:
                        System.err.println("Unknown message type: " + type);
                        break;
                }
            } catch (Exception e) {
                System.err.println("Invalid message format: " + messageJson);
                e.printStackTrace();
            }
        }



        private void handleMove(JsonObject data) {
            String from = data.get("from").getAsString();
            String to = data.get("to").getAsString();
            String fen = data.get("fen").getAsString();

            System.out.println("Processing move from: " + from + " to: " + to);
            currentRoom.setCurrentFen(fen);

            // Broadcast move + FEN to all clients
            data.addProperty("fen", fen);
            currentRoom.sendGameUpdate(createMessage("move", data).toString());
        }


        private void handleChat(JsonObject data) {
            String message = data.get("message").getAsString();
            System.out.println("Broadcasting chat: " + message);
            currentRoom.sendGameUpdate(createMessage("chat", data).toString());
        }

        private JsonObject createMessage(String type, Object data) {
            JsonObject message = new JsonObject();
            message.addProperty("type", type);
            message.add("data", gson.toJsonTree(data));
            return message;
        }

        private void disconnect() {
            try {
                if (currentRoom != null) {
                    currentRoom.removePlayer(socket);
                    if (currentRoom.getPlayerCount() == 0) {
                        rooms.remove(currentRoom);
                        System.out.println("Room " + currentRoom.getRoomCode() + " removed (empty).");
                    }
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class GameRoom {
        private List<Socket> players = new ArrayList<>();
        private Map<Socket, User> playerMap = new HashMap<>();
        private List<Socket> spectators = new ArrayList<>();
        private String roomCode;
        private String currentFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"; // Initial FEN

        public GameRoom() {
            this.roomCode = generateRoomCode();
        }

        public String getCurrentFen() {
            return currentFen;
        }

        public void setCurrentFen(String fen) {
            this.currentFen = fen;
        }

        public String getRoomCode() {
            return roomCode;
        }

        public synchronized int getPlayerCount() {
            return players.size();
        }

        public synchronized void sendBoardState(Socket client) throws IOException {
            String fen = getCurrentFen(); // Get the current FEN
            JsonObject boardStateMessage = createMessage("boardState", fen);
            sendMessage(client, boardStateMessage.toString());
        }



        public synchronized String addPlayer(Socket client, String username) throws IOException {
            User existingUser = playerMap.values().stream()
                    .filter(user -> user.getUsername().equals(username))
                    .findFirst()
                    .orElse(null);

            if (existingUser != null) {
                // Reassign the player to their existing role
                playerMap.keySet().removeIf(s -> playerMap.get(s).getUsername().equals(username));
                playerMap.put(client, existingUser);
                players.add(client);
                sendMessage(client, createMessage("role", existingUser.getRole()).toString());
                sendUsernames(); // Broadcast updated usernames

                // Send the current FEN to the player
                sendBoardState(client);
                return existingUser.getRole();
            }

            // Logic for new players or spectators
            String role;
            if (players.size() < 2) {
                role = (players.size() == 0) ? "White" : "Black";
                players.add(client);
                playerMap.put(client, new User(username, role));
            } else {
                role = "Spectator";
                spectators.add(client);
            }

            sendMessage(client, createMessage("role", role).toString());

            sendUsernames(); // Broadcast updated usernames

            // Send the current FEN to the player
            sendBoardState(client);
            return role;
        }


        public synchronized void sendUsernames() {
            List<String> usernames = new ArrayList<>();
            for (User user : playerMap.values()) {
                usernames.add(user.getUsername());
            }

            System.out.println("Server-> Sending usernames: " + usernames);
            sendGameUpdate(createMessage("usernames", usernames).toString());
        }





        public synchronized void broadcastToSpectators(String message) {
            for (Socket spectator : spectators) {
                try {
                    sendMessage(spectator, message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public synchronized void sendGameUpdate(String message) {

            sendMessageToAll(message);
            broadcastToSpectators(message);
        }

        public synchronized void sendMessageToAll(String message) {
            for (Socket player : players) {
                try {
                    sendMessage(player, message);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }



        public synchronized void removePlayer(Socket player) {
            players.remove(player);
            playerMap.remove(player);
            sendMessageToAll(createMessage("status", "A player has left the game.").toString());
        }

        private void sendMessage(Socket client, String message) throws IOException {
            if (client != null && !client.isClosed()) {
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                out.println(message);
            }
        }

        private String generateRoomCode() {
            return Integer.toString((int) (Math.random() * 10000));
        }

        private JsonObject createMessage(String type, Object data) {
            JsonObject message = new JsonObject();
            message.addProperty("type", type);
            message.add("data", gson.toJsonTree(data));
            return message;
        }
    }

    private static GameRoom getRoomByCode(String roomCode) {
        return rooms.stream().filter(room -> room.getRoomCode().equals(roomCode)).findFirst().orElse(null);
    }

    static class User {
        private final String username;
        private final String role;

        public User(String username, String role) {
            this.username = username;
            this.role = role;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }

        @Override
        public String toString() {
            return "User{" + "username='" + username + '\'' + ", role='" + role + '\'' + '}';
        }
    }
}
