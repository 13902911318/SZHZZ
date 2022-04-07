package szhzz.Utils;

import szhzz.App.AppManager;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Administrator on 2018/7/28.
 * 用于测试程序段占用时间的通用工具
 */
public class SpeedGroup {
    private static final long n2s = 1000000000;

    private HashMap<String, SpeedTest> summary = new HashMap<>();
    private LinkedList<String> index = new LinkedList<>();
    private static int no = 0;
    private long countTo = 0;
    private long interval = 0;
    private String title = "SpeedGroup" + (++no);
    private boolean onCount = false;
    private long lastTime = System.nanoTime();


    private SpeedGroup() {
        title = this.getClass().getSimpleName() + (++no);
    }

    public SpeedGroup(long countTo, long interval /*seconds*/) {
        this.countTo = countTo;
        this.interval = interval * 1000000; //this.interval Nano
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

        long sum = System.nanoTime() - lastTime;
        if (sum < interval) return;
        lastTime = System.nanoTime();

        long total = 0L;

        for (String id : index) {
            total += summary.get(id).summery();
        }
        if (total == 0d) return;

        StringBuffer sb = new StringBuffer();
        sb.append("Summary: Total timer (" + FT.format(total/n2s) + ")\n============================\n");
        sb.append(title+"\n");

        for (String id : index) {
            long t = summary.get(id).getSummaryTime();
            sb.append(id + "\t" + FT.format00(t/n2s) + "s\t" + FT.format00(100d * t / sum) + "%\n");
        }
        sb.append("-----------------------------\n");
        sb.append("TOTAL:\t" + FT.format00(sum/n2s) + "s\n");
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
