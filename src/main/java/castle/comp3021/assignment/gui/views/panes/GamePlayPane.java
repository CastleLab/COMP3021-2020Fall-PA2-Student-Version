package castle.comp3021.assignment.gui.views.panes;

import castle.comp3021.assignment.gui.DurationTimer;
import castle.comp3021.assignment.gui.FXJesonMor;
import castle.comp3021.assignment.gui.ViewConfig;
import castle.comp3021.assignment.gui.controllers.AudioManager;
import castle.comp3021.assignment.gui.controllers.Renderer;
import castle.comp3021.assignment.gui.controllers.SceneManager;
import castle.comp3021.assignment.gui.views.BigButton;
import castle.comp3021.assignment.gui.views.BigVBox;
import castle.comp3021.assignment.gui.views.GameplayInfoPane;
import castle.comp3021.assignment.gui.views.SideMenuVBox;
import castle.comp3021.assignment.player.ConsolePlayer;
import castle.comp3021.assignment.player.RandomPlayer;
import castle.comp3021.assignment.protocol.Configuration;
import castle.comp3021.assignment.protocol.Move;
import castle.comp3021.assignment.protocol.Place;
import castle.comp3021.assignment.protocol.Player;
import castle.comp3021.assignment.protocol.io.Serializer;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;

public class GamePlayPane extends BasePane {
    @NotNull
    private final HBox topBar = new HBox(20);
    @NotNull
    private final SideMenuVBox leftContainer = new SideMenuVBox();
    @NotNull
    private final Label title = new Label("Jeson Mor");
    @NotNull
    private final Text parameterText = new Text();
    @NotNull
    private final BigButton returnButton = new BigButton("Return");
    @NotNull
    private final BigButton saveRecordButton = new BigButton("Save");
    @NotNull
    private final BigButton startButton = new BigButton("Start");
    @NotNull
    private final BigButton restartButton = new BigButton("Restart");
    @NotNull
    private final BigVBox centerContainer = new BigVBox();
    @NotNull
    private final Label historyLabel = new Label("History");

    @NotNull
    private final Text historyFiled = new Text();
    @NotNull
    private final ScrollPane scrollPane = new ScrollPane();

    private final IntegerProperty ticksElapsed = new SimpleIntegerProperty();


    @NotNull
    private final Canvas gamePlayCanvas = new Canvas();

    private GameplayInfoPane infoPane = null;

    private FXJesonMor fxJesonMor = null;

    private Place lastSourcePlace;
    private Place lastTargetPlace;
    private Player winner = null;



    public GamePlayPane() {
        setScrollPane();
        connectComponents();
        styleComponents();
        setCallbacks();
    }

    @Override
    void connectComponents() {
        topBar.getChildren().add(title);
        topBar.setAlignment(Pos.CENTER);
        leftContainer.getChildren().addAll(
                parameterText,
                historyLabel,
                scrollPane,
                startButton,
                restartButton,
                returnButton
        );
        centerContainer.getChildren().addAll(
                gamePlayCanvas
        );

        this.setTop(topBar);
        this.setLeft(leftContainer);
        this.setCenter(centerContainer);
    }

    @Override
    void styleComponents() {
        title.getStyleClass().add("head-size");
    }

    @Override
    void setCallbacks() {
        startButton.setOnAction(event -> startGame());
        returnButton.setOnAction(event -> doQuitToMenuAction());
        restartButton.setOnAction(event -> onRestartButtonClick());
        saveRecordButton.setOnAction(event -> Serializer.getInstance().saveToFile(fxJesonMor));
        gamePlayCanvas.setOnMousePressed(this::onCanvasPressed);
        gamePlayCanvas.setOnMouseDragged(this::onCanvasDragged);
        gamePlayCanvas.setOnMouseReleased(this::onCanvasReleased);

    }

    void setScrollPane(){
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(ViewConfig.WIDTH / 4.0, ViewConfig.HEIGHT / 3.0 );
        scrollPane.setContent(historyFiled);
    }

    private void setParameterText(Configuration configuration){
        parameterText.setText(
                "Parameters:\n\n" +
                "Size of board: " + configuration.getSize() + "\n" +
                "Num of protection moves: " + configuration.getNumMovesProtection() + "\n" +
                "Player " + configuration.getPlayers()[0].getName() + (configuration.getPlayers()[0] instanceof ConsolePlayer ? "(human)": "(computer)") + "\n" +
                "Player " + configuration.getPlayers()[1].getName() + (configuration.getPlayers()[1] instanceof ConsolePlayer ? "(human)": "(computer)") + "\n"
        );
        parameterText.setDisable(true);
    }

    void initializeGame(@NotNull FXJesonMor fxJesonMor) {
        if (this.fxJesonMor != null) {
            endGame();
        }

        this.fxJesonMor = fxJesonMor;

        this.infoPane = new GameplayInfoPane(fxJesonMor.getPlayer1Score(), fxJesonMor.getPlayer2Score(), fxJesonMor.getCurPlayerName(), ticksElapsed);
        this.infoPane.setPrefHeight(10);
        HBox.setHgrow(infoPane, Priority.ALWAYS);

        this.centerContainer.getChildren().add(infoPane);
        this.startButton.setDisable(false);
        this.restartButton.setDisable(true);
        this.saveRecordButton.setDisable(true);

        gamePlayCanvas.setWidth(0);
        gamePlayCanvas.setHeight(0);

        disnableCanvas();

        this.lastSourcePlace = null;
        this.lastTargetPlace = null;
        this.winner = null;

        setParameterText(fxJesonMor.getConfiguration());

        fxJesonMor.getCurPlayerName().addListener((observable, oldValue, newValue) -> {
            // if player changed, restart timer
            Platform.runLater(()->ticksElapsed.set(0));
        });

        gamePlayCanvas.setWidth(fxJesonMor.getConfiguration().getSize() * ViewConfig.PIECE_SIZE);
        gamePlayCanvas.setHeight(fxJesonMor.getConfiguration().getSize() * ViewConfig.PIECE_SIZE);

        fxJesonMor.addOnTickHandler(() -> Platform.runLater(() -> ticksElapsed.set(ticksElapsed.get() + 1)));

        fxJesonMor.addOnFlowHandler(() -> {
            if (winner == null) {
                Platform.runLater(this::playEachRound);
            }

            // if duration timer is over 30 seconds, current player failed the game
            if (ticksElapsed.get() >= DurationTimer.getDefaultEachRound() - 1){
                AudioManager.getInstance().playSound(AudioManager.SoundRes.LOSE);
                Platform.runLater(this::createLosePopup);
                Platform.runLater(() -> ticksElapsed.set(0));
                fxJesonMor.stopCountdown();
            }
        });

        fxJesonMor.renderBoard(gamePlayCanvas);
    }

    private void enableCanvas(){
        gamePlayCanvas.setDisable(false);
    }

    private void disnableCanvas(){
        gamePlayCanvas.setDisable(true);
    }


    public void startGame() {
        startButton.setDisable(true);
        restartButton.setDisable(false);
        fxJesonMor.startCountdown();
    }

    /**
     * Each round, check current player
     * If is human player, enable mouse click
     * If is computer, play automatically
     * After get next move by either mouse clicking or automatic, validate this move
     * If move is valid and available for current player, then make the move
     * Re-render the board
     */
    private void playEachRound(){
        var lastPlayer = this.fxJesonMor.getConfiguration().getPlayers()[fxJesonMor.getNumMoves() % fxJesonMor.getConfiguration().getPlayers().length];
        checkOutOfMove(lastPlayer);

        if (winner == null){
            Move lastMove = null;

            if (lastPlayer instanceof ConsolePlayer) {
                enableCanvas();
                if (this.lastSourcePlace != null && this.lastTargetPlace != null) {
                    lastMove = new Move(this.lastSourcePlace, this.lastTargetPlace);
                    lastSourcePlace = null;
                    lastTargetPlace = null;
                    disnableCanvas();
                }
            } else if (lastPlayer instanceof RandomPlayer) {
                disnableCanvas();
                lastMove = lastPlayer.nextMove(fxJesonMor, fxJesonMor.getAvailableMoves(lastPlayer));
            }

            if (lastPlayer != null & lastMove != null){
                var hasMadeMove = tryMove(lastPlayer, lastMove);

                if (hasMadeMove) {
                    updateHistoryField(lastMove);
                }
                lastMove = null;
                lastPlayer = null;
                fxJesonMor.renderBoard(gamePlayCanvas);
            }
        }
        checkWinner();
    }

    /**
     * Restart the game, clear the board, history text field, and restart timer
     */
    private void onRestartButtonClick(){
        endGame();
        initializeGame(new FXJesonMor(this.fxJesonMor.getConfiguration()));
        disnableCanvas();
    }

    /**
     * When press, note down the source point
     * Play click.mp3
     * draw a rectangle at clicked board tile to show which tile is selected
     * @param event mouse click, note down the (x,y) as source point of move
     */
    private void onCanvasPressed(MouseEvent event){
        Renderer.drawRectangle(gamePlayCanvas.getGraphicsContext2D(), toBoardCoordinate(event.getX()), toBoardCoordinate(event.getY()));
        AudioManager.getInstance().playSound(AudioManager.SoundRes.CLICK);

        this.lastSourcePlace = new Place(toBoardCoordinate(event.getX()), toBoardCoordinate(event.getY()));
    }

    /**
     * When mouse dragging, draw a path
     * @param event mouse position
     */
    private void onCanvasDragged(MouseEvent event){
        Renderer.drawOval(gamePlayCanvas.getGraphicsContext2D(), event.getX(), event.getY());
    }

    /**
     * When mouse release, a Move is finished, check whether the move is valid and available for the current player
     * @param event mouse release
     */
    private void onCanvasReleased(MouseEvent event){
        this.lastTargetPlace = new Place(toBoardCoordinate(event.getX()), toBoardCoordinate(event.getY()));
        fxJesonMor.renderBoard(gamePlayCanvas);
    }

    /**
     * Creates a popup which tells the player they have completed the map.
     */
    private void createWinPopup(String winnerName){
        final var box = new Alert(Alert.AlertType.CONFIRMATION);
        box.setTitle("Congratulations!");
        box.setContentText(winnerName +" wins!");
        choicesOfPopup(box);
    }

    /**
     * if current player is out of move, namely no available move, then update winner
     */
    private void checkOutOfMove(Player lastPlayer){
        var availableMoves = fxJesonMor.getAvailableMoves(lastPlayer);

        if (availableMoves.length <= 0) {
            Platform.runLater(()->showInvalidMoveMsg("No available moves for the player " + lastPlayer.getName()));
            if (fxJesonMor.getConfiguration().getPlayers()[0].getScore() < fxJesonMor.getConfiguration().getPlayers()[1].getScore()) {
                this.winner = fxJesonMor.getConfiguration().getPlayers()[0];
            } else if (fxJesonMor.getConfiguration().getPlayers()[0].getScore() > fxJesonMor.getConfiguration().getPlayers()[1].getScore()) {
                this.winner = fxJesonMor.getConfiguration().getPlayers()[1];
            } else {
               this.winner = lastPlayer;
            }
        }
    }

    /**
     * check winner, if winner comes out, then play the win.mp3 and popup window.
     */
    private void checkWinner(){
        if (this.winner != null){
            AudioManager.getInstance().playSound(AudioManager.SoundRes.WIN);
            fxJesonMor.stopCountdown();
            endGame();
            createWinPopup(winner.getName());
        }
    }

    /**
     * try lastMove, if the last move is available for current player and obey the rules, then make move
     * otherwise, abort this move
     * @param lastPlayer pass in the current player
     * @param lastMove last move want to make
     * @return whether the move has been successfully made or not.
     */
    private boolean tryMove(@NotNull Player lastPlayer, @NotNull Move lastMove){
        boolean moveResult = false;
        var availableMoves = fxJesonMor.getAvailableMoves(lastPlayer);

        boolean isAvailable = false;

        // check availability
        for (var availableMove: availableMoves){
            if (lastMove.equals(availableMove)){
                isAvailable = true;
                break;
            }
        }

        // check validation
        var checkMoveResult =  lastPlayer.validateMove(fxJesonMor, lastMove);
        if (checkMoveResult == null){
            if (isAvailable){
                makeMove(lastPlayer, lastMove);
                moveResult = true;
            } else{
                showInvalidMoveMsg("The piece you moved does not belong to you!");
            }
        }
        else {
            showInvalidMoveMsg(checkMoveResult);
        }
        return moveResult;
    }
    /**
     * If lastMove is valid and available, then make this move
     * Move the move, update numMove and scores, update this move to move record, update winner if available
     * @param lastMove: last move want to make
     */
    private void makeMove(Player lastPlayer, Move lastMove){
        AudioManager.getInstance().playSound(AudioManager.SoundRes.PLACE);

        var movedPiece = fxJesonMor.getPiece(lastMove.getSource());
        fxJesonMor.movePiece(lastMove);
        fxJesonMor.increaseNumMove();
        fxJesonMor.updateScore(lastPlayer, movedPiece, lastMove);

        this.winner = fxJesonMor.getWinner(lastPlayer, movedPiece, lastMove);
    }

    private void createLosePopup(){
        final var box = new Alert(Alert.AlertType.CONFIRMATION);
        box.setTitle("Sorry! Time's out!");
        box.setContentText(fxJesonMor.getCurrentPlayer().getName() +" Lose!");

        choicesOfPopup(box);
    }

    private void choicesOfPopup(Alert box) {
        final var newGameButton = new ButtonType("Start New Game");
        final var saveRecordButton = new ButtonType("Export Move Records");
        final var returnButton = new ButtonType("Return to Main Menu");
        box.getButtonTypes().setAll(newGameButton, saveRecordButton, returnButton);

        final var result = box.showAndWait().orElseThrow();
        final var resultText = result.getText();
        switch (resultText) {
            case "Start New Game" -> onRestartButtonClick();
            case "Return to Main Menu" -> doQuitToMenu();
            case "Export Move Records" -> {
                Serializer.getInstance().saveToFile(fxJesonMor);
                onRestartButtonClick();
            }
        }
    }

    private void showInvalidMoveMsg(String errorMsg){
        final var alertBox = new Alert(Alert.AlertType.ERROR);
        alertBox.setTitle("Invalid Move");
        alertBox.setHeaderText("Your movement is invalid due to following reason(s):");
        alertBox.setContentText(errorMsg);
        alertBox.showAndWait();
    }

    private void doQuitToMenuAction() {
        final var box = new Alert(Alert.AlertType.CONFIRMATION);
        box.setTitle("Confirm");
        box.setHeaderText("Return to menu?");
        box.setContentText("Game progress will be lost.");
        box.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);

        box.showAndWait();
        if (box.getResult().equals(ButtonType.OK)) {
            doQuitToMenu();
        }
    }

    private void updateHistoryField(Move move){
        String moveString = String.format("[%d, %d] -> [%d, %d]",
                move.getSource().x(), move.getSource().y(), move.getDestination().x(), move.getDestination().y());
        historyFiled.setText(historyFiled.getText() + "\n" + moveString);
    }

    /**
     * Go back to the Level Select scene.
     */
    private void doQuitToMenu() {
        endGame();
        this.fxJesonMor = null;
        SceneManager.getInstance().showPane(MainMenuPane.class);
    }

    private int toBoardCoordinate(double x){
        return ((int) Math.floor(x) / ViewConfig.PIECE_SIZE);
    }

    private void endGame() {
        if (fxJesonMor != null){
            fxJesonMor.stopCountdown();
        }

        historyFiled.setText("");

        gamePlayCanvas.setWidth(0);
        gamePlayCanvas.setHeight(0);
        startButton.setDisable(false);
        restartButton.setDisable(true);

        ticksElapsed.set(0);

        this.centerContainer.getChildren().remove(infoPane);
        this.infoPane = null;
    }
}
