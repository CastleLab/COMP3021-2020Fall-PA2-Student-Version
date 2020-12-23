package castle.comp3021.assignment.player;

import castle.comp3021.assignment.piece.Knight;
import castle.comp3021.assignment.protocol.*;
import org.jetbrains.annotations.NotNull;

import java.util.NavigableMap;
import java.util.Objects;
import java.util.Random;
import java.util.TreeMap;

/**
 * A computer player that makes a move randomly.
 */
public class RandomPlayer extends Player {
    public RandomPlayer(String name, Color color) {
        super(name, color);
    }

    public RandomPlayer(String name) {
        this(name, Color.BLUE);
    }

    @Override
    public @NotNull Move nextMove(Game game, Move[] availableMoves) {
        RandomCollection rc = new RandomCollection();

        for (int i = 0; i < availableMoves.length; i++){
            rc.add(assignWeight(game, availableMoves[i]), i);
        }
        int nextIndex;
        try {
            nextIndex = rc.next();
        } catch (NullPointerException e){
            nextIndex = 0;
        }
        return availableMoves[nextIndex];
    }

    private int assignWeight(Game game, Move move){
        var weight = 0;

        // if kill an enemy, add 3 weights
        if (game.getPiece(move.getDestination()) != null){
            if (Objects.requireNonNull(game.getPiece(move.getDestination())).getPlayer() != null){
                if (Objects.requireNonNull(game.getPiece(move.getSource())).getPlayer() != game.getCurrentPlayer()) {
                    weight += 3;
                }
            }
        }

        var sourceToCenter = distanceFromCentral(move.getSource(), game.getCentralPlace());
        var targetToCenter = distanceFromCentral(move.getDestination(), game.getCentralPlace());

        // if move a knight, add 1 weight
        // if a knight is going to leave / arrive central place, add 5 weights
        if (game.getPiece(move.getSource()) instanceof Knight){
            weight += 1;
            if (targetToCenter == 0 || sourceToCenter == 0){
                weight += 5;
            }
        }

        //if a piece gets closer to central place, add 2 weights
        if (targetToCenter < sourceToCenter){
            weight += 2;
        }

        return weight;
    }

    private int distanceFromCentral(Place targetPlace, Place centralPlace){
        return Math.abs(targetPlace.x() - centralPlace.x()) + Math.abs(targetPlace.y() - centralPlace.y());
    }
}

class RandomCollection {
    private final NavigableMap<Double, Integer> map = new TreeMap<>();
    private final Random random;
    private double total = 0;

    public RandomCollection() {
        this(new Random());
    }

    public RandomCollection(Random random) {
        this.random = random;
    }

    public RandomCollection add(double weight, int result) {
        if (weight <= 0) return this;
        total += weight;
        map.put(total, result);
        return this;
    }

    public Integer next() {
        double value = random.nextDouble() * total;
        return map.higherEntry(value).getValue();
    }
}
