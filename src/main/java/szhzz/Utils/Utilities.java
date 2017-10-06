package szhzz.Utils;

import org.apache.commons.io.FileUtils;
import org.mozilla.universalchardet.UniversalDetector;
import szhzz.Calendar.MyDate;
import szhzz.Files.ExtensionFileFilter;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: vmuser
 * Date: 2007-11-20
 * Time: 10:46:12
 * To change this template use File | Settings | File Templates.
 */
public class Utilities {

    public static String TAB = "\t";
    public static String NEW_LINE = "\n";
    private static Hashtable<String, PrintWriter> openedFiles = null;


    public static void String2File_s(String S) {
        try {
            String2File(MyDate.getToday().getTime2() + " " + S, "ThreadTest.txt", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void String2File(String s, String fileName, boolean ab_append) throws IOException {
        String2File(s, new File(fileName), ab_append);
    }

    public static void String2File(String s, File file, boolean ab_append) throws IOException {
        FileUtils.writeStringToFile(file, s, Charset.forName("UTF-8"), ab_append);
    }


    public static void deleteFolder(String defaltPath) throws IOException {
        FileUtils.deleteDirectory(new File(defaltPath));
    }

    public static String File2String(String fileName) {
        return File2String(new File(fileName));
    }

    public static String File2String(File f) {
        try {
            return FileUtils.readFileToString(f, Charset.forName("UTF-8"));
        } catch (IOException e) {

        }
        return "";
    }

    public static boolean isFileClosed(String file, int mms) {
        boolean closed = false;
        Channel channel = null;
        int count = 0;
        while (count < mms) {
            try {
                channel = new RandomAccessFile(file, "rw").getChannel();
                closed = true;
            } catch (Exception ex) {
                closed = false;
            } finally {
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (IOException ex) {
                        // exception handling
                    }
                }
            }
            if (closed) break;
            try {
                TimeUnit.MICROSECONDS.sleep(5);
            } catch (InterruptedException e1) {

            }
            count += 5;
        }
        return closed;
    }

    public static void makeDir(String folder) {
        new File(folder).mkdirs();
    }

    public static void copyFile(String fromFileName, String toFileName, boolean ovewrite)
            throws IOException {
        File fromFile = new File(fromFileName);
        File toFile = new File(toFileName);

        if (!fromFile.exists())
            throw new IOException("FileCopy: " + "no such source file: "
                    + fromFileName);
        if (!fromFile.isFile())
            throw new IOException("FileCopy: " + "can't copy directory: "
                    + fromFileName);
        if (!fromFile.canRead())
            throw new IOException("FileCopy: " + "source file is unreadable: "
                    + fromFileName);

        if (toFile.isDirectory())
            toFile = new File(toFile, fromFile.getName());

        if (toFile.exists()) {
            if (!toFile.canWrite())
                throw new IOException("FileCopy: "
                        + "destination file is unwriteable: " + toFileName);
            if (!ovewrite) {
                System.out.print("Overwrite existing file " + toFile.getName()
                        + "? (Y/N): ");
                System.out.flush();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        System.in));
                String response = in.readLine();
                if (!response.equals("Y") && !response.equals("y"))
                    throw new IOException("FileCopy: "
                            + "existing file was not overwritten.");
            }
        } else {
            String parent = toFile.getParent();
            if (parent == null)
                parent = System.getProperty("user.dir");
            File dir = new File(parent);
            if (!dir.exists())
                throw new IOException("FileCopy: "
                        + "destination directory doesn't exist: " + parent);
            if (dir.isFile())
                throw new IOException("FileCopy: "
                        + "destination is not a directory: " + parent);
            if (!dir.canWrite())
                throw new IOException("FileCopy: "
                        + "destination directory is unwriteable: " + parent);
        }

        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = from.read(buffer)) != -1)
                to.write(buffer, 0, bytesRead); // saveAs
        } finally {
            if (from != null)
                try {
                    from.close();
                } catch (IOException e) {
                    ;
                }
            if (to != null)
                try {
                    to.close();
                } catch (IOException e) {
                    ;
                }
        }
    }

    public static void copyFolder(String fromDir, String toDir) throws IOException {
        makeDir(toDir);
        FileUtils.copyDirectory(new File(fromDir), new File(toDir));
    }

    public static String getTableName(String query) {
        String token;
        StringTokenizer tk = new StringTokenizer(query);
        while (tk.hasMoreTokens()) {
            token = tk.nextToken();
            if ("from".equalsIgnoreCase(token) || "update".equalsIgnoreCase(token)) {
                return readTableName(tk);
            } else if ("insert".equalsIgnoreCase(token)) {
                if (isInsert(tk)) return readTableName(tk);
            }
        }
        return "";
    }

    private static boolean isInsert(StringTokenizer tk) {
        if (tk.hasMoreTokens()) {
            if ("into".equalsIgnoreCase(tk.nextToken())) return true;
        }
        return false;
    }

    private static String readTableName(StringTokenizer tk) {
        String tName = "";
        String token;
        while (tk.hasMoreTokens()) {
            tName += tk.nextToken();
            if (tk.hasMoreTokens()) {
                token = tk.nextToken();
                if (",".equals(token))
                    tName += token;
                else
                    break;
            } else {
                break;
            }
        }
        return tName;
    }


    public static String getEquationLeft(String equation) {
        String e[] = getEquation(equation);
        if (e != null) {
            return e[0];
        }
        return null;
    }

    public static String getEquationRight(String equation) {
        String e[] = getEquation(equation);
        if (e != null) {
            return e[1];
        }
        return null;
    }

    public static void delDir(String folder) throws IOException {
        FileUtils.deleteDirectory(new File(folder));
    }

    public static void delDir(File folder) throws IOException {
        FileUtils.deleteDirectory(folder);
    }

    public static String getEquationComment(String equation) {
        String e[] = getEquation(equation);
        if (e != null) {
            return e[2];
        }
        return null;
    }

    public static String[] getEquation(String equation) {
        String e[] = new String[3];
        int pos;
        pos = equation.indexOf("=");
        if (pos >= 0 && pos < equation.length()) {
            e[0] = equation.substring(0, pos).trim();

            equation = equation.substring(pos + 1);
            pos = equation.indexOf("//");
            if (pos >= 0 && pos < equation.length()) {
                e[1] = equation.substring(0, pos).trim();
                e[2] = equation.substring(pos + 2).trim();
            } else {
                e[1] = equation.substring(pos + 1).trim();
                e[2] = "";
            }
        } else {
            if (equation.startsWith("#")) {
                e[0] = "";
                e[1] = "";
                e[2] = equation;
            } else {
                pos = equation.indexOf("//");
                if (pos >= 0 && pos < equation.length()) {
                    e[0] = "";
                    e[1] = "";
                    e[2] = equation;
                }
            }
        }
        return e;
    }


    public static String fileChoise(String title, File currentDir, String extenstion, String[] types) {
        String fileName = null;
        JFileChooser fc = new JFileChooser(currentDir);
        fc.setDialogTitle(title);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        javax.swing.filechooser.FileFilter jpegFilter = new ExtensionFileFilter(extenstion, types);
        fc.addChoosableFileFilter(jpegFilter);
        int returnValue = fc.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fc.getSelectedFile();
            if (selectedFile.exists()) {
                fileName = selectedFile.getAbsolutePath();
            }
        }
        return fileName;
    }

    public static boolean isNumber(String val) {
        try {
            float testnumber = Float.parseFloat(val.replace(",", ""));
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean isNumber(Object val) {
        if (val == null) return false;
        try {
            float testnumber = Float.parseFloat(val.toString());
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static synchronized void playSound(final String url) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Clip clip = AudioSystem.getClip();
                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(url));   //Trade.class.getResourceAsStream("/path/to/sounds/" + url)
                    clip.open(inputStream);
                    clip.start();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }).start();
    }

    public static String currentDir() {
        return System.getProperty("user.dir");
    }

    public static String programX86Dir() {
        if ("amd64".equals(System.getProperty("os.arch"))) {
            return System.getenv("ProgramFiles") + " (x86)";
        } else {
            return System.getenv("ProgramFiles");
        }
    }

    public static String parentDir() {
        try {
            return new File("..").getCanonicalPath();
        } catch (IOException ignored) {

        }
        return "..";
    }

//    public static void playSound(String Filename) throws IOException {
//        //** add this into your application code as appropriate
//        // Open an input stream  to the audio file.
//        InputStream in = new FileInputStream(Filename);
//
//        // Create an AudioStream object from the input stream.
//        AudioStream as = new AudioStream(in);
//
//        // Use the static class member "player" from class AudioPlayer to play
//        // clip.
//        AudioPlayer.player.start(as);
//
//        // Similarly, to stop the audio.
//        AudioPlayer.player.stop(as);
//    }


    public static String encodeString(String s, String fromCharset, String toCharset) {
        try {
            byte ptext[] = s.getBytes(fromCharset);
            return new String(ptext, toCharset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static String detectCharset(String file) throws IOException {
        return detectCharset(new File(file));
    }

    public static String detectCharset(File file) {
        String encoding = null;
        byte[] buf = new byte[4096];

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            // (1)
            UniversalDetector detector = new UniversalDetector(null);
            // (2)
            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            // (3)
            detector.dataEnd();
            // (4)
            encoding = detector.getDetectedCharset();
            if (encoding == null) {
                Charset cs = detectCharset_(file);
                if (cs != null)
                    encoding = cs.name();
            }
            if ("GB18030".equals(encoding)) {
                encoding = "GBK";
            }
            // (5)
            detector.reset();
        } catch (IOException ignored) {

        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {

                }
            }
        }
        return encoding;
        //return detectCharset(new File(f));
    }


    private static Charset detectCharset_(File f) {
        return detectCharset_(f, new String[]{"utf-8", "GB2312", "gbk", "windows-1253", "ISO-8859-7"});
    }

    private static Charset detectCharset_(File f, String[] charsets) {

        Charset charset = null;

        for (String charsetName : charsets) {
            charset = detectCharset_(f, Charset.forName(charsetName));
            if (charset != null) {
                break;
            }
        }

        return charset;
    }

    private static Charset detectCharset_(File f, Charset charset) {
        try {
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(f));

            CharsetDecoder decoder = charset.newDecoder();
            decoder.reset();

            byte[] buffer = new byte[512];
            boolean identified = false;
            while ((input.read(buffer) != -1)) {
                identified = identify(buffer, decoder);
                if (!identified) break;
            }

            input.close();

            if (identified) {
                return charset;
            } else {
                return null;
            }

        } catch (Exception e) {
            return null;
        }
    }

    private static boolean identify(byte[] bytes, CharsetDecoder decoder) {
        try {
            decoder.decode(ByteBuffer.wrap(bytes));
        } catch (CharacterCodingException e) {
            return false;
        }
        return true;
    }

    public static BufferedReader getBufferedReader(String f) throws IOException {
        return getBufferedReader(new File(f));
    }

    public static BufferedReader getBufferedReader(File f) throws IOException {
//        String cs = detectCharset(f);
        String cs = null;
        if (cs == null) {
            cs = System.getProperty("file.encoding");
        }
        return new BufferedReader(new InputStreamReader(new FileInputStream(f), cs));
    }

    public static String slashify(String path) {
        File f = new File(path);

        String p = f.getAbsolutePath();
        if (!p.endsWith(File.separator) && f.isDirectory())
            p = p + File.separator;
        return p;
    }

    public static void main(String[] args) {
        try {
            playSound("D:\\JNIProject\\JavaProj\\AlertSound\\alert.wav");
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static int xorShift(int y) {
        y ^= (y << 6);
        y ^= (y >>> 21);
        y ^= (y << 7);
        return y;
    }
}



