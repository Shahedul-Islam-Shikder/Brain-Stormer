package brain.brainstormer.components.elements;

public abstract class Components {
    private String id;            // Unique identifier for the component
    private String name;          // Display name of the component
    private String description;   // Description of the component

    // Constructor
    public Components(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Abstract method to render the component
    public abstract javafx.scene.control.Control render();

    // Getters and Setters for common fields
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Method to display the component's information (for debugging purposes)
    public void displayInfo() {
        System.out.println("Component ID: " + id);
        System.out.println("Name: " + name);
        System.out.println("Description: " + description);
    }
}
