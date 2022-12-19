package szhzz.Utils;

/**
 * Created by Administrator on 2017/9/14.
 */

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Chelper {

    private static SecretKeySpec secretKey;
    private static byte[] key;

    public static void setKey(String myKey)
    {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(String strToEncrypt, String secret)
    {
        try
        {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        }
        catch (Exception e)
        {
            // System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static String decrypt(String strToDecrypt, String secret)
    {
        try
        {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            String n = cipher.getClass().getName();
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        }
        catch (Exception e)
        {
            // System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }


    public static String dynamicKey() {
        Calendar C = new GregorianCalendar();
        return
                C.get(Calendar.YEAR) + "-" +
                        (C.get(Calendar.MONTH) + 1) + "-" +
                        C.get(Calendar.DAY_OF_MONTH) + "-" +
                        C.get(Calendar.HOUR) + ":" +
                        C.get(Calendar.MINUTE);
    }

    public static String dynamicKey2() {
        return Chelper.encrypt("Aeder0uv[re7q[a077865kl23j412j/lk](AS*098VDFL090-d9safsii9F8-ADSFDF", dynamicKey());
    }


    public static void main(String[] args)
    {
        final String secretKey = "ssshhhhhhhhhhh!!!!";

        String originalString = "howtodoinjava.com";
        String encryptedString = Chelper.encrypt(originalString, secretKey) ;
        String decryptedString = Chelper.decrypt(encryptedString, secretKey) ;

        // System.out.println(originalString);
        // System.out.println(encryptedString);
        // System.out.println(decryptedString);
    }

}
