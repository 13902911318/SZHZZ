package szhzz.Timer;

import szhzz.App.AppManager;
import szhzz.Calendar.MiscDate;
import szhzz.Calendar.MyDate;
import szhzz.Utils.CanStop;
import szhzz.Utils.DawLogger;
import szhzz.Utils.Utilities;
import szhzz.sql.database.DBException;
import szhzz.sql.database.DataStore;
import szhzz.sql.database.DataValidater;
import szhzz.sql.gui.DataWindow;
import szhzz.sql.gui.DwPanel;
import szhzz.sql.gui.DwToobar_Event;
import szhzz.sql.gui.DwToolBar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2007-12-26
 * Time: 15:15:58
 * To change this template use File | Settings | File Templates.
 */
public class AlarmClock implements DawCountdown {
    static DecimalFormat fT = new DecimalFormat("00");
    static DawIdleTimer clock = null;
    static Vector<TimerEvent> alarmPool = new Vector<TimerEvent>();
    static LinkedList<TimerEvent> freeTimePool = new LinkedList<TimerEvent>();
    static LinkedList<TimerEvent> imediateRunPool = new LinkedList<TimerEvent>();
    static AppManager App = null;
    static AlarmClock onlyOne = null;
    private static DawLogger logger = DawLogger.getLogger(AlarmClock.class);

    //    static long longInterval = 10 * 1000 * 60;
//    static long sortInterval = 1000 * 5;
//
//    long Interval = sortInterval;
//    boolean taskRunning = false;
    //    private alrmTime freeTime = null;
    String logfile = "scedu.log";
    Hashtable<Integer, TimerEvent> evetMap = new Hashtable<Integer, TimerEvent>();
    DwPanel timerGui = null;
    DataStore ds = null;
    DataWindow dw = null;
    ArrayList<TimerEvent> execJobs = new ArrayList<TimerEvent>();
    private UdfMenu udfMenu = null;

    private AlarmClock() {
        clock = new DawIdleTimer();
//        new File(logfile).delete();
    }

    public static AlarmClock getInstance() {
        if (onlyOne == null) onlyOne = new AlarmClock();
        return onlyOne;
    }

    public synchronized Object setAlarm(TimerEvent event) {
        event.setAlarmClock(this);
        alarmPool.add(event);
        logIt("Added " + event + " Left time " + toTimeString(event.getLeftMSeconds()));
        timeup();
        return event;
    }

    public synchronized Object setAlarm(MyDate timer, Runnable requestor, boolean loop, int priority) {
        return setAlarm(timer.getHour(), timer.getMinute(), timer.getSecond(),
                timer.getMillisOfSecond(), requestor, loop, priority);
    }

    public synchronized Object setAlarm(int hour, int minute, int seconds, Runnable requestor, boolean loop, int priority) {
        return setAlarm(hour, minute, seconds, 0, requestor, loop, priority);
    }

    public synchronized Object setAlarm(int hour, int minute, int seconds, int millis, Runnable requestor, boolean loop, int priority) {
        alarmTime a = new alarmTime(hour, minute, seconds, millis, requestor, loop, priority);
        a.setAlarmClock(this);
        alarmPool.add(a);
        logIt("Added " + a + " Left time " + toTimeString(a.getLeftMSeconds()));
        timeup();
        return a;
    }


    public synchronized void removeAlarm(Object o) {
        try {
            if (o instanceof String) {
                Vector<TimerEvent> tbd = new Vector<>();
                for (Object obj : alarmPool) {
                    if (((TimerEvent) obj).getCaseName().equals(o)) {
                        tbd.add((TimerEvent) obj);
                        this.logIt(o + " Removed");
                    }
                }
                alarmPool.removeAll(tbd);

                //导致 ConcurrentModificationException
//                Iterator var2 = alarmPool.iterator();
//
//                while(var2.hasNext()) {
//                    Object obj = var2.next();
//                    if (((TimerEvent)obj).getCaseName().equals(o) && alarmPool.remove(obj)) {
//                        this.logIt(o + " Removed");
//                        break;
//                    }
//                }
            } else if (alarmPool.remove(o)) {
                this.logIt(o + " Removed");
            }
        } finally {
            timeup();
        }
    }

    public synchronized void removeAllAlarms() {
        alarmPool.clear();
        timeup();
    }

    public synchronized void setFreeTimeJob(Runnable requestor) {
        freeTimePool.add(new alarmTime(24, 0, 0, 0, requestor, false, 0));
        logIt(" Free time job (" + requestor.toString() + ") will run in any free time.");
        timeup();
    }


    public void setUi(DwPanel timerGui) {
        Collections.sort(alarmPool);
        if (timerGui == null) return;
        if (dw != null) return;

        this.timerGui = timerGui;
        evetMap.clear();
        int c = 0;
        dw = timerGui.getDataWindow();

        ds = new DataStore();
        ds.setReadOnly(false);

        ds.setColName("任务", c);
        ds.setColTypeName("String", c);
        ds.setColLength(c, 120);
        ds.setReadOnlyCol(c, true);

        c++;
        ds.setColName("触发时间", c);
        ds.setColTypeName("String", c);
        ds.setColLength(c, 25);
        ds.setReadOnlyCol(c, true);

        c++;
        ds.setColName("权重", c);
        ds.setColTypeName("Integer", c);
        ds.setColLength(c, 8);
        ds.setReadOnlyCol(c, true);

        c++;
        ds.setColName("循环", c);
        ds.setColTypeName("Boolean", c);
        ds.setColLength(c, 8);
        ds.setDefaltValues(c, false);
        ds.setReadOnlyCol(c, true);

        c++;
        ds.setColName("暂停", c);
        ds.setColTypeName("Boolean", c);
        ds.setColLength(c, 8);
        ds.setDefaltValues(c, false);
        ds.setColValidator(c, new SuspendListener());

//        ds.setReadOnlyCol(c, false);

        ds.setName(Utilities.getTableName("ScheduCase"));

        try {
            dw.shareData(ds, false);
        } catch (DBException e) {

        }


        for (int i = 0; i < alarmPool.size(); i++) {
            int row = ds.appendRow();
            TimerEvent a = alarmPool.get(i);
            ds.setValueAt(a.getCaseName(), row, 0);
            ds.setValueAt(a.getDateTime(), row, 1);
            ds.setValueAt(a.getPriority(), row, 2);
            ds.setValueAt(a.isSuspend(), row, 3);
            evetMap.put(row, a);
        }


        timerGui.getToolBar().setFloatable(false);
        timerGui.getToolBar().setCanAdd(false);
        timerGui.getToolBar().setCanDele(false);
        timerGui.getToolBar().setCanRetrieve(true);
        timerGui.getToolBar().setCanSave(false);
        timerGui.setTitle(null);
        timerGui.addToolbarEvent(new triggerRetrive());

        dw.repaint(100);

        if (udfMenu == null) {
            udfMenu = new UdfMenu();
            dw.addUdfMenu("立刻运行", udfMenu);
            dw.addUdfMenu("删除并立刻运行", udfMenu);
            dw.addUdfMenu("暂停并立刻运行", udfMenu);
        }

//        dw.fireTableStructureChanged();
//        dw.fireTableDataChanged();
    }

    public synchronized void taskChanged() {
        if (dw != null) {
            try {
                ds.clear();
                evetMap.clear();

                for (int i = 0; i < imediateRunPool.size(); i++) {
                    int row = ds.appendRow();
                    TimerEvent a = imediateRunPool.get(i);
                    ds.setValueAt(a.getCaseName(), row, 0);
                    ds.setValueAt(a.getDateTime(), row, 1);
                    ds.setValueAt(a.getPriority(), row, 2);
                    ds.setValueAt(a.isLoop(), row, 3);
                    ds.setValueAt(a.isSuspend(), row, 4);
                    evetMap.put(row, a);
                }
                for (int i = 0; i < freeTimePool.size(); i++) {
                    int row = ds.appendRow();
                    TimerEvent a = freeTimePool.get(i);
                    ds.setValueAt(a.getCaseName(), row, 0);
                    ds.setValueAt(a.getDateTime(), row, 1);
                    ds.setValueAt(a.getPriority(), row, 2);
                    ds.setValueAt(a.isLoop(), row, 3);
                    ds.setValueAt(a.isSuspend(), row, 4);

                    evetMap.put(row, a);
                }
                for (int i = 0; i < alarmPool.size(); i++) {
                    int row = ds.appendRow();
                    TimerEvent a = alarmPool.get(i);
                    ds.setValueAt(a.getCaseName(), row, 0);
                    ds.setValueAt(a.getDateTime(), row, 1);
                    ds.setValueAt(a.getPriority(), row, 2);
                    ds.setValueAt(a.isLoop(), row, 3);
                    ds.setValueAt(a.isSuspend(), row, 4);
                    evetMap.put(row, a);
                }
            } catch (Exception e) {
                logger.error(e);
            }
            dw.repaint(100);
        }
    }

    public void setSuspend(int row, boolean suspend) {
        TimerEvent a = evetMap.get(row);
        if (a != null) {
            a.setSuspend(suspend);
            dw.setValueAt(suspend, row, 4);
            AppManager.logit(a.getCaseName() + " @" + a.getDateTime() + (suspend ? "设置暂停" : "设置继续"));
        }
    }

    public void suspendAll(boolean suspend) {
        for (int row = 0; row < ds.getRowCount(); row++) {
            TimerEvent a = evetMap.get(row);
            if (a != null) {
                a.setSuspend(suspend);
                ds.setValueAt(suspend, row, 4);
                AppManager.logit(a.getCaseName() + " @" + a.getDateTime() + (suspend ? "设置暂停" : "设置继续"));
            }
        }
        dw.repaint(100);
    }

    private synchronized ArrayList<TimerEvent> nextAlarm() {
        TimerEvent firstJob = null;
        execJobs.clear();

        /**
         * 如有立刻执行的任务，立刻执行
         */
        if (imediateRunPool.size() > 0) {
            execJobs.addAll(imediateRunPool);
            imediateRunPool.clear();
        }
        try {
            if (alarmPool.size() > 0) {
                Collections.sort(alarmPool);

                for (TimerEvent peekJob : alarmPool) {
                    long t = peekJob.getLeftMSeconds();
                    if (t <= 0) {
                        execJobs.add(peekJob);
                    } else {
                        break;
                    }
                }
                alarmPool.removeAll(execJobs);
            }
        } catch (Exception ignored) {

        }

        /**
         * 没有定时计划的任务，执行空闲时间任务
         */
        if (execJobs.size() == 0) {
            execJobs.addAll(freeTimePool);
            freeTimePool.clear();
        }

        return execJobs;
    }


    /**
     * 添加任务或计时结束都触发此事件
     */
    public synchronized void timeup() {
        execJobs = nextAlarm();

        for (TimerEvent job : execJobs) {
            runAjob(job);
        }
        for (TimerEvent job : execJobs) {
            String msg = MiscDate.todaysDate() + ">" + job.toString() + "\n";
            try {
                Utilities.String2File(msg, logfile, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //设定下一任务的时间
        if (alarmPool.size() > 0) {
            TimerEvent peekJob = alarmPool.get(0);
            long t = peekJob.getLeftMSeconds();
            if (t <= 0) {
                timeup();
                return;
            }
            clock.timer(t, this);
        } else {
            clock.stopTimer();
        }

        taskChanged();
    }

    public void setApp(AppManager App) {
        this.App = App;
    }

    private void logIt(String msg) {
        if (App != null) {
            AppManager.logit(msg);
        } else {
            // System.out.println(msg);
        }
    }

    void runAjob(TimerEvent job) {
        String msg;
        try {
            AppManager.executeInBack(job);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Exception e) {

        }
    }


    protected void poolBack(TimerEvent a) {
        if (a.isLoop()) {
            alarmPool.add(a);
        }
        timeup();
    }

    String toTimeString(long mSeconds) {
        long seconds = mSeconds / 1000;
        long h = seconds / (3600);
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return fT.format(h) + ":" + fT.format(m) + ":" + fT.format(s);
    }

    private class alarmTime extends TimerEvent {
        MyDate day;
        Runnable requestor = null;
        //        String caseName = "No named";
        boolean exclude = true;
        boolean isRunning = false;

        //        alarmTime(int hour, int minuts, int seconds, Runnable requestor, boolean loop, int priorol) {
//            setup(hour, minuts, seconds, 0, requestor, loop, priorol);
//        }
//
        alarmTime(int hour, int minuts, int seconds, int millis, Runnable requestor, boolean loop, int priority) {
            setup(hour, minuts, seconds, millis, requestor, loop, priority);
        }

        private void setup(int hour, int minuts, int seconds, int millis, Runnable requestor, boolean loop, int priority) {
            //           caseName = requestor.toString();
            if (requestor instanceof TaskExclutable) {
                exclude = ((TaskExclutable) requestor).isExclude();
            }
            day = new MyDate(MyDate.getToday().getDate());

            if ((hour * 60 * 60 + minuts * 60 + seconds) < (day.getHour() * 60 * 60 + day.getMinute() * 60 + day.getSecond())) {
                day.advance_day();
            }

            day.setHOUR_OF_DAY(hour);
            day.setMinute(minuts);
            day.setSecond(seconds);
            day.setMilliSecond(millis);

            setPriority(priority);
            this.requestor = requestor;
            this.setLoop(loop);

        }

        public void stopIt() {
            if (isRunning && requestor != null && requestor instanceof CanStop) {
                ((CanStop) requestor).stopIt();
            }
        }

        public long getIndex() {
            return day.getTimeInMillis();
        }

        public long getLeftMSeconds() {
            return (getIndex() - Calendar.getInstance().getTimeInMillis()) ;  //+ AppManager.getSystemTimeDiff()) 会导致与程序的其它部分基准不同
//            System.currentTimeMillis()
        }


        public void run() {
            if (requestor != null) {
                try {
                    day.advance_day();
                    if (!isSuspend()) {
                        isRunning = true;
                        try {
                            requestor.run();
                        } catch (Error ignored) {
                            //阻止任何错误传导出来破坏 alarm
                        }
                        isRunning = false;
                    }
                } finally {
                    this.poolBack();
                }
            }
        }

        public String getCaseName() {
            return requestor.toString();
        }

        public String getDateTime() {
            return day.getDateTimeFormat("yyyy-MM-dd HH:mm:ss.SSS");
        }


        public String toString() {
            if (requestor != null) {
                return requestor.toString() + " schedule at " + getDateTime();
            }
            return " schedule at " + getDateTime();
        }

        // function for freeTime only

        Runnable getRequestor() {
            return requestor;
        }

        void setRequestor(Runnable requestor) {
            this.requestor = requestor;
        }
    }

    class SuspendListener extends DataValidater {
        public SuspendListener() {

        }

        public boolean validate(Object value) {
            Boolean d = null;

            if (value == null) return true;

            if (value instanceof Boolean) {


                d = (Boolean) value;
                int row = dw.getSelectedRow();
                setSuspend(row, d);

                return true;
            }
            return false;
        }

        public String name() {
            return "SuspendListener";
        }
    }


    class UdfMenu implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            alarmTime a = null;
            int row = dw.getSelectedRow();

            if ("立刻运行".equals(command)) {
                a = (alarmTime) evetMap.get(row);
            } else if ("删除并立刻运行".equals(command)) {
                a = (alarmTime) evetMap.remove(row);
                removeAlarm(a);
            } else if ("暂停并立刻运行".equals(command)) {
                a = (alarmTime) evetMap.get(row);
                if (a != null) {
                    setSuspend(row, true);
                }
            }

            if (a != null) {
                String msg;
                try {
                    AppManager.executeInBack(a.getRequestor());

                    msg = MiscDate.todaysDate() + ">" + a.toString() + "\n";
                    Utilities.String2File(msg, logfile, true);
                    App.logit(command + ">" + a.toString());
                } catch (InterruptedException e1) {
                    logger.error(e1);
                } catch (IOException e2) {
                    logger.error(e2);
                }
            }
        }
    }

    private class triggerRetrive implements DwToobar_Event {
        public boolean toolbarClicked(int key, DwToolBar tbar, Object parms) {

            if (DwToolBar.B_RETRIEVE == key) {
                taskChanged();
            } else {
                return true;
            }
            return false;
        }
    }
}


