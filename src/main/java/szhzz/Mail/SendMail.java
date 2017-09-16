package szhzz.Mail;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2007-12-25
 * Time: 21:37:35
 * To change this template use File | Settings | File Templates.
 */

import szhzz.Calendar.MiscDate;
import szhzz.Utils.DawLogger;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Date;
import java.util.Properties;


/**
 * @author HuangFang
 */
public class SendMail implements Runnable {

    private static DawLogger logger =
            DawLogger.getLogger(SendMail.class);
    private MimeMessage mimeMsg; //MIME邮件对象
    private Session session; //邮件会话对象
    private Properties properties; //系统属性
    //private boolean needAuth = false; //smtp是否需要认证
    private String username = ""; //smtp认证用户名和密码
    private String password = "";
    private Multipart multipart; //Multipart对象,邮件内容,标题,附件等内容均添加到其中后再生成MimeMessage对象
    private boolean isWorking = false;
    private DawAuthenticator dawAuthenticator = null;
    private Transport transport = null;


    /**
     *
     */
    public SendMail() {
        //setSmtpHost(getConfig.mailHost);//如果没有指定邮件服务器,就从getConfig类中获取
        //createMimeMessage();
    }

    public SendMail(String smtp) {
        setSmtpHost(smtp);
        //createMimeMessage();
    }

    /**
     * Just do it as this
     */
    public static void main(String[] args) {
        try {
            SendMail themail = new SendMail("smtp.139.com");
            // pop3.vip.sina.com, www.szhzz.com
            themail.createMimeMessage();
            themail.setNeedAuth(true);

            if (themail.setSubject("量指报告") == false) return;
            if (themail.setBody("Test" + MiscDate.todaysDate()) == false) return;
            if (themail.setTo("13902911318@139.com") == false) return;
            if (themail.setFrom("13902911308@139.com") == false) return;
//            if (themail.addFileAffix(configData.getVHIReportFoulder() + "/Page.htm") == false) return;
            themail.setNamePass("13902911308@139.com", "w13902911308");
            //themail.setCopyTo("wxm5599@sina.com");
            if (themail.sendout() == false)
                System.out.println("Send error");

            System.out.println("Send OK");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param hostName String
     */
    public void setSmtpHost(String hostName) {
        logger.debug("system setting：mail.smtp.host = " + hostName);
        if (properties == null) properties = (Properties) System.getProperties().clone(); //获得系统属性对象
        properties.put("mail.smtp.host", hostName); //设置SMTP主机
    }

    /**
     * @return boolean
     */
    public boolean createMimeMessage() throws Exception {
        if (session == null) {
            try {
                logger.debug("get a default section...");
                //session = Session.getDefaultInstance(properties ,dawAuthenticator ); //获得邮件会话对象
                session = Session.getInstance(properties, dawAuthenticator); //获得邮件会话对象
            } catch (Exception e) {
                logger.error("get section error dawAuthenticator:" + dawAuthenticator);
                throw e;
            }
        }


        logger.debug("To create a MIME object");
        try {
            mimeMsg = new MimeMessage(session); //创建MIME邮件对象
            multipart = new MimeMultipart();

            return true;
        } catch (Exception e) {
            logger.error("False to create a MIME object", e);
            return false;
        }
    }

    /**
     * @param need boolean
     */
    public void setNeedAuth(boolean need) {
        System.out.println("设置smtp身份认证：mail.smtp.auth = " + need);
        if (properties == null) properties = (Properties) System.getProperties().clone(); //获得系统属性对象

        if (need) {
            properties.put("mail.smtp.auth", "true");
        } else {
            properties.put("mail.smtp.auth", "false");
        }
    }

    /**
     * @param name String
     * @param pass String
     */
    public void setNamePass(String name, String pass) {
        username = name;
        password = pass;
        dawAuthenticator = new DawAuthenticator();
        dawAuthenticator.performCheck(username, password);
    }

    /**
     * @param mailSubject String
     * @return boolean
     */
    public boolean setSubject(String mailSubject) throws Exception {
        logger.debug("set title...");
        try {
            mimeMsg.setSubject(mailSubject);
            return true;
        } catch (Exception e) {
            logger.error("set title error mailSubject=" + mailSubject + "\nmimeMsg=" + mimeMsg);
            throw e;
        }
    }

    /**
     * @param mailBody String
     */
    public boolean setBody(String mailBody) throws Exception {
        try {
            BodyPart bp = new MimeBodyPart();
            bp.setContent("<meta http-equiv=Content-Type content=text/html; charset=gb2312>" + mailBody, "text/html;charset=GB2312");
            multipart.addBodyPart(bp);

            return true;
        } catch (Exception e) {
            logger.error("set content error");
            throw e;
        }
    }

    /**
     */
    public boolean addFileAffix(String filename) throws Exception {

        logger.debug("addDemorecord Attatches " + filename);

        try {
            BodyPart bp = new MimeBodyPart();
            FileDataSource fileds = new FileDataSource(filename);
            bp.setDataHandler(new DataHandler(fileds));
            bp.setFileName(fileds.getName());

            multipart.addBodyPart(bp);

            return true;
        } catch (Exception e) {
            logger.error("Add atach " + filename + " error");
            throw e;
        }
    }

    public boolean addFileAffix(File file) {

        logger.debug("addDemorecord Attatches " + file);

        try {
            BodyPart bp = new MimeBodyPart();
            FileDataSource fileds = new FileDataSource(file);
            bp.setDataHandler(new DataHandler(fileds));
            bp.setFileName(fileds.getName());

            multipart.addBodyPart(bp);

            return true;
        } catch (Exception e) {
            logger.error("Add atach " + file + " error", e);
            return false;
        }
    }

    /**
     *
     */
    public boolean setFrom(String from) throws Exception {
        logger.debug("set from =" + from);
        try {
            mimeMsg.setFrom(new InternetAddress(from)); //设置发信人
            return true;
        } catch (Exception e) {
            logger.error("set from error");
            throw e;
        }
    }

    /**
     */
    public boolean setTo(String to) throws Exception {
        try {
            logger.debug("set to=" + to);
            if (to == null)
                throw new IllegalArgumentException("Recipant address can not be empty!");


            mimeMsg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            return true;
        } catch (Exception e) {
            logger.error("set to error");
            throw e;
        }

    }

    /**
     */
    public boolean setCopyTo(String copyto) throws Exception {
        if (copyto == null) return false;
        logger.debug("set copyto=" + copyto);
        try {
            mimeMsg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(copyto));
            return true;
        } catch (Exception e) {
            logger.error("set CC to error");
            throw e;
        }
    }

    /**
     */
    public synchronized boolean sendout() {
        try {
            isWorking = true;
            mimeMsg.setSentDate(new Date());
            mimeMsg.setContent(multipart);
            mimeMsg.saveChanges();
            logger.debug("sendding mail to ...." + mimeMsg.getRecipients(Message.RecipientType.TO));
            long testTimer = System.currentTimeMillis();
            //Session mailSession = Session.getInstance(properties,null);
            if (transport == null) {

                transport = session.getTransport("smtp");
                logger.debug("getTransport timer=" + (System.currentTimeMillis() - testTimer));

            }
            if (!transport.isConnected()) {
                testTimer = System.currentTimeMillis();
                transport.connect((String) properties.get("mail.smtp.host"), username, password);
                logger.debug("connect timer=" + (System.currentTimeMillis() - testTimer));
            }
            transport.sendMessage(mimeMsg, mimeMsg.getRecipients(Message.RecipientType.TO));

            return true;
        } catch (Exception e) {
            logger.info("send mail false (可忽略)");

            disconnect();
            return false;
        } finally {
            isWorking = false;
        }
    }

    public boolean isWorking() {
        return isWorking;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see java.lang.Thread#run()
     */
    public void run() {
        sendout();
    }

    public void disconnect() {
        try {
            if (transport != null) {
                if (transport.isConnected()) transport.close();
                logger.debug("Mail Transaction disconnected!");
            }
        } catch (Exception e1) {
        }
    }

}