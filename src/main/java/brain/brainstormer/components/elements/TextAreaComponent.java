package brain.brainstormer.components.elements;

import brain.brainstormer.service.ComponentService;
import javafx.scene.control.Control;
import javafx.scene.control.TextArea;
import org.bson.Document;

import java.util.Timer;
import java.util.TimerTask;

public class TextAreaComponent extends Components {
    private String initialText;
    private int rows;

    private Timer debounceTimer; // Timer for debouncing

    // Constructor
    public TextAreaComponent(String id, String name, String description, String initialText, int rows) {
        super(id, name, description);
        this.initialText = initialText != null ? initialText : ""; // Default to empty string if null
        this.rows = rows > 0 ? rows : 5; // Default to 5 if rows <= 0
    }

    @Override
    public Control render() {
        TextArea textArea = new TextArea(initialText);   // Create JavaFX TextArea with initial text
        textArea.setPrefRowCount(rows);                  // Set the number of visible rows
        textArea.setWrapText(true);                      // Enable text wrapping

        // Apply modern styling
        textArea.setStyle(
                "-fx-control-inner-background: rgba(0, 0, 0, 0.7);" + // Semi-transparent input area
                        "-fx-background-color: rgba(0, 0, 0, 0.5);" +         // Semi-transparent border area
                        "-fx-text-fill: #FFFFFF;" +                           // White text
                        "-fx-border-color: #666666;" +                       // Subtle gray border
                        "-fx-border-radius: 8px;" +                          // Rounded corners
                        "-fx-font-size: 14px;"                            // Font size

        );

        // Add a listener to track changes in the text with debouncing
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            initialText = newValue; // Update the initialText field dynamically
            debounceSave(ComponentService.getInstance(), getId());
        });

        return textArea;
    }

    // Debounced save to database
    private void debounceSave(ComponentService componentService, String templateId) {
        if (debounceTimer != null) {
            debounceTimer.cancel(); // Cancel any previous scheduled task
        }

        debounceTimer = new Timer();
        debounceTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                saveToDatabase(componentService, templateId);
            }
        }, 500); // Delay in milliseconds (e.g., 500ms)
    }

    public void saveToDatabase(ComponentService componentService, String templateId) {
        // Create a MongoDB document for this component
        Document componentDocument = new Document("id", getId())
                .append("name", getName())
                .append("description", getDescription())
                .append("initialText", initialText)
                .append("rows", rows);

        // Update the component in the database
        componentService.updateComponentInTemplate(templateId, getId(), componentDocument);
    }

    // Getters and Setters
    public String getInitialText() {
        return initialText;
    }

    public void setInitialText(String initialText) {
        this.initialText = initialText;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows > 0 ? rows : 5; // Ensure rows are positive
    }
}
