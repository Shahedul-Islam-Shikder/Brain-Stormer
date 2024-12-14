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
import javafx.scene.media.AudioClip;
import javafx.util.Duration;
import org.bson.Document;

public class Timer extends CoreComponent {
    private long remainingTime; // in milliseconds
    private boolean isRunning;
    private transient Timeline timeline; // Non-serializable timeline for animation

    // Constructor
    public Timer(String id, String description, long remainingTime, boolean isRunning) {
        super(id, "timer", description != null ? description : "Timer");
        this.remainingTime = remainingTime;
        this.isRunning = isRunning;
    }

    @Override
    public Node render() {
        // Create label to display remaining time
        Label timeLabel = new Label(formatTime(remainingTime));
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
        HBox buttonContainer = new HBox(10, startButton, stopButton, resetButton);
        buttonContainer.setAlignment(Pos.CENTER);

        VBox secondaryContainer = new VBox(10, timeLabel, buttonContainer);
        secondaryContainer.setAlignment(Pos.CENTER);

        VBox mainContainer = new VBox(10, secondaryContainer);
        applyGlobalComponentStyles(mainContainer);

        return mainContainer;
    }


    private void start(Label timeLabel) {
        if (isRunning) return;

        isRunning = true;
        timeline = new Timeline(
                new KeyFrame(Duration.millis(1000), e -> {
                    remainingTime -= 1000;
                    timeLabel.setText(formatTime(remainingTime));

                    // Check if time is up and play sound (if needed)
                    if (remainingTime <= 0) {
                        stop();
                        // You can add sound here
                        playSound();
                    }
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void stop() {
        isRunning = false;
        if (timeline != null) {
            timeline.stop();
        }
    }

    private long initialDuration = remainingTime; // Store initial duration

    private void reset(Label timeLabel) {
        remainingTime = initialDuration; // Reset to initial duration
        timeLabel.setText(formatTime(remainingTime));
        stop(); // Ensure the timer stops when reset
    }

    private String formatTime(long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void playSound() {
        // Placeholder for the sound effect
        // Use something like Java's AudioClip or any library to play a sound
        System.out.println("Time's up! Play sound here.");
    }

    @Override
    public Document toDocument() {
        return new Document("_id", getId())
                .append("type", "timer")
                .append("config", new Document("remainingTime", remainingTime)
                        .append("isRunning", isRunning)
                        .append("description", getDescription()));
    }

    @Override
    public void saveToDatabase() {
        //no need
    }
}
