package brain.brainstormer.chess;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.*;

public class ChessClient {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson;

    public ChessClient(String serverAddress, int port) {
        gson = new Gson();
        try {
            socket = new Socket(serverAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to connect to server. Make sure the server is running.");
        }
    }

    public void start(JsonObject startMessage) {
        try {
            out.println(gson.toJson(startMessage));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMove(String moveJson) {
        out.println(moveJson);
    }

    public void sendChat(String chatMessage) {
        JsonObject chatData = new JsonObject();
        chatData.addProperty("message", chatMessage);
        JsonObject message = createMessage("chat", chatData);
        out.println(gson.toJson(message));
    }

    public JsonObject receiveMessage() {
        try {
            String messageJson = in.readLine();
            return gson.fromJson(messageJson, JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JsonObject createMessage(String type, JsonObject data) {
        JsonObject message = new JsonObject();
        message.addProperty("type", type);
        message.add("data", data);
        return message;
    }
}
