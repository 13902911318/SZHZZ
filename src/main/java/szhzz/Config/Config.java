package szhzz.Config;


import org.apache.commons.io.FileUtils;
import szhzz.Calendar.MiscDate;
import szhzz.Utils.DawLogger;
import szhzz.Utils.NU;
import szhzz.Utils.Utilities;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.*;

import static szhzz.Utils.Utilities.String2File;
import static szhzz.Utils.Utilities.getEquation;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2008-1-25
 * Time: 9:45:22
 * To change this template use File | Settings | File Templates.
 */
public abstract class Config {
    private static DawLogger logger = DawLogger.getLogger(Config.class);

    static int masterIndex = 0;
    Hashtable<String, item> datas;
    LinkedList<item> index;
    String configID = null;
    boolean hideProtect = true;
    private DataModel dataModel = null;
    private boolean cfgDirty = false;
    private boolean reloadProtect = false;
    private Hashtable<String, Config> children = null;
    private LinkedList<String> childrenIndex = null;

    public Config() {
        datas = new Hashtable<String, item>();
        index = new LinkedList<item>();
    }

    public boolean isMerged() {
        return false;
    }

    public Config copyTo(Config target) {
        if(target == null){
            target = new ConfigF() ;
        }
        target.loadDataVal(getTxt());
        return target;
    }

    public Config copyTo() {
        return copyTo(null);
    }

    public Config merge(Config c) {
        if (c == null) return this;
        if (c.isMerged()) return c;
        return new ConfigMerged(this, c);
    }

    public abstract boolean save();

    public abstract void load(String cfgID);


    public boolean saveAs(File file) {
        return saveAs(file, false);
    }

    public boolean saveAs(String file) {
        return saveAs(new File(file), false);
    }
    public void setConfigFileName(String configFileName){}
    public abstract void reLoad();

    public LinkedList<item> getIndex() {
        return index;
    }

    public String getConfigID() {
        return configID;
    }


    public Enumeration getEnumeration() {
        return datas.keys();
    }

    public int size() {
        return datas.size();
    }

    public boolean hasProperty(String name) {
        return getProperty(name) != null;
    }

    public boolean isProtected(String name) {
        if (datas.get(name) != null) {
            return datas.get(name).isProtected();
        }
        return false;
    }

    public String getProperty(String name) {
        String ret = null;
        if (datas.get(name) != null)
            ret = decodeLine(datas.get(name).value);
        return ret;
    }

    public String getLastVal(String name) {
        String ret = null;
        item i = datas.get(name);
        if (i != null)
            ret = i.old_value;
        return ret;
    }

    public boolean isDirty() {
        return cfgDirty;
//        item i = datas.get(name);
//        return i != null && i.isDirty();
    }

//    public void clearDirty(String name) {
//        item i = datas.get(name);
//        if (i != null)
//            i.clearDirty();
//    }

    public abstract String getConfigUrl();

    public void insertProperty(String afterName, String name, String val) {
        insertProperty(afterName, name, val, null);
    }

    public void insertProperty(int atIndex, String name, String val, String comment) {
        if (atIndex < 0) {
            atIndex = 0;
        }

        val = encodeLine(val);
        item e = datas.get(name);
        if (e == null) {
            e = new item(name, val);
            if (atIndex < index.size()) {
                index.add(atIndex, e);
            } else {
                index.add(e);
            }
            datas.put(name, e);
        } else {
            e.setValue(val);
        }
        if (comment != null) e.setComment(comment);
    }

    public void insertProperty(String afterName, String name, String val, String comment) {
        int i = index.indexOf(datas.get(afterName));
        if (i < 0) {
            setVal(name, val, null);
            return;
        }

        val = encodeLine(val);
        item e = datas.get(name);
        i++;
        if (e == null) {
            e = new item(name, val);
            if (i < index.size()) {
                index.add(i, e);
            } else {
                index.add(e);
            }
            datas.put(name, e);
        } else {
            e.setValue(val);
        }
        if (comment != null) e.setComment(comment);

    }

    public void setProperty(String name, String val) {
        setVal(name, val, null);
    }

    public void setCfgID(String id) {
        this.configID = id;
    }

    public void setProperty(String name, String val, String comment) {
        setVal(name, val, comment);
    }

    public void setProperty(String name, boolean val) {
        setVal(name, val ? "true" : "false", null);
    }

    public void setProperty(String name, boolean val, String comment) {
        setVal(name, val ? "true" : "false", comment);
    }

    public void removeProperty(String key) {
        index.remove(datas.remove(key));
    }

    public void renameProperty(String oldKey, String newKey) {
        if (datas.get(newKey) != null) return;

        item p = datas.remove(oldKey);
        if (p != null) {
            p.name = newKey;
            datas.put(p.name, p);
        }
    }

    public void clear() {
        datas.clear();
        index.clear();
    }

    private String encodeLine(String lines) {
        if (lines.contains("\n")) {
            lines = lines.replace("\n", "\\n");
        }
        return lines;
    }

    private String decodeLine(String lines) {
        if (lines.contains("\\n")) {
            lines = lines.replace("\\n", "\n");
        }
        return lines;
    }

    public void setVal(String name, String val, String comment) {
        val = encodeLine(val);
        item e = datas.get(name);
        if (e == null) {
            e = new item(name, val);
            index.add(e);
            datas.put(name, e);
        } else {
            e.setValue(val);
        }
        if (comment != null) e.setComment(comment);
    }

    public void setComment(String name, String comment) {
        item e = datas.get(name);
        if (comment != null && e != null) {
            e.setComment(comment);
        }
    }

    public String getComment(String name) {
        item e = datas.get(name);
        if (e != null) {
            return e.getComment();
        }
        return null;
    }

    public void insertVal(String beforeName, String name, String val, String comment) {
        val = encodeLine(val);
        item e = datas.get(name);
        if (e == null) {
            e = new item(name, val);
            datas.put(name, e);

            item bf = datas.get(beforeName);
            if (bf != null) {
                int i = index.indexOf(bf);
                if (i + 1 < index.size()) {
                    i++;
                }
                index.add(i, e);
            } else {
                index.add(e);
            }

        } else {
            e.setValue(val);
        }
        if (comment != null) e.setComment(comment);
    }

    public boolean propertyEquals(String name, String value) {
        return getProperty(name, "").equals(value);
    }

    public String getProperty(String name, String defaultValue) {
        String val = getProperty(name);
        if (val == null || "".equals(val) || "''".equals(val) || "\"\"".equals(val)) {
            val = defaultValue;
        }
        return val;
    }

    public String getComment(String name, String defaultValue) {
        String ret = null;
        if (datas.get(name) != null) {
            ret = datas.get(name).getComment();
        }
        if (ret == null || "".equals(ret)) {
            ret = defaultValue;
        }
        return ret;
    }

    public int getIntVal(String name, int defalt) {
        String val = getProperty(name);
        if (val != null)
            try {
                return NU.parseLong(val, (long) defalt).intValue();
            } catch (Exception e) {

            }
        return defalt;
    }

    public int getIntVal(String name) {
        return getIntVal(name, 0);
    }

    public boolean getBooleanVal(String name, boolean defalt) {
        String val = getProperty(name);
        if (val != null) {
            val = val.toLowerCase();
            return (val.equals("true") || val.equals("yes"));
        }

        return defalt;
    }

    public long getLongVal(String name, long defalt) {
        String val = getProperty(name);
        if (val != null)
            try {
                return NU.parseLong(val, defalt);
            } catch (Exception ignored) {

            }

        return defalt;
    }



    public long getLongVal(String name) {
        return getLongVal(name, 0l);
    }

    public float getFloatVal(String name, float defalt) {
        String val = getProperty(name);
        if (val != null)
            try {
                return NU.parseDouble(val, (double) defalt).floatValue();
            } catch (Exception ignored) {

            }
        return defalt;
    }

    public float getFloatVal(String name) {
        return getFloatVal(name, 0f);
    }

    public double getDoubleVal(String name, double defalt) {
        String val = getProperty(name);
        if (val != null)
            try {
                return NU.parseDouble(val, defalt);
            } catch (Exception ignored) {

            }

        return defalt;
    }


    public double getDoubleVal(String name) {
        return getDoubleVal(name, 0d);
    }


    public Vector<String> getKeys() {
        Vector<String> e = new Vector();
        for (String k : datas.keySet()) {
            if (hideProtect && isProtected(k)) {
                continue;
            }

            if (getProperty(k, null) != null) {
                e.add(k);
            }
        }
        return e;
    }

    //*************************************
    public String peekProperty(String name) {
        String val = getProperty(name);
        if (val == null) {
            JOptionPane.showMessageDialog(null, "Can not find data " + name);
            return null;
        }
        return val;
    }

    public Integer peekIntVal(String name) {
        String val = getProperty(name);
        try {
            if (val == null) {
                JOptionPane.showMessageDialog(null, "Can not find data " + name);
                return null;
            }
            return Integer.parseInt(val);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Data type mismatch," + name);
            return null;
        }
    }


    public Boolean peekBooleanVal(String name) {
        String val = getProperty(name);
        if (val == null) {
            JOptionPane.showMessageDialog(null, "Can not find data " + name);
            return null;
        }
        val = val.toLowerCase();
        return (val.equals("true") || val.equals("yes"));
    }

    public Long peekLongtVal(String name) {
        String val = getProperty(name);
        if (val == null) {
            JOptionPane.showMessageDialog(null, "Can not find data " + name);
            return null;
        }
        try {
            return Long.parseLong(val);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Data type mismatch," + name);
            return null;
        }
    }


    public Float peekFloatVal(String name) {
        String val = getProperty(name);
        if (val == null) {
            JOptionPane.showMessageDialog(null, "Can not find data " + name);
            return null;
        }
        try {
            return Float.parseFloat(val);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Data type mismatch," + name);
            return null;
        }
    }

    public DataModel getDataModel() {
        if (dataModel == null) dataModel = new DataModel();
        return dataModel;
    }

    public boolean isReloadProtect() {
        return reloadProtect;
    }

    public void setReloadProtect(boolean reloadProtect) {
        this.reloadProtect = reloadProtect;
    }


    public boolean saveAs(File file, boolean append) {
        StringBuffer sb = new StringBuffer("");
        PrintWriter f = null;
        boolean saved = false;

        sb.append("//# LastUpdate ").append(MiscDate.now()).append("\n");
        for (item e : index) {
            if (!e.toString().startsWith("//# LastUpdate")) {
                sb.append(e.toString());
                sb.append("\n");
            }
        }
        if (this.getBooleanVal("锁定", false)) {
            this.setProperty("锁定", false);
        }

        if (childrenIndex != null) {
            for (String key : childrenIndex) {
                Config child = children.get(key);
                sb.append("\n").append("[").append(key).append("]\n").append(child.getTxt());
            }
        }
        try {
            FileUtils.writeStringToFile(file, sb.toString(), Charset.forName("UTF-8"), false);
            saved = true;
        } catch (IOException e) {
            logger.error(e);
        }
        cfgDirty = !saved;

        return saved;
    }

    public String getTxt() {
        StringBuffer sb = new StringBuffer("");
        PrintWriter f = null;
//        if (index.size() > 0 && index.get(0).toString().startsWith("//# LastUpdate")) {
//            index.remove(0);
//        }

//        sb.append("//# LastUpdate ").append(MiscDate.now()).append("\n");
        for (item e : index) {
            if (!e.toString().startsWith("//# LastUpdate")) {
                sb.append(e.toString());
                sb.append("\n");
            }
        }
        if (this.getBooleanVal("锁定", false)) {
            this.setProperty("锁定", false);
        }
        return sb.toString();
    }

    public void setNanoTime() {
        setProperty("System_nanoTime", String.valueOf(System.nanoTime()));
    }

    public long getNanoTime() {
        return getLongVal("System_nanoTime", 0);
    }

    public String loadChild(BufferedReader in) {
        String tk;
        try {
            while ((tk = in.readLine()) != null) {
                if (tk.trim().length() == 0) continue;
                String trim = tk.trim();
                if (trim.startsWith("[") && trim.endsWith("]")) {
                    return tk.trim();
                }
                item e = new item(tk);
                if (e.name != null && !"".equals(e.name)) {
                    datas.put(e.name, e);
                    index.add(e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        cfgDirty = false;
        return "";
    }

    public Set<String> getChildrenNames() {
        if (children == null) return null;
        return children.keySet();
    }

    public Config getChild(String sectionName) {
        if (children == null) return null;
        return children.get(sectionName);
    }

    public void loadDataVal(BufferedReader in) {
        String tk;
        try {
            while ((tk = in.readLine()) != null) {
                String trim = tk.trim();
                while (trim.startsWith("[") && trim.endsWith("]")) {
                    if (children == null) {
                        children = new Hashtable<>();
                        childrenIndex = new LinkedList<String>();
                    }
                    Config cfg = new ConfigF();
                    String name = trim.replace("[", "").replace("]", "").toUpperCase();
                    children.put(name, cfg);
                    childrenIndex.add(name);
                    trim = cfg.loadChild(in);
                }
                if (trim.length() == 0) continue;
                item e = new item(tk);
                if (e.name != null && !"".equals(e.name)) {
                    datas.put(e.name, e);
                    index.add(e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        cfgDirty = false;
    }

    public void loadDataVal(String lines) {
        String[] tk = lines.split("\n");

        for (String l : tk) {
            if (l.length() == 0) continue;
            item e = new item(l);

            if (e.name != null && !"".equals(e.name)) {
                datas.put(e.name, e);
                index.add(e);
            }
        }
        cfgDirty = false;
    }

    public boolean saveAsCode(String file) {
        StringBuffer sb = new StringBuffer("");
        if (index.get(0).toString().startsWith("//# LastUpdate")) {
            index.remove(0);
        }
        boolean saved = true;
        sb.append("//# LastUpdate ").append(MiscDate.now()).append("\n");
        for (item e : index) {
            if (e.isBoolean()) {
                sb.append("public static boolean ");
            } else if (e.isNumber()) {
                sb.append("public static int ");
            } else {
                sb.append("public static String ");
            }
            sb.append(e.name);
            sb.append(" = ");
            if (e.isBoolean() || e.isNumber()) {
                sb.append(e.getValue());
            } else {
                sb.append("\"").append(e.getValue()).append("\"");
            }
            sb.append(";");
            if (e.getComment() != null && e.getComment().length() > 0) {
                sb.append("\t//").append(e.getComment());
            }

            sb.append("\n");
        }
        sb.append("\n");
        sb.append("\n");
        sb.append("static boolean dataloaded = loadData();");
        sb.append("\n");
        sb.append("\n");

        sb.append("\tpublic static Config getCfg() {");
        sb.append("\n");
        sb.append("\t\treturn StockAnalyzeConfiger.getCfg();");
        sb.append("}\n");


        String s = "OldSystem";
        sb.append("\tpublic static final String oldSystem = ");
        sb.append("\"");
        sb.append(s);
        sb.append("\";");
        sb.append("\n");
        sb.append("\tpublic static boolean loadData(String file) {");
        sb.append("\tConfig cfg = StockAnalyzeConfiger.getCfg();");
        sb.append("\tcfg.load(file);");
        sb.append("\treturn loadData();");
        sb.append("\t}");

        sb.append("\tpublic static boolean useOldSystem = false;");

        sb.append("\tpublic static boolean loadData() {");
        sb.append("\n");
        sb.append("\t\tConfig cfg = StockAnalyzeConfiger.getCfg();");
        sb.append("\n");


        for (item e : index) {
            sb.append(e.name);
            sb.append(" = ");
            if (e.isBoolean()) {
//                sb.append("cfg.getBooleanVal(\"").append(e.name).append("\",").append(e.value).append(")");
                sb.append("cfg.peekBooleanVal(\"").append(e.name).append("\")");
            } else if (e.isNumber()) {
                //sb.append("cfg.getIntVal(\"").append(e.name).append("\",").append(e.value).append(")");
                sb.append("cfg.peekIntVal(\"").append(e.name).append("\")");
            } else {
                //sb.append("cfg.getProperty(\"").append(e.name).append("\",\"").append(e.value).append("\")");
                sb.append("cfg.peekProperty(\"").append(e.name).append("\")");
            }
            sb.append(";");
            if (e.getComment() != null && e.getComment().length() > 0) {
                sb.append("\t//").append(e.getComment());
            }
            sb.append("\n");
        }
        sb.append("\n");
        sb.append("return true;");
        sb.append("\n");
        sb.append("\t}");
        try {
            String2File(sb.toString().trim(), file, false);
        } catch (IOException e) {
            e.printStackTrace();
            saved = false;
        }
        return saved;
    }

    public abstract String getConfigFolder();

    public Config getSlave() {
        return this;
    }

    public boolean equals(Object o) {
        return o != null && o instanceof Config && this.getConfigUrl().equals(((Config) o).getConfigUrl());
    }

    public class item {
        String name = null;
        String value = null;
        String comment = "";
        String old_value = null;

        item(String name, String value) {
            this.name = name;
            this.setValue(value);
            cfgDirty = true;
        }

        item(String line) {
            old_value = value;
            String e[] = getEquation(decodeLine(line));
            name = e[0];
            value = e[1];
            comment = e[2];
            cfgDirty = true;
        }


        boolean isProtected() {
            return "Protect".equals(getComment());
        }

        public String toString() {
            String l = "";
            if ("".equals(name)) {
                l = getComment();
            } else {
                l = name + "=" + getValue();
                if (getComment().trim().length() > 0) l += "    //" + getComment();
            }
            return l;
        }

        public boolean isNumber() {
            return Utilities.isNumber(getValue());
        }

        public boolean isBoolean() {
            return "yes".equals(getValue().toLowerCase()) || "true".equals(getValue().toLowerCase()) ||
                    "no".equals(getValue().toLowerCase()) || "false".equals(getValue().toLowerCase());
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            old_value = this.value;
            this.value = decodeLine(value);
            cfgDirty = true;
        }

//        boolean isDirty() {
//            if (value == null) {
//                return old_value != null;
//            }
//            return !value.equals(old_value);
//        }
//
//        void clearDirty() {
//            old_value = null;
//        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = encodeLine(comment);
        }
    }

    class DataModel extends AbstractTableModel {

        public int getRowCount() {
            return index.size();  //To change body of implemented methods use File | Settings | File Templates.
        }

        public int getColumnCount() {
            return 3;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Object getValueAt(int i, int i1) {
            if (i1 == 0) return index.get(i).name;
            if (i1 == 1) return truncate(index.get(i).getValue(), 50);
            if (i1 == 2) return truncate(index.get(i).getComment(), 50);
            return "";
        }

        String truncate(String v, int L) {
            if (v.length() > L) {
                return v.substring(0, L - 3) + "...";
            }
            return v;
        }
    }

    public void readLine(String s) {
        if (s.trim().length() == 0) return;
        item e = new item(s);
        if (e.name != null && !"".equals(e.name)) {
            datas.put(e.name, e);
            index.add(e);
        }
        cfgDirty = true;

    }
}
