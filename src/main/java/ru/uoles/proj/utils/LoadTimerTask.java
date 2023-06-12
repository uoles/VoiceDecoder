package ru.uoles.proj.utils;

import java.util.TimerTask;

public class LoadTimerTask extends TimerTask {

    private final TaskCallback taskCallback;

    public LoadTimerTask(final TaskCallback taskCallback) {
        this.taskCallback = taskCallback;
    }

    @Override
    public void run() {
        taskCallback.execute();
    }
}