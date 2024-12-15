package brain.brainstormer.api;

import brain.brainstormer.utils.EnvUtil;
import com.google.gson.JsonObject;
import com.google.gson.Gson;

import java.io.IOException;

public class WeatherApi {
    private static final String API_KEY = EnvUtil.getEnv("WEATHER_API_KEY"); // Ensure the API key is set correctly
    private static final String BASE_URL = "https://api.weatherapi.com/v1/current.json?key=" + API_KEY + "&q=";

    private ApiClient apiClient;

    public WeatherApi() {
        apiClient = new ApiClient(); // Initialize the ApiClient
    }

    public String getWeather(String cityName) {
        String endpoint = BASE_URL + cityName; // Construct the full API endpoint URL
        String jsonResponse;

        try {
            // Use GET method to fetch weather data
            jsonResponse = apiClient.makeApiCall(endpoint, "GET");

            // Parse the response JSON using Gson
            JsonObject responseJson = new Gson().fromJson(jsonResponse, JsonObject.class);

            // Check if the response contains an error
            if (responseJson.has("error")) {
                JsonObject error = responseJson.getAsJsonObject("error");
                return "Error: " + error.get("message").getAsString();
            }

            // Extract the current weather information from the response JSON
            JsonObject current = responseJson.getAsJsonObject("current");
            if (current == null) {
                return "Error: Current weather data not found.";
            }

            double temperature = current.get("temp_c").getAsDouble(); // Temperature in Celsius
            double humidity = current.get("humidity").getAsDouble();

            JsonObject condition = current.getAsJsonObject("condition");
            if (condition == null) {
                return "Error: Weather condition not found.";
            }

            String description = condition.get("text").getAsString(); // Weather condition (e.g., "Clear", "Cloudy")

            // Format and return the weather information
            return String.format("Weather in %s: %s, Temperature: %.2f°C, Humidity: %.2f%%",
                    cityName, description, temperature, humidity);

        } catch (IOException e) {
            e.printStackTrace();
            return "Error fetching data from WeatherAPI: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "An unexpected error occurred: " + e.getMessage();
        }
    }

    public String getTemperature(String cityName) {
        String endpoint = BASE_URL + cityName;
        String jsonResponse;

        try {
            // Fetch the weather data from the API
            jsonResponse = apiClient.makeApiCall(endpoint, "GET");

            // Parse the response JSON using Gson
            JsonObject responseJson = new Gson().fromJson(jsonResponse, JsonObject.class);

            // Check for errors in the response
            if (responseJson.has("error")) {
                JsonObject error = responseJson.getAsJsonObject("error");
                return "Error: " + error.get("message").getAsString();
            }

            // Extract the current temperature from the response JSON
            JsonObject current = responseJson.getAsJsonObject("current");
            if (current == null) {
                return "Error: Current temperature data not found.";
            }

            double temperature = current.get("temp_c").getAsDouble(); // Temperature in Celsius

            // Return the temperature as a formatted string
            return String.format("%.2f°C", temperature);

        } catch (IOException e) {
            e.printStackTrace();
            return "Error fetching data from WeatherAPI: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "An unexpected error occurred: " + e.getMessage();
        }
    }

    // Test the API call with a city name
    public static void main(String[] args) {
        WeatherApi weatherApi = new WeatherApi();
        String city = "London";  // Example city
        String weatherInfo = weatherApi.getWeather(city);
        System.out.println(weatherInfo);

        String temperature = weatherApi.getTemperature(city);
        System.out.println("Temperature: " + temperature);
    }
}
