package szhzz.Utils;

import java.math.BigDecimal;

/**
 * Created by Administrator on 13-12-8.
 */
public class NU {



    public static boolean isDouble(Object o) {
        return (o != null && o.toString().contains("."));
    }


    public static Double parseDouble(Object o, Double defaultValue) {
        if (o == null) return defaultValue;
        if (o instanceof Double) {
            if (((Double) o).compareTo(Double.NaN) == 0) {
                return defaultValue;
            }
            return (Double) o;
        }

        try {
            return Double.parseDouble(o.toString());
        } catch (NumberFormatException e) {
            try {
                return Double.parseDouble(o.toString().replace(",", "").trim());
            } catch (NumberFormatException e2) {

            }
        }
        return defaultValue;
    }

    public static int parseInt(Object o, Integer defaultValue) {
        return parseLong(o, (long) defaultValue).intValue();
    }

    public static Long parseLong(Object o, Long defaultValue) {
        if (o == null) return defaultValue;
        if (o instanceof Long) {
            return (Long) o;
        }

        try {
            return Long.parseLong(o.toString());
        } catch (NumberFormatException e) {
            try {
                Double v;
                if (defaultValue == null) {
                    v = parseDouble(o, null);
                } else {
                    v = parseDouble(o, defaultValue.doubleValue());
                }
                if (v != null) {
                    return v.longValue();
                }
            } catch (NumberFormatException e1) {

            }
        }
        return defaultValue;
    }

    public static boolean isNumber(Object o) {
        if (o == null) return false;
        try {
            double a = Double.parseDouble(o.toString());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static int compare(Number v1, Number v2, int scale){
        double v1_ = Double.parseDouble(v1.toString());
        double v2_ = Double.parseDouble(v2.toString());;
        BigDecimal b1 = BigDecimal.valueOf(v1_).setScale(scale, BigDecimal.ROUND_HALF_UP);
        BigDecimal b2 = BigDecimal.valueOf(v2_).setScale(scale, BigDecimal.ROUND_HALF_UP);
        return  b1.compareTo(b2);
    }

}
