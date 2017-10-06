package szhzz.StatusInspect;

/**
 * Created by HuangFang on 2015/3/30.
 * 11:41
 */
public class StatusData {
    public String name;
    public boolean need = true;  //必须
    public boolean status = false;  // 状态
    public boolean forceAlarm = false; // 发出强制警告
    public boolean sendMail = false; // 发出Email
    public String relate = "";      //关联
    public String note = "未启动";
    public String locate = "未知";


    public StatusData() {

    }

    public StatusData(String name) {
        this.name = name;
    }

    public String toString() {
        return name + "\t" +
                need + "\t" +
                status + "\t" +
                forceAlarm + "\t" +
                relate + "\t" +
                note + "\t" +
                locate;
    }

    public String toMessage() {
        return name + ", " +
                "状态=" + status + ", " +
                "说明=" + note + ", " +
                "关联=" + relate + ", " +
                "位置=" + locate;
    }
}
