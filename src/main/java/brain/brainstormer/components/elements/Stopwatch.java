package brain.brainstormer.components.elements;

import brain.brainstormer.components.core.CoreComponent;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.bson.Document;

public class Stopwatch extends CoreComponent {
    private long elapsedTime; // in milliseconds
    private boolean isRunning;
    private transient Timeline timeline; // Non-serializable timeline for animation

    public Stopwatch(String id, String description, long elapsedTime, boolean isRunning) {
        super(id, "stopwatch", description != null ? description : "Stopwatch");
        this.elapsedTime = elapsedTime;
        this.isRunning = isRunning;
    }

    @Override
    public Node render() {
        // Create label to display elapsed time
        Label timeLabel = new Label(formatTime(elapsedTime));
        timeLabel.setStyle("-fx-font-size: 24px;");

        // Create control buttons
        Button startButton = new Button("Start");
        startButton.getStyleClass().add("button-primary");
        Button stopButton = new Button("Stop");
        stopButton.getStyleClass().add("button-danger");
        Button resetButton = new Button("Reset");
        resetButton.getStyleClass().add("button-primary");

        // Add button actions
        startButton.setOnAction(e -> start(timeLabel));
        stopButton.setOnAction(e -> stop());
        resetButton.setOnAction(e -> reset(timeLabel));

        // Arrange buttons and label
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER);

        FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        deleteIcon.getStyleClass().add("delete-icon");

        Button deleteButton = new Button("", deleteIcon); // Icon-only button
        deleteButton.setOnAction(event -> {
            System.out.println("Deleting Heading component with ID: " + getId());
            delete(); // Call the delete method
        });

        buttonContainer.getChildren().addAll(startButton, stopButton, resetButton);

        VBox secondaryContainer = new VBox(10, timeLabel, buttonContainer);
        secondaryContainer.setAlignment(Pos.CENTER);

        VBox deleteContainer = new VBox(deleteButton);
        deleteContainer.setAlignment(Pos.BOTTOM_LEFT);
        deleteContainer.getStyleClass().add("button-container");

        VBox mainContainer = new VBox(10,secondaryContainer,deleteContainer);
        applyGlobalComponentStyles(mainContainer);

        return mainContainer;
    }

    private void start(Label timeLabel) {
        if (isRunning) return;

        isRunning = true;
        timeline = new Timeline(
                new KeyFrame(Duration.millis(100), e -> {
                    elapsedTime += 100;
                    timeLabel.setText(formatTime(elapsedTime));
                })
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void stop() {
        isRunning = false;
        if (timeline != null) {
            timeline.stop();
        }
    }

    private void reset(Label timeLabel) {
        elapsedTime = 0;
        timeLabel.setText(formatTime(elapsedTime));
    }

    private String formatTime(long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public Document toDocument() {
        return new Document("_id", getId())
                .append("type", "stopwatch")
                .append("config", new Document("elapsedTime", elapsedTime)
                        .append("isRunning", isRunning)
                        .append("description", getDescription()));
    }

    @Override
    public void saveToDatabase() {
        //no need
    }
}
