package castle.comp3021.assignment.gui.views.panes;

import castle.comp3021.assignment.gui.FXJesonMor;
import castle.comp3021.assignment.gui.ViewConfig;
import castle.comp3021.assignment.gui.controllers.AudioManager;
import castle.comp3021.assignment.gui.controllers.SceneManager;
import castle.comp3021.assignment.gui.views.BigButton;
import castle.comp3021.assignment.gui.views.BigVBox;
import castle.comp3021.assignment.protocol.Configuration;
import castle.comp3021.assignment.protocol.MoveRecord;
import castle.comp3021.assignment.protocol.Place;
import castle.comp3021.assignment.protocol.Player;
import castle.comp3021.assignment.protocol.exception.InvalidConfigurationError;
import castle.comp3021.assignment.protocol.exception.InvalidGameException;
import castle.comp3021.assignment.protocol.io.Deserializer;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Thread.sleep;

public class ValidationPane extends BasePane{
    @NotNull
    private final VBox leftContainer = new BigVBox();
    @NotNull
    private final BigVBox centerContainer = new BigVBox();
    @NotNull
    private final Label title = new Label("Jeson Mor");
    @NotNull
    private final Label Explanation = new Label("Upload and validation the game history.");
    @NotNull
    private final Button loadButton = new BigButton("Load file");
    @NotNull
    private final Button validationButton = new BigButton("Validate");
    @NotNull
    private final Button replayButton = new BigButton("Replay");
    @NotNull
    private final Button returnButton = new BigButton("Return");

    private Canvas gamePlayCanvas = new Canvas();

    private FXJesonMor loadedGame;
    private Configuration loadedConfiguration;

    private Integer[] storedScores;
    private Place loadedcentralPlace;

    private ArrayList<MoveRecord> moveRecords = new ArrayList<>();

//    private List<InvalidConfigurationError> encounteredExceptions = new LinkedList<InvalidConfigurationError>();

    private BooleanProperty isValid = new SimpleBooleanProperty(false);
    private BooleanProperty isDisplay = new SimpleBooleanProperty(false);


    public ValidationPane() {
        connectComponents();
        styleComponents();
        setCallbacks();
        replayButton.setDisable(true);
        validationButton.setDisable(true);
    }

    @Override
    void connectComponents() {
        leftContainer.getChildren().addAll(
                title,
                Explanation,
                loadButton,
                validationButton,
                replayButton,
                returnButton
        );
        centerContainer.getChildren().addAll(
                gamePlayCanvas
        );
        this.setLeft(leftContainer);
        this.setCenter(centerContainer);
    }

    @Override
    void styleComponents() {
        title.getStyleClass().add("head-size");
    }

    @Override
    void setCallbacks() {
        loadButton.setOnAction(event -> {
            replayButton.setDisable(true);
            final var loaded = loadFromFile();
            if (loaded){
                validationButton.setDisable(false);
            }
        });
        validationButton.setOnAction(event -> onClickValidationButton());

        returnButton.setOnAction(event -> returnToMainMenu());

        isValid.addListener((observable, oldValue, newValue) -> {
            if (newValue){
                replayButton.setDisable(false);
                validationButton.setDisable(true);
            }
        });
        replayButton.setOnAction(event -> onClickReplayButton());
    }

    private boolean loadFromFile() {
        final var targetDir = getTargetLoadFile();

        if (targetDir != null){
            try{
                Deserializer deserializer = new Deserializer(targetDir.toPath());
                deserializer.parseGame();
                loadedConfiguration = deserializer.getLoadedConfiguration();

                storedScores = deserializer.getStoredScores();
                moveRecords = deserializer.getMoveRecords();
                loadedcentralPlace = deserializer.getCentralPlace();

                return true;
            } catch (InvalidConfigurationError | FileNotFoundException | InvalidGameException e) {
                showErrorConfiguration(e.getMessage());
//                e.printStackTrace();
            }
        }
        return false;
    }

    private void onClickValidationButton(){
        if (loadedConfiguration == null){
            showErrorMsg();
            return;
        }
        if (checkConfiguration()){
            if (checkGameInitialization()){
                if (checkMoves()){
                    if (checkFinalScores()){
                        passValidationWindow();
                        isValid.set(true);
                    }
                    else{
                        loadedGame = null;
                        loadedConfiguration = null;
                        storedScores = null;
                        moveRecords = null;
                    }
                }
            }
        }
    }

    private void onClickReplayButton(){
        if (isDisplay.get()){
            return;
        }

        FXJesonMor newJesonMor = new FXJesonMor(loadedConfiguration);
        gamePlayCanvas.setWidth(newJesonMor.getConfiguration().getSize() * ViewConfig.PIECE_SIZE);
        gamePlayCanvas.setHeight(newJesonMor.getConfiguration().getSize() * ViewConfig.PIECE_SIZE);

        newJesonMor.renderBoard(gamePlayCanvas);

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                for (var moveRecord : moveRecords) {
                    if (!isValid.get()) {
                        break;
                    }

                    Platform.runLater(() -> {
                        newJesonMor.movePiece(moveRecord.getMove());
                        newJesonMor.renderBoard(gamePlayCanvas);
                        AudioManager.getInstance().playSound(AudioManager.SoundRes.PLACE);
                    });

                    try {
                        sleep(1000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
                return true;
            }
        };

        Thread t = new Thread(task);
        t.setDaemon(true);
        isDisplay.set(true);
        t.start();
    }

    private boolean checkConfiguration(){
        try {
            loadedConfiguration.validateConfiguration();
        } catch (InvalidConfigurationError ex){
            showErrorConfiguration(ex.getMessage());
            return false;
        }
        if (checkCentralPlace()){
            return  true;
        } else {
            showErrorConfiguration("Invalid Central Place, should be " +
                    loadedConfiguration.getCentralPlace().toString() +
                    " but get " + loadedcentralPlace.toString());
            return false;
        }
    }

    private boolean checkCentralPlace(){
        return loadedConfiguration.getCentralPlace().equals(loadedcentralPlace);
    }
    private boolean checkGameInitialization(){
        try {
            loadedGame = new FXJesonMor(loadedConfiguration);
            return true;
        } catch (InvalidConfigurationError e){
            showErrorConfiguration(e.getMessage());
            return false;
        }
    }

    private boolean checkMoves(){
        // check for each move, whether they break rules
        String moveResult = null;
        Player winner = null;
        for (int i = 0; i < moveRecords.size(); i++){
            var curPlayer =  moveRecords.get(i).getPlayer();
            var curMove = moveRecords.get(i).getMove();
            moveResult = curPlayer.validateMove(loadedGame, curMove);

            try {
                loadedGame.movePiece(curMove);
                loadedGame.updateScore(loadedGame.getCurrentPlayer(),loadedGame.getPiece(curMove.getSource()), curMove);
                loadedGame.increaseNumMove();
                winner = loadedGame.getWinner(curPlayer,loadedGame.getPiece(curMove.getSource()), curMove);
            } catch (Exception e){
                moveResult = e.getMessage();
            }

            if (moveResult != null) {break;}
            if (winner != null && i < moveRecords.size() - 1){
                moveResult = "Winner achieved before move record ends.";
                break;
            }
        }

        if (moveResult == null){
            return true;
        } else{
//                encounteredExceptions.add(new InvalidConfigurationError(moveResult));
            showErrorConfiguration(moveResult);
            return false;
        }
    }

    private boolean checkFinalScores(){
        boolean checkResult = true;
        for (int i=0; i < loadedGame.getConfiguration().getPlayers().length; i++){
            if (loadedGame.getConfiguration().getPlayers()[i].getScore() != storedScores[i]){
                checkResult = false;
                showErrorConfiguration(String.format("Player %s's score was incorrect! Recorded: %d, should be %d",
                        loadedGame.getConfiguration().getPlayers()[i].getName(),
                        storedScores[i],
                        loadedGame.getConfiguration().getPlayers()[i].getScore()));

                System.out.printf("Player %s's score was incorrect! Recorded: %d, should be %d%n",
                        loadedGame.getConfiguration().getPlayers()[i].getName(),
                        storedScores[i],
                        loadedGame.getConfiguration().getPlayers()[i].getScore()
                        );
            }
        }
        return checkResult;
    }

    private void showErrorConfiguration(String errorMsg){
        final var alertBox = new Alert(Alert.AlertType.ERROR);
        alertBox.setTitle("Invalid configuration or game process!");
        alertBox.setHeaderText("Due to following reason(s):");
        alertBox.setContentText(errorMsg);
        alertBox.showAndWait();
    }

    private void showErrorMsg(){
        final var alertBox = new Alert(Alert.AlertType.ERROR);
        alertBox.setTitle("Error!");
        alertBox.setContentText("You haven't loaded a record, Please load first.");
        alertBox.showAndWait();
    }

    private void returnToMainMenu(){
        loadedGame = null;
        loadedConfiguration = null;
        storedScores = null;
        moveRecords = null;
        gamePlayCanvas.setHeight(0);
        gamePlayCanvas.setWidth(0);

        loadButton.setDisable(false);
        validationButton.setDisable(true);
        replayButton.setDisable(true);

        isValid.set(false);
        isDisplay.set(false);

        SceneManager.getInstance().showPane(MainMenuPane.class);
    }

    private void passValidationWindow(){
        final var box = new Alert(Alert.AlertType.CONFIRMATION);
        box.setTitle("Confirm");
        box.setHeaderText("Pass validation!");
        box.getButtonTypes().setAll(ButtonType.OK);
        box.showAndWait();

    }



    /**
     * Prompts the user for the file to load.
     * <p>
     * Hint:
     * Use {@link FileChooser} and {@link FileChooser#setSelectedExtensionFilter(FileChooser.ExtensionFilter)}.
     *
     * @return {@link File} to load, or {@code null} if the operation is canceled.
     */
    @Nullable
    private File getTargetLoadFile() {
        final var chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Map File", Collections.singletonList("*.map")));

        return chooser.showOpenDialog(null);
    }

}
