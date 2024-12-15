package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.components.interfaces.Initializable;
import brain.brainstormer.utilGui.AlertUtil;
import brain.brainstormer.utils.FileIOUtil;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bson.Document;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class File extends CoreComponent implements Initializable {
    private String fileUrl; // URL of the uploaded file
    private String fileName; // Name of the file

    public File(String id, String description, String fileUrl, String fileName) {
        super(id, "file", description);
        this.fileUrl = fileUrl != null ? fileUrl : ""; // Default to empty string
        this.fileName = fileName != null ? fileName : "Unnamed File"; // Default file name
    }

    @Override

    public Node render() {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER_LEFT);

        // File label (with bold text)
        Label fileLabel = new Label(fileName != null ? fileName : "No file selected");
        fileLabel.setStyle("-fx-font-weight: bold;");

        // Create a Region to push buttons to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS); // Make spacer grow horizontally

        // Upload button with icon
        FontAwesomeIconView uploadIcon = new FontAwesomeIconView(FontAwesomeIcon.UPLOAD);


        Button uploadButton = new Button("", uploadIcon); // Icon-only button
        uploadButton.setOnAction(event -> uploadFile(fileLabel));



        // Download button with icon
        FontAwesomeIconView downloadIcon = new FontAwesomeIconView(FontAwesomeIcon.DOWNLOAD);


        Button downloadButton = new Button("", downloadIcon); // Icon-only button
        downloadButton.setDisable(fileUrl == null || fileUrl.isEmpty());
        downloadButton.setOnAction(event -> downloadFile());


        // Delete button with icon
        FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);

        Button deleteButton = new Button("", deleteIcon); // Icon-only button
        deleteButton.setDisable(fileUrl == null || fileUrl.isEmpty());
        deleteButton.setOnAction(event -> {
            deleteFile();
            fileLabel.setText("No file selected");
            downloadButton.setDisable(true);
            deleteButton.setDisable(true);
        });

        // Action buttons (aligned to the right) with icons
        HBox actionButtons = new HBox(10, uploadButton, downloadButton, deleteButton);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);

        // File container with file label and action buttons
        HBox fileContainer = new HBox(10, fileLabel, spacer, actionButtons);
        fileContainer.setAlignment(Pos.CENTER_LEFT);

        // Apply global component styles
        applyGlobalComponentStyles(container);
        container.getChildren().addAll(fileContainer);
        // add     private HBox createActionButtons()

        HBox buttonContainer = createActionButtons();

        container.getChildren().addAll(buttonContainer);




        return container;
    }

    private HBox createActionButtons() {
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER_LEFT);
        buttonContainer.getStyleClass().add("button-container"); // CSS class for styling
        // Delete button with FontAwesome icon
        FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        deleteIcon.getStyleClass().add("delete-icon");

        Button deleteButton = new Button("", deleteIcon); // Icon-only button
        deleteButton.setOnAction(event -> {
            System.out.println("Deleting Image Component with ID: " + getId());
            delete();
        });


        buttonContainer.getChildren().addAll(deleteButton);
        applyGlobalComponentStyles(buttonContainer);
        return buttonContainer;
    }

    private void uploadFile(Label fileLabel) {
        FileChooser fileChooser = new FileChooser();
        java.io.File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                // Upload file to file.io
                fileUrl = FileIOUtil.uploadFile(selectedFile);
                fileName = selectedFile.getName();

                // Update label and enable buttons
                fileLabel.setText(fileName);
                saveToDatabase();

                System.out.println("File uploaded successfully: " + fileUrl);
            } catch (IOException e) {
                System.err.println("Failed to upload file: " + e.getMessage());
            }
        } else {
            AlertUtil.showInfo("Info","No file selected for upload.");
        }
    }

    private void downloadFile() {
        if (fileUrl == null || fileUrl.isEmpty()) {
            AlertUtil.showInfo("Info","No file to download.");
            return;
        }

        try {
            // Default download location (Downloads folder)
            String userHome = System.getProperty("user.home");
            java.io.File downloadsDir = new java.io.File(userHome, "Downloads");

            // Ensure the Downloads directory exists
            if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
                AlertUtil.showError("Error","Failed to create Downloads directory.");
                return;
            }

            // File to save
            java.io.File downloadedFile = new java.io.File(downloadsDir, fileName != null ? fileName : "downloaded_file");

            // Create a request to fetch the file
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(fileUrl)
                    .build();

            // Execute the request
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                // Write the file to the Downloads directory
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(downloadedFile)) {
                    fos.write(response.body().bytes());
                }

                AlertUtil.showInfo("Success","File downloaded successfully.");
            }

        } catch (Exception e) {
            AlertUtil.showError("Error","Failed to download file: " + e.getMessage());
        }
    }


    private void deleteFile() {
        if (fileUrl == null || fileUrl.isEmpty()) {
            System.out.println("No file to delete.");
            return;
        }

        boolean success = FileIOUtil.deleteFile(fileUrl);
        if (success) {
            System.out.println("File deleted successfully.");
            fileUrl = null;
            fileName = null;
            saveToDatabase();
        }
    }

    @Override
    public Document toDocument() {
        return new Document("_id", getId())
                .append("type", "file")
                .append("config", new Document("fileUrl", fileUrl)
                        .append("fileName", fileName)
                        .append("description", getDescription()))
                .append("createdAt", new Date())
                .append("lastUpdated", new Date());
    }

    @Override
    public void saveToDatabase() {
        try {
            brain.brainstormer.service.TemplateService.getInstance().updateComponentInTemplate(
                    brain.brainstormer.utils.TemplateData.getInstance().getCurrentTemplateId(),
                    getId(),
                    toDocument()
            );
            System.out.println("File Component saved to database.");
        } catch (Exception e) {
            System.err.println("Failed to save File Component: " + e.getMessage());
        }
    }

    @Override
    public List<Node> getInputFields() {
        TextField fileNameField = new TextField(fileName);
        fileNameField.setPromptText("Enter file name");
        fileNameField.textProperty().addListener((observable, oldValue, newValue) -> fileName = newValue);

        return List.of(fileNameField);
    }
}

