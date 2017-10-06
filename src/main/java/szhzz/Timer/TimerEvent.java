package szhzz.Timer;


import szhzz.Utils.CanStop;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 2010-7-8
 * Time: 21:20:02
 * To change this template use File | Settings | File Templates.
 */
public abstract class TimerEvent extends CanStop implements Comparable, Runnable {
    private AlarmClock alarmClock = null;
    private boolean loop;
    private int priority = 10;
    private boolean suspend = false;

    public abstract String getCaseName();

    public abstract String getDateTime();

    public abstract long getLeftMSeconds();

    public abstract long getIndex();

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isLoop() {
        return loop && !calledStop();
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public int compareTo(Object o) {
        TimerEvent fo = (TimerEvent) o;
        long t = getIndex() - fo.getIndex();
        if (t == 0) {
            t = fo.getPriority() - getPriority();
        }
        return (t > 0 ? 1 : -1);
    }

    public void stopIt() {
        super.stopIt();
        if (alarmClock != null) {
            alarmClock.removeAlarm(this);
        }
    }

    public AlarmClock getAlarmClock() {
        return alarmClock;
    }

    public void setAlarmClock(AlarmClock alarmClock) {
        this.alarmClock = alarmClock;
    }

    protected void poolBack() {
        if (alarmClock != null) {
            alarmClock.poolBack(this);
        }
    }

    public boolean isSuspend() {
        return suspend;
    }

    public void setSuspend(boolean suspend) {
        this.suspend = suspend;
    }
}
