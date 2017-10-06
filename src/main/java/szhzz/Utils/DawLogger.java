package szhzz.Utils;

/*
 * Logger.java
 *
 * Created on 2003年7月10日, 上午10:01
 */


import org.apache.log4j.Level;
import org.apache.log4j.xml.DOMConfigurator;
import szhzz.Calendar.MiscDate;
import szhzz.Calendar.MyDate;

import java.io.*;


/**
 * @author HuangFang
 *         <p/>
 *         public static void main(String[] args) throws Exception {
 *         // 使用 Log4J， 否则自动使用 DawLogger
 *         DawLogger.useLog4J(APP.getRootFolder() + "logger.xml");
 *         ...
 *         ...
 *         }
 *         <p/>
 *         在其它需要debug， log 的 class里，加入,"MyClass" 是需要debug， log 的 class的名称。
 *         private static DawLogger logger = DawLogger.getLogger(MyClass.class);
 *         <p/>
 *         下载 log4j
 *         http://jakarta.apache.org/log4j
 */
public class DawLogger {
    public static final int OFF = 0;
    public static final int ERROR = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ALL = 5;
    public static PrintStream out = System.out;  //Modified
    private static boolean useLog4J = false;
    private static DawLogger Log4jAgent = null;
    private static File outFile = null;
    private static StringBuffer buffer = new StringBuffer();
    private static boolean buffered = false;           //Modified
    private static int level = ALL;
    private static int maxBufferSize = 102400;  // 100K
    private static int errorCount = 0;

    private org.apache.log4j.Logger logger = null;

    private DawLogger() {
        // use default systemout
    }

    /**
     * 实现一个文件log, 即使已经声明使用log4j
     */
    private DawLogger(File outFile) {
        setLogFile(outFile);
    }

    /**
     * 实现一个流log, 即使已经声明使用log4j
     */
    private DawLogger(PrintStream out) {
        this.out = out;
    }

    /**
     * 实现一个log4j log, 应已经声明使用log4j
     */
    private DawLogger(org.apache.log4j.Logger lg) {
        this.logger = lg;
    }

    /**
     * 为log4J初始化
     */
    public static void useLog4J(String initFile) {
        if (useLog4J) return;
        useLog4J = true;
        try {
            // if has no log4j avalable, do't use that
            Class.forName("org.apache.log4j.Logger");
        } catch (Exception e) {
            return;
        }
        DOMConfigurator.configure(initFile);

    }

    /**
     * 返回一个文件log, 即使已经声明使用log4j
     * 用于 DawTX 处理某一特定数据文件时的特定log
     */
    public static DawLogger getLogger(File outFile) {
        try {
            //if (dawLogger != null) throw new Exception("Logger initated!");
            DawLogger dawLogger = new DawLogger(outFile);
            return dawLogger;
        } catch (Exception e) {
            return getLogger(System.out);
        }
    }

    /**
     * 返回一个流log, 即使已经声明使用log4j
     */
    public static DawLogger getLogger(PrintStream out) {
        DawLogger dawLogger;
        try {
            //if (dawLogger != null) throw new Exception("Logger initated!");
            dawLogger = new DawLogger(out);
        } catch (Exception e) {
            dawLogger = new DawLogger(System.out);
        }
        return dawLogger;
    }

    /**
     * 返回一个 log4j log, 应已经声明使用log4j,否则返回一个系统输出log
     */
    public static DawLogger getLogger(Class c) {
        if (useLog4J) {
            //if (Log4jAgent == null ) Log4jAgent = new daw.logger.DawLogger(org.apache.log4j.Logger.getLogger(c));
            return new DawLogger(org.apache.log4j.Logger.getLogger(c));
        } else
            return new DawLogger();
    }

    /**
     * 返回一个 log4j log, 应已经声明使用log4j， 否则返回一个系统输出log
     */
    public static DawLogger getLogger(String s) {
        if (useLog4J)
            return new DawLogger(org.apache.log4j.Logger.getLogger(s));
        else
            return new DawLogger();
    }

    public static int getErrorCount() {
        return errorCount;
    }

    public void setLogFile(String f) {
        setLogFile(f, false);
    }

    public void setLogFile(File f) {
        setLogFile(f, false);
    }

    public void setLogFile(String f, boolean append) {
        setLogFile(new File(f), append);
    }

    public void setLogFile(File f, boolean append) {
        this.outFile = f;

        if (out != null && !out.equals(System.out)) {
            out.close();
        }
        try {
            out = new PrintStream(new FileOutputStream(f, append));
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            out = System.out;
        }
    }

    public void info(Object o) {
        info(o.toString());
    }

    // services
    public void info(String s) {
        if (logger == null) {
            if (level < INFO) return;
            pr(s);
        } else
            logger.info(s);
    }

    public void info(String s, Throwable e) {
        if (level < INFO) return;
        if (logger == null) {
            if (level < INFO) return;
            pr(s, e);
        } else {
            Writer result = new StringWriter();
            PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            logger.error(s + System.getProperty("line.separator") + result.toString(), e);
        }
    }

    public void debug(String s) {
        if (logger == null) {
            if (level < DEBUG) return;
            pr(s);
        } else {
            logger.debug(s);
        }
    }

    public void debug(String s, Throwable e) {
        if (logger == null) {
            if (level < DEBUG) return;
            pr(s, e);
        } else {
            Writer result = new StringWriter();
            PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            logger.debug(s + System.getProperty("line.separator") + result.toString(), e);
        }
    }

    public void error(String s) {
        errorCount++;
        if (errorCount <= 2) {
            try {
                Utilities.String2File(MyDate.getToday().getDateTime() + ">" + s, "error.txt", true);
            } catch (IOException e) {

            }
        }
        if (logger == null) {
            if (level < ERROR) return;
            pr(s);
        } else {
            logger.error(s);
        }
    }

    public void error(String s, Throwable e) {
        errorCount++;
        if (errorCount <= 2) {
            try {
                Writer result = new StringWriter();
                PrintWriter printWriter = new PrintWriter(result);
                e.printStackTrace(printWriter);
                Utilities.String2File(MyDate.getToday().getDateTime() + ">" + s + "\n" + result.toString(), "error.txt", true);
            } catch (IOException e1) {

            }
        }
        if (logger == null) {
            if (level < ERROR) return;
            pr(s, e);
        } else {
            logger.error(s, e);
//            Writer result = new StringWriter();
//            PrintWriter printWriter = new PrintWriter(result);
//            e.printStackTrace(printWriter);
//            logger.error(s + System.getProperty("line.separator") + result.toString(), e);
        }
    }

    public void error(Throwable e) {
        errorCount++;
        if (errorCount <= 2) {
            try {
                Writer result = new StringWriter();
                PrintWriter printWriter = new PrintWriter(result);
                e.printStackTrace(printWriter);
                Utilities.String2File(MyDate.getToday().getDateTime() + ">" + result.toString(), "error.txt", true);
            } catch (IOException e1) {

            }
        }
        if (logger == null) {
            if (level < ERROR) return;
            pr("", e);
        } else {
            logger.error(e);
//            Writer result = new StringWriter();
//            PrintWriter printWriter = new PrintWriter(result);
//            e.printStackTrace(printWriter);
//            logger.error(result.toString(), e);
        }
    }

    public void warn(String s) {
        if (logger == null) {
            if (level < WARN) return;
            pr(s);
        } else {
            logger.warn(s);
        }
    }

    public void warn(String s, Throwable e) {
        if (logger == null) {
            if (level < WARN) return;
            pr(s, e);
        } else {
            logger.warn(s, e);
        }
    }

    public synchronized void pr(String s) {
        if (buffered) {
            if (maxBufferSize - s.length() > buffer.length()) flashBuffer();
            buffer.append(MiscDate.todaysDate());
            buffer.append("   ");
            buffer.append(s);
        } else {
            if (logger == null)
                out.println(MiscDate.todaysDate() + "   " + s);
            else
                logger.warn(s);
        }
    }

    public synchronized void pr(String s, Throwable e) {
        if (buffered) {
            if (maxBufferSize - s.length() > buffer.length()) flashBuffer();
            buffer.append(MiscDate.todaysDate());
            buffer.append("   ");
            buffer.append(s);
            buffer.append(e.toString());
        } else {
            if (logger == null) {
                out.println(MiscDate.todaysDate() + "   " + s);
                e.printStackTrace(out);
            } else {
                logger.warn(s, e);
            }
        }
    }

    protected void finalize() throws Throwable {
        flashBuffer();
        if (out != null && !out.equals(System.out)) {
            out.close();
        }

        super.finalize();
    }

    public org.apache.log4j.Logger getLog4JObj() {
        return logger;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        if (this.level < OFF || this.level > ALL) {
            this.level = ERROR;
        } else {
            this.level = level;
        }
        if (logger != null) {
            if (this.level == ALL) logger.setLevel(Level.ALL);
            else if (this.level == ERROR) logger.setLevel(Level.ERROR);
            else if (this.level == DEBUG) logger.setLevel(Level.DEBUG);
            else if (this.level == INFO) logger.setLevel(Level.INFO);
            else if (this.level == WARN) logger.setLevel(Level.WARN);
            else logger.setLevel(Level.OFF);
        }
    }

    public boolean isBuffered() {
        return buffered;
    }

    /**
     * 调用该函数将导致缓存中的内容被写出
     *
     * @param buffered
     */
    public void setBuffered(boolean buffered) {
        flashBuffer();
        this.buffered = buffered;
    }

    private void flashBuffer() {
        if (buffer != null && buffer.length() > 0 && out != null)
            out.println(buffer.toString());
        buffer = new StringBuffer();
    }

    public void setMaxBufferSize(int maxBufferSize) {
        this.maxBufferSize = maxBufferSize;
    }

    public void closeOutput() {
        flashBuffer();
        if (out != null) out.close();
    }
}
