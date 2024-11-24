package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.components.interfaces.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Control;
import org.bson.Document;

import java.time.LocalDate;
import java.util.List;

public class DatePicker extends CoreComponent implements Initializable {
    private LocalDate selectedDate;

    public DatePicker(String id, String type, String description, LocalDate initialDate) {
        super(id, type, description);
        this.selectedDate = initialDate != null ? initialDate : LocalDate.now();
    }

    public Control render() {
        javafx.scene.control.DatePicker datePicker = new javafx.scene.control.DatePicker(selectedDate);
        datePicker.getStyleClass().add("date-picker"); // Apply custom style from CSS

        // Update selectedDate whenever the value changes and save to database
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            selectedDate = newValue;
            saveToDatabase();
        });

        return datePicker;
    }

    @Override
    public List<Node> getInputFields() {
        javafx.scene.control.DatePicker datePickerField = new javafx.scene.control.DatePicker(selectedDate);
        datePickerField.setPromptText("Select a date");
        datePickerField.getStyleClass().add("date-picker");

        // Return the DatePicker as a list of nodes to display in the dialog
        return List.of(datePickerField);
    }

    public Document toDocument() {
        return new Document("_id", getId())
                .append("type", "datepicker")
                .append("config", new Document("selectedDate", selectedDate != null ? selectedDate.toString() : "")
                        .append("description", getDescription()))
                .append("createdAt", "2024-11-16T08:00:00Z")  // Placeholder for timestamp
                .append("lastUpdated", "2024-11-16T09:00:00Z");  // Placeholder for timestamp
    }

    public void saveToDatabase() {
        // Implement save logic here
    }
}
