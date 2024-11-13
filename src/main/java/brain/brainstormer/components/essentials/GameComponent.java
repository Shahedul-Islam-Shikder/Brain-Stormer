package brain.brainstormer.components.essentials;

import brain.brainstormer.chess.ChessClient;
import brain.brainstormer.utils.SceneSwitcher;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GameComponent {

    Button playButton;

    private ChessClient chessClient; // ChessClient to manage communication

    // Method to load the game view and show the Chess logo with options
    public void loadGameView(VBox mainContentArea) {
        // Clear the current content
        mainContentArea.getChildren().clear();

        // Create a button with the Chess logo image
        playButton = new Button();

        // Load the Chess logo image
        Image image = new Image(getClass().getResourceAsStream("/brain/brainstormer/icons/chess-icon.png"));
        ImageView chessLogo = new ImageView(image);
        chessLogo.setFitHeight(150); // Adjust size as needed
        chessLogo.setFitWidth(150); // Adjust size as needed

        // Set the image inside the button
        playButton.setGraphic(chessLogo);

        // Style the button
        playButton.setStyle(
                "-fx-background-color: #121212;" +  // Button background color
                        "-fx-background-radius: 15;" +      // Rounded corners
                        "-fx-padding: 20;" +                 // Padding around the image
                        "-fx-border-color: #1E1E1E;" +      // Border color
                        "-fx-border-radius: 15;"            // Rounded border
        );

        // Add click event to the button to show game options
        playButton.setOnAction(event -> showGameOptionsDialog());

        // Create the label for the title "Chess"
        Label chessTitle = new Label("Chess");
        chessTitle.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");

        // Add the button and label to the main content area
        mainContentArea.getChildren().addAll(playButton, chessTitle);
    }

    // Method to display the game options dialog
    private void showGameOptionsDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Game Options");
        dialog.initModality(Modality.APPLICATION_MODAL);

        // Buttons for hosting or joining a game
        Button hostButton = new Button("Host a Game");
        Button joinButton = new Button("Join a Game");

        // Set actions for the buttons
        hostButton.setOnAction(e -> {
            // Host game: Create a new room
            System.out.println("Host a Game button clicked!");

            // Switch scene to chess game
            Stage stage = (Stage) playButton.getScene().getWindow();
            SceneSwitcher.switchScene(stage, "/brain/brainstormer/chess-game.fxml", true);

            // Close the dialog
            dialog.close();
        });

        joinButton.setOnAction(e -> {
            // Join game: Join a room with a code
            System.out.println("Host a Game button clicked!");

            // Switch scene to chess game
            Stage stage = (Stage) playButton.getScene().getWindow();
            SceneSwitcher.switchScene(stage, "/brain/brainstormer/chess-game.fxml", true);

            // Close the dialog
            dialog.close();
        });

        // Add buttons to layout
        VBox layout = new VBox(10, hostButton, joinButton);
        layout.setStyle("-fx-padding: 10;");
        dialog.setScene(new Scene(layout));

        // Show the dialog
        dialog.show();
    }

}
