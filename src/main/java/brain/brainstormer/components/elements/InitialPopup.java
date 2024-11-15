package brain.brainstormer.components.elements;

import javafx.scene.control.Control;

public interface InitialPopup {
    /**
     * Creates a configuration pop-up for initializing the component's settings.
     */
    void showInitialPopup();

    /**
     * Returns the configured JavaFX control for this component.
     * @return the configured JavaFX control
     */
    Control render();
}
