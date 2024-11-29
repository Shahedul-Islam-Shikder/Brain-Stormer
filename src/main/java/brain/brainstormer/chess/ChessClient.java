package brain.brainstormer.chess;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

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
            // Send the JSON object to the server
            out.println(gson.toJson(startMessage));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void sendMove(String moveJson) {
        out.println(moveJson); // Send the JSON string to the server
    }


    public void sendChat(String chatMessage) {
        try {
            JsonObject chatData = new JsonObject();
            chatData.addProperty("message", chatMessage);

            JsonObject message = createMessage("chat", chatData);
            out.println(gson.toJson(message)); // Send the chat as a JSON message
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JsonObject receiveMessage() {
        try {
            String messageJson = in.readLine(); // Read incoming message
            if (messageJson != null) {
                // Ignore plain text prompts
                if (!messageJson.trim().startsWith("{")) {
                    System.out.println("Server: " + messageJson); // Log the prompt
                    return null;
                }
                // Try parsing as a JSON object
                return gson.fromJson(messageJson, JsonObject.class);
            }
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
