package castle.comp3021.assignment.gui;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Timer for handling flow events.
 */
public class DurationTimer {
    private static int defaultEachRound = 30;

    @NotNull
    private Timer flowTimer = new Timer(true);


    private final List<Runnable> onTickCallbacks = new ArrayList<>();

    private final List<Runnable> onFlowCallbacks = new ArrayList<>();

    private int ticksElapsed;


    public static void setDefaultEachRound(int duration) {
        defaultEachRound = duration;
    }


    public static int getDefaultEachRound() {
        return defaultEachRound;
    }


    DurationTimer() {
        ticksElapsed = 0;
    }

    void registerFlowCallback(@NotNull final Runnable cb) {
        onFlowCallbacks.add(cb);
    }

    void registerTickCallback(@NotNull final Runnable cb) {
        onTickCallbacks.add(cb);
    }


    void start() {
        try {
            flowTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    ++ticksElapsed;
                    onTickCallbacks.forEach(Runnable::run);
                    onFlowCallbacks.forEach(Runnable::run);

                }
            }, 1000, 1000);
        } catch (IllegalStateException ex){
            if (ex.getMessage() == "Timer already cancelled."){
                flowTimer = new Timer(true);
            }
        }
    }


    void stop() {
        flowTimer.cancel();
    }

}
