package szhzz.Utils;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-6-11
 * Time: 下午4:31
 * To change this template use File | Settings | File Templates.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;


public class TestMac {
    public static void main(String[] args) {
        // System.out.println("Operation System=" + getOsName());
        // System.out.println("Mac Address=" + getMACAddress());
        // System.out.println("通过ip获取mac" + getMACAddress("192.168.0.100"));
    }

    public static String getOsName() {
        String os = "";
        os = System.getProperty("os.name");
        return os;
    }

    public static String getMACAddress() {
        String address = "";
        String os = getOsName();
        if (os.startsWith("Windows")) {
            try {
                String command = "cmd.exe /c ipconfig /all";
                Process p = Runtime.getRuntime().exec(command);
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), "GB2312"));
                String line;
                while ((line = br.readLine()) != null) {
                    // System.out.println(line);
                    if (line.indexOf("物理地址") > 0) {
                        int index = line.indexOf(":");
                        index += 2;
                        address = line.substring(index);
                        break;
                    }
                }
                br.close();
                return address.trim();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (os.startsWith("Linux")) {
            String command = "/bin/sh -c ifconfig -a";
            Process p;
            try {
                p = Runtime.getRuntime().exec(command);
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.indexOf("HWaddr") > 0) {
                        int index = line.indexOf("HWaddr") + "HWaddr".length();
                        address = line.substring(index);
                        break;
                    }
                }
                br.close();
            } catch (IOException e) {
            }
        }
        address = address.trim();
        return address;
    }


    public static String getMACAddress(String ipAddress) {
        String str = "", strMAC = "", macAddress = "";
        try {
            Process pp = Runtime.getRuntime().exec("nbtstat -a " + ipAddress);
            InputStreamReader ir = new InputStreamReader(pp.getInputStream(), "GB2312");
            LineNumberReader input = new LineNumberReader(ir);
            for (int i = 1; i < 100; i++) {
                str = input.readLine();
                if (str != null) {
                    // System.out.println(str);
                    if (str.indexOf("物理地址") > 1) {
                        strMAC = str.substring(str.indexOf("-") - 2,
                                str.length());
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            return "Can't Get MAC Address!";
        }
        //
        if (strMAC.length() < 17) {
            return "Error!";
        }

        macAddress = strMAC.substring(0, 2) + ":" + strMAC.substring(3, 5)
                + ":" + strMAC.substring(6, 8) + ":" + strMAC.substring(9, 11)
                + ":" + strMAC.substring(12, 14) + ":"
                + strMAC.substring(15, 17);
        //
        return macAddress;
    }
}


//ip地址
//<span>import java.util.*;
//import java.net.*
//;public class getMyIP {
//    public static void main(String[] args) {
//        String IP = null; String host = null;
//        try {
//            InetAddress ia = InetAddress.getLocalHost();
//            host = ia.getHostName();//获取计算机名字
//            IP= ia.getHostAddress();//获取IP
//        } catch(UnknownHostException e) {
//            e.printStackTrace();
//        }
//        System.out.println(host);
//        System.out.println(IP);
//    }
//}
