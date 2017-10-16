package szhzz.sql.database;


import szhzz.Config.CfgProvider;
import szhzz.Config.SharedCfgProvider;

import java.io.*;
import java.util.Properties;

/**
 * <p>Title: INFO2820</p>
 * <p/>
 * <p>Description: home work INFO2820</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p/>
 * <p>Company: </p>
 *
 * @author John
 * @version 1.0
 */
public class DBProperties {
    /**
     * Plattform independent line break.
     */
    public static String lineSep = System.getProperty("line.separator");
    String fileName = SharedCfgProvider.getInstance("MySql").getDir() + "\\MySQL.ini";
    private Properties props;

    public DBProperties() throws DBException {
        setup(fileName);
    }


    public DBProperties(String propFileName) throws DBException {
        setup(propFileName);
    }

    public static DBProperties getInstance() throws DBException {
        return new DBProperties();
    }

    protected void setup(String fileName) throws DBException {
        if (fileName != null && new File(fileName).exists())
            this.fileName = fileName;

        FileInputStream in = null;
        props = new Properties();
        if (!autoload()) return;

        try {
            in = new FileInputStream(this.fileName);
            props.load(in);

        } catch (FileNotFoundException fnfex) {
            throw new DBException("Property file " + this.fileName + " not found!",
                    DBException.PROPERTYFILENOTFOUND);
        } catch (IOException ioex) {
            throw new DBException("Could not read property file: " + this.fileName,
                    DBException.PROPERTYFILEREADERROR);
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

    }

    /**
     * Access method for the list of properties
     *
     * @return the properties object
     */
    public Properties getProperties() {
        return props;
    }

    public void setProperties(Properties props) {
        this.props = props;
    }


    public void save() throws IOException {
        if (fileName == null) return;

        FileOutputStream objFStream = new FileOutputStream(fileName);
        this.props.store(objFStream, "");

    }


    public void saveAs(String f) throws IOException {
        FileOutputStream objFStream = new FileOutputStream(f);
        this.props.store(objFStream, "");
    }

    /**
     * Searches for the property with the specified key in the property list.
     * The method returns <code>null</code> if the property is not found.
     *
     * @param key the property key.
     * @return the value in this property list with the specified key value.
     * @see DBProperties#setProperty
     */
    public String getProperty(String key) {
        return props.getProperty(key);
    }

    /**
     * Tests if the specified property is in the property list.
     * The method returns <code>true</code> if the property is present.
     *
     * @param key the property key.
     * @return true if the specified property in the property list;
     * false otherwise.
     */
    public boolean containsKey(String key) {
        return props.getProperty(key) != null;
    }

    /**
     * Searches for the property with the specified key in the property
     * list. If its value is not <code>null</code> and is equal,
     * ignoring case, to the string
     * <code>"true"</code> the method returns <code>true</code>,
     * and <code>false</code> otherwise.
     *
     * @param key the property key.
     * @return the <code>"boolean"</code> value in this property list
     * @see DBProperties#setBooleanProperty
     */
    public boolean getBooleanProperty(String key) {
        return Boolean.valueOf(getProperty(key));
    }

    public boolean Debug() {
        return getBooleanProperty("DEBUG");
    }

    /**
     * Searches for the property to determine whether the specified XML
     * attribute should be used.
     *
     * @param name the queryName of the attribute
     * @return the <code>"true"</code> if the attribute is in the list
     * and should be used, otherwise <code>"false"</code>.
     */
    public boolean useAttribute(String name) {
        return getBooleanProperty(name);
    }

    /**
     * Changes the usage of the named XML attribute
     *
     * @param key   the queryName of the attribute
     * @param value usage of attribute
     */
    public void setUseAttribute(String key, boolean value) {
        setBooleanProperty(key, value);
    }

    /**
     * Checks whether the specified property has the given value.
     *
     * @param key   the queryName of the property
     * @param value value of the property
     */
    public boolean propertyHasValue(String key, String value) {
        if (value == null)
            return false;
        return value.equals(getProperty(key));
    }

    public boolean HasProperty(String key) {
        if (key == null)
            return false;
        return this.containsKey(key);
    }

    /**
     * Maps the specified <code>key</code> to the specified
     * <code>value</code> in the property list. Neither the key nor the
     * value can be <code>null</code>.
     *
     * @param key   the property key
     * @param value the property value
     * @see DBProperties#getProperty
     */
    public void setProperty(String key, String value) {
        props.setProperty(key, value);
    }

    public void setProperty(String childName, String key, String value) {
        props.setProperty(key, value);
    }

    /**
     * Removes a named property from the list.
     *
     * @param key the queryName of the property to be removed
     */
    public void removeProperty(String key) {
        props.remove(key);
    }

    /**
     * Updates or inserts property with specified queryName. The value will
     * be <code>"true"</code> or <code>"false"</code> (i.e. a string)
     * depending on the parameter.
     *
     * @param key the property key.
     * @param b   the boolean value
     * @see DBProperties#getBooleanProperty
     */
    public void setBooleanProperty(String key, boolean b) {
        String value = b ? "true" : "false";
        props.setProperty(key, value);
    }


    /**
     * Check several constraints among the settings. Throws an exception
     * in case of a violation.
     *
     * @throws DBException in case of violation
     */
    public void checkConstraints() throws DBException {
    }

    protected boolean autoload() {
        return true;
    }

    public String getFieldPatten(String QName) {
        return this.getProperty("Pattern." + QName);
    }

    public String getDriverClass() {
        return this.props.getProperty(Database.DRIVER);
    }

    public void setDriverClass(String drv) {
        this.props.setProperty(Database.DRIVER, drv);
    }

    public String getDbURL() {
        return this.props.getProperty(Database.DBURL);
    }

    public void setDbURL(String url) {
        this.props.setProperty(Database.DBURL, url);
    }

    public String getUser() {
        return this.props.getProperty(Database.USER);
    }

    public void setUser(String user) {
        this.props.setProperty(Database.USER, user);
    }

    public String getPassword() {
        return this.props.getProperty(Database.PASSWORD);
    }

    public void setPassword(String password) {
        this.props.setProperty(Database.PASSWORD, password);
    }

    public String getPort() {
        return this.props.getProperty(Database.PORT);
    }

    public void setPort(final String port) {
        this.props.setProperty(Database.PORT, port);
    }

    public String getFileName() {
        return fileName;
    }

}
