package szhzz.Mail;


import szhzz.App.AppManager;
import szhzz.Calendar.MiscDate;
import szhzz.DataBuffer.DataConsumer;
import szhzz.DataBuffer.ObjBufferedIO;
import szhzz.Timer.CircleTimer;
import szhzz.Utils.DawLogger;
import szhzz.Utils.Utilities;

import java.io.File;
import java.io.IOException;

/**
 * Created by Administrator on 2016/4/11.
 */
public class Mailer implements DataConsumer {
    private static DawLogger logger = DawLogger.getLogger(Mailer.class);
    AppManager App = AppManager.getApp();
    ObjBufferedIO dataBuffer = null;
    StringBuilder sb = null;
    BatchSender batchSender = null;
    private static final Object locker = new Object();
    String lineSeparator = System.lineSeparator();
    //            java.security.AccessController.doPrivileged(
//            new sun.security.action.GetPropertyAction("line.separator"));
    private String title = "";

    public void sendMail(MailMsg mail) {
        if (dataBuffer != null) {
            dataBuffer.push(mail);
        } else {
            in(mail);
        }
    }

    public void sendBatchMail(String title, String msg) {
        if (batchSender == null) {
            batchSender = new BatchSender();
        }
        synchronized (locker) {
            if (sb == null) {
                sb = new StringBuilder();
            }
            sb.append("\r\n").append(msg);
            this.title = title;
            batchSender.setCircleTime(30 * 1000); //10秒之内没有新的信息，批量信息一起发出1封邮件
        }
    }

    public void sendMailNow(MailMsg mail) {
        sendMail_(mail);
    }

    public void setBuffer(int size) {
        dataBuffer = new ObjBufferedIO();
        try {
            dataBuffer.setReader(this, size);
        } catch (InterruptedException e) {
            logger.equals(e);
        }
    }

    private void sendMail_(MailMsg mail) {
        int tryCount = 4;
        while (--tryCount > 0) {
            try {
                SendMail theMail = new SendMail(mail.getSmtp());
                theMail.createMimeMessage();
                theMail.setNeedAuth(true);

                if (!theMail.setSubject(mail.getKeyWord() + ";" + App.getHostName() + ":" + App.getAppName()))
                    return;
                if (!theMail.setBody(mail.getMsg() + "," + MiscDate.todaysDate())) return;
                if (!theMail.setTo(mail.getMailTo())) return;
                if (!theMail.setFrom(mail.getMailFrom())) return;
                if (mail.getAttFile() != null) {
                    if (!theMail.addFileAffix(mail.getAttFile())) return;
                }
                theMail.setNamePass(mail.getMailBoxUser(), mail.getMailBoxPassword());

                //themail.setCopyTo("wxm5599@sina.com");
                try {
                    if (!theMail.sendout()) {
                        MailMsg.changeMailbox();
                        mail = mail.copy();
                    } else {
                        logger.info("Send OK");
                        break;
                    }
                } catch (Exception e0) {
                    logger.error(e0);
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    @Override
    public long in(Object obj) {
        sendMail_((MailMsg) obj);
        return 1;
    }

    @Override
    public long in(long dataID, Object obj) {
        return 0;
    }


    class BatchSender extends CircleTimer {
        @Override
        public void execTask() {
            synchronized (locker) {
                new File("c:/tbd").mkdirs();

                String f = "c:/tbd/batchFile.txt";
                try {
                    Utilities.String2File(sb.toString(), f, false);
                } catch (IOException e) {

                }
                if (title == null || title.length() == 0) {
                    title = "见附件";
                }
                sendMail_(MailMsg.copy(title, f));
                sb = null;
            }
        }
    }
}
