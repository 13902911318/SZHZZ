package szhzz.Utils;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-6-11
 * Time: 下午4:24
 * To change this template use File | Settings | File Templates.
 */

import szhzz.App.AppManager;

import java.io.*;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


public class HardwareIDs {
    static Map<String, String> map = System.getenv();
    public static void main(String[] args) {

        //****************获取硬盘ID*****************//
//        System.out.println("硬盘编号=" + getSerialNumber("C"));
//        System.out.println("Realtek PCIe GBE Family Controller IP=" + getIP("Realtek PCIe GBE Family Controller"));
//        System.out.println("Endpoint VPN Client IP=" + getIP("Endpoint VPN Client"));
//        System.out.println("CPU ID=" + getCPUSerial());
//        System.out.println("Mac ID=" + getMACAddress("Endpoint VPN Client"));
//
//        getJvmProperties();
        rvnTest();
        AppManager.debugLogit(System.getProperty("user.home"));
    }

    public static String getIP() {
//        InetAddress addr = null;
//        String ip = "";
//        try {
//            addr = InetAddress.getLocalHost();
//            ip = addr.getHostAddress().toString(); //获取本机ip
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//        return ip ;

        String address = null;
        BufferedReader br = null;
        try {
            Process p = Runtime.getRuntime().exec("ipconfig /all");
            br = new BufferedReader(new InputStreamReader(p.getInputStream(), "GB2312"));//
            String line;
            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().contains("ipv4")) {
                    int p1 = line.indexOf(":") + 2;
                    int p2 = line.indexOf("(");
                    if (p2 > 0) {
                        address = line.substring(p1, p2);
                    } else {
                        address = line.substring(p1);
                    }
                    break;
                }
            }
            return address;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {

                }
            }
        }
        return address;
    }


    public static String getIP(String signature) {
        if (signature == null) {
            return HardwareIDs.getIP();
        }
        String address = null;
        BufferedReader br = null;
        try {
            Process p = Runtime.getRuntime().exec("ipconfig /all");
            br = new BufferedReader(new InputStreamReader(p.getInputStream(), "GB2312"));//
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(signature)) {
                    while ((line = br.readLine()) != null) {
                        if (line.toLowerCase().contains("ipv4")) {
                            int p1 = line.indexOf(":") + 2;
                            int p2 = line.indexOf("(");
                            if (p2 > 0) {
                                address = line.substring(p1, p2);
                            } else {
                                address = line.substring(p1);
                            }
                            break;
                        }
                        if (line.trim().equals("")) {
                            break;
                        }
                    }
                    if (address != null) {
                        break;
                    }
                }
            }
            br.close();
            return address;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {

                }
            }
        }
        return address;
    }

    /**
     * 该方法很慢!!
     *
     * @return
     */
    public static String getMACAddress(String signature) {
        String address = null;
        BufferedReader br = null;
        try {
            Process p = Runtime.getRuntime().exec("ipconfig /all");
            br = new BufferedReader(new InputStreamReader(p.getInputStream(), "GB2312"));//
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(signature)) {
                    while ((line = br.readLine()) != null) {
                        if (line.indexOf("物理地址") > 0) {
                            int index = line.indexOf(":");
                            index += 2;
                            address = line.substring(index);
                            break;
                        }
                        if (line.trim().equals("")) {
                            break;
                        }
                    }
                    if (address != null) {
                        break;
                    }
                }
            }
            br.close();
            return address;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {

                }
            }
        }
        return address;
    }

    /**
     * 该方法很慢!!
     *
     * @return
     */
    public static String getMACAddress() {
        String address = null;
        String os = getOsName();
        BufferedReader br = null;
        if (os.startsWith("Windows")) {
            try {
                Process p = Runtime.getRuntime().exec("ipconfig /all");
                br = new BufferedReader(new InputStreamReader(p.getInputStream(), "GB2312"));//
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.indexOf("物理地址") > 0) {
                        int index = line.indexOf(":");
                        index += 2;
                        address = line.substring(index);
                        break;
                    }
                }
                br.close();
                return address;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException ignored) {

                    }
                }
            }
        } else if (os.startsWith("Linux")) {
            String command = "/bin/sh -c ifconfig -a";
            Process p;
            try {
                p = Runtime.getRuntime().exec(command);
                br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.indexOf("HWaddr") > 0) {
                        int index = line.indexOf("HWaddr") + "HWaddr".length();
                        address = line.substring(index);
                        break;
                    }
                }
                br.close();
            } catch (IOException ignored) {
            }
        }

        return address;
    }

    public String getHostName() {
        String hostName = "";
        try {
            InetAddress addr = InetAddress.getLocalHost();
            String ip = addr.getHostAddress().toString(); //获取本机ip
            hostName = addr.getHostName().toString(); //获取本机计算机名称
//            System.out.println("本机IP："+ip+"\n本机名称:"+hostName);
//            Properties props=System.getProperties();
//            System.out.println("操作系统的名称："+props.getProperty("os.name"));
//            System.out.println("操作系统的版本："+props.getProperty("os.version"));
        } catch (Exception e) {

        }
        return hostName;
    }

    public static int getJreBitVersion() {
        if (HardwareIDs.getJvmName().contains("64-Bit")) return 64;
        return 32;
    }

    public static String getCPUSerial() {
        String result = "";
        try {
            File file = File.createTempFile("tmp", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);
            String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
                    + "Set colItems = objWMIService.ExecQuery _ \n"
                    + "   (\"Select * from Win32_Processor\") \n"
                    + "For Each objItem in colItems \n"
                    + "    Wscript.Echo objItem.ProcessorId \n"
                    + "    exit for  ' do the first cpu only! \n" + "Next \n";

            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec(
                    "cscript //NoLogo " + file.getPath());
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getOsName() {
        String os = "";
        os = System.getProperty("os.name");
        return os;
    }

    public static String getSerialNumber(String drive) {
        String result = "";
        try {
            File file = File.createTempFile("damn", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);
            String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
                    + "Set colDrives = objFSO.Drives\n"
                    + "Set objDrive = colDrives.item(\""
                    + drive
                    + "\")\n"
                    + "Wscript.Echo objDrive.SerialNumber"; // see note
            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec(
                    "cscript //NoLogo " + file.getPath());
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;

            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.trim();
    }

    public static String getJvmName() {
        return System.getProperties().getProperty("java.vm.name");
    }

    public static String getJvmProperties() {
        Properties props = System.getProperties(); //系统属性
        System.out.println("Java的运行环境版本：" + props.getProperty("java.version"));
        System.out.println("Java的运行环境供应商：" + props.getProperty("java.vendor"));
        System.out.println("Java供应商的URL：" + props.getProperty("java.vendor.url"));
        System.out.println("Java的安装路径：" + props.getProperty("java.home"));
        System.out.println("Java的虚拟机规范版本：" + props.getProperty("java.vm.specification.version"));
        System.out.println("Java的虚拟机规范供应商：" + props.getProperty("java.vm.specification.vendor"));
        System.out.println("Java的虚拟机规范名称：" + props.getProperty("java.vm.specification.name"));
        System.out.println("Java的虚拟机实现版本：" + props.getProperty("java.vm.version"));
        System.out.println("Java的虚拟机实现供应商：" + props.getProperty("java.vm.vendor"));
        System.out.println("Java的虚拟机实现名称：" + props.getProperty("java.vm.name"));
        System.out.println("Java运行时环境规范版本：" + props.getProperty("java.specification.version"));
        System.out.println("Java运行时环境规范供应商：" + props.getProperty("java.specification.vender"));
        System.out.println("Java运行时环境规范名称：" + props.getProperty("java.specification.name"));
        System.out.println("Java的类格式版本号：" + props.getProperty("java.class.version"));
        System.out.println("Java的类路径：" + props.getProperty("java.class.path"));
        System.out.println("加载库时搜索的路径列表：" + props.getProperty("java.library.path"));
        System.out.println("默认的临时文件路径：" + props.getProperty("java.io.tmpdir"));
        System.out.println("一个或多个扩展目录的路径：" + props.getProperty("java.ext.dirs"));
        System.out.println("操作系统的名称：" + props.getProperty("os.name"));
        System.out.println("操作系统的构架：" + props.getProperty("os.arch"));
        System.out.println("操作系统的版本：" + props.getProperty("os.version"));
        System.out.println("文件分隔符：" + props.getProperty("file.separator"));   //在 unix 系统中是＂／＂
        System.out.println("路径分隔符：" + props.getProperty("path.separator"));   //在 unix 系统中是＂:＂
        System.out.println("行分隔符：" + props.getProperty("line.separator"));   //在 unix 系统中是＂/n＂
        System.out.println("用户的账户名称：" + props.getProperty("user.name"));
        System.out.println("用户的主目录：" + props.getProperty("user.home"));
        System.out.println("用户的当前工作目录：" + props.getProperty("user.dir"));
        return "";
    }

    public static String getEnv(String key){
        String evn =  map.get(key);
        if(evn == null) {
            evn = System.getProperty("user.home");
        }
        return evn;
    }

    static void rvnTest() {
        for (Iterator<String> itr = map.keySet().iterator(); itr.hasNext(); ) {
            String key = itr.next();
            System.out.println(key + "=" + map.get(key));
        }
    }
}

