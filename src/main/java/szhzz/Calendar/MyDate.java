package szhzz.Calendar;


import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.Days;
import szhzz.App.AppManager;
import szhzz.App.ClareBuffer;
import szhzz.Utils.DawLogger;
import szhzz.Utils.NU;
import szhzz.sql.database.DBException;
import szhzz.sql.database.Database;
import szhzz.sql.jdbcpool.DbStack;

import java.io.Serializable;
import java.sql.ResultSet;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: zhua8634
 * Date: 2007-11-17
 * Time: 20:42:21
 * To change this template use File | Settings | File Templates.
 */

/**
 * Symbol  Meaning                      Presentation  Examples
 * ------  -------                      ------------  -------
 * G       era                          text          AD
 * C       century of era (>=0)         number        20
 * Y       year of era (>=0)            year          1996
 * <p/>
 * x       weekyear                     year          1996
 * w       week of weekyear             number        27
 * e       day of week                  number        2
 * E       day of week                  text          Tuesday; Tue
 * <p/>
 * y       year                         year          1996
 * D       day of year                  number        189
 * M       month of year                month         July; Jul; 07
 * d       day of month                 number        10
 * <p/>
 * a       halfday of day               text          PM
 * K       hour of halfday (0~11)       number        0
 * h       clockhour of halfday (1~12)  number        12
 * <p/>
 * H       hour of day (0~23)           number        0
 * k       clockhour of day (1~24)      number        24
 * m       minute of hour               number        30
 * s       second of minute             number        55
 * S       fraction of second           number        978
 * <p/>
 * z       time zone                    text          Pacific Standard Time; PST
 * Z       time zone offset/id          zone          -0800; -08:00; America/Los_Angeles
 * <p/>
 * '       escape for text              delimiter
 * ''      single quote                 literal
 * '
 * <p/>
 * TODO!!!!
 */
public class MyDate implements Serializable {
    public final static int MONDAY = 1;
    public final static int TUESDAY = 2;
    public final static int WEDNESDAY = 3;
    public final static int THURSDAY = 4;
    public final static int FRIDAY = 5;
    public final static int SATURDAY = 6;
    public final static int SUNDAY = 7;
    public static final int msInMinute = 60000;
    public static final int preOpenTimeAM = 9 * 60 + 15;
    public static final int openTimeAM = 9 * 60 + 30;
    public static final int closeTimeAM = 11 * 60 + 30;
    public static final int openTimePM = 13 * 60;
    public static final int closeTimePM = 15 * 60;
    public static String[] m1 = null;
    public static final String[] m5 = new String[]{"09:30:00",
            "09:35:00", "09:40:00", "09:45:00", "09:50:00", "09:55:00", "10:00:00",
            "10:05:00", "10:10:00", "10:15:00", "10:20:00", "10:25:00", "10:30:00",
            "10:35:00", "10:40:00", "10:45:00", "10:50:00", "10:55:00", "11:00:00",
            "11:05:00", "11:10:00", "11:15:00", "11:20:00", "11:25:00", "11:30:00",
            "13:05:00", "13:10:00", "13:15:00", "13:20:00", "13:25:00", "13:30:00",
            "13:35:00", "13:40:00", "13:45:00", "13:50:00", "13:55:00", "14:00:00",
            "14:05:00", "14:10:00", "14:15:00", "14:20:00", "14:25:00", "14:30:00",
            "14:35:00", "14:40:00", "14:45:00", "14:50:00", "14:55:00", "15:00:00"
    };
    public static final String[] m15 = new String[]{"09:30:00",
            "09:45:00", "10:00:00",
            "10:15:00", "10:30:00", "10:45:00", "11:00:00",
            "11:15:00", "11:30:00",
            "13:15:00", "13:30:00", "13:45:00", "14:00:00",
            "14:15:00", "14:30:00", "14:45:00", "15:00:00",
    };
    public static final String[] m30 = new String[]{"09:30:00",
            "10:00:00",
            "10:30:00", "11:00:00",
            "11:30:00",
            "13:30:00", "14:00:00",
            "14:30:00", "15:00:00",
    };
    private static final long serialVersionUID = 1L;
    private static final int millisOneDay = 24 * 60 * 60 * 1000;
    public static final int secondsOneDay = 24 * 60 * 60;
    private static final String DELIMITER = "-";
    public static String[] m60 = new String[]{"09:30:00",
            "10:30:00",
            "11:30:00",
            "14:00:00",
            "15:00:00",
    };
    private static DawLogger logger = DawLogger.getLogger(MyDate.class);
    private static String preOpenTime = "09:15:00";
    private static String openTime1 = "09:30:00";
    private static String closeTime1 = "11:30:00";
    private static String openTime2 = "13:00:00";
    private static String closeTime2 = "15:00:00";
    private static MyDate lastOpenDay = null;
    private static String lastTradeDay = null;
    private static String minTradeDay = null;
    private static MyDate today = null;
    private static HashSet<String> stockCalendar = null;
    private static MyDate lastCloseDay = null;
    //    Calendar c = GregorianCalendar.getInstance(); // current date
    DateTime in = new DateTime();
    private boolean readOnly = false;

    private static ClareBuffer clareBuffer = new ClareBuffer() {
        @Override
        public void clare() {
            logIt(MyDate.class);
            calendarChanged();
        }
    };

    private MyDate(boolean readOnly) {
        this.readOnly = readOnly;
    }


    public MyDate(Date d) {
        in = new DateTime(d);
    }

    public MyDate(String input) {
        if (input != null) {
            try {
                String dt[] = input.split(" ");
                setDate(dt[0]);
                if (dt.length > 1)
                    setTime(dt[1]);
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    public MyDate() {
    }

    public MyDate(Long millis) {
        in = new DateTime(millis);
    }

    public static MyDate getToday() {
        if (today == null) {
            today = new MyDate(true);
        } else {
            today.current();
        }
        return today;
    }

    public static String parseDate(String input) {
        String dt[] = input.split(" ");
        return dt[0];
    }

    public static MyDate getLastOpenDay() {
        initCalendar();

        MyDate lastOpenDay = new MyDate(false);
        try {
            if (lastOpenDay.beforOpenTime()) {
                lastOpenDay.nextNday(-1);
            }

            while (!lastOpenDay.isOpenDay()) {
                lastOpenDay.nextNday(-1);
            }
        } finally {

        }
        return lastOpenDay;
    }

    private static synchronized void initCalendar() {
        if (stockCalendar == null) {
            ResultSet rs = null;
            lastCloseDay = null;
            stockCalendar = new HashSet<String>();
            String sql = "select  CalendarDate, closed from stockCalendar  " +
                    //" where CalendarDate " +  // <= '" + getToday().getDate() + "'" +
                    " order by 1 desc ";
            Database db = DbStack.getDb(MyDate.class);
            try {
                rs = db.dynamicSQL(sql);
                while (rs.next()) {
                    lastTradeDay = rs.getObject(1).toString();
                    boolean closed = rs.getBoolean(2);
//                    if (minTradeDay == null) {
//                        minTradeDay = lastTradeDay;
//                    }
                    stockCalendar.add(lastTradeDay);
                    if (lastCloseDay == null && closed) {
                        lastCloseDay = new MyDate(lastTradeDay);
                    }
                }
                minTradeDay = lastTradeDay;
            } catch (Exception e) {
                logger.error(e);
            } finally {
                Database.closeResultSet(rs);
                DbStack.closeDB(db);
            }
        }
    }

    public static MyDate getLastClosedDay() {
        initCalendar();
        return lastCloseDay;
    }

    public static void setLastClosedDay(String date) {
        String update = "update stockCalendar set closed = 1 where calendarDate <= '" + date + "'";
        Database db = DbStack.getDb(MyDate.class);
        try {
            db.executeUpdate(update);
            lastCloseDay.setDate(date);
        } catch (DBException e) {
            logger.error(e);
        } finally {
            DbStack.closeDB(db);
        }
    }

    private static String[] getM1Section() {
        if (m1 != null) return m1;
        m1 = new String[242];
        m1[0] = "09:15:00";

//        LinkedList<String> section = new LinkedList<>();
        MyDate t = new MyDate();
        t.setTime("09:30:00");
        int i = 1;
        while (t.isBeforeTime("11:31:00")) {
            m1[i++] = t.getTime();
//            System.out.println(m1[i-1]);
            t.addTimeInMinuts(1);
        }
        t.setTime("13:01:00");
        while (t.isBeforeTime("15:01:00")) {
            m1[i++] = t.getTime();
//            System.out.println(m1[i-1]);
            t.addTimeInMinuts(1);
        }
        return m1;
    }

    public static String[] getTimeSection(int period) {
        String[] times = null;
        switch (period) {
            case 1:
                return getM1Section();
            case 5:
                times = m5;
                break;
            case 15:
                times = m15;
                break;
            case 30:
                times = m30;
                break;
            case 60:
                times = m60;
                break;
        }
        return times;
    }

    public static MyDate getFirstWorkDayOfMonth(MyDate date) {
        int year = date.getYEAR();
        int month = date.getMONTH() + 1;
        MyDate d = new MyDate(year + "-" + month + "-01");
        while (!d.isOpenDay()) {
            d.advance_day();
        }
        return d;
    }

    public static int getElapsedTradeMinuts(MyDate a, MyDate d) {
        return getElapsedTradeMinuts(a.getHour() * 60 + a.getMinute(), d.getHour() * 60 + d.getMinute());
    }

    public static int getElapsedTradeMinuts(int a, int d) {

        if (d == a) {
            return 0;
        }

        //if (dM > aM)
        int B = d;
        int b = a;

        if (a > d) {
            B = a;
            b = d;
        }

        if (b < closeTimeAM) {
            if (B > openTimePM) {
                return (B - openTimePM) + (closeTimeAM - b);
            } else if (B > closeTimeAM) {
                return (closeTimeAM - b);
            }
        }
        return B - b;
    }

    ///////////////////////////////////////
    public static boolean IS_BEFORE_TIME(String time) {
        today.now();
        return today.isBeforeTime(time);
    }

    public static boolean IS_BEFORE_TIME(int hour, int minute, int second) {
        today.now();
        return today.isBeforeTime(hour, minute, second);
    }

    public static boolean IS_AFTER_TIME(int hour, int minute, int second) {
        today.now();
        return today.isAfterTime(hour, minute, second);
    }

    public static boolean IS_AFTER_TIME(String time) {
        today.now();
        return today.isAfterTime(time);
    }

    public static String getOpenTime1() {
        return openTime1;
    }

    public static void setOpenTime1(String openTime1) {
        MyDate.openTime1 = openTime1;
    }

    public static String getCloseTime1() {
        return closeTime1;
    }

    public static void setCloseTime1(String closeTime1) {
        MyDate.closeTime1 = closeTime1;
    }

    public static String getOpenTime2() {
        return openTime2;
    }

    public static void setOpenTime2(String openTime2) {
        MyDate.openTime2 = openTime2;
    }

    public static String getCloseTime2() {
        return closeTime2;
    }

    public static void setCloseTime2(String closeTime2) {
        MyDate.closeTime2 = closeTime2;
    }

    public static String getPreOpenTime() {
        return preOpenTime;
    }

    public static void setPreOpenTime(String preOpenTime) {
        MyDate.preOpenTime = preOpenTime;
    }

    public static String getMonthNo(String m) {
        String M = m.toUpperCase().trim();
        if (M.startsWith("JAN")) {
            return "01";
        } else if (M.startsWith("FEB")) {
            return "02";
        } else if (M.startsWith("MAR")) {
            return "03";
        } else if (M.startsWith("APR")) {
            return "04";
        } else if (M.startsWith("MAY")) {
            return "05";
        } else if (M.startsWith("JUN")) {
            return "06";
        } else if (M.startsWith("JUL")) {
            return "07";
        } else if (M.startsWith("AUG")) {
            return "08";
        } else if (M.startsWith("SEP")) {
            return "09";
        } else if (M.startsWith("OCT")) {
            return "10";
        } else if (M.startsWith("NOV")) {
            return "11";
        } else if (M.startsWith("DEC")) {
            return "12";
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        MyDate test1 = new MyDate("2007-11-18");
        test1.print();
        MyDate test2 = new MyDate("2008-3-30");
        test2.print();
        System.out.println(test1.compareDays(test2));
    }


    public static void calendarChanged() {
        stockCalendar = null;
    }

    private void current() {
        in = new DateTime();
    }

    public MyDate now() {
        in = new DateTime();
        return this;
    }


    public void futureOpenDay() {
        do {
            this.advance_day();
        } while (!this.isOpenDay());
    }

    public synchronized void setToFutureOpenDay() {
        Database db = DbStack.getDb(MyDate.class);
        try {
            setToFutureOpenDay(db);
        } finally {
            DbStack.closeDB(db);
        }
    }

    public synchronized void setToFutureOpenDay(Database db) {
        String sql = "select  CalendarDate from stockCalendar  " +
                " where CalendarDate  > '" + getDate() + "'" +
                " order by CalendarDate " +
                " limit 1 ";


        ResultSet rs = null;


        try {
            rs = db.dynamicSQL(sql);
            if (rs.next()) {
                String nextTradeDay = rs.getObject(1).toString();
                this.setDate(nextTradeDay);
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            Database.closeResultSet(rs);
        }
    }

    public boolean setTime(long input) {
        String times = String.valueOf(input);
        int hour = 0;
        int min = 0;
        int sec = 0;
        int mms = 0;

        try {
            while (times.length() < 9) {
                times = "0" + times;
            }
            hour = NU.parseInt(times.substring(0, 2), 0);
            min = NU.parseInt(times.substring(2, 4), 0);
            sec = NU.parseInt(times.substring(4, 6), 0);
            if (times.length() > 6) {
                mms = NU.parseInt(times.substring(6), 0);
            }
            in = in.withTime(hour, min, sec, mms);
            return true;
        } catch (Exception e) {

        }
        return false;
    }

    public long getTimeInMillis() {
        return in.getMillis();
    }

    public void addTimeInSecond(int ms) {
        in = in.plusSeconds(ms);

    }

    public void addTimeInMillis(int mms) {
        in = in.plusMillis(mms);
    }

    public void addDays(int ds) {
        in = in.plusDays(ds);
    }

    public void addYear(int ys) {
        in = in.plusYears(ys);
    }

    public void addMonths(int ms) {
        in = in.plusMonths(ms);
    }

    public int openDayCount(String date) {
        MyDate d_A = new MyDate(this.getDate());
        MyDate d_B = new MyDate(date);
        int i = 1;
        int count = 0;

        if (d_B.compareDays(d_A) > 0) {
            i = -1;
            MyDate d = d_B;
            d_B = d_A;
            d_A = d;
        }
        while (d_B.compareDays(d_A) < 0) {
            count++;
            if (d_B.isEquals(getLastOpenDay())) {
                break;
            }
            d_B.nextOpenDay();
        }
        return i * count;
    }

    public String toString() {
        return this.getDate() + " " + this.getTime();
    }

    public boolean setDate(Calendar d) {
        in = new DateTime(d);
        return true;
    }

    public boolean setDate(String input) {
        int year;
        int month;
        int day;
        if (readOnly) {
            logger.error(new Error("READONLY TODAY CAN NOT BE CHANGED!"));
            System.out.println("READONLY TODAY CAN NOT BE CHANGED!");
            System.exit(1);
            return false;
        }

        if (input == null || "".equals(input)) return false;
        if ("today".equalsIgnoreCase(input)) {
            current();
            return true;
        }

        String dtDele = "/- :";
        StringTokenizer tok = new StringTokenizer(input, dtDele);
        try {
            year = Integer.parseInt(tok.nextToken());
            month = Integer.parseInt(tok.nextToken());
            day = Integer.parseInt(tok.nextToken());
            in = in.withDate(year, month, day);
        } catch (Exception e) {
            return setDate_(input);
        }
        return true;
    }


    public boolean setDateTime(String input) {
        int year;
        int month;
        int day;
        String dtDele = "- :.";
        if (readOnly) {
            logger.error(new Error("READONLY TODAY CAN NOT BE CHANGED!"));
            System.exit(1);
            return false;
        }

        if (input == null || "".equals(input)) return false;

        StringTokenizer tok = new StringTokenizer(input, dtDele);
        try {

            year = Integer.parseInt(tok.nextToken());
            month = Integer.parseInt(tok.nextToken());
            day = Integer.parseInt(tok.nextToken());
            in = in.withDate(year, month, day);

            return setTime(tok);
        } catch (Exception e) {
            logger.error(e);
        }
        return true;
    }

    private boolean setDate_(String input) {
        int year;
        int month;
        int day;
        if (readOnly) {
            logger.error(new Error("READONLY TODAY CAN NOT BE CHANGED!"));
            System.exit(1);
            return false;
        }

        if (input == null || "".equals(input)) return false;

        StringTokenizer tok = new StringTokenizer(input);
        try {
            year = Integer.parseInt(tok.nextToken(DELIMITER));
            month = Integer.parseInt(tok.nextToken(DELIMITER));
            day = Integer.parseInt(tok.nextToken(DELIMITER));
            in = in.withDate(year, month, day);
        } catch (NumberFormatException e) {
            logger.error(e);
            return false;
        }
        return true;
    }

    public boolean setTime(StringTokenizer tok) {
        int hour = 0;
        int min = 0;
        int sec = 0;
        int mms = 0;
        String val;

        try {

            if (tok.hasMoreTokens()) {
                val = tok.nextToken(":");
                val = val.trim();
                if (val.startsWith("0")) val = val.substring(1, 2);
                hour = new Integer(val);
            }

            if (tok.hasMoreTokens()) {
                val = tok.nextToken(":");
                val = val.trim();
                if (val.startsWith("0")) val = val.substring(1, 2);
                min = new Integer(val);
            }

            if (tok.hasMoreTokens()) {
                val = tok.nextToken(":");
                val = val.trim();
                if (val.startsWith("0")) val = val.substring(1, 2);
                sec = new Float(val).intValue();
            }

            if (tok.hasMoreTokens()) {
                val = tok.nextToken(".");
                val = val.trim();
                while (val.startsWith("0")) val = val.substring(1);
                mms = new Float(val).intValue();
            }

            in = in.withTime(hour, min, sec, mms);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean setTime(String input) {
        int hour = 0;
        int min = 0;
        int sec = 0;
        int mms = 0;

        if (input == null || "".equals(input)) return false;

        String ts[] = input.split(":");

        try {
            if (ts.length > 0) hour = Integer.parseInt(ts[0]);
            if (ts.length > 1) min = Integer.parseInt(ts[1]);
            if (ts.length > 2) {
                if (ts[2].indexOf(".") > 0) {
                    sec = Integer.parseInt(ts[2].substring(0, ts[2].indexOf(".")));
                    mms = Integer.parseInt(ts[2].substring(ts[2].indexOf(".") + 1));
                } else {
                    sec = Integer.parseInt(ts[2]);
                }
            }

        } catch (NumberFormatException e) {
            logger.error(e);
            return false;
        }
        in = in.withTime(hour, min, sec, mms);
        return true;
    }

    public void setHOUR_OF_DAY(int hour) {
        in = in.withHourOfDay(hour);
    }

    public boolean isToday() {
        return getToday().isEquals(this);
    }

    public boolean beforToday() {
        DateTimeComparator comp = DateTimeComparator.getDateOnlyInstance();
        return comp.compare(in, new DateTime()) < 0;
    }

    public boolean beforDay(String date) {
        DateTimeComparator comp = DateTimeComparator.getDateOnlyInstance();
        return comp.compare(in, new DateTime(date)) < 0;
    }

    public boolean afterDay(String date) {
        DateTimeComparator comp = DateTimeComparator.getDateOnlyInstance();
        return comp.compare(in, new DateTime(date)) > 0;
    }

    public boolean afterToday() {
        return this.compareDays(getToday()) > 0;
    }

    public void print() {
        if (isWeekEnd()) {
            logger.debug(" Week end " + in.dayOfWeek() + "  ");
        }

        logger.debug(this.getDate());
    }

    public void advance_day() {
        if (readOnly) {
            logger.error(new Error("READONLY TODAY CAN NOT BE CHANGED!"));
            System.exit(1);
            return;
        }
        in = in.plusDays(1);
    }

    public void nextMonth() {
        if (readOnly) {
            logger.error(new Error("READONLY TODAY CAN NOT BE CHANGED!"));
            System.exit(1);
            return;
        }
        in = in.plusMonths(1);
    }

    public int NextWeekNo(int amount) {
        if (readOnly) {
            logger.error(new Error("READONLY TODAY CAN NOT BE CHANGED!"));
            System.exit(1);
        }

        return in.plusMonths(1).getWeekOfWeekyear();
    }

    public void nextWeek(int n) {
        if (readOnly) {
            logger.error(new Error("READONLY TODAY CAN NOT BE CHANGED!"));
            System.exit(1);
        }
        in = in.plusWeeks(n);
    }

    public void nextMonth(int n) {
        if (readOnly) {
            logger.error(new Error("READONLY TODAY CAN NOT BE CHANGED!"));
            System.exit(1);
            return;
        }
        in = in.plusMonths(n);
    }

    public void nextNday(int n) {
        if (readOnly) {
            logger.error(new Error("READONLY TODAY CAN NOT BE CHANGED!"));
            System.exit(1);
            return;
        }
        if (n == 0) return;
        in = in.plusDays(n);
    }

    public void nextOpenDay() {
        do {
//            if (this.isToday()) break;
            this.advance_day();
        } while (!this.isOpenDay());
    }

    public void nextTradeTime(int period) {
        String[] times = getTimeSection(period);
        if (getTime().equals(times[times.length - 1])) {
            this.nextOpenDay();
            this.setTime(times[1]);
        } else {
            for (int i = 0; i < (times.length - 1); i++) {
                if (getTime().equals(times[i])) {
                    this.setTime(times[i + 1]);
                    break;
                }
            }
        }
    }

    public void addTimeInMinuts(int minuts) {
        if (readOnly) {
            logger.error(new Error("READONLY TODAY CAN NOT BE CHANGED!"));
            System.exit(1);
        }
        in = in.plusMinutes(minuts);
    }

    public void nextOpenTime(int m) {
        if (m != 0)
            addTimeInMinuts(m);

//        int Minuts = this.getHour() * 60 + this.getMinute();
        int minute = in.getMinuteOfDay();

        if (minute < openTimeAM) {
            this.setTime(getOpenTime1());
        } else if (minute >= closeTimeAM && minute < openTimePM) {
            this.setTime(getOpenTime2());
        } else if (minute >= closeTimePM) {
            this.setTime(MyDate.getOpenTime1());
            this.advance_day();
            while (!this.isOpenDay() && this.compareDays(getToday()) < 0)
                this.advance_day();
        }
    }

    public void backwardOpenDayes(int n) {
        int openDayies = 0;
        while (openDayies < n) {
            nextNday(-1);

            if (isOpenDay()) {
                openDayies++;
            }
        }
    }

    public void forwardOpenDayes(int n) {
        int openDayies = 0;
        while (openDayies < n) {
            if (isToday()) return;

            nextNday(1);

            if (isOpenDay()) {
                openDayies++;
            }
        }
    }

    public boolean isEquals(MyDate input) {
        return this.compareDays(input) == 0;
    }

    Calendar getCalendar() {
        return in.toCalendar(Locale.getDefault());
    }

    public int getSeazen() {
        return 1 + (getMONTH()) / 3;
    }

    public Date getCDate() {
        return getCalendar().getTime();
    }

    public String getDate() {
        return in.toString("yyyy-MM-dd");
    }

    public void setDate(Date date) {
        in = new DateTime(date);
    }

    public String getTime() {
        return in.toString("HH:mm:ss");
    }

    public void setTime(Date d) {
        in = new DateTime(d);
    }

    public String getTime2() {
        return in.toString("HH:mm:ss.SS");
    }

    public String getTime3() {
        return in.toString("HH:mm:ss.SSS");
    }

    public String getTimeMms() {
        return in.toString();
    }

    public long getMillisOfDay() {
        return in.getMillisOfDay();
    }

    public String format(String formatString) {
        return in.toString(formatString);
    }

    /**
     * @param format yyyy-MM-dd HH:mm:ss.SSS
     * @return
     */
    public String getDateTimeFormat(String format) {
        return in.toString(format);
    }

    public String getDateTime() {
        return in.toString("yyyy-MM-dd HH:mm:ss");
    }

    public int getHour() {
        return in.getHourOfDay();
    }

    public int getYEAR() {
        return in.getYear();
    }

    public int getMONTH() {
        return in.getMonthOfYear() - 1;
    }

    public int getMinute() {
        return in.getMinuteOfHour();
    }

    public void setMinute(int min) {
        if (min > 59) min = 59;
        in = in.withMinuteOfHour(min);
    }

    public int getSecond() {
        return in.getSecondOfMinute();
    }

    public int getMillisOfSecond() {
        return in.getMillisOfSecond();
    }

    public void setSecond(int ss) {
        if (ss > 59) ss = 59;
        in = in.withSecondOfMinute(ss);
    }

    public void setMilliSecond(int ms) {
        if (ms > 1000) return;
        in = in.withMillisOfSecond(ms);
    }

    public boolean isOpenDay() {
        initCalendar();
        if (this.compareDays(minTradeDay) < 0) return true;
//        if (this.compareDays(getToday()) > 0) return false;
        return (stockCalendar.contains(this.getDate()));
    }

    public boolean willBeOpenDay() {
        initCalendar();
        if (this.compareDays(minTradeDay) < 0) return true;
        if (this.compareDays(getToday()) <= 0) return false;
        boolean debug = stockCalendar.contains(this.getDate());
        return (stockCalendar.contains(this.getDate()));
    }

    public boolean isWeekEnd() {
        return getWeekDay() == Calendar.SATURDAY || getWeekDay() == Calendar.SUNDAY;
    }

    public boolean isFriday() {
        return getWeekDay() == Calendar.FRIDAY;
    }

    public boolean isMonday() {
        return getWeekDay() == Calendar.MONDAY;
    }

    public int getDayofYEAR() {
        return in.getDayOfYear();
    }

    public int getWeek_of_YEAR() {
        return in.getWeekOfWeekyear();
    }

    public int getDAY_OF_MONTH() {
        return in.getDayOfMonth();
    }

    /**
     * 兼容于 Calendar
     */
    public int getWeekDay() {
        int wd = in.getDayOfWeek();
        if (wd == 7) {
            wd = 1;
        } else {
            wd++;
        }
        return wd;
    }

    public int compareDays(MyDate d) {
        DateTimeComparator comp = DateTimeComparator.getDateOnlyInstance();
        return comp.compare(in, d.in);
    }

    public long getDaysDiff(MyDate d) {
        return Days.daysBetween(d.in, in).getDays();
    }

    public long getDaysDiff(String d) {
        return Days.daysBetween(new MyDate(d).in, in).getDays();
    }

    public int compareDays(String d) {
        return this.compareDays(new MyDate(d));
    }

    public boolean isPassedTime() {
        return !isFutureByTime();
    }

    public boolean isFutureByTime() {
        MyDate d = getToday();
        return compareTimeIgnoreDate(d) > 0;
    }

    public boolean isPassedIgnoreDate(String time) {
        MyDate d = new MyDate();
        d.setTime(time);
        return compareTimeIgnoreDate(d) > 0;
    }

    /**
     * @param time
     * @return
     */
    public long compareTimeIgnoreDate(String time) {
        MyDate d = new MyDate();
        d.setTime(time);
        DateTimeComparator comp = DateTimeComparator.getTimeOnlyInstance();
        return comp.compare(in, d.in);
    }

    public long compareTimeIgnoreDate(MyDate d) {
        DateTimeComparator comp = DateTimeComparator.getTimeOnlyInstance();
        return comp.compare(in, d.in);
    }

    public long compareMinuts(MyDate d) {
        return (in.getMillis() - d.in.getMillis()) / (60 * 1000);
    }

    public long compareSeconds(String time) {
        MyDate d = new MyDate();
        d.setTime(time);
        return compareSeconds(d);
    }

    public long compareSeconds(MyDate d) {
        return (in.getMillis() - d.in.getMillis()) / 1000;
    }

    public long compareMmSeconds(MyDate d) {
        return (in.getMillis() - d.in.getMillis());
    }

    public long compareMmSeconds(String time) {
        MyDate d = new MyDate();
        d.setTime(time);
        return compareMmSeconds(d);
    }

    public boolean isLastWorkingDayOfSeazen() {
//        int m = this.getMONTH();
//        if (m == Calendar.MARCH || m == Calendar.JUNE || m == Calendar.SEPTEMBER || m == Calendar.DECEMBER) {
//            int maxD = c.getActualMaximum(Calendar.DAY_OF_MONTH);
//            if (maxD == c.get(Calendar.DAY_OF_MONTH)) return true;
//            if (MyDate.getToday().getMONTH() > m) return true;
//        }
        return false;
    }

    public boolean beforOpenTime() {
        if (isToday()) {
            return isBeforeTime(9, 15, 0);
        }
        return false;
    }

    public boolean afterClosedTime() {
        return afterClosedTime(0);
    }

    public boolean afterClosedTime(int afteMinuts) {
        if (beforToday()) return true;
        if (isWeekEnd()) return true;

        if (isToday()) {
            int h = getHour();
            int m = getMinute();
            if (getHour() > 15) return true;
            if (getHour() == 15 && getMinute() > afteMinuts) return true;
        }
        return false;
    }

    public boolean isBeforeTime(int hour, int minute, int second) {
        long secondsOfDay = hour * 3600 + minute * 60 + second;
        return this.getSecondOfDay() < secondsOfDay;
    }

    public boolean isBeforeTime(String time) {
        try {
            String[] hms = time.split(":");
            return isBeforeTime(Integer.parseInt(hms[0]), Integer.parseInt(hms[1]), Integer.parseInt(hms[2]));
        } catch (Exception e) {

        }
        return false;
    }

    public boolean isAfterTime(int hour, int minute, int second) {
        long secondsOfDay = hour * 3600 + minute * 60 + second;
        return this.getSecondOfDay() >= secondsOfDay;
    }

    public boolean isAfterTime(String time) {
        try {
            String[] hms = time.split(":");
            return isAfterTime(Integer.parseInt(hms[0]), Integer.parseInt(hms[1]), Integer.parseInt(hms[2]));
        } catch (Exception e) {

        }
        return false;
    }

    ///////////////////////////////////////
    public int getSecondOfDay() {
        return in.getSecondOfDay();
    }

    /**
     * 开市时间
     * 09:15-09:25
     * 09:30-11:30
     * 13:00-15:00
     *
     * @return deviation Seconds
     */
    public boolean isOpenTime() {
        return isOpenTime(0);
    }

    public boolean isOpenTime(int deviation) {
        boolean opening = false;
        if (this.isToday() && this.isOpenDay()) {
            if (!beforeAmAuctionOpenTime(deviation) && !afterAmAuctionCloseTime(deviation)) {
                return true;
            } else if ((!beforeAmOpenTime(deviation) && !afterAmCloseTime(deviation))) {
                return true;
            } else if ((!beforePmOpenTime(deviation) && !afterPmCloseTime(deviation))) {
                return true;
            }
        }
        return opening;
    }

    public boolean isOpenTimePM() {
        boolean opening = false;
        long timeInseconds = getHour() * 60 + getMinute();
        if (this.isToday() && this.isOpenDay()) {
            if ((timeInseconds >= preOpenTimeAM && timeInseconds <= closeTimeAM)) {
                opening = true;
            } else if ((timeInseconds >= openTimePM && timeInseconds <= closeTimePM)) {
                opening = true;
            }
        }
        return opening;
    }

    public boolean regulateTime(int intevel) {
        int tStart = preOpenTimeAM;
        int tEnd = closeTimeAM;

        int currentTime = getHour() * 60 + getMinute();
        if (currentTime < preOpenTimeAM) {
            setTime(MyDate.getPreOpenTime());
            return true;
        }
        if (currentTime >= closeTimeAM && currentTime < openTimePM) {
            setTime(MyDate.getOpenTime2());
            return true;
        }
        if (currentTime > closeTimePM + intevel) {
            return false;
        }

        if (currentTime >= openTimePM) {
            tStart = openTimePM;
            tEnd = closeTimePM;
        }

        if (intevel == 0) return true;

        for (int i = tStart; i < tEnd; i += intevel) {
            if (currentTime >= i && currentTime < i + intevel) {
                if (intevel == 1) i++;
                int h = i / 60;
                int m = i % 60;  // 60分钟的余数
                String minute = (h < 10 ? "0" + h : "" + h) + (m < 10 ? ":0" + m : ":" + m) + ":00";
                setTime(minute);
                return true;
            }
        }
        return false;
    }

    /**
     * |<--
     *
     * @param deviation
     * @return
     */
    public boolean beforeAmAuctionOpenTime(int deviation) {
        if (deviation < 0 || deviation > 59) deviation = 0;
        if (getHour() == 9) {
            if (getMinute() == 14) {
                return getSecond() <= (59 - deviation);
            } else {
                return getMinute() < 14;
            }
        } else {
            return getHour() < 9;
        }
    }

    public boolean beforeAmAuctionOpenTime() {
        return beforeAmAuctionOpenTime(0);
    }

    public boolean afterAmAuctionCloseTime(int deviation) {
        if (deviation < 0 || deviation > 59) deviation = 0;
        if (getHour() == 9) {
            if (getMinute() == 25) {
                return getSecond() >= (deviation);
            } else {
                return getMinute() > 25;
            }
        } else {
            return getHour() > 9;
        }
    }

    public boolean afterAmAuctionCloseTime() {
        return afterAmAuctionCloseTime(0);
    }

    public boolean beforeAmOpenTime(int deviation) {
        if (deviation < 0 || deviation > 59) deviation = 0;
        if (getHour() == 9) {
            if (getMinute() == 29) {
                return getSecond() <= (59 - deviation);
            } else {
                return getMinute() < 29;
            }
        } else {
            return getHour() < 9;
        }
    }

    public boolean beforeAmOpenTime() {
        return beforeAmOpenTime(0);
    }

    public boolean afterAmCloseTime(int deviation) {
        if (deviation < 0 || deviation > 59) deviation = 0;
        if (getHour() == 11) {
            if (getMinute() == 30) {
                return getSecond() >= deviation;
            } else {
                return getMinute() > 30;
            }
        } else {
            return getHour() > 11;
        }
    }

    public boolean afterAmCloseTime() {
        return afterAmCloseTime(0);
    }

    public boolean beforePmOpenTime(int deviation) {
        if (deviation < 0 || deviation > 59) deviation = 0;
        if (getHour() == 12) {
            if (getMinute() == 59) {
                return getSecond() <= (59 - deviation);
            } else {
                return getMinute() < 59;
            }
        } else {
            return getHour() < 12;
        }
    }

    public boolean beforePmOpenTime() {
        return beforePmOpenTime(0);
    }

    public boolean afterPmCloseTime() {
        return afterPmCloseTime(0);
    }

    public boolean afterPmCloseTime(int deviation) {
        if (deviation < 0 || deviation > 59) deviation = 0;
        if (getHour() == 15) {
            if (getMinute() < 1) {
                int s = getSecond();
                return getSecond() >= deviation;
            } else {
                return true;
            }
        } else {
            return getHour() > 15;
        }
    }

    public boolean beforePmActionStartTime() {
        return beforePmActionStartTime(0);
    }

    public boolean beforePmActionStartTime(int deviation) {
        if (deviation < 0 || deviation > 59) deviation = 0;
        if (getHour() == 14) {
            if (getMinute() == 56) {
                return getSecond() < (59 - deviation);
            } else {
                return getMinute() < 56;
            }
        } else {
            return getHour() < 15;
        }
    }

    public boolean isAmActionTime() {
        return !beforeAmAuctionOpenTime() && !afterAmAuctionCloseTime();
    }

    public boolean isPmActionTime() {
        return !afterPmCloseTime() && !beforePmActionStartTime();
    }

    public void getFutureOpenDay() {
        String sql = "select  CalendarDate from stockCalendar  " +
                " where CalendarDate  > '" + getToday().getDate() + "'" +
                " order by CalendarDate " +
                " limit 1 ";

        Database db = AppManager.getApp().getDatabase(MyDate.class);
        AppManager.getApp().tryOpendb(db);
        ResultSet rs = null;

        try {
            rs = db.dynamicSQL(sql);
            while (rs.next()) {
                String nextTradeDay = rs.getObject(1).toString();
                this.setDate(nextTradeDay);
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            Database.closeResultSet(rs);
            db.close();
        }
    }

    //    ///////////////////////////////////////////////////////////
//    public static boolean NowIsBeforeAmAuctionCloseTime() {
//        today = getToday();
//        if (today.getHour() == 9) {
//            return today.getMinute() < 25;
//        } else {
//            return today.getHour() < 9;
//        }
//    }
//
//    public static boolean NowIsBeforeAmAuctionOpenTime() {
//        today = getToday();
//        if (today.getHour() == 9) {
//            return today.getMinute() < 15;
//        } else {
//            return today.getHour() < 9;
//        }
//    }
//
//    public static boolean NowIsBeforeAmOpenTime() {
//        today = getToday();
//        if (today.getHour() == 9) {
//            return today.getMinute() < 30;
//        } else {
//            return today.getHour() < 9;
//        }
//    }
//
//
//    public static boolean NowIsBeforeAmCloseTime() {
//        today = getToday();
//        if (today.getHour() == 11) {
//            return today.getMinute() < 30;
//        } else {
//            return today.getHour() < 11;
//        }
//    }
//
//    public static boolean NowIsInOpenTime() {
//        return (NowIsBeforeAmCloseTime() && !NowIsBeforeAmAuctionOpenTime()) ||
//                (NowIsBeforePmCloseTime() && !NowIsBeforePmOpenTime());
//    }
//
//    public static boolean NowIsBeforePmOpenTime() {
//        return getToday().getHour() < 13;
//    }
//
//    public static boolean NowIsBeforePmCloseTime() {
//        return getToday().getHour() < 15;
//    }
//
//    public static boolean NowIsAfterPmActionStartTime() {
//        return getToday().getHour() > 14 &&
//                getToday().getHour() < 15 &&
//                getToday().getMinute() > 56;
//    }
//
//    public boolean NowIsAmActionTime(){
//        return !NowIsBeforeAmAuctionOpenTime() && NowIsBeforeAmAuctionCloseTime();
//    }
//
//    public boolean NowIsPmActionTime(){
//        return !NowIsBeforePmCloseTime() && NowIsAfterPmActionStartTime();
//    }
}

