package szhzz.sql.database;

public class DBProper extends DBProperties {

    public DBProper() throws DBException {
        super();
    }

    public DBProper(String propFileName) throws DBException {

        setup(propFileName);
    }

    public void reload(String propFileName) throws DBException {
        setup(propFileName);
    }

    public String getDbURL() {
        return this.getProperty(Database.DBURL);
    }

    public String getHost() {
        return this.getProperty("host");
    }

    public void setHost(final String host) {
        this.setProperty("host", host);
    }

    public String getSchema() {
        return this.getProperty("schema");
    }

    public void setSchema(final String schema) {
        this.setProperty("schema", schema);
    }

    public void composeMySqlUrl() {
        String url = "jdbc:mysql://" + getHost() + ":" + this.getPort() + "/" + this.getSchema();
        this.setProperty(Database.DBURL, url);
    }
}