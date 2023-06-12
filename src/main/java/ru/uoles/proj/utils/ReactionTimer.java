package ru.uoles.proj.utils;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class ReactionTimer {

    private Timer timer = new Timer();

    private static final int TIMER_DELAY = 3;
    private static final int TIMER_EXECUTE = 3;

    private boolean enableLowerWing = false;

    public ReactionTimer() {
    }

    private void execute() {
        try {

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void startTimer() {
        timer.schedule(
                new LoadTimerTask(this::execute),
                TimeUnit.SECONDS.toMillis(TIMER_DELAY),
                TimeUnit.SECONDS.toMillis(TIMER_EXECUTE)
        );
    }
}
