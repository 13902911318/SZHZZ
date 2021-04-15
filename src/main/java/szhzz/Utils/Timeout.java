package szhzz.Utils;

/**
 * Project: ControlCenter
 * Package: szhzz.Blade.NetBridge
 * <p>
 * User: HuangFang
 * Date: 2021/1/29
 * Time: 18:17
 * <p>
 * Created with IntelliJ IDEA
 */
public class Timeout {
    private final Object locker = new Object();
    long minSsm = 1;
    long maxSsm = 1000;
    long sleepTime = 1000; //缺省延时1秒

    public Timeout() {
    }

    public Timeout(long minSsm, long maxSsm) {
        this.minSsm = minSsm;
        this.maxSsm = maxSsm;
    }

    public void setRange(long minSsm, long maxSsm) {
        this.minSsm = minSsm;
        this.maxSsm = maxSsm;
    }

    public void wakeup() {
        synchronized (locker) {
            locker.notify();
        }
    }


    public void sleep() throws InterruptedException {
        synchronized (locker) {
            sleepTime *= 2;
            if (sleepTime > maxSsm) {
                reset();
            }
            locker.wait(sleepTime);
        }
    }

    public void reset() {
        sleepTime = minSsm;
    }
}
