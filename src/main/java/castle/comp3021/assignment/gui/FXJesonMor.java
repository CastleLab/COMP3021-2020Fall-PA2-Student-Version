package castle.comp3021.assignment.gui;

import castle.comp3021.assignment.gui.controllers.Renderer;
import castle.comp3021.assignment.protocol.Configuration;
import castle.comp3021.assignment.protocol.Move;
import castle.comp3021.assignment.protocol.Piece;
import castle.comp3021.assignment.protocol.Player;
import castle.comp3021.assignment.textversion.JesonMor;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.canvas.Canvas;
import org.jetbrains.annotations.NotNull;

public class FXJesonMor extends JesonMor {

    @NotNull
    private DurationTimer durationTimer;

    private final IntegerProperty scorePlayer1Property = new SimpleIntegerProperty(0);

    private final IntegerProperty scorePlayer2Property = new SimpleIntegerProperty(0);

    private final StringProperty currentPlayerNameProperty = new SimpleStringProperty(getCurrentPlayer().getName());

    public FXJesonMor(Configuration configuration){
        super(new Configuration(configuration.getSize(), configuration.getPlayers(), configuration.getNumMovesProtection()));

        //initialize players' score
        for (var player:this.configuration.getPlayers()){
            player.setScore(0);
        }

        this.configuration.setAllInitialPieces();
        this.board = this.configuration.getInitialBoard();

        this.durationTimer = new DurationTimer();
    }

    public void renderBoard(@NotNull Canvas canvas){
        Platform.runLater(() -> Renderer.renderChessBoard(canvas, this.configuration.getSize(), this.configuration.getCentralPlace()));
        Platform.runLater(() -> Renderer.renderPieces(canvas, this.board));
    }

    /**
     * Adds a handler to be run when a tick elapses.
     *
     * @param handler {@link Runnable} to execute.
     */
    public void addOnTickHandler(@NotNull Runnable handler) {
        durationTimer.registerTickCallback(handler);
    }

    /**
     * Adds a handler to be run when the water flows into an additional tile.
     *
     * @param handler {@link Runnable} to execute.
     */
    public void addOnFlowHandler(@NotNull Runnable handler) {
        durationTimer.registerFlowCallback(handler);
    }

    /**
     * Starts the flow of water.
     */
    public void startCountdown() {
        durationTimer.start();
    }

    /**
     * Stops the flow of water.
     */
    public void stopCountdown() {
        durationTimer.stop();
    }

    public StringProperty getCurPlayerName(){
        return currentPlayerNameProperty;
    }

    public IntegerProperty getPlayer1Score(){
        return scorePlayer1Property;
    }

    public IntegerProperty getPlayer2Score(){
        return scorePlayer2Property;
    }

    /**
     * Update the score of a player according to the piece and corresponding move made by him just now.
     *
     * @param player the player who just makes a move
     * @param piece  the piece that is just moved
     * @param move   the move that is just made
     */
    @Override
    public void updateScore(Player player, Piece piece, Move move) {
        var newScore = 0;
        newScore = Math.abs(move.getSource().x() - move.getDestination().x());
        newScore += Math.abs(move.getSource().y() - move.getDestination().y());
        player.setScore(player.getScore() + newScore);

        // update score to 2 properties
        Platform.runLater(() -> scorePlayer1Property.set(configuration.getPlayers()[0].getScore()));
        Platform.runLater(() -> scorePlayer2Property.set(configuration.getPlayers()[1].getScore()));
    }

    public void increaseNumMove(){
        numMoves ++;
        currentPlayer = configuration.getPlayers()[numMoves % configuration.getPlayers().length];
        Platform.runLater(() -> currentPlayerNameProperty.set(currentPlayer.getName()));
    }
}