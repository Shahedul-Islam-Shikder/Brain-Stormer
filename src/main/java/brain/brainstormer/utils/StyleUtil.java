package brain.brainstormer.utils;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class StyleUtil {
    // Centralized constants for stylesheets
    public static final String GLOBAL_STYLESHEET_PATH = "/styles/base/global.css";
    public static final String COMPONENT_GLOBAL_STYLESHEET_PATH = "/styles/base/component-global.css";

    // Apply global styles (always used)
    public static void applyGlobalStylesheet(Scene scene) {
        scene.getStylesheets().add(StyleUtil.class.getResource(GLOBAL_STYLESHEET_PATH).toExternalForm());
    }

    public static void applyGlobalStylesheet(Parent parent) {
        parent.getStylesheets().add(StyleUtil.class.getResource(GLOBAL_STYLESHEET_PATH).toExternalForm());
    }

    // Apply component-global.css
    public static void applyComponentGlobalStylesheet(Scene scene) {
        scene.getStylesheets().add(StyleUtil.class.getResource(COMPONENT_GLOBAL_STYLESHEET_PATH).toExternalForm());
    }

    public static void applyComponentGlobalStylesheet(Parent parent) {
        parent.getStylesheets().add(StyleUtil.class.getResource(COMPONENT_GLOBAL_STYLESHEET_PATH).toExternalForm());
    }

    // Apply a specific stylesheet (e.g., for a component or dialog)
    public static void applyCustomStylesheet(Scene scene, String stylesheetPath) {
        scene.getStylesheets().add(StyleUtil.class.getResource(stylesheetPath).toExternalForm());
    }

    public static void applyCustomStylesheet(Parent parent, String stylesheetPath) {
        parent.getStylesheets().add(StyleUtil.class.getResource(stylesheetPath).toExternalForm());
    }

    // Apply custom stylesheet to a Node
    public static void applyCustomStylesheet(Node node, String stylesheetPath) {
        if (node instanceof Parent parent) {
            parent.getStylesheets().add(StyleUtil.class.getResource(stylesheetPath).toExternalForm());
        } else if (node.getScene() != null) {
            node.getScene().getStylesheets().add(StyleUtil.class.getResource(stylesheetPath).toExternalForm());
        } else {
            System.err.println("Cannot apply stylesheet: Node is not a Parent and is not attached to a Scene.");
        }
    }
}
