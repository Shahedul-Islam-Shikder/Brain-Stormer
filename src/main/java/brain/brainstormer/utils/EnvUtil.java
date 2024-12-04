package brain.brainstormer.utils;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvUtil {
    private static final Dotenv dotenv = Dotenv.load(); // Automatically loads .env from the root directory

    public static String getEnv(String key) {
        return dotenv.get(key); // Get the value of the environment variable
    }
}
