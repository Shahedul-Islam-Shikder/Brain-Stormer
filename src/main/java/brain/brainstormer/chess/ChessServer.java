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
                String messageJson = in.readLine(); // Initial message containing username and room code
                JsonObject startMessage = gson.fromJson(messageJson, JsonObject.class);

                String username = startMessage.get("username").getAsString();
                String roomCode = startMessage.get("roomCode").getAsString();

                System.out.println("Received username: " + username);
                System.out.println("Received room code from client: " + roomCode);

                // Handle room creation or joining
                if ("new".equalsIgnoreCase(roomCode)) {
                    currentRoom = new GameRoom();
                    rooms.add(currentRoom);

                    JsonObject roomCodeMessage = createMessage("roomCode", currentRoom.getRoomCode());
                    out.println(gson.toJson(roomCodeMessage));

                    System.out.println("New room created with code: " + currentRoom.getRoomCode());
                } else {
                    currentRoom = getRoomByCode(roomCode);
                    if (currentRoom == null) {
                        out.println(gson.toJson(createMessage("error", "Room not found. Closing connection.")));
                        socket.close();
                        return;
                    }

                    out.println(gson.toJson(createMessage("status", "Joined room: " + roomCode)));
                    System.out.println("Client joined room: " + roomCode);
                }

                // Assign the player's role and add the username
                String playerRole = currentRoom.addPlayer(socket, username);
                out.println(gson.toJson(createMessage("role", playerRole)));
                System.out.println("Player " + username + " assigned role: " + playerRole);

                // Wait for both players to join
                while (currentRoom.getPlayerCount() < 2) {
                    Thread.sleep(500);
                    out.println(gson.toJson(createMessage("status", "Waiting for another player...")));
                }

                // Broadcast user information when the game starts
                currentRoom.sendMessageToAll(createMessage("users", currentRoom.getUsers()).toString());
                currentRoom.sendMessageToAll(createMessage("status", "Game start!").toString());

                // Process incoming messages
                while ((messageJson = in.readLine()) != null) { // Reuse the same variable
                    System.out.println("Received from client: " + messageJson);
                    processMessage(messageJson);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }




        private void processMessage(String messageJson) {
            try {
                // Parse incoming message
                JsonObject message = gson.fromJson(messageJson, JsonObject.class);
                String type = message.get("type").getAsString();
                JsonObject data = message.getAsJsonObject("data");

                // Route based on the message type
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
            System.out.println("Processing move from: " + from + " to: " + to);
            currentRoom.broadcastMove(createMessage("move", data).toString());
        }

        private void handleChat(JsonObject data) {
            String message = data.get("message").getAsString();
            System.out.println("Broadcasting chat: " + message);
            currentRoom.sendMessageToAll(createMessage("chat", data).toString());
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
        private String roomCode;

        public GameRoom() {
            this.roomCode = generateRoomCode();
        }

        public String getRoomCode() {
            return roomCode;
        }
        public synchronized int getPlayerCount() {
            return players.size(); // This method resolves the error
        }

        public synchronized String addPlayer(Socket player, String username) throws IOException {
            players.add(player);

            // Assign role based on connection order
            String role = (players.size() == 1) ? "White" : "Black";
            User user = new User(username, role);
            playerMap.put(player, user);

            sendMessage(player, createMessage("role", role).toString());
            return role;
        }

        public synchronized List<User> getUsers() {
            return new ArrayList<>(playerMap.values());
        }

        public synchronized void broadcastMove(String moveJson) {
            sendMessageToAll(moveJson);
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

        private void sendMessage(Socket player, String message) throws IOException {
            if (player != null && !player.isClosed()) {
                PrintWriter out = new PrintWriter(player.getOutputStream(), true);
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
        public  String username; // Immutable field
        public String role;     // Immutable field

        // Constructor for creating User objects
        public User(String username, String role) {
            //! TODO: Make the fields private
            this.username = username;
            this.role = role;
        }

        // Public getter for 'username'
        public String getUsername() {
            return username;
        }

        // Public getter for 'role'
        public String getRole() {
            return role;
        }

        // Override 'toString' for better debugging
        @Override
        public String toString() {
            return "User{" +
                    "username='" + username + '\'' +
                    ", role='" + role + '\'' +
                    '}';
        }
    }


}
