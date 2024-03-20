package raft;

import java.util.Timer;
import java.util.TimerTask;

public class RaftTimer {
    Timer timer;
    TimerTask timerTask;
    Runnable action;

    public RaftTimer() {
        timer = new Timer();
        timerTask = null;
        action = null;
    }

    public synchronized void scheduleTask(long timeout, Runnable action) {
        this.action = action;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                action.run();
            }
        };

        timer.schedule(timerTask, timeout);
    }

    // once you cancel it, you can not reset it
    public synchronized void cancelTimeout() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    // once you cancel it, you can not reset it
    public synchronized void resetTimeout(long timeout) {
        if (timerTask != null) {
            cancelTimeout();
            scheduleTask(timeout, action);
        }
    }

    // we don't use this
    public void stopTimer() {
        timer.cancel();
    }

}
