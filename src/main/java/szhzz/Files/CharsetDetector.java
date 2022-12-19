package szhzz.Files;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 11-3-13
 * Time: 上午11:36
 * To change this template use File | Settings | File Templates.
 */
public class CharsetDetector {
    public static void main(String[] args) {
        File f = new File("example.txt");

        String[] charsetsToBeTested = {"GBK", "GB2312", "UTF-8", "windows-1253", "ISO-8859-7", "ASCII7"};

        CharsetDetector cd = new CharsetDetector();
        Charset charset = cd.detectCharset(f, charsetsToBeTested);

        if (charset != null) {
            try {
                InputStreamReader reader = new InputStreamReader(new FileInputStream(f), charset);
                int c = 0;
                while ((c = reader.read()) != -1) {
                    // System.out.print((char) c);
                }
                reader.close();
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

        } else {
            // System.out.println("Unrecognized charset.");
        }
    }

    public Charset detectCharset(File f, String[] charsets) {

        Charset charset = null;

        for (String charsetName : charsets) {
            charset = detectCharset(f, Charset.forName(charsetName));
            if (charset != null) {
                break;
            }
        }

        return charset;
    }

    private Charset detectCharset(File f, Charset charset) {
        try {
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(f));

            CharsetDecoder decoder = charset.newDecoder();
            decoder.reset();

            byte[] buffer = new byte[512];
            boolean identified = true;
            while ((input.read(buffer) != -1) && (identified)) {
                identified = identify(buffer, decoder);
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

    private boolean identify(byte[] bytes, CharsetDecoder decoder) {
        try {
            decoder.decode(ByteBuffer.wrap(bytes));
        } catch (CharacterCodingException e) {
            return false;
        }
        return true;
    }
}
