package brain.brainstormer.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.File;
import java.io.IOException;

public class FileIOUtil {
    private static final String API_KEY = EnvUtil.getEnv("FILE_IO_API_KEY");
    private static final String BASE_URL = EnvUtil.getEnv("FILE_IO_BASE_URL");

    private static final OkHttpClient client = new OkHttpClient();

    public static String uploadFile(File file) throws IOException {
        // Create a multipart body for the file
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, MediaType.parse("application/octet-stream")))
                .addFormDataPart("expires", "14d") // Set file expiration to 14 days
                .build();

        // Build the request
        Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(requestBody)
                .build();

        // Execute the request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // Parse the JSON response using Gson
            JsonObject responseObject = JsonParser.parseString(response.body().string()).getAsJsonObject();
            if (responseObject.get("success").getAsBoolean()) {
                return responseObject.get("link").getAsString(); // Return the file URL
            } else {
                throw new IOException("File upload failed: " + responseObject.get("message").getAsString());
            }
        }
    }

    public static boolean deleteFile(String fileUrl) {
        // file.io automatically deletes files after expiration; no explicit delete API exists.
        // Implement custom server logic here if needed.
        System.out.println("Files expire automatically on file.io.");
        return true;
    }
}
