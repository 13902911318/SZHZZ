package szhzz.Timer;

import szhzz.App.AppManager;
import szhzz.Calendar.MyDate;
import szhzz.Utils.DawLogger;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: HuangFang
 * Date: 13-11-24
 * Time: 上午10:20
 * To change this template use File | Settings | File Templates.
 */
public abstract class CircleTimer implements DawCountdown, Runnable {
    private static DawLogger logger = DawLogger.getLogger(CircleTimer.class);
    protected DawIdleTimer clock = new DawIdleTimer();
    protected long minIdleTime = 1000;
    protected MyDate date = new MyDate();
    protected String title = null;
    protected boolean useThread = true;
    private AtomicBoolean processing = new AtomicBoolean(false);
    private long interval = 100;
    private boolean waiting = false;

    public CircleTimer() {
        init();
    }

    public void init() {
    }

    public long getInterval() {
        return interval;
    }

    public void setCircleTime(long millis) {
        this.interval = millis;
        circleTime();
    }

    public void circleTime() {
        clock.stopTimer();
        if (!AppManager.isQuitApp()) {
            date.now();
            waiting = true;
            clock.timer(interval, this);
        }
    }

    public abstract void execTask();

    public void run() {
        execTask();
    }

    public void stopTimer() {
        clock.stopTimer();
    }

    @Override
    public void timeup() {
        // Block to process or skip
        if (processing.compareAndSet(false, true)) {
            try {
                waiting = false;
                if (interval < minIdleTime) {
                    minIdleTime = 0;
                }
                clock.stopTimer();
                if (useThread) {
                    AppManager.executeInBack(this);
                } else {
                    run();
                }
            } catch (InterruptedException e) {
                logger.error(e);
            } finally {
                processing.set(false);
            }
        }else{
            logger.info(new Exception("过于频繁使用CircleTimer, execTask() 出现重叠调用."));
            return;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMinIdleTime(long minIdleTime) {
        this.minIdleTime = minIdleTime;
    }

    public void setUseThread(boolean useThread) {
        this.useThread = useThread;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public boolean isProcessing() {
        return processing.get();
    }

    public void setProcessing(boolean processing) {
        this.processing.set(processing);
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }
}
