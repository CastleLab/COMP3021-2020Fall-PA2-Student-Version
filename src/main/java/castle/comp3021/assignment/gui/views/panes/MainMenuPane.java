package castle.comp3021.assignment.gui.views.panes;

import castle.comp3021.assignment.gui.controllers.SceneManager;
import castle.comp3021.assignment.gui.views.BigButton;
import castle.comp3021.assignment.gui.views.BigVBox;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

public class MainMenuPane extends BasePane {
    @NotNull
    private final VBox container = new BigVBox();
    @NotNull
    private final Label title = new Label("Jeson Mor");
    @NotNull
    private final Button playButton = new BigButton("Play Game");

    @NotNull
    private final Button settingsButton = new BigButton("Settings / About ");
    @NotNull
    private final Button validationButtion = new BigButton("Validation");
    @NotNull
    private final Button quitButton = new BigButton("Quit");

    public MainMenuPane() {
        connectComponents();
        styleComponents();
        setCallbacks();
    }

    @Override
    void connectComponents() {
        container.getChildren().addAll(
                title,
                playButton,
                settingsButton,
                validationButtion,
                quitButton
        );
        this.setCenter(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void styleComponents() {
        title.getStyleClass().add("head-size");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void setCallbacks() {
        playButton.setOnAction(event -> {
            final var gamePane = SceneManager.getInstance().<GamePane>getPane(GamePane.class);
            gamePane.fillValues();
            SceneManager.getInstance().showPane(GamePane.class);
        });
        settingsButton.setOnAction(event -> SceneManager.getInstance().showPane(SettingPane.class));
        validationButtion.setOnAction(event -> SceneManager.getInstance().showPane(ValidationPane.class));
        quitButton.setOnAction(event -> Platform.exit());

    }

}
