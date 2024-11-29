
package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import brain.brainstormer.components.interfaces.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class TableComponent extends CoreComponent implements Initializable {
    private List<List<String>> rowData;
    private List<String> columnTypes; // New: Tracks column types (Text/Date Picker)

    public TableComponent(String id, String description) {
        super(id, "table", description);
        this.rowData = new ArrayList<>();
        this.columnTypes = new ArrayList<>();
    }

    public List<List<String>> getRows() {
        return rowData;
    }

    public void setRows(List<List<String>> rows) {
        this.rowData = rows;
    }

    @Override
    public Node render() {
        VBox container = new VBox(10);
        container.setAlignment(Pos.TOP_LEFT);
        container.setPadding(new Insets(20));

        Button openInputDialogButton = new Button("Configure Table");

        GridPane tableGrid = new GridPane();
        tableGrid.setAlignment(Pos.TOP_LEFT);
        tableGrid.setPadding(new Insets(10));
        tableGrid.setHgap(10);
        tableGrid.setVgap(5);

        // Open a dialog to input rows and columns
        openInputDialogButton.setOnAction(event -> showInputDialog(tableGrid));

        container.getChildren().addAll(openInputDialogButton, tableGrid);
        return container;
    }

    private void showInputDialog(GridPane tableGrid) {
        Dialog<int[]> inputDialog = new Dialog<>();
        inputDialog.setTitle("Table Configuration");
        inputDialog.setHeaderText("Enter the number of rows and columns");

        // Create input fields
        TextField rowField = new TextField();
        rowField.setPromptText("Rows");

        TextField columnField = new TextField();
        columnField.setPromptText("Columns");

        GridPane dialogContent = new GridPane();
        dialogContent.setHgap(10);
        dialogContent.setVgap(10);
        dialogContent.setPadding(new Insets(20));
        dialogContent.add(new Label("Rows:"), 0, 0);
        dialogContent.add(rowField, 1, 0);
        dialogContent.add(new Label("Columns:"), 0, 1);
        dialogContent.add(columnField, 1, 1);

        inputDialog.getDialogPane().setContent(dialogContent);

        // Add OK and Cancel buttons
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        inputDialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        inputDialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                try {
                    int rows = Integer.parseInt(rowField.getText());
                    int columns = Integer.parseInt(columnField.getText());
                    if (rows > 0 && columns > 0) {
                        return new int[]{rows, columns};
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid positive integers.");
                }
            }
            return null;
        });

        var result = inputDialog.showAndWait();
        result.ifPresent(dimensions -> createTable(tableGrid, dimensions[0], dimensions[1]));
    }

    private void createTable(GridPane tableGrid, int rows, int columns) {
        tableGrid.getChildren().clear(); // Clear existing table

        rowData.clear(); // Clear existing data
        columnTypes.clear(); // Clear existing column types

        // Create the type-selection row
        createTypeSelectionRow(tableGrid, columns);

        // Create data rows
        for (int row = 1; row <= rows; row++) {
            List<String> rowValues = new ArrayList<>();
            for (int col = 0; col < columns; col++) {
                addCell(tableGrid, row, col, rowValues);
            }
            rowData.add(rowValues);
        }

        // Add "Add Row" and "Add Column" buttons
        Button addRowButton = new Button("+");
        addRowButton.setOnAction(event -> addRow(tableGrid, columns));
        tableGrid.add(addRowButton, columns, 1);

        Button addColumnButton = new Button("+");
        addColumnButton.setOnAction(event -> addColumn(tableGrid, rows));
        tableGrid.add(addColumnButton, 0, rows + 1);
    }

    private void createTypeSelectionRow(GridPane tableGrid, int columns) {
        for (int col = 0; col < columns; col++) {
            ComboBox<String> typeSelector = new ComboBox<>();
            typeSelector.getItems().addAll("Text", "Date Picker");
            typeSelector.setValue("Text"); // Default to Text
            final int columnIndex = col;
            typeSelector.valueProperty().addListener((observable, oldValue, newValue) -> {
                columnTypes.set(columnIndex, newValue);
                updateColumnType(tableGrid, columnIndex, newValue);
            });

            columnTypes.add("Text"); // Initialize column type to Text
            tableGrid.add(typeSelector, col, 0); // Add to the first row
        }
    }

    private void updateColumnType(GridPane tableGrid, int columnIndex, String newType) {
        for (Node node : tableGrid.getChildren()) {
            Integer row = GridPane.getRowIndex(node);
            Integer col = GridPane.getColumnIndex(node);
            if (row != null && row > 0 && col != null && col == columnIndex) {
                tableGrid.getChildren().remove(node);
                if ("Date Picker".equals(newType)) {
                    javafx.scene.control.DatePicker datePicker = new javafx.scene.control.DatePicker();
                    datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                        rowData.get(row - 1).set(columnIndex, newVal != null ? newVal.toString() : "");
                    });
                    tableGrid.add(datePicker, columnIndex, row);
                } else {
                    TextField textField = new TextField(rowData.get(row - 1).get(columnIndex));
                    textField.textProperty().addListener((obs, oldVal, newVal) -> {
                        rowData.get(row - 1).set(columnIndex, newVal);
                    });
                    tableGrid.add(textField, columnIndex, row);
                }
            }
        }
    }

    private void addCell(GridPane tableGrid, int row, int col, List<String> rowValues) {
        String columnType = columnTypes.size() > col ? columnTypes.get(col) : "Text";

        if ("Date Picker".equals(columnType)) {
            javafx.scene.control.DatePicker datePicker = new DatePicker();
            datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                rowValues.set(col, newVal != null ? newVal.toString() : "");
            });
            tableGrid.add(datePicker, col, row);
        } else {
            TextField cell = new TextField();
            cell.setPromptText("Row " + row + " Col " + (col + 1));
            cell.textProperty().addListener((obs, oldVal, newVal) -> {
                rowValues.set(col, newVal);
            });
            tableGrid.add(cell, col, row);
        }
        rowValues.add(""); // Initialize with an empty value
    }

    private void addRow(GridPane tableGrid, int columns) {
        int newRow = rowData.size() + 1;
        List<String> newRowValues = new ArrayList<>();
        for (int col = 0; col < columns; col++) {
            addCell(tableGrid, newRow, col, newRowValues);
        }
        rowData.add(newRowValues);
    }

    private void addColumn(GridPane tableGrid, int rows) {
        int newCol = columnTypes.size();
        columnTypes.add("Text");

        ComboBox<String> typeSelector = new ComboBox<>();
        typeSelector.getItems().addAll("Text", "Date Picker");
        typeSelector.setValue("Text");
        typeSelector.valueProperty().addListener((obs, oldVal, newVal) -> updateColumnType(tableGrid, newCol, newVal));
        tableGrid.add(typeSelector, newCol, 0);

        for (int row = 1; row <= rows; row++) {
            List<String> rowValues = rowData.get(row - 1);
            addCell(tableGrid, row, newCol, rowValues);
        }
    }

    private Node getNodeByRowColumnIndex(int row, int column, GridPane gridPane) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                return node;
            }
        }
        return null;
    }

    @Override
    public List<Node> getInputFields() {
        return List.of();
    }

    @Override
    public Document toDocument() {
        return new Document("_id", getId())
                .append("type", "table")
                .append("description", getDescription())
                .append("config", new Document("rowData", rowData))
                .append("createdAt", "2024-11-27T08:00:00Z")
                .append("lastUpdated", "2024-11-27T09:00:00Z");
    }

    @Override
    public void saveToDatabase() {

    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
