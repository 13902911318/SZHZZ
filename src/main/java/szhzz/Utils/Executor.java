package szhzz.Utils;


import szhzz.App.AppManager;
import szhzz.App.MessageAbstract;
import szhzz.App.MessageCode;

import java.io.*;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-8-10
 * Time: 下午9:07
 * To change this template use File | Settings | File Templates.
 */
public class Executor {
    private static DawLogger logger = DawLogger.getLogger(Executor.class);
    private static Vector<String> runningJobs = new Vector<>();
    private static Vector<MessageCode> mainJobs = new Vector<>();
    private static Executor onlyOne = null;
    private static Boolean privileges = null;

    private Executor() {
    }

    public static Executor getInstance() {
        if (onlyOne == null) {
            onlyOne = new Executor();
        }
        return onlyOne;
    }

    public static int getJobs() {
        return runningJobs.size();
    }

    public static boolean canShutdown() {
        return mainJobs.size() == 0;
    }

    public static String getJobNames() {
        StringBuffer sb = new StringBuffer();
        for (String s : runningJobs) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }

    public static boolean checkRunningByTitle(String title) {
        return runningJobs.contains(title);
    }

    public static boolean isRunning(String processName, String title) {
        BufferedReader bufferedReader = null;
        Process proc = null;


        try {
            if (processName != null) {
                proc = Runtime.getRuntime().exec("tasklist /FI \"IMAGENAME eq " + processName + "\"");
            } else if (title != null) {
                title = title.replace("*", "");
                proc = Runtime.getRuntime().exec("tasklist /FI \"WINDOWTITLE eq " + title + "*\"");
            } else {
                return false;
            }

            bufferedReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            int listCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                if (processName != null && line.contains(processName)) //判断是否存在
                {
                    return true;
                } else if (title != null && line.contains("=====")) //判断是否存在
                {
                    listCount = 1;
                    return true;
                } else if (line.contains("没有运行的任务匹配指定标准")) {
                    return false;
                }
            }
            return false;
        } catch (Exception ex) {
            logger.equals(ex);
            return false;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    public void execute(String[] commands) throws IOException, InterruptedException {
        execute(commands, true, false, null, false);
    }

    public void execute(String[] commands, String title) throws IOException, InterruptedException {
        execute(commands, true, false, title, false);
    }

    public void execute(String[] commands, String title, boolean silent) throws IOException, InterruptedException {
        execute(commands, true, false, title, silent);
    }

    public void execute(String[] commands, boolean wait, boolean block, String title, boolean silent) throws IOException, InterruptedException {
        if (wait) {
            if (block) {
                executeWaitOnBlock(commands);
            } else {
                ExecWaitForEnd exec = new ExecWaitForEnd(commands);
                exec.title = title;
                exec.silent = silent;
                AppManager.executeInBack(exec);
            }
        } else {
            executeNoWait(commands);
        }
    }

    public void executeNoWait(String[] commands) throws IOException, InterruptedException {
        ExecWaitForEnd exec = new ExecWaitForEnd(commands);
        exec.waitFor = false;
        AppManager.executeInBack(exec);
    }

    public void executeWaitOnBlock(String[] commands) throws IOException, InterruptedException {
        ExecWaitForEnd exec = new ExecWaitForEnd(commands);
        exec.waitFor = true;
        exec.run();
    }

    public void executeIfRunningAbandon(String[] commands, MessageCode exitInformation, String title,
                                        OutputResolve outputReader, OutputResolve errorReader, boolean silent)
            throws IOException, InterruptedException {
        if (commands.length == 0) return;

        if (title == null) {
            title = commands[0];
        }

        if (runningJobs.contains(title)) {
            AppManager.logit(title + " 正在运行");
            return;
        }

        ExecWaitForEnd exec = new ExecWaitForEnd(commands, outputReader, errorReader);
        exec.exitInformation = exitInformation;
        exec.waitFor = (exitInformation != null);
        exec.title = title;
        exec.silent = silent;
        AppManager.executeInBack(exec);
    }

    private void addMainJob(MessageCode code) {
        if (code == null) return;
        if (code == MessageCode.AutoTradeEvent ||
                code == MessageCode.StockWinEvent ||
                code == MessageCode.MarketClientEvent)
            if (!mainJobs.contains(code)) {
                mainJobs.add(code);
            }
    }

    public void executeIfRunningAbandon(String[] commands, MessageCode exitInformation, String title, boolean silent) throws IOException, InterruptedException {
        executeIfRunningAbandon(commands, exitInformation, title, null, null, silent);
    }

    public void executeIfRunningAbandon(String[] commands, MessageCode exitInformation, String title) throws IOException, InterruptedException {
        executeIfRunningAbandon(commands, exitInformation, title, false);
    }

    public class StreamGobbler extends Thread {
        InputStream is;
        String type;
        boolean isError = false;
        private OutputResolve extReader = null;

        public StreamGobbler(InputStream is, String type) {
            this.is = is;
            this.type = type;
            isError = type.contains("Error");
        }

        public StreamGobbler(InputStream is, String type, OutputResolve extReader) {
            this.is = is;
            this.type = type;
            isError = type.contains("Error");
            this.extReader = extReader;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (extReader != null) {
                        //需要对输出进行处理
                        extReader.readLine(line);
                    } else {
                        AppManager.logit(type + "=>" + line);
                    }
                    logger.info(type + "=>" + line);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private class ExecWaitForEnd implements Runnable {
        String title = null;
        Process proc = null;
        boolean waitFor = true;
        boolean silent = false;
        File workFolder = null;
        private MessageCode exitInformation = null;
        private String[] command;
        private OutputResolve outputReader = null;
        private OutputResolve errorReader = null;
        private String firstLine = "";


        ExecWaitForEnd(String[] command) {
            this.command = command;
        }

        ExecWaitForEnd(String[] command, OutputResolve outputReader, OutputResolve errorReader) {
            this.command = command;
            this.outputReader = outputReader;
            this.errorReader = errorReader;
        }

        @Override
        public void run() {
            int exitCode = 0;
            firstLine = command[0];

            //            checkPrivileges();

            if (title == null) {
                title = firstLine;
            }
            try {
//                AppManager.logit(title + " sendMessage(" + exitInformation.name() + ", true) ");

                // 受控制的关键应用
                if (exitInformation != null) {
                    addMainJob(exitInformation);
                    MessageAbstract.getInstance().sendMessage(exitInformation, true);
                }
                File f = null;
                if (workFolder == null && new File(firstLine).exists()) {
                    workFolder = new File(firstLine).getParentFile();
                }


                if (firstLine.toLowerCase().contains(".exe") || firstLine.toLowerCase().contains(".com")) {
//                    if(privileges){
//                    proc = Runtime.getRuntime().exec("runas /profile /user:Administrator "+command, null, workFolder);

                    proc = Runtime.getRuntime().exec(command, null, workFolder);
                } else if(firstLine.toLowerCase().endsWith(".lnk") || firstLine.endsWith("快捷方式")){
                    //"cmd /c start \"\" \"D:/JNIProject/VC2010/MarketAp/Debug/MarketAp.exe - 快捷方式\"";
                    proc = Runtime.getRuntime().exec("cmd /c start \"\" \"" + firstLine + "\"");//避开win10的权限限制
                } else {
                    proc = Runtime.getRuntime().exec("cmd /c " + firstLine, null, workFolder);
                }
                StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), title + " Error", errorReader);
                StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), title + " Output", outputReader);
                errorGobbler.start();
                outputGobbler.start();
                if (waitFor) {
                    runningJobs.add(title);
                    if (!silent) {
                        AppManager.logEvent("启动 " + title);
                    } else {
                        logger.info("启动 " + title);
                    }
                    exitCode = proc.waitFor();
                }
                errorGobbler.interrupt();
                outputGobbler.interrupt();
            } catch (Exception e) {
                logger.error(e);
            } finally {
                if (exitInformation != null) {
                    mainJobs.remove(exitInformation);
                }

                if (!runningJobs.remove(title) && waitFor) {
                    AppManager.logEvent(title + " 退出时进程丢失错误");
                }

                if (proc != null && !silent && waitFor) {
                    AppManager.logEvent(title + " 退出, Exit(" + exitCode + ")");
                } else if (waitFor) {
                    logger.info(title + " 退出, Exit(" + exitCode + ")");
                }
//                AppManager.logit(title + " sendMessage(" + exitInformation.name() + ", false)");
                if (exitInformation != null) {
                    MessageAbstract.getInstance().sendMessage(exitInformation, false);
                }
            }
        }
    }

    private static boolean checkPrivileges() {
        if(privileges == null) {
            File testPriv = new File("C:\\Program Files\\");
            if (!testPriv.canWrite()) {
                privileges = new Boolean(false);
            }else {
                File fileTest = null;
                try {
                    fileTest = File.createTempFile("test", ".dll", testPriv);
                    privileges = new Boolean(true);
                } catch (IOException e) {
                    privileges = new Boolean(false);
                } finally {
                    if (fileTest != null)
                        fileTest.delete();
                }
            }
        }
        return privileges;
    }
}
