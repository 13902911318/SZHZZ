package szhzz.Timer;

import szhzz.App.AppManager;
import szhzz.Calendar.MyDate;
import szhzz.Utils.DawLogger;

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
    private boolean processing = false;
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
        if(processing){
            logger.error(new Exception("可能错误使用CircleTimer, execTask() 出现重叠调用."));
            return;
        }
        processing =true;
        execTask();
        processing = false;
    }

    public void stopTimer() {
        clock.stopTimer();
    }

    @Override
    public void timeup() {
        waiting = false;
        if (interval < minIdleTime) {
            minIdleTime = 0;
        }
        // idleTime/1000 秒内多次启动
        //if (interval - clock.leftTime() < idleTime) return;

        clock.stopTimer();
        if (useThread) {
            try {
                AppManager.executeInBack(this);
            } catch (InterruptedException e) {

            }
        } else {
            run();
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
        return processing;
    }

    public void setProcessing(boolean processing) {
        this.processing = processing;
    }
}
