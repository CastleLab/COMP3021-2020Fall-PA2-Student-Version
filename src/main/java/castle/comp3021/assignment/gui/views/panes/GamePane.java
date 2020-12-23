package castle.comp3021.assignment.gui.views.panes;

import castle.comp3021.assignment.gui.FXJesonMor;
import castle.comp3021.assignment.gui.ViewConfig;
import castle.comp3021.assignment.gui.controllers.SceneManager;
import castle.comp3021.assignment.gui.views.BigButton;
import castle.comp3021.assignment.gui.views.BigVBox;
import castle.comp3021.assignment.gui.views.NumberTextField;
import castle.comp3021.assignment.protocol.Configuration;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class GamePane extends BasePane {
    @NotNull
    private final VBox container = new BigVBox();
    @NotNull
    private final Label title = new Label("Jeson Mor");
    @NotNull
    private final Button playButton = new BigButton("Play");
    @NotNull
    private final Button returnButton = new BigButton("Return");
    @NotNull
    private final Button useDefaultButton = new BigButton("Use Default");
    @NotNull
    private final Button isHumanPlayer1Button = new BigButton("");
    @NotNull
    private final Button isHumanPlayer2Button = new BigButton("");

    @NotNull
    private final NumberTextField sizeFiled = new NumberTextField("");


    private final BorderPane sizeBox = new BorderPane(null, null, sizeFiled, null, new Label("Size of Board:"));

    @NotNull
    private final NumberTextField numMovesProtectionField = new NumberTextField("");

    @NotNull
    private final BorderPane numMovesProtectionBox = new BorderPane(null, null, numMovesProtectionField, null, new Label("Protection Moves:"));


    private FXJesonMor fxJesonMor = null;

    public GamePane() {
        fillValues();
        connectComponents();
        styleComponents();
        setCallbacks();
    }

    @Override
    void connectComponents() {
        container.getChildren().addAll(
                title,
                sizeBox,
                numMovesProtectionBox,
                isHumanPlayer1Button,
                isHumanPlayer2Button,
                useDefaultButton,
                playButton,
                returnButton
        );
        this.setCenter(container);
    }

    @Override
    void styleComponents() {
        title.getStyleClass().add("head-size");
    }

    @Override
    void setCallbacks() {
        playButton.setOnAction(event -> validate(sizeFiled.getValue(), numMovesProtectionField.getValue()).
                ifPresentOrElse(msg -> {
            final var alertBox = new Alert(Alert.AlertType.ERROR);
            alertBox.setTitle("Error");
            alertBox.setHeaderText("Validation Failed");
            alertBox.setContentText(msg);
            alertBox.showAndWait();
        }, () -> {
            fxJesonMor = new FXJesonMor(new Configuration(sizeFiled.getValue(), globalConfiguration.getPlayers(), numMovesProtectionField.getValue()));

            startGame(fxJesonMor);
        }));

        useDefaultButton.setOnAction(event ->{
            sizeFiled.setText(String.valueOf(globalConfiguration.getSize()));
            numMovesProtectionField.setText(String.valueOf(globalConfiguration.getNumMovesProtection()));
            setChoiceButtons(globalConfiguration.isWhitePlayerHuman(), globalConfiguration.isBlackPlayerHuman());
        });

        isHumanPlayer1Button.setOnAction(event -> {
            globalConfiguration.setWhitePlayer(!globalConfiguration.isWhitePlayerHuman());
            isHumanPlayer1Button.setText("Player 1: " + (globalConfiguration.isWhitePlayerHuman() ? "Human" : "Computer"));
        });
        isHumanPlayer2Button.setOnAction(event -> {
            globalConfiguration.setBlackPlayer(!globalConfiguration.isBlackPlayerHuman());
            isHumanPlayer2Button.setText("Player 2: " + (globalConfiguration.isBlackPlayerHuman() ? "Human" : "Computer"));
        });

        returnButton.setOnAction(event -> SceneManager.getInstance().showPane(MainMenuPane.class));
    }

    void startGame(@NotNull FXJesonMor fxJesonMor) {
        final var gameplayPane = SceneManager.getInstance().<GamePlayPane>getPane(GamePlayPane.class);
        gameplayPane.initializeGame(fxJesonMor);
        SceneManager.getInstance().showPane(GamePlayPane.class);
    }

    void fillValues(){
        // set parameters as default, editable
        sizeFiled.setText(String.valueOf(globalConfiguration.getSize()));
        numMovesProtectionField.setText(String.valueOf(globalConfiguration.getNumMovesProtection()));
        setChoiceButtons(globalConfiguration.isWhitePlayerHuman(), globalConfiguration.isBlackPlayerHuman());

        // text fields are editable
        sizeFiled.setEditable(true);
        numMovesProtectionField.setEditable(true);
        isHumanPlayer1Button.setDisable(false);
        isHumanPlayer2Button.setDisable(false);

        // enable play and useDefault buttons
        playButton.setDisable(false);
        useDefaultButton.setDisable(false);
    }

    private void setChoiceButtons(boolean enableButton1, boolean enableButton2){
        isHumanPlayer1Button.setText("Player 1: " + (enableButton1 ? "Human" : "Computer"));
        isHumanPlayer2Button.setText("Player 2: " + (enableButton2 ? "Human" : "Computer"));
    }

    public static Optional<String> validate(int size, int numProtection) {
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
        return Optional.empty();
    }
}
