package brain.brainstormer.utils.seeds;

import brain.brainstormer.config.DatabaseConnection;
import com.mongodb.client.MongoDatabase;

public class SeedRunner {
    public static void main(String[] args) {
        // Get the database instance from the DatabaseConnection singleton
        MongoDatabase database = DatabaseConnection.getInstance().getDatabase();

        // Initialize the seeder and seed the components
        ComponentSeeder seeder = new ComponentSeeder(database);
        seeder.seedComponents();

        // Close the database connection after seeding
        DatabaseConnection.getInstance().close();

        System.out.println("Seeding complete.");
    }
}
