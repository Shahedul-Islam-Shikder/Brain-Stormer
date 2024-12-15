package brain.brainstormer.api;

import brain.brainstormer.utils.EnvUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.Gson;

import java.io.IOException;

public class GiphyApi {
    private static final String API_KEY = EnvUtil.getEnv("GIPHY_API_KEY"); // Replace with your Giphy API key
    private static final String BASE_URL = "https://api.giphy.com/v1/gifs/search?api_key=" + API_KEY + "&limit=1&q=";

    private ApiClient apiClient;

    public GiphyApi() {
        apiClient = new ApiClient(); // Initialize the ApiClient
    }

    public String searchGif(String query) {
        String endpoint = BASE_URL + query; // Construct the full API endpoint URL
        String jsonResponse;

        try {
            // Use GET method to fetch GIF data
            jsonResponse = apiClient.makeApiCall(endpoint, "GET");
        } catch (IOException e) {
            e.printStackTrace();
            return "Error fetching data from Giphy API.";
        }

        // Parse the response JSON using Gson
        JsonObject responseJson = new Gson().fromJson(jsonResponse, JsonObject.class);

        // Extract the "data" array
        JsonArray data = responseJson.getAsJsonArray("data");

        // If there's at least one result, extract the GIF URL
        if (data != null && data.size() > 0) {
            JsonObject firstGif = data.get(0).getAsJsonObject(); // Get the first GIF object
            JsonObject images = firstGif.getAsJsonObject("images");
            JsonObject originalImage = images.getAsJsonObject("original");
            return originalImage.get("url").getAsString(); // Return the URL of the original image
        }

        return "No GIF found for query: " + query;
    }


}
