package brain.brainstormer.components.essentials;

import brain.brainstormer.chess.ChessClient;
import brain.brainstormer.utils.Chessutils;
import brain.brainstormer.utils.SceneSwitcher;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
        dialog.setResizable(false);

        // Label for dialog title
        Label titleLabel = new Label("What would you like to do?");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");


        // Host and Join Game buttons
        Button hostButton = new Button("Host a Game");
        Button joinButton = new Button("Join a Game");

        // Set button styles
        hostButton.setStyle("-fx-background-color: #1E3A8A; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-border-radius: 10; -fx-background-radius: 10;");
        joinButton.setStyle("-fx-background-color: #1E3A8A; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-border-radius: 10; -fx-background-radius: 10;");

        // Text field for room code input
        TextField roomCodeField = new TextField();
        roomCodeField.setPromptText("Room Code");
        roomCodeField.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-prompt-text-fill: gray; -fx-padding: 10; -fx-border-radius: 10; -fx-background-radius: 10;");

        // Set button actions
        hostButton.setOnAction(e -> {
            System.out.println("Host a Game button clicked!");
            Chessutils.roomCode = roomCodeField.getText().trim();

            Platform.runLater(() -> {

                Stage stage = (Stage) hostButton.getScene().getWindow();
                SceneSwitcher.switchScene(stage, "/brain/brainstormer/chess-game.fxml", true);


            });


            dialog.close();

        });

        joinButton.setOnAction(e -> {
            System.out.println("Join a Game button clicked!");
            Chessutils.roomCode = roomCodeField.getText().trim();



            Platform.runLater(() -> {

                Stage stage = (Stage) hostButton.getScene().getWindow();
                SceneSwitcher.switchScene(stage, "/brain/brainstormer/chess-game.fxml", true);


            });
            dialog.close();
        });

        // Layout for buttons and room code field
        VBox layout = new VBox(15, titleLabel, hostButton, roomCodeField, joinButton);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 30; -fx-background-color: #1E1E1E;");

        // Set scene and show dialog
        Scene scene = new Scene(layout, 300, 400);
        dialog.setScene(scene);
        dialog.show();
    }


}