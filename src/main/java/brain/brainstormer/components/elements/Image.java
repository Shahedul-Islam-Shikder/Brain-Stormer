package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.components.interfaces.Initializable;
import brain.brainstormer.service.TemplateService;
import brain.brainstormer.utilGui.AlertUtil;
import brain.brainstormer.config.TemplateData;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class Image extends CoreComponent implements Initializable {
    private String imageUrl; // URL of the uploaded image
    private String altText; // Alt-text for accessibility and clarity

    public Image(String id, String description, String imageUrl, String altText) {
        super(id, "image", description);
        this.imageUrl = imageUrl != null ? imageUrl : ""; // Default to empty string
        this.altText = altText != null ? altText : "Image Component"; // Default alt text
    }

    @Override
    public Node render() {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER_LEFT);

        // Collapsible section for the image
        TitledPane collapsiblePane = new TitledPane();

        collapsiblePane.setText(altText != null ? altText : "Image Component");
        collapsiblePane.setCollapsible(true);

        // Image display
        ImageView imageView = new ImageView();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                imageView.setImage(new javafx.scene.image.Image(imageUrl));
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid image URL: " + imageUrl + ". No image will be displayed.");
            }
        } else {
            System.out.println("No image URL provided. Image view will remain empty.");
        }
        imageView.setFitWidth(600);
        imageView.setPreserveRatio(true);

        // Upload button
        Button uploadButton = new Button("Upload Image");
        uploadButton.setOnAction(event -> uploadImage(imageView));

        VBox imageContainer = new VBox(10, imageView, uploadButton);
        imageContainer.setAlignment(Pos.CENTER_LEFT);

        collapsiblePane.setContent(imageContainer);

        // Replacing old action buttons with the unified CheckBox-style buttons
        HBox actionButtons = createActionButtons();

        container.getChildren().addAll(collapsiblePane, actionButtons);
        applyGlobalComponentStyles(container);

        return container;
    }

    private HBox createActionButtons() {
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER_LEFT);
        buttonContainer.getStyleClass().add("button-container"); // CSS class for styling

        // Edit button with FontAwesome icon
        FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
        editIcon.getStyleClass().add("edit-icon");

        Button editButton = new Button("", editIcon); // Icon-only button
        editButton.setOnAction(event -> {
            Alert editDialog = new Alert(Alert.AlertType.CONFIRMATION);
            editDialog.setTitle("Edit Image Component");
            editDialog.setHeaderText("Update Alt Text or Upload New Image");

            TextField altTextField = new TextField(altText);
            altTextField.setPromptText("Enter alt text");

            VBox dialogContent = new VBox(10, altTextField);
            editDialog.getDialogPane().setContent(dialogContent);

            editDialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    altText = altTextField.getText();
                    saveToDatabase();
                }
            });
        });

        // Delete button with FontAwesome icon
        FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        deleteIcon.getStyleClass().add("delete-icon");

        Button deleteButton = new Button("", deleteIcon); // Icon-only button
        deleteButton.setOnAction(event -> {
            System.out.println("Deleting Image Component with ID: " + getId());
            delete();
        });


        buttonContainer.getChildren().addAll(editButton, deleteButton);
        applyGlobalComponentStyles(buttonContainer);
        return buttonContainer;
    }

    private void uploadImage(ImageView imageView) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                // Upload image to Cloudinary
                Cloudinary cloudinary = brain.brainstormer.utils.CloudinaryUtil.getInstance();
                var uploadResult = cloudinary.uploader().upload(selectedFile, ObjectUtils.emptyMap());
                imageUrl = uploadResult.get("url").toString();

                // Update image view and save to database
                imageView.setImage(new javafx.scene.image.Image(imageUrl));
                saveToDatabase();

            } catch (IOException e) {
                AlertUtil.showError("Error", "Failed to upload image: " + e.getMessage());
            }
        } else {
            AlertUtil.showWarning("Warning", "No image selected. Image URL will remain unchanged.");
        }
    }

    @Override
    public List<Node> getInputFields() {
        TextField altTextField = new TextField(altText);
        altTextField.setPromptText("Enter alt text for the image");
        altTextField.textProperty().addListener((observable, oldValue, newValue) -> altText = newValue);

        return List.of(altTextField);
    }

    @Override
    public Document toDocument() {
        return new Document("_id", getId())
                .append("type", "image")
                .append("config", new Document("imageUrl", imageUrl)
                        .append("altText", altText)
                        .append("description", getDescription()))
                .append("createdAt", new Date())
                .append("lastUpdated", new Date());
    }

    @Override
    public void delete() {
        // First, delete the image from Cloudinary if it exists
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                Cloudinary cloudinary = brain.brainstormer.utils.CloudinaryUtil.getInstance();

                // Extract the public ID of the image from the URL
                String publicId = imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.lastIndexOf("."));

                // Delete the image from Cloudinary
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                System.out.println("Image deleted from Cloudinary: " + publicId);
            } catch (Exception e) {
                System.err.println("Failed to delete image from Cloudinary: " + e.getMessage());
            }
        }

        // Then, delete the component from the database
        super.delete();
        System.out.println("Image Component deleted from database.");
    }

    @Override
    public void saveToDatabase() {
        try {
            TemplateService templateService = TemplateService.getInstance(); // Use singleton TemplateService

            String templateId = TemplateData.getInstance().getCurrentTemplateId();
            if (templateId == null || templateId.isEmpty()) {
                System.err.println("No current template ID set in TemplateData.");
                return;
            }

            Document updatedComponent = new Document("config", new Document("imageUrl", imageUrl)
                    .append("altText", altText)
                    .append("description", getDescription()))
                    .append("lastUpdated", new Date());

            templateService.updateComponentInTemplate(templateId, getId(), updatedComponent);

            System.out.println("Image Component saved to database.");
        } catch (Exception e) {
            System.err.println("Failed to save Image Component: " + e.getMessage());
        }
    }
}
