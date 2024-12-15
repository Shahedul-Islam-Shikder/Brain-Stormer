package brain.brainstormer.components.elements;

import brain.brainstormer.api.GiphyApi;
import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.components.interfaces.Initializable;
import brain.brainstormer.service.TemplateService;
import brain.brainstormer.utilGui.AlertUtil;
import brain.brainstormer.config.TemplateData;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.bson.Document;

import java.util.Date;
import java.util.List;

public class Gif extends CoreComponent{
    private String gifUrl; // URL of the fetched GIF
    private String altText; // Alt-text for accessibility and clarity
    private TextField searchField; // To search for GIFs
    private Button searchButton; // To trigger the search

    public Gif(String id, String description, String gifUrl, String altText) {
        super(id, "gif", description);
        this.gifUrl = gifUrl != null ? gifUrl : ""; // Default to empty string
        this.altText = altText != null ? altText : "GIF Component"; // Default alt text
    }

    @Override
    public Node render() {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER_LEFT);

        // Search bar and button to fetch GIF
        HBox searchContainer = new HBox(10);
        searchContainer.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Enter search query for GIF");
        searchField.setStyle("-fx-background-color: #2c2f33; -fx-text-fill: white; -fx-prompt-text-fill: #888; -fx-padding: 10px; -fx-border-radius: 5px; -fx-font-size: 14px; -fx-pref-width: 300px;");

        // FontAwesome search icon for the search button
        FontAwesomeIconView searchIcon = new FontAwesomeIconView(FontAwesomeIcon.SEARCH);
        searchIcon.setStyle("-fx-fill: white; -fx-font-size: 16px;");

        searchButton = new Button("", searchIcon); // Button with icon only


        searchButton.setOnAction(event -> fetchGif());

        // Add event listener for "Enter" key to trigger search
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                fetchGif();
            }
        });

        searchContainer.getChildren().addAll(searchField, searchButton);

        // Displaying GIF image
        ImageView gifView = new ImageView();
        if (gifUrl != null && !gifUrl.isEmpty()) {
            try {
                gifView.setImage(new javafx.scene.image.Image(gifUrl));
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid GIF URL: " + gifUrl + ". No GIF will be displayed.");
            }
        } else {
            System.out.println("No GIF URL provided. GIF view will remain empty.");
        }
        gifView.setFitWidth(500); // GIF image size (you can adjust it as needed)
        gifView.setPreserveRatio(true);

        // Container for the GIF and search elements
        VBox gifContainer = new VBox(10, searchContainer, gifView);
        gifContainer.setAlignment(Pos.CENTER_LEFT);

        // Replacing old action buttons with the unified CheckBox-style buttons
        HBox actionButtons = createActionButtons();

        // Add GIF container and action buttons to the main container
        container.getChildren().addAll(gifContainer, actionButtons);
        applyGlobalComponentStyles(container);



        return container;
    }


    private void fetchGif() {
        // Get the search query entered by the user
        String searchQuery = searchField.getText();

        // Check if the search query is not empty
        if (!searchQuery.isEmpty()) {
            GiphyApi giphyApi = new GiphyApi();  // Create an instance of the GiphyApi class
            String fetchedGifUrl = giphyApi.searchGif(searchQuery);  // Fetch the GIF URL from the API

            if (fetchedGifUrl.startsWith("http")) {  // Check if a valid URL was returned
                this.gifUrl = fetchedGifUrl;  // Update the gifUrl
                System.out.println("Found GIF URL: " + gifUrl);

                // Update the ImageView with the fetched GIF URL
                render();  // Re-render the component to show the new GIF

                // Save the new GIF URL to the database
                saveToDatabase();
            } else {
                // Handle case where no valid GIF is found
                AlertUtil.showWarning("Warning", fetchedGifUrl);  // Show the error message from GiphyApi
            }
        } else {
            // Show a warning if no search query is entered
            AlertUtil.showWarning("Warning", "Please enter a search query.");
        }
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
    @Override
    public Document toDocument() {
        return new Document("_id", getId())
                .append("type", "gif")
                .append("config", new Document("gifUrl", gifUrl)
                        .append("altText", altText)
                        .append("description", getDescription()))
                .append("createdAt", new Date())
                .append("lastUpdated", new Date());
    }

    @Override
    public void delete() {
        // Logic for deleting the GIF if necessary (e.g., delete from a cloud service)
        super.delete();
        System.out.println("GIF Component deleted.");
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

            Document updatedComponent = new Document("config", new Document("gifUrl", gifUrl)
                    .append("altText", altText)
                    .append("description", getDescription()))
                    .append("lastUpdated", new Date());

            templateService.updateComponentInTemplate(templateId, getId(), updatedComponent);

            System.out.println("GIF Component saved to database.");
        } catch (Exception e) {
            System.err.println("Failed to save GIF Component: " + e.getMessage());
        }
    }
}
