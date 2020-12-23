package castle.comp3021.assignment.gui.views;

import castle.comp3021.assignment.gui.DurationTimer;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;

import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * Displays info about the current level being played by the user.
 */
public class GameplayInfoPane extends BigVBox {
    private final Label score1Label = new Label();
    private final Label score2Label = new Label();
    private final Label curPlayerLabel = new Label();
    private final Label timerLabel = new Label();

    public GameplayInfoPane(IntegerProperty score1Property, IntegerProperty score2Property, StringProperty curPlayer,
                            IntegerProperty ticksElapsed) {
        bindTo(score1Property, score2Property, curPlayer, ticksElapsed);
        this.getChildren().addAll(
                score1Label,
                score2Label,
                timerLabel,
                curPlayerLabel);
    }

    /**
     * @param s Seconds duration
     * @return A string that formats the duration stopwatch style
     */
    private static String formatTime(int s) {
        final var d = Duration.of(s, SECONDS);

        int seconds = d.toSecondsPart();
        int minutes = d.toMinutesPart();

        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * @param s Seconds duration
     * @return A string that formats the duration stopwatch style
     */
    private static String countdownFormat(int s) {
        final var d = Duration.of(DurationTimer.getDefaultEachRound() - s, SECONDS);

        int seconds = d.toSecondsPart();
        int minutes = d.toMinutesPart();

        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Binds all properties to their respective UI elements.
     *
     * @param score1Property Score of Player 1
     * @param score2Property Score of Player 2
     * @param ticksElapsed Timer Property, count down
     * @param curPlayer current player name
     */
    private void bindTo(IntegerProperty score1Property, IntegerProperty score2Property, StringProperty curPlayer,
                        IntegerProperty ticksElapsed) {
        Platform.runLater(()->{
            score1Label.textProperty().bind(Bindings.createStringBinding(() -> "Score of player 1: " + score1Property.get(), score1Property));
            score2Label.textProperty().bind(Bindings.createStringBinding(() -> "Score of player 2: " + score2Property.get(), score2Property));
            timerLabel.textProperty().bind(Bindings.createStringBinding(() -> "Time: " + countdownFormat(ticksElapsed.get()), ticksElapsed));
            curPlayerLabel.textProperty().bind(Bindings.createStringBinding(() -> "Current player: " + curPlayer.get(), curPlayer));
        });
    }
}
