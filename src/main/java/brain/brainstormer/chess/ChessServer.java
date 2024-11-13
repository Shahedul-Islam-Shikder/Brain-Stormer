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

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("Enter room code or type 'new' to create a room:");
                String roomCode = in.readLine();
                System.out.println("Received room code from client: " + roomCode);

                if ("new".equalsIgnoreCase(roomCode)) {
                    currentRoom = new GameRoom();
                    rooms.add(currentRoom);
                    out.println("Room created. Code: " + currentRoom.getRoomCode());
                    System.out.println("New room created with code: " + currentRoom.getRoomCode());
                } else {
                    currentRoom = getRoomByCode(roomCode);
                    if (currentRoom == null) {
                        out.println("Room not found. Closing connection.");
                        socket.close();
                        return;
                    }
                    out.println("Joined room: " + roomCode);
                    System.out.println("Client joined room: " + roomCode);
                }

                String playerRole = currentRoom.addPlayer(socket);
                out.println(playerRole); // Send role ("White" or "Black")

                while (currentRoom.getPlayerCount() < 2) {
                    out.println("Waiting for another player...");
                    Thread.sleep(1000);
                }

                currentRoom.sendMessageToAll("Game start!");

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("exit")) {
                        break;
                    }

                    System.out.println("Received move from client: " + message);
                    currentRoom.broadcastMove(message);
                }
            } catch (IOException | InterruptedException e) {
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
        private String roomCode;

        public GameRoom() {
            this.roomCode = generateRoomCode();
        }

        public String getRoomCode() {
            return roomCode;
        }

        public synchronized String addPlayer(Socket player) throws IOException {
            players.add(player);

            String role = (players.size() == 1) ? "White" : "Black";
            sendMessage(player, role);

            if (players.size() == 1) {
                sendMessage(player, "Waiting for another player...");
            }

            return role;
        }

        public synchronized int getPlayerCount() {
            return players.size();
        }

        public synchronized void broadcastMove(String move) {
            for (Socket player : players) {
                try {
                    sendMessage(player, move);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
