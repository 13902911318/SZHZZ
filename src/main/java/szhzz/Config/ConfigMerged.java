package szhzz.Config;

import java.io.BufferedReader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: SZHZZ
 * Date: 13-3-30
 * Time: 上午11:36
 * To change this template use File | Settings | File Templates.
 */
public class ConfigMerged extends Config {
    Config master;
    Config slave;

    private ConfigMerged() {
    }

    public ConfigMerged(Config master, Config slave) {
        this.master = master;
        if (slave != null) {
            this.slave = slave;
        } else {
            this.slave = new ConfigF();
        }
    }

    public Config getMaster() {
        return master;
    }

    public Config getSlave() {
        return slave;
    }

    public LinkedList<item> getIndex() {
        return index;
    }

    public String getConfigID() {
        return master.getConfigID();
    }

    public Enumeration getEnumeration() {
        Vector h = new Vector();
        h.addAll((Collection) master.getEnumeration());
        h.addAll((Collection) slave.getEnumeration());
        return h.elements();
    }

    public int size() {
        return master.size() + slave.size();
    }

    public boolean hasProperty(String name) {
        return master.hasProperty(name) || slave.hasProperty(name);
    }

    public String getProperty(String name) {
        if (master.hasProperty(name)) return master.getProperty(name);
        return slave.getProperty(name);
    }

    public void insertProperty(String afterName, String name, String val) {
        if (slave.hasProperty(name)) {
            slave.insertProperty(afterName, name, val, null);
        } else {
            master.insertProperty(afterName, name, val, null);
        }
    }

    public void setProperty(String name, String val) {
        if (slave.hasProperty(name)) {
            slave.setProperty(name, val);
        } else {
            master.setProperty(name, val);
        }
    }

    public void setProperty(String name, String val, String comment) {
        if (slave.hasProperty(name)) {
            slave.setProperty(name, val, comment);
        } else {
            master.setProperty(name, val, comment);
        }
    }

    public void setProperty(String name, boolean val) {
        if (slave.hasProperty(name)) {
            slave.setProperty(name, val);
        } else {
            master.setProperty(name, val);
        }
    }

    public void setProperty(String name, boolean val, String comment) {
        if (slave.hasProperty(name)) {
            slave.setProperty(name, val, comment);
        } else {
            master.setProperty(name, val, comment);
        }
    }

    public void removeProperty(String key) {
        if (slave.hasProperty(key)) {
            slave.removeProperty(key);
        } else {
            master.removeProperty(key);
        }
    }

    public void renameProperty(String oldKey, String newKey) {
        if (slave.hasProperty(oldKey)) {
            slave.renameProperty(oldKey, newKey);
        } else {
            master.renameProperty(oldKey, newKey);
        }
    }

    public void clear() {
        master.clear();
        slave.clear();
    }


    public String getProperty(String name, String defaultValue) {
        if (slave.hasProperty(name)) {
            return slave.getProperty(name, defaultValue);
        } else {
            return master.getProperty(name, defaultValue);
        }
    }


    public Vector<String> getKeys() {
        Vector e = new Vector();
        e.addAll(master.getKeys());
        e.addAll(slave.getKeys());
        return e;
    }


    public void loadDataVal(BufferedReader in) {
    }


    @Override
    public boolean save() {
        //To change body of implemented methods use File | Settings | File Templates.
        return master.save();
    }

    @Override
    public void load(String cfgID) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void reLoad() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getConfigUrl() {
        if (slave != null) {
            return master.getConfigUrl() + "+" + slave.getConfigUrl();
        } else {
            return master.getConfigUrl();
        }
    }

    @Override
    public String getConfigFolder() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
