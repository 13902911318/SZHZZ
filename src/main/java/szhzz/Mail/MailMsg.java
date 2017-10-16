package szhzz.Mail;

import szhzz.Config.CfgProvider;
import szhzz.Config.Config;
import szhzz.Config.SharedCfgProvider;

import java.util.LinkedList;

/**
 * Created by Administrator on 2016/4/11.
 */
public class MailMsg {
    public static LinkedList<MailMsg> mailBox = new LinkedList<>();
    private static MailMsg currentMailBox = null;
    private static int index = 0;

    private String msg = "";
    private String attFile = null;
    private String keyWord = "AppMail";
    private String smtp = "smtp.139.com";
    private String mailTo = "13902911318@139.com,szhzz0702@qq.com";
    private String mailFrom = "13902911308@139.com";
    private String mailBoxUser = "13902911308@139.com";
    private String mailBoxPassword = "";


    private static void loadMailBox() {
        LinkedList<String> ids = SharedCfgProvider.getInstance("MailBox").getCfgIDs();
        for (String id : ids) {
            Config cfg = SharedCfgProvider.getInstance("MailBox").getCfg(id);
            MailMsg mm = new MailMsg();
            mm.smtp = cfg.getProperty("smtp", mm.smtp);
            mm.mailTo = cfg.getProperty("mailTo", mm.mailTo);
            mm.mailFrom = cfg.getProperty("mailFrom", mm.mailFrom);
            mm.mailBoxUser = cfg.getProperty("mailBoxUser", mm.mailBoxUser);
            mm.mailBoxPassword = cfg.getProperty("mailBoxPassword", mm.mailBoxPassword);
            if (cfg.getIntVal("id", 100) == 0) {
                mailBox.addFirst(mm);
                currentMailBox = mm;
            } else {
                mailBox.add(mm);
            }
        }
//        mailBox.add(new MailMsg());

    }

    public static MailMsg copy(String msg, String attFile) {
        if (currentMailBox == null) {
            loadMailBox();
        }
        MailMsg mm = new MailMsg();
        mm.msg = msg;
        mm.attFile = attFile;
        if (currentMailBox != null) {
            mm.smtp = currentMailBox.smtp;
            mm.mailTo = currentMailBox.mailTo;
            mm.mailFrom = currentMailBox.mailFrom;
            mm.mailBoxUser = currentMailBox.mailBoxUser;
            mm.mailBoxPassword = currentMailBox.mailBoxPassword;
        }

        return mm;
    }

    public MailMsg copy() {
        if (currentMailBox == null) {
            loadMailBox();
        }
        MailMsg mm = new MailMsg();
        mm.msg = this.msg;
        mm.attFile = this.attFile;
        mm.keyWord = this.keyWord;
        mm.smtp = currentMailBox.smtp;
        mm.mailTo = currentMailBox.mailTo;
        mm.mailFrom = currentMailBox.mailFrom;
        mm.mailBoxUser = currentMailBox.mailBoxUser;
        mm.mailBoxPassword = currentMailBox.mailBoxPassword;
        return mm;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getAttFile() {
        return attFile;
    }

    public void setAttFile(String attFile) {
        this.attFile = attFile;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public String getMailTo() {
        return mailTo;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
    }

    public String getMailBoxUser() {
        return mailBoxUser;
    }

    public void setMailBoxUser(String mailBoxUser) {
        this.mailBoxUser = mailBoxUser;
    }

    public String getMailBoxPassword() {
        return mailBoxPassword;
    }

    public void setMailBoxPassword(String mailBoxPassword) {
        this.mailBoxPassword = mailBoxPassword;
    }

    public String getSmtp() {
        return smtp;
    }

    public void setSmtp(String smtp) {
        this.smtp = smtp;
    }

    public static void changeMailbox() {

        index++;
        if (index >= mailBox.size()) {
            index = 0;
        }
        if (mailBox.size() > 0) {
            currentMailBox = mailBox.get(index);
        }
        if (currentMailBox == null) {
            currentMailBox = new MailMsg();
        }
    }

}
