package szhzz.Utils;

/**
 * Created by Administrator on 13-12-8.
 */
public class NU {

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

    public static int parseInt(Object o, int defaultValue) {
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


}
