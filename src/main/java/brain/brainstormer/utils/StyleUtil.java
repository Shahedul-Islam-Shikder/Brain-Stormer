package brain.brainstormer.utils;

import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.File;

public class StyleUtil {
    private static final String STYLESHEET_PATH = "/styles/style.css";

    public static void applyStylesheet(Scene scene) {
        //System.out.println(StyleUtil.class.getResource("/styles/style.css"));

        try {
            scene.getStylesheets().add(StyleUtil.class.getResource(STYLESHEET_PATH).toExternalForm());
        }
        catch (Exception e){
            System.out.println(""+e.getMessage());
        }

    }

    public static void applyStylesheet(Parent parent) {
        parent.getStylesheets().add(StyleUtil.class.getResource(STYLESHEET_PATH).toExternalForm());
    }
}
