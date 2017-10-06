package szhzz.PinYin;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 2009-6-23
 * Time: 0:09:21
 * To change this template use File | Settings | File Templates.
 */
public class getPingYin {
    //    private static Hashtable<String, String> DuoYinZi = null;
    private static final String[] dyc = new String[]{"银行", "YH", "重庆", "CQ", "西藏", "XZ", "大厦", "DS", "广厦", "GS", "成长", "CZ", "空调", "KT"};

    public static String getPingYin(String src) {
        char[] t1 = null;
        t1 = src.toCharArray();
        String[] t2 = new String[t1.length];
        HanyuPinyinOutputFormat t3 = new HanyuPinyinOutputFormat();
        t3.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        t3.setVCharType(HanyuPinyinVCharType.WITH_V);
        String t4 = "";
        int t0 = t1.length;
        try {
            for (int i = 0; i < t0; i++) {
                //判断是否为汉字字符
                if (java.lang.Character.toString(t1[i]).matches("[\\u4E00-\\u9FA5]+")) {
                    t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], t3);
                    t4 += t2[0];
                } else
                    t4 += java.lang.Character.toString(t1[i]);
            }
//       System.out.println(t4);
            return t4;
        } catch (BadHanyuPinyinOutputFormatCombination e1) {
            e1.printStackTrace();
        }
        return t4;
    }

    //返回中文的首字母
    public static String getPinYinHeadChar(String str) {
//        if (DuoYinZi == null) {
//            DuoYinZi = new Hashtable<String, String>();
//            DuoYinZi.put("长", "c");
//            DuoYinZi.put("行", "h");
//        }

        String convert = "";
        for (int i = 0; i < dyc.length; i += 2) {
            str = str.replace(dyc[i], dyc[i + 1]);
        }

        for (int j = 0; j < str.length(); j++) {
            char word = str.charAt(j);
            try {
//                String h = DuoYinZi.get("" + word);
//                if (h != null) {
//                    convert += h;
//                } else {
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
                if (pinyinArray != null) {
                    convert += pinyinArray[0].charAt(0);
                } else {
                    convert += word;
                }
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return convert;
    }

    //将字符串转移为ASCII码
    public static String getCnASCII(String cnStr) {
        StringBuffer strBuf = new StringBuffer();
        byte[] bGBK = cnStr.getBytes();
        for (int i = 0; i < bGBK.length; i++) {
//              System.out.println(Integer.toHexString(bGBK[i]&0xff));
            strBuf.append(Integer.toHexString(bGBK[i] & 0xff));
        }
        return strBuf.toString();
    }

    public static void main(String[] args) {

        String cnStr = "招商银行";
        System.out.println(getPingYin(cnStr));
        System.out.println(getPinYinHeadChar(cnStr));
        cnStr = "重庆水务";
        System.out.println(getPingYin(cnStr));
        System.out.println(getPinYinHeadChar(cnStr));
    }

}
//下面为结果:
//zhonghuarenmingongheguo
//zhrmghg }
