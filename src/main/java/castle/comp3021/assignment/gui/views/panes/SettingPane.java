package castle.comp3021.assignment.gui.views.panes;

import castle.comp3021.assignment.gui.DurationTimer;
import castle.comp3021.assignment.gui.ViewConfig;
import castle.comp3021.assignment.gui.controllers.AudioManager;
import castle.comp3021.assignment.gui.controllers.SceneManager;
import castle.comp3021.assignment.gui.views.BigButton;
import castle.comp3021.assignment.gui.views.BigVBox;
import castle.comp3021.assignment.gui.views.NumberTextField;
import castle.comp3021.assignment.gui.views.SideMenuVBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SettingPane extends BasePane {
    @NotNull
    private final Label title = new Label("Jeson Mor <Game Setting>");
    @NotNull
    private final Button saveButton = new BigButton("Save");
    @NotNull
    private final Button returnButton = new BigButton("Return");
    @NotNull
    private final Button isHumanPlayer1Button = new BigButton("Player 1: " + (globalConfiguration.isWhitePlayerHuman() ? "Human" : "Computer"));
    @NotNull
    private final Button isHumanPlayer2Button = new BigButton("Player 2: " + (globalConfiguration.isBlackPlayerHuman() ? "Human" : "Computer"));
    @NotNull
    private final Button toggleSoundButton = new BigButton("Sound FX: Enabled");

    @NotNull
    private final VBox leftContainer = new SideMenuVBox();

    @NotNull
    private final NumberTextField sizeFiled = new NumberTextField(String.valueOf(globalConfiguration.getSize()));

    @NotNull
    private final BorderPane sizeBox = new BorderPane(null, null, sizeFiled, null, new Label("Board size"));

    @NotNull
    private final NumberTextField durationField = new NumberTextField(String.valueOf(DurationTimer.getDefaultEachRound()));
    @NotNull
    private final BorderPane durationBox = new BorderPane(null, null, durationField, null, new Label("Max Duration (s)"));

    @NotNull
    private final NumberTextField numMovesProtectionField = new NumberTextField(String.valueOf(globalConfiguration.getNumMovesProtection()));
    @NotNull
    private final BorderPane numMovesProtectionBox = new BorderPane(null, null, numMovesProtectionField, null, new Label("Steps of protection"));

    @NotNull
    private final VBox centerContainer = new BigVBox();
    @NotNull
    private final TextArea infoText = new TextArea(ViewConfig.getAboutText());


    public SettingPane() {
        connectComponents();
        styleComponents();
        setCallbacks();
    }

    @Override
    void connectComponents() {
        leftContainer.getChildren().addAll(
                title,
                sizeBox,
                numMovesProtectionBox,
                durationBox,
                isHumanPlayer1Button,
                isHumanPlayer2Button,
                toggleSoundButton,
                saveButton,
                returnButton
        );
        centerContainer.getChildren().addAll(
                infoText
        );
        this.setLeft(leftContainer);
        this.setCenter(centerContainer);
    }

    @Override
    void styleComponents() {
        infoText.getStyleClass().add("text-area");
        infoText.setEditable(false);
        infoText.setWrapText(true);
        infoText.setPrefHeight(ViewConfig.HEIGHT);
    }

    @Override
    void setCallbacks() {
        saveButton.setOnAction(event -> validate(sizeFiled.getValue(), numMovesProtectionField.getValue(),
                durationField.getValue()).ifPresentOrElse(msg -> {
                    final var alertBox = new Alert(Alert.AlertType.ERROR);
                    alertBox.setTitle("Error!");
                    alertBox.setHeaderText("Validation Failed");
                    alertBox.setContentText(msg);
                    alertBox.showAndWait();
                },
                () -> returnToMainMenu(true)));

        isHumanPlayer1Button.setOnAction(event -> {
            globalConfiguration.setWhitePlayer(!globalConfiguration.isWhitePlayerHuman());
            isHumanPlayer1Button.setText("Player 1: " + (globalConfiguration.isWhitePlayerHuman() ? "Human" : "Computer"));
        });
        isHumanPlayer2Button.setOnAction(event -> {
            globalConfiguration.setBlackPlayer(!globalConfiguration.isBlackPlayerHuman());
            isHumanPlayer2Button.setText("Player 2: " + (globalConfiguration.isBlackPlayerHuman() ? "Human" : "Computer"));
        });
        returnButton.setOnAction(event -> returnToMainMenu(false));
        toggleSoundButton.setOnAction(event -> {
            final var audio = AudioManager.getInstance();
            audio.setEnabled(!audio.isEnabled());
            toggleSoundButton.setText("Sound FX: " + (AudioManager.getInstance().isEnabled() ? "Enabled" : "Disabled"));
//            fillValues();
        });

    }

    /**
     * Fill in the default values for all editable fields.
     */
    private void fillValues() {
        toggleSoundButton.setText("Sound FX: " + (AudioManager.getInstance().isEnabled() ? "Enabled" : "Disabled"));
        isHumanPlayer1Button.setText("Player 1: " + (globalConfiguration.isWhitePlayerHuman() ? "Human" : "Computer"));
        isHumanPlayer2Button.setText("Player 2: " + (globalConfiguration.isBlackPlayerHuman() ? "Human" : "Computer"));

        sizeFiled.clear();
        numMovesProtectionField.clear();
        durationField.clear();

        sizeFiled.replaceSelection(String.valueOf(globalConfiguration.getSize()));
        numMovesProtectionField.replaceSelection(String.valueOf(globalConfiguration.getNumMovesProtection()));
        durationField.replaceSelection(String.valueOf(DurationTimer.getDefaultEachRound()));
    }

    /**
     * Switches back to the {@link MainMenuPane}.
     *
     * @param writeBack Whether to save the values present in the text fields to their respective classes.
     */
    private void returnToMainMenu(final boolean writeBack) {
        if (writeBack) {
            globalConfiguration.setSize(sizeFiled.getValue());
            globalConfiguration.setNumMovesProtection(numMovesProtectionField.getValue());
            DurationTimer.setDefaultEachRound(durationField.getValue());
        }
        fillValues();
        SceneManager.getInstance().showPane(MainMenuPane.class);
    }

    public static Optional<String> validate(int size, int numProtection, int duration) {
        if (size < 3) {
            return Optional.of(ViewConfig.MSG_BAD_SIZE_NUM);
        }
        if (size % 2 != 1) {
            return Optional.of(ViewConfig.MSG_ODD_SIZE_NUM);
        }
        if (size > 26) {
            return Optional.of(ViewConfig.MSG_UPPERBOUND_SIZE_NUM);
        }

        if (numProtection < 0){
            return Optional.of(ViewConfig.MSG_NEG_PROT);
        }

        if (duration <= 0){
            return Optional.of(ViewConfig.MSG_NEG_DURATION);
        }
        return Optional.empty();
    }
}
