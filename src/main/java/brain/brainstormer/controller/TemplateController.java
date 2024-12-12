package brain.brainstormer.controller;

import brain.brainstormer.components.core.ComponentFactory;
import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.service.ComponentService;
import brain.brainstormer.service.TemplateService;
import brain.brainstormer.socket.Socket;
import brain.brainstormer.utilGui.AddComponentDialog;
import brain.brainstormer.utilGui.ManageUsersDialog;
import brain.brainstormer.utils.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TemplateController {

    @FXML
    private VBox templateContentArea;
    @FXML
    private ScrollPane templateScrollPane;
    @FXML
    private Button homeButton;
    @FXML
    private Label templateTitle;
    @FXML
    private Label templateDescription;
    @FXML
    private Button addComponentButton;
    @FXML
    private Button manageUsersButton;

    @FXML
    private Button chatButton;

    private final TemplateService templateService = TemplateService.getInstance();
    private final ComponentService componentService = ComponentService.getInstance();
    private Socket socket; // WebSocket client

    @FXML
    private void initialize() {
        String userId = SessionManager.getInstance().getUserId();
        String templateId = TemplateData.getInstance().getCurrentTemplateId();

        refreshTemplateContent(templateId);

        // Check if template is private
        boolean isPrivate = "private".equals(TemplateData.getInstance().getCurrentTemplateType());

        // Role-based button visibility
        if (isPrivate) {
            // Private templates: full access
            addComponentButton.setVisible(true);
            manageUsersButton.setVisible(false);
            chatButton.setVisible(false);
        } else {
            // Public templates: roles dictate visibility
            connectToWebSocket(templateId);
            manageUsersButton.setVisible(TemplateData.getInstance().isAuthor(userId));
            chatButton.setVisible(RoleUtils.canEdit(userId));
            addComponentButton.setVisible(RoleUtils.canEdit(userId));
        }
        // Connect to WebSocket if the template is public


        homeButton.setOnAction(event -> switchToHome());
        addComponentButton.setOnAction(event -> addComponent(templateId));

        manageUsersButton.setOnAction(event -> {
            ManageUsersDialog dialog = new ManageUsersDialog(TemplateData.getInstance().getCurrentTemplateId());
            dialog.init();
        });

        chatButton.setOnAction(event -> loadChatView());
    }

    private void loadChatView() {
        templateContentArea.getChildren().clear();

        VBox chatLayout = new VBox();
        chatLayout.getStyleClass().add("chat-layout");

        // Back Button
        Button backButton = new Button("Back");
        backButton.setOnAction(event -> refreshTemplateContent(TemplateData.getInstance().getCurrentTemplateId()));
        backButton.getStyleClass().add("chat-back-button");

        // ScrollPane with Messages
        VBox messageBox = new VBox(10);
        messageBox.getStyleClass().add("message-box");

        ScrollPane scrollPane = new ScrollPane(messageBox);
        scrollPane.getStyleClass().add("chat-scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setVvalue(1.0);

        // Input Area
        TextField messageInput = new TextField();
        messageInput.setPromptText("Type your message...");
        messageInput.getStyleClass().add("chat-input-field");

        Button sendButton = new Button("Send");
        sendButton.getStyleClass().add("chat-send-button");
        sendButton.setOnAction(event -> {
            sendChatMessage(messageInput.getText(), messageBox);
            messageInput.clear(); // Clear the input field after sending

            // Scroll to the bottom of the scroll pane
            Platform.runLater(() -> scrollPane.setVvalue(1.0));
        });


        HBox inputArea = new HBox(10, messageInput, sendButton);
        inputArea.getStyleClass().add("chat-input-area");
        inputArea.setAlignment(Pos.CENTER);

        // Add Components to Layout
        chatLayout.getChildren().addAll(backButton, scrollPane, inputArea);
        templateContentArea.getChildren().add(chatLayout);

//         Dynamically Adjust ScrollPane Height



        scrollPane.setPrefHeight(templateScrollPane.getHeight() - 150);

        // Apply Styles
        StyleUtil.applyCustomStylesheet(chatLayout, "/styles/chat.css");

        // Load Chat History
        loadChatHistory(messageBox);
        // Scroll to the bottom once chat history is loaded
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }




    private void loadChatHistory(VBox messageBox) {
        String templateId = TemplateData.getInstance().getCurrentTemplateId();
        List<Document> chatHistory = TemplateService.getInstance().getChatMessages(templateId);

        for (Document message : chatHistory) {
            boolean isCurrentUser = message.getString("user").equals(SessionManager.getInstance().getUsername());
            displayChatMessage(message.getString("user"), message.getString("text"), messageBox, isCurrentUser);
        }
    }

    private void sendChatMessage(String text, VBox messageBox) {
        if (text.trim().isEmpty()) return;

        String username = SessionManager.getInstance().getUsername();
        String templateId = TemplateData.getInstance().getCurrentTemplateId();

        // Save the message to MongoDB
        TemplateService.getInstance().addChatMessage(templateId, username, text);

        // Send the message via WebSocket
        Document message = new Document("type", "chat")
                .append("roomId", templateId)
                .append("payload", new Document("user", username)
                        .append("text", text)
                        .append("timestamp", new Date()));
        socket.sendMessage(message.toJson());
    }



    private void displayChatMessage(String username, String text, VBox messageBox, boolean isCurrentUser) {
        HBox messageBubble = new HBox();


        Label usernameLabel = new Label(username);
        usernameLabel.getStyleClass().add("message-username");

        Label textLabel = new Label(text);
        textLabel.setWrapText(true);


        if (isCurrentUser) {
            textLabel.getStyleClass().add("message-bubble-user");
        } else {
            textLabel.getStyleClass().add("message-bubble-other");
        }

        if (isCurrentUser) {
            messageBubble.setAlignment(Pos.CENTER_RIGHT);
            messageBubble.getChildren().addAll(textLabel, usernameLabel);
        } else {
            messageBubble.setAlignment(Pos.CENTER_LEFT);
            messageBubble.getChildren().addAll(usernameLabel, textLabel);
        }

        Platform.runLater(() -> messageBox.getChildren().add(messageBubble));
    }



    private void connectToWebSocket(String templateId) {
//        String serverUrl = "ws://localhost:8000"; // Replace with your WebSocket server URL
        // add url from env
        String serverUrl = EnvUtil.getEnv("SERVER_URL");
        System.out.println("Server URL: " + serverUrl);
        socket = new Socket(serverUrl);

        socket.connect(
                message -> handleWebSocketMessage(message),
                () -> System.out.println("Connected to WebSocket for template: " + templateId),
                () -> System.out.println("Disconnected from WebSocket"),
                ex -> ex.printStackTrace()
        );

        // Join the room for this template
        socket.sendMessage("{\"type\": \"join\", \"roomId\": \"" + templateId + "\"}");
    }
    private void handleWebSocketMessage(String messageJson) {
        Document message = Document.parse(messageJson);

        String type = message.getString("type");
        if ("refresh".equals(type)) {
            System.out.println("Received refresh message from server.");

            // Use Platform.runLater to execute UI updates on the JavaFX Application Thread
            Platform.runLater(() -> {
                System.out.println("Refreshing template content...");
                refreshTemplateContent(TemplateData.getInstance().getCurrentTemplateId());
            });

        } else if ("chat".equals(type)) {
            // Handle real-time chat message
            Document payload = message.get("payload", Document.class);
            Platform.runLater(() -> {
                VBox messageBox = getMessageBoxFromChatLayout();
                if (messageBox != null) {
                    boolean isCurrentUser = payload.getString("user").equals(SessionManager.getInstance().getUsername());
                    displayChatMessage(payload.getString("user"), payload.getString("text"), messageBox, isCurrentUser);
                }
            });

        } else if ("chat-history".equals(type)) {
            // Handle chat history when joining a room
            List<Document> chatHistory = message.getList("payload", Document.class);
            Platform.runLater(() -> {
                VBox messageBox = getMessageBoxFromChatLayout();
                if (messageBox != null) {
                    for (Document chatMessage : chatHistory) {
                        boolean isCurrentUser = chatMessage.getString("user").equals(SessionManager.getInstance().getUsername());
                        displayChatMessage(chatMessage.getString("user"), chatMessage.getString("text"), messageBox, isCurrentUser);
                    }
                }
            });
        }
    }


    private VBox getMessageBoxFromChatLayout() {
        // Ensure the templateContentArea is not empty and the first child is a VBox
        if (!templateContentArea.getChildren().isEmpty() && templateContentArea.getChildren().get(0) instanceof VBox) {
            VBox chatLayout = (VBox) templateContentArea.getChildren().get(0);

            // Loop through children to find the ScrollPane
            for (var child : chatLayout.getChildren()) {
                if (child instanceof ScrollPane) {
                    ScrollPane scrollPane = (ScrollPane) child;

                    // Check if the content of the ScrollPane is a VBox (messageBox)
                    if (scrollPane.getContent() instanceof VBox) {
                        return (VBox) scrollPane.getContent();
                    }
                }
            }
        }

        // Return null if the messageBox is not found
        System.err.println("MessageBox not found in chat layout.");
        return null;
    }







    private void setTemplateDetails(Document templateData) {
        // Extract the ObjectId and convert it to a hexadecimal string
        ObjectId userIdObj = templateData.getObjectId("userId");
        String userId = userIdObj != null ? userIdObj.toHexString() : null;
        TemplateData.getInstance().setAuthor(userId);

        // Similarly, handle the "editors" and "viewers" fields
        List<ObjectId> editorIds = templateData.getList("editors", ObjectId.class);
        List<String> editors = editorIds != null ? editorIds.stream()
                .map(ObjectId::toHexString)
                .collect(Collectors.toList()) : Collections.emptyList();
        TemplateData.getInstance().setEditors(editors);

        List<ObjectId> viewerIds = templateData.getList("viewers", ObjectId.class);
        List<String> viewers = viewerIds != null ? viewerIds.stream()
                .map(ObjectId::toHexString)
                .collect(Collectors.toList()) : Collections.emptyList();
        TemplateData.getInstance().setViewers(viewers);

        // Set the template type
        TemplateData.getInstance().setCurrentTemplateType(templateData.getString("type"));

        // Set the template title and description
        templateTitle.setText(templateData.getString("name"));
        templateDescription.setText(templateData.getString("description"));
        templateContentArea.getChildren().clear();
    }


    private void addComponentsToTemplate(List<Document> components) {
        for (Document componentDoc : components) {
            CoreComponent component = ComponentFactory.createComponent(componentDoc);

            if (component != null) {
                templateContentArea.getChildren().add(component.render());
            } else {
                addPlaceholderComponent(componentDoc);
            }
        }
    }

    private void addPlaceholderComponent(Document componentDoc) {
        String componentName = componentDoc.getString("name");
        Label componentLabel = new Label(componentName != null ? componentName : "Unnamed Component");
        componentLabel.setStyle("-fx-text-fill: #E0E0E0; -fx-font-size: 14px;");
        templateContentArea.getChildren().add(componentLabel);
    }

    private void displayEmptyTemplateMessage() {
        Label emptyLabel = new Label("This page is empty. Start adding components!");
        emptyLabel.setStyle("-fx-text-fill: #B0B0B0; -fx-font-size: 16px;");
        templateContentArea.getChildren().add(emptyLabel);
    }

    private void displayTemplateNotFound() {
        templateTitle.setText("Template not found");
        templateDescription.setText("");
        templateContentArea.getChildren().clear();

        Label notFoundLabel = new Label("The template could not be loaded.");
        notFoundLabel.setStyle("-fx-text-fill: #B0B0B0; -fx-font-size: 16px;");
        templateContentArea.getChildren().add(notFoundLabel);
    }

    private void switchToHome() {
        // Disconnect from WebSocket before switching scenes
        if (socket != null) {
            socket.disconnect();
        }
        Stage stage = (Stage) homeButton.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "/brain/brainstormer/home.fxml", true,"Home");
    }

    private void addComponent(String templateId) {
        TemplateData templateData = TemplateData.getInstance();




        // For public templates, check roles (Author or Editor)
        String userId = SessionManager.getInstance().getUserId();
        if (!RoleUtils.canEdit(userId)) {
            System.out.println("Permission denied: Cannot add component.");
            return;
        }

        AddComponentDialog addComponentDialog = new AddComponentDialog(templateId, componentService, socket);
        addComponentDialog.init();

        // not here




    }

    public Socket getSocket() {
        return socket;
    }




    public void refreshTemplateContent(String templateId) {


        Document templateData = templateService.getTemplateById(templateId);
        if (templateData == null) {
            System.out.println("Template not found: " + templateId);
            displayTemplateNotFound();
            return;
        }

        setTemplateDetails(templateData);

        List<Document> components = templateData.getList("components", Document.class);

        if (components == null || components.isEmpty()) {

            displayEmptyTemplateMessage();
        } else {

            templateContentArea.getChildren().clear();


            addComponentsToTemplate(components);

        }
    }
}
