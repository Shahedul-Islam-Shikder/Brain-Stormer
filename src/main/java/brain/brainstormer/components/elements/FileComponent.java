package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.components.interfaces.Initializable;
import brain.brainstormer.utils.FileIOUtil;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class FileComponent extends CoreComponent implements Initializable {
    private String fileUrl; // URL of the uploaded file
    private String fileName; // Name of the file

    public FileComponent(String id, String description, String fileUrl, String fileName) {
        super(id, "file", description);
        this.fileUrl = fileUrl != null ? fileUrl : ""; // Default to empty string
        this.fileName = fileName != null ? fileName : "Unnamed File"; // Default file name
    }

    @Override
    public Node render() {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER_LEFT);

        // File label and buttons
        Label fileLabel = new Label(fileName != null ? fileName : "No file selected");

        // Upload button
        Button uploadButton = new Button("Upload File");
        uploadButton.setOnAction(event -> uploadFile(fileLabel));

        // Download button
        Button downloadButton = new Button("Download File");
        downloadButton.setDisable(fileUrl == null || fileUrl.isEmpty());
        downloadButton.setOnAction(event -> downloadFile());

        // Delete button
        Button deleteButton = new Button("Delete File");
        deleteButton.setDisable(fileUrl == null || fileUrl.isEmpty());
        deleteButton.setOnAction(event -> {
            deleteFile();
            fileLabel.setText("No file selected");
            downloadButton.setDisable(true);
            deleteButton.setDisable(true);
        });

        HBox actionButtons = new HBox(10, uploadButton, downloadButton, deleteButton);
        actionButtons.setAlignment(Pos.CENTER);

        VBox fileContainer = new VBox(10, fileLabel, actionButtons);
        fileContainer.setAlignment(Pos.CENTER);

        container.getChildren().addAll(fileContainer);

        return container;
    }

    private void uploadFile(Label fileLabel) {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(null);

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
            System.out.println("No file selected for upload.");
        }
    }

    private void downloadFile() {
        if (fileUrl == null || fileUrl.isEmpty()) {
            System.out.println("No file to download.");
            return;
        }

        try {
            // Default download location (Downloads folder)
            String userHome = System.getProperty("user.home");
            File downloadsDir = new File(userHome, "Downloads");

            // Ensure the Downloads directory exists
            if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
                System.err.println("Failed to create Downloads directory.");
                return;
            }

            // File to save
            File downloadedFile = new File(downloadsDir, fileName != null ? fileName : "downloaded_file");

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

                System.out.println("File downloaded successfully to: " + downloadedFile.getAbsolutePath());
            }

        } catch (Exception e) {
            System.err.println("Failed to download file: " + e.getMessage());
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

