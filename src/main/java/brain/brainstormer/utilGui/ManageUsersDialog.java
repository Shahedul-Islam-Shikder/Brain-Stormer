package brain.brainstormer.utilGui;

import brain.brainstormer.service.TemplateService;
import brain.brainstormer.service.UserService;
import brain.brainstormer.utils.TemplateData;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bson.Document;

import java.util.List;

import java.util.List;
import java.util.stream.Collectors;

public class ManageUsersDialog {

    private final TemplateService templateService = TemplateService.getInstance();
    private final UserService userService = new UserService(); // Fetch users from UserService
    private final String templateId;

    public ManageUsersDialog(String templateId) {
        this.templateId = templateId;
    }

    public void init() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Manage Users");

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-background-color: #f4f4f4;");

        // Search bar
        TextField searchField = new TextField();
        searchField.setPromptText("Search users...");
        layout.getChildren().add(searchField);

        // User list
        ListView<UserItem> userListView = new ListView<>();
        layout.getChildren().add(userListView);

        // Fetch real users
        List<Document> allUsers = fetchAllUsers();
        String authorId = TemplateData.getInstance().getAuthor(); // Fetch the author ID

        allUsers.forEach(user -> {
            String userId = user.getObjectId("_id").toHexString();

            // Skip the author
            if (userId.equals(authorId)) {
                return;
            }

            String username = user.getString("username");

            // Check if the user is an editor or viewer
            boolean isEditor = isEditor(userId);
            boolean isViewer = isViewer(userId);

            userListView.getItems().add(new UserItem(userId, username, isEditor, isViewer));
        });

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            userListView.getItems().clear();
            allUsers.stream()
                    .filter(user -> {
                        String userId = user.getObjectId("_id").toHexString();
                        String username = user.getString("username");

                        // Skip the author
                        if (userId.equals(authorId)) {
                            return false;
                        }

                        return username.toLowerCase().contains(newValue.toLowerCase());
                    })
                    .forEach(user -> {
                        String userId = user.getObjectId("_id").toHexString();
                        String username = user.getString("username");

                        boolean isEditor = isEditor(userId);
                        boolean isViewer = isViewer(userId);

                        userListView.getItems().add(new UserItem(userId, username, isEditor, isViewer));
                    });
        });

        // Add Save and Close buttons
        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {
            userListView.getItems().forEach(userItem -> {
                if (userItem.isNone()) {
                    templateService.removeEditor(templateId, userItem.getUserId());
                    templateService.removeViewer(templateId, userItem.getUserId());
                } else if (userItem.isViewer()) {
                    templateService.removeEditor(templateId, userItem.getUserId());
                    templateService.addViewer(templateId, userItem.getUserId());
                } else if (userItem.isEditor()) {
                    templateService.removeViewer(templateId, userItem.getUserId());
                    templateService.addEditor(templateId, userItem.getUserId());
                }
            });

            stage.close();
        });

        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> stage.close());

        layout.getChildren().addAll(saveButton, closeButton);

        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.show();
    }



    private List<Document> fetchAllUsers() {
        return userService.getAllUsers(); // Retrieve all users
    }

    private boolean isEditor(String userId) {
        List<String> editors = TemplateData.getInstance().getEditors();
        return editors != null && editors.contains(userId);
    }

    private boolean isViewer(String userId) {
        List<String> viewers = TemplateData.getInstance().getViewers();
        return viewers != null && viewers.contains(userId);
    }

    static class UserItem extends VBox {
        private final String userId;
        private final RadioButton editorRadioButton;
        private final RadioButton viewerRadioButton;
        private final RadioButton noneRadioButton;

        public UserItem(String userId, String username, boolean isEditor, boolean isViewer) {
            this.userId = userId;

            Label userLabel = new Label(username);
            userLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

            editorRadioButton = new RadioButton("Editor");
            viewerRadioButton = new RadioButton("Viewer");
            noneRadioButton = new RadioButton("None");

            ToggleGroup roleGroup = new ToggleGroup();
            editorRadioButton.setToggleGroup(roleGroup);
            viewerRadioButton.setToggleGroup(roleGroup);
            noneRadioButton.setToggleGroup(roleGroup);

            // Set initial role
            if (isEditor) {
                editorRadioButton.setSelected(true);
            } else if (isViewer) {
                viewerRadioButton.setSelected(true);
            } else {
                noneRadioButton.setSelected(true);
            }

            this.getChildren().addAll(userLabel, editorRadioButton, viewerRadioButton, noneRadioButton);
            this.setSpacing(5);
            this.setStyle("-fx-padding: 10; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5;");
        }

        public String getUserId() {
            return userId;
        }

        public boolean isEditor() {
            return editorRadioButton.isSelected();
        }

        public boolean isViewer() {
            return viewerRadioButton.isSelected();
        }

        public boolean isNone() {
            return noneRadioButton.isSelected();
        }
    }
}
