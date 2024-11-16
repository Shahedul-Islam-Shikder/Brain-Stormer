package brain.brainstormer.utils.seeds;

import org.bson.Document;

public class ComponentMetadata {
    private String name;
    private String description;
    private String category;
    private String defaultProperties;

    public ComponentMetadata(String name, String description, String category, String defaultProperties) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.defaultProperties = defaultProperties;
    }

    public Document toDocument() {
        return new Document("name", name)
                .append("description", description)
                .append("category", category)
                .append("defaultProperties", defaultProperties);
    }
}
