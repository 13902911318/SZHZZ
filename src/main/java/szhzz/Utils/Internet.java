package szhzz.Utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import szhzz.App.AppManager;
import szhzz.Calendar.MyDate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by IntelliJ IDEA.
 *
 * @author HuangFang
 * Date 2022/10/27
 * Time 11:32
 */
public class Internet {
    private static DawLogger logger = DawLogger.getLogger(Internet.class);

    public static void main(String[] args) {
        getIp();
    }
    static long lastRead = 0;
    static String toIp = "";
    /**
     * This method is used to get all ip addresses from the network interfaces.
     * network interfaces: eth0, wlan0, l0, vmnet1, vmnet8
     */
    public static String getIp() {
        String ip = "http://pv.sohu.com/cityjson?ie=utf-8";
        String inputLine = "";
        String read = "";
        if(toIp.equals("") || System.currentTimeMillis() - lastRead < 10000 * 60) // 10分钟
            return toIp;

        lastRead = System.currentTimeMillis();
        try {
            String toIp_ = "?";
            URL url = new URL(ip);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF8"));
            while ((read = in.readLine()) != null) {
                inputLine += read;
//                AppManager.logit("Read URL:" + read);
            }
            String objJson = inputLine.substring(inputLine.indexOf("=") + 1, inputLine.length() - 1);

//            AppManager.logit("parseObject...");
            JSONObject jsonObj = JSON.parseObject(objJson);
//            AppManager.logit("JSONObject?");
//            AppManager.logit("JSONObject=" + jsonObj.toString());
            toIp_ = jsonObj.getString("cip");
//            AppManager.logit("!! cip=" + toIp);
            if(!toIp.equals(toIp_)){
                toIp = toIp_;
                AppManager.logit(objJson);
            }
        } catch (Exception ignored) {
            //logger.error(ignored);
        }
        return toIp;
    }
}
