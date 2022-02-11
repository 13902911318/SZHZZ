package szhzz.sql.gui;

import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;
import szhzz.PinYin.getPingYin;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 11-2-23
 * Time: 下午4:59
 * To change this template use File | Settings | File Templates.
 */
public class PYConverter extends ObjectToStringConverter {

    public static Vector<String> getPyTable(String name) {
        Vector<String> pinyin = null;
        if (name != null) {
            pinyin = new Vector<String>();
            pinyin.add(name);
            pinyin.add(isChinaese(name));
        }

        return pinyin;
    }

    public static String isChinaese(String s) {
        int i = -1;
        int j = -1;
        String c = s.replaceAll("[*|_|-|\\[|\\]| ]", "");
        String t = c;

        for (i = c.length() - 1; i >= 0; i--) {
            if (Character.toString(c.charAt(i)).matches("[\\u4E00-\\u9FA5]+")) {
                break;
            }
        }
        if (i > 0) {  // 至少有两个中文字符
            for (j = 0; j < c.length(); j++) {
                if (Character.toString(c.charAt(j)).matches("[\\u4E00-\\u9FA5]+")) {
                    break;
                }
            }
            if (j < 3) // 前置2个以上英文，
            {
                if (i + 1 < c.length()) {
                    c = c.substring(0, i + 1);
                }
                c = getPingYin.getPinYinHeadChar(c);
                if (c.length() > 4) {
                    c = c.substring(0, 4);
                }

            }
        }
        if (c.equals(t)) c = "";
        return c.toUpperCase();
    }

    public String[] getPossibleStringsForItem(Object item) {
        if (item == null) return null;
        if (!(item instanceof Vector)) return new String[0];
        Vector value = (Vector) item;
        String[] sa = new String[value.size() + 1];

        sa[0] = "";
        for (int i = 0; i < value.size(); i++) {
            if (sa[0].length() > 0) sa[0] += " ";
            sa[0] += (value.get(i) == null ? "" : value.get(i).toString());
            sa[i + 1] = (value.get(i) == null ? "" : value.get(i).toString());
//                sa[i] = (value.get(i) == null ? "" : value.get(i).toString());
        }
        return sa;
    }

    public String getPreferredStringForItem(Object item) {
        String[] possible = getPossibleStringsForItem(item);
        String preferred = null;
        if (possible != null && possible.length > 0) {
            preferred = possible[0];
        }
        return preferred;
    }
}


