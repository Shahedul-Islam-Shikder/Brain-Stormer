package brain.brainstormer.controller;

import brain.brainstormer.components.core.ComponentFactory;
import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.service.ComponentService;
import brain.brainstormer.service.TemplateService;
import brain.brainstormer.socket.Socket;
import brain.brainstormer.utilGui.AddComponentDialog;
import brain.brainstormer.utilGui.ManageUsersDialog;
import brain.brainstormer.utils.RoleUtils;
import brain.brainstormer.utils.SceneSwitcher;
import brain.brainstormer.utils.SessionManager;
import brain.brainstormer.utils.TemplateData;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TemplateController {

    @FXML
    private VBox templateContentArea;
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
        } else {
            // Public templates: roles dictate visibility
            connectToWebSocket(templateId);
            manageUsersButton.setVisible(TemplateData.getInstance().isAuthor(userId));
            addComponentButton.setVisible(RoleUtils.canEdit(userId));
        }
        // Connect to WebSocket if the template is public


        homeButton.setOnAction(event -> switchToHome());
        addComponentButton.setOnAction(event -> addComponent(templateId));

        manageUsersButton.setOnAction(event -> {
            ManageUsersDialog dialog = new ManageUsersDialog(TemplateData.getInstance().getCurrentTemplateId());
            dialog.init();
        });
    }

    private void connectToWebSocket(String templateId) {
        String serverUrl = "ws://localhost:8000"; // Replace with your WebSocket server URL
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
    private void handleWebSocketMessage(String message) {
        if (message.contains("refresh")) {
            System.out.println("Received refresh message from server.");


            // Use Platform.runLater to execute UI updates on the JavaFX Application Thread
            Platform.runLater(() -> {
                System.out.println("Refreshing template content... BOOM chaaay chaay chaay boom chaaah");
                refreshTemplateContent(TemplateData.getInstance().getCurrentTemplateId());

            });
        }
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
        SceneSwitcher.switchScene(stage, "/brain/brainstormer/home.fxml", true);
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
