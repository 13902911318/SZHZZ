package szhzz.Utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * Created with IntelliJ IDEA.
 * User: HuangFang
 * Date: 13-11-6
 * Time: 下午8:11
 * To change this template use File | Settings | File Templates.
 */
public class FT {
    private static DecimalFormat fTk00 = new DecimalFormat("##,##0.00");
    private static DecimalFormat fT00 = new DecimalFormat("##0.00");
    private static DecimalFormat fT0000 = new DecimalFormat("##0.0000");
    private static DecimalFormat fTk = new DecimalFormat("##,##0");
    private static DecimalFormat fT = new DecimalFormat("##0.00");
    private static SimpleDateFormat fTDate = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat fTTime = new SimpleDateFormat("HH:mm:ss");

    private static DecimalFormat fTLong = new DecimalFormat("##0");

    public static String formatLong(Number n) {
        return fTLong.format(n);
    }

    public static String format00(Number n) {
        return fT00.format(n);
    }

    public static String formatK00(Number n) {
        return fTk00.format(n);
    }

    public static String format0000(Number n) {
        return fT0000.format(n);
    }

    public static String format(Number n) {
        return fT.format(n);
    }

    public static String formatK(Number n) {
        return fTk.format(n);
    }

    public static String formatDate(String n) {
        return fTDate.format(n);
    }

    public static String formatTime(String n) {
        return fTTime.format(n);
    }
}
