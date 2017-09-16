package szhzz.sectionmath;

import szhzz.Calendar.MyDate;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2008-7-11
 * Time: 15:47:19
 * To change this template use File | Settings | File Templates.
 */
public class SectionDayPoints {
    public static final int calculatePeriodDay = 0;
    public static final int calculatePeriodWeek = 1;

    public static final int LS = 1;
    public static final int SL = 2;
    public static final int SS = 3;
    public static final int LL = 5;
    int calculatePeriod = calculatePeriodDay;

    //    LinkedList<KeyPoint> dayPoints;
    float c1 = 0f, c2 = 0f;
    float v1 = 0f, v2 = 0f;
    String d1 = "", d2 = "";
    int len = 0;
    MyDate period = null;

    public SectionDayPoints(int len) {
        this.len = len;
//        dayPoints = new LinkedList<KeyPoint>();
    }

    public void setcalculatePeriod(int calculatePeriod) {
        this.calculatePeriod = calculatePeriod;
    }

    private void weekAdd(float v, float c, String date) {
        if (period == null) {
            period = new MyDate(date);
            add_(v, c, date);
            return;
        } else {
            if (period.getWeek_of_YEAR() != new MyDate(date).getWeek_of_YEAR()) {
                period.setDate(date);
                add_(v, c, date);
                return;
            }
        }
        this.c1 = c;
        this.v1 = v;
        this.d1 = date;
    }

    public void add(float v, float c, String date) {
        if (calculatePeriod == calculatePeriodWeek) {
            weekAdd(v, c, date);
        } else {
            add_(v, c, date);
        }
    }

    private void add_(float v, float c, String date) {
        c2 = this.c1;
        v2 = this.v1;
        d2 = d1;
        this.c1 = c;
        this.v1 = v;
        this.d1 = date;
    }


    public int getStatu() {
        int val = 0;
        if (v1 > v2) {
            if (c1 > c2) {
                val = LL;
            } else {
                val = LS;
            }
        } else {
            if (c1 > c2) {
                val = SL;
            } else {
                val = SS;
            }
        }
        return val;

    }


    public String statuName() {
        int s = getStatu();
        String statu = "";

        if (s == LS) {
            statu += "LS";
        } else if (s == LL) {
            statu += "LL";
        } else if (s == SL) {
            statu += "SL";
        } else if (s == SS) {
            statu += "SS";
        }
        return statu;
    }
}

