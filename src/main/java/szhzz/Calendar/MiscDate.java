package szhzz.Calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: vmuser
 * Date: 2007-11-30
 * Time: 20:12:21
 * To change this template use File | Settings | File Templates.
 */
public class MiscDate {

    private static Date d1 = new Date();

    public static String todaysDate() {
        Calendar C = new GregorianCalendar();

        return
                C.get(Calendar.YEAR) + "-" +
                        (C.get(Calendar.MONTH) + 1) + "-" +
                        C.get(Calendar.DAY_OF_MONTH) + " " +
                        C.get(Calendar.HOUR_OF_DAY) + ":" +
                        C.get(Calendar.MINUTE) + ":" +
                        C.get(Calendar.SECOND) + "." +
                        C.get(Calendar.MILLISECOND);
//                        (Calendar.AM == C.get(Calendar.AM_PM) ? "AM" : "PM");
    }

    public static long mms() {
        Calendar C = new GregorianCalendar();
        return C.get(Calendar.MILLISECOND);
    }

    public static String timeMM() {
        Calendar C = new GregorianCalendar();

        return
                C.get(Calendar.HOUR_OF_DAY) + ":" +
                        C.get(Calendar.MINUTE) + ":" +
                        C.get(Calendar.SECOND) + "." +
                        C.get(Calendar.MILLISECOND);
    }

    public static String now() {
        Calendar C = new GregorianCalendar();
        return
                C.get(Calendar.YEAR) + "-" +
                        (C.get(Calendar.MONTH) + 1) + "-" +
                        C.get(Calendar.DAY_OF_MONTH) + " " +
                        C.get(Calendar.HOUR_OF_DAY) + ":" +
                        C.get(Calendar.MINUTE) + "." +
                        C.get(Calendar.SECOND);
    }

    public static String DateString() {
        Calendar C = new GregorianCalendar();
        return C.get(Calendar.YEAR) + "-" +
                (C.get(Calendar.MONTH) + 1) + "-" +
                C.get(Calendar.DAY_OF_MONTH) + "";
    }

    public synchronized static long timeCPU() {
        Date d2 = new Date();
        long elapsed_time = d2.getTime() - d1.getTime();
        d1 = d2;
        return elapsed_time;
    }

    public synchronized static long elapsTime() {
        Date d2 = new Date();
        return d2.getTime() - d1.getTime();
    }

    public static String timeStamp() {
        Calendar C = new GregorianCalendar();
        String s = Integer.toString(C.get(Calendar.MONTH) + 1);

        return s + C.get(Calendar.DAY_OF_MONTH) +
                C.get(Calendar.HOUR) +
                C.get(Calendar.MINUTE) +
                C.get(Calendar.SECOND);
    }
}
