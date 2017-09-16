package szhzz.Mail;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2007-12-25
 * Time: 22:16:55
 * To change this template use File | Settings | File Templates.
 */

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * 用于发邮件时的身份认证
 *
 * @author HuangFang
 */
public class DawAuthenticator extends Authenticator {
    String username = null;
    String password = null;

    /**
     * Creates a new instance of DawAuthenticator
     */
    public DawAuthenticator() {
    }

    public void performCheck(String user, String pass) {
        username = user;
        password = pass;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
    }

    public String toString() {
        return "Username=" + username + " Password=" + password;
    }
}


