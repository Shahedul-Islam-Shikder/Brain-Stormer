package brain.brainstormer.api;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class ApiClient {

    public String makeApiCall(String endpoint, String method) throws IOException {
        return makeApiCall(endpoint, method, null);
    }

    // Method to support different HTTP methods and custom headers or body (for POST, PUT, etc.)
    public String makeApiCall(String endpoint, String method, String requestBody) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        // Handle POST or PUT requests (with request body)
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
            connection.setDoOutput(true);
            if (requestBody != null) {
                byte[] input = requestBody.getBytes("utf-8");
                connection.getOutputStream().write(input, 0, input.length);
            }
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Error: " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

}
