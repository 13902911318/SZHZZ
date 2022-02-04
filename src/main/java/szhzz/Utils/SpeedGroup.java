package szhzz.Utils;

import szhzz.App.AppManager;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Administrator on 2018/7/28.
 * 用于测试程序段占用时间的通用工具
 */
public class SpeedGroup {
    private HashMap<String, SpeedTest> summary = new HashMap<>();
    private LinkedList<String> index = new LinkedList<>();
    private static int no = 0;
    private long countTo = 0;
    private long interval = 0;
    private String title = "SpeedGroup" + (++no);

    private SpeedGroup() {
        title = this.getClass().getSimpleName() + (++no);
    }

    private boolean onCount = false;
    private long lastTime = System.currentTimeMillis();

    public SpeedGroup(long countTo, long interval) {
        this.countTo = countTo;
        this.interval = interval;
    }

    public void startTimer(String ID) {
        if (onCount) getInstance(ID).straTimer();
    }

    public void endTimer(String ID) {
        if (onCount) getInstance(ID).endTimer();
    }

    SpeedTest getInstance(String ID) {
        SpeedTest ret = summary.get(ID);
        if(ret == null) {
            ret = new SpeedTest(ID, countTo);
            summary.put(ID, ret);
            index.add(ID);
        }
        return ret;
    }

    public void showSummery() {
        if (!onCount) return;

        if ((System.currentTimeMillis() - lastTime) < interval) return;
        lastTime = System.currentTimeMillis();

        long total = 0L;

        for (String id : index) {
            total += summary.get(id).summery();
        }
        if (total == 0d) return;

        StringBuffer sb = new StringBuffer();
        sb.append("Summary: Total timer (" + FT.format(total/1000000000) + ")\n============================\n");
        sb.append(title+"\n");
        long sum = 0;
        for (String id : index) {
            long t = summary.get(id).getSummaryTime();
            sum += t;
            sb.append(id + "\t" + FT.format00(t/1000000000d) + "min\t" + FT.format00(100d * t / total) + "%\n");
        }
        sb.append("-----------------------------\n");
        sb.append("TOTAL:\t" + FT.format00(sum/1000000000d) + "min\n");
        sb.append("============================\n");
        AppManager.logit("\n" + sb.toString());

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOnCount(boolean onCount) {
        this.onCount = onCount;
    }
}
