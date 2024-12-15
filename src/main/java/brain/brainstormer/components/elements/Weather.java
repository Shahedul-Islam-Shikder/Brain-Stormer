package brain.brainstormer.components.elements;

import brain.brainstormer.api.WeatherApi;
import brain.brainstormer.components.core.CoreComponent;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.bson.Document;

import java.util.Date;

public class Weather extends CoreComponent {
    private String cityName;
    private String weatherInfo;
    private String temperature;
    private Label weatherLabel;
    private Label temperatureLabel;

    public Weather(String id, String description, String cityName) {
        super(id, "weather", description);
        this.cityName = cityName != null ? cityName : "London"; // Default to "London"
        this.weatherInfo = "Fetching weather...";
        this.temperature = "Loading..."; // Placeholder until weather is fetched
    }

    @Override
    public Node render() {
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: #333333; -fx-padding: 20px; -fx-border-radius: 10px; -fx-background-radius: 20px; -fx-max-width: 600px; -fx-min-width: 250px;");

        // Fetching weather information
        WeatherApi weatherApi = new WeatherApi();
        weatherInfo = weatherApi.getWeather(cityName); // Assuming getWeather() returns a string with weather info
        temperature = weatherApi.getTemperature(cityName); // Assuming this method exists

        // Create temperature label (bolder and bigger font)
        temperatureLabel = new Label(temperature);
        temperatureLabel.setStyle("-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold;");

        // Create weather display label (city and weather info)
        weatherLabel = new Label(cityName + "\n" + weatherInfo);
        weatherLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: normal;");



        // Create search bar and button to change city
        HBox searchContainer = new HBox(10);
        searchContainer.setAlignment(Pos.CENTER);

        TextField cityField = new TextField(cityName);
        cityField.setPromptText("Enter city name");
        cityField.setStyle("-fx-background-color: #2c2f33; -fx-text-fill: white; -fx-prompt-text-fill: #888; -fx-padding: 10px; -fx-border-radius: 5px; -fx-font-size: 14px;");

        Button searchButton = new Button("Get Weather");
        searchButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px; -fx-border-radius: 5px;");
        searchButton.setOnAction(event -> {
            cityName = cityField.getText();
            weatherInfo = weatherApi.getWeather(cityName);
            temperature = weatherApi.getTemperature(cityName);
            weatherLabel.setText(cityName + "\n" + weatherInfo);
            temperatureLabel.setText(temperature);
        });

        searchContainer.getChildren().addAll(cityField, searchButton);

        // Add weather information and search container to the main container
        container.getChildren().addAll(temperatureLabel, weatherLabel, searchContainer);

        // Replacing old action buttons with the unified CheckBox-style buttons
        HBox actionButtons = createActionButtons();

        container.getChildren().addAll(actionButtons);

        applyGlobalComponentStyles(container);
        return container;
    }

    @Override
    public void saveToDatabase() {

    }

    private HBox createActionButtons() {
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER_LEFT);
        buttonContainer.getStyleClass().add("button-container");





        // Delete button with FontAwesome icon
        FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        deleteIcon.getStyleClass().add("delete-icon");

        Button deleteButton = new Button("", deleteIcon);
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
                .append("type", "weather")
                .append("config", new Document("cityName", cityName)
                        .append("weatherInfo", weatherInfo)
                        .append("temperature", temperature)
                        .append("description", getDescription()))
                .append("createdAt", new Date())
                .append("lastUpdated", new Date());
    }
}
