package szhzz.Timer;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;

/**
 * Project: SZHZZ
 * Package: szhzz.Timer
 * <p>
 * User: HuangFang
 * Date: 2020/9/6
 * Time: 18:54
 * <p>
 * Created with IntelliJ IDEA
 */
public class NtpClient {

    private static long offset = 0;
    private static int defaultTimeOut = 1000;

    public static Date getTime(String timeServerUrl) throws IOException  {
        if(timeServerUrl == null) return null;
        NTPUDPClient timeClient = new NTPUDPClient();
        InetAddress timeServerAddress = InetAddress.getByName(timeServerUrl);
        timeClient.setDefaultTimeout(defaultTimeOut);
        TimeInfo timeInfo = timeClient.getTime(timeServerAddress);
        TimeStamp timeStamp = timeInfo.getMessage().getTransmitTimeStamp();
        return timeStamp.getDate();
    }

    public static long getOffset()  {
        return offset;
    }
    public static long getOffset(String timeServerUrl) throws IOException  {
        offset = Long.MIN_VALUE;
        if(timeServerUrl == null) return offset;

        NTPUDPClient timeClient = new NTPUDPClient();
        InetAddress timeServerAddress = InetAddress.getByName(timeServerUrl);
        timeClient.setDefaultTimeout(defaultTimeOut);
        TimeInfo timeInfo = timeClient.getTime(timeServerAddress);
        timeInfo.computeDetails();
        offset = timeInfo.getOffset();
        return offset;
    }

    public static void setDefaultTimeOut(int defaultTimeOut) {
        NtpClient.defaultTimeOut = defaultTimeOut;
    }
}
