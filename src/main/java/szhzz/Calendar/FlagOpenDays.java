package szhzz.Calendar;

import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXMonthView;
import szhzz.Utils.DawLogger;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 *
 * @author HuangFang
 * Date 2022/9/12
 * Time 10:18
 */
public class FlagOpenDays {
    private static DawLogger logger = DawLogger.getLogger(FlagOpenDays.class);

    public static void flagOpenDays(JXDatePicker calenda){
        flagOpenDays(calenda, null);
    }
    public static void flagOpenDays(JXDatePicker calenda, MyDate d){
        if(calenda == null) return;
        if(d == null){
            d = new MyDate(calenda.getDate());
        }
        JXMonthView month = calenda.getMonthView();
        int monthNo = d.getMONTH();
        String dStr = d.getYEAR() + "-" + (monthNo+1) + "-01";
        d.setDate(dStr);
        ArrayList<Date> flagDates = new ArrayList<>();
        while(true){
            if(!d.isOpenDay()){
                flagDates.add(d.getCDate());
            }
            d.nextNday(1);
            if(monthNo != d.getMONTH()) {
                break;
            }
        }
        month.addFlaggedDates(flagDates.toArray(new Date[flagDates.size()]));
    }
}


