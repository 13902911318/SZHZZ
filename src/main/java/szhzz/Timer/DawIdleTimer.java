package szhzz.Timer;/*
 * DawTimer.java
 *
 * Created on May 21, 2003, 11:13 AM
 */


import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;

/**
 * @author HuangFang
 */
public class DawIdleTimer {
    private static ClockDaemon Scheduler;
    private static ClockDaemon clock = new ClockDaemon(); //ServerMaster.getScheduler();

    private DawCountdown requestor;
    private long lastTime = 0;

    private Object clookHandle = null;

    public DawIdleTimer() {

    }

    /**
     * stop the clock thread
     * we can use timer(n) n > 0 to start clock again
     * unles we want clock stop for a long time, do not use this method
     */
    public void stopTimer() {
        if (clookHandle != null && clock != null) {
            clock.cancel(clookHandle);
            clookHandle = null;
        }
    }

    public void timer(long IdleTimeout, DawCountdown requestor) {
        if (IdleTimeout > 0) {
            if (clock == null) clock = new ClockDaemon();
            stopTimer();
            lastTime = System.currentTimeMillis() + IdleTimeout;
            clookHandle = clock.executeAfterDelay(IdleTimeout, new timerHandle(requestor));
        }
    }

    public long leftTime() {
        return lastTime - System.currentTimeMillis();
    }

    /**
     *
     */
    private class timerHandle implements Runnable {
        DawCountdown requestor;

        timerHandle(DawCountdown requestor) {
            this.requestor = requestor;
        }

        public void run() {
            requestor.timeup();
            requestor = null;
        }
    }
}

