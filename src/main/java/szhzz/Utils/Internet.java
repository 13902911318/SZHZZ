package szhzz.Utils;

import szhzz.App.AppManager;
import szhzz.Config.Config;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by IntelliJ IDEA.
 *
 * @author HuangFang
 * Date 2022/10/27
 * Time 11:32
 */
public class Internet {
    private static DawLogger logger = DawLogger.getLogger(Internet.class);
    private static String vpnName = null;

    public static void main(String[] args) {
//        getIp();
        getVpnIp("OrayBoxVPN Virtual Ethernet Adapter");
    }

    static long lastRead = 0;
    static String publicIp = "";

    /**
     * This method is used to get all ip addresses from the network interfaces.
     * network interfaces: eth0, wlan0, l0, vmnet1, vmnet8
     */
    public static String getIp() {

        if (!publicIp.equals("")) //||
            return publicIp;

        publicIp = getIp2();
        return publicIp;
    }

    public static String getIp2() {
        String ip = "";
        String chinaz = "https://ip.chinaz.com/";
        StringBuilder inputLine = new StringBuilder();
        String read = "";
        URL url = null;
        HttpURLConnection urlConnection = null;
        BufferedReader in = null;
        try {
            url = new URL(chinaz);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            while ((read = in.readLine()) != null) {
                inputLine.append(read + "\n");
            }
            //System.out.println(inputLine.toString());
        } catch (Exception e) {
            logger.error(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    logger.error(e);
                }
            }
        }


        Pattern p = Pattern.compile("<dd class=\"fz24\">(.*?)</dd>");
        Matcher m = p.matcher(inputLine.toString());
        if (m.find()) {
            String ipstr = m.group(1);
            ip = ipstr;
//            System.out.println(ipstr);
        }
        return ip;

    }

    public static String getVpnIp() {
        if(vpnName == null) {
            Config cfg = AppManager.getApp().getCfg();
            if (cfg == null) return null;
            vpnName = cfg.getProperty("VpnName", "OrayBoxVPN Virtual Ethernet Adapter");
        }
        return getVpnIp(vpnName) ;
    }

    public static String getVpnIp(String VpnName) {
        String vpnIP = null;
        Enumeration<NetworkInterface> en = null;
        Enumeration<InetAddress> addresses;

        try {
            en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface networkinterface = en.nextElement();
                String name = networkinterface.getDisplayName();
                if (VpnName.equals(name)) {
                    addresses = networkinterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        if(vpnIP == null) {
                            vpnIP = addresses.nextElement().getHostAddress();
                        }else{
                            vpnIP += ";" + addresses.nextElement().getHostAddress();
                        }
                    }
                    break;
                }
            }
        } catch (SocketException e) {
            logger.error(e);
        }

        return vpnIP;
    }
}
