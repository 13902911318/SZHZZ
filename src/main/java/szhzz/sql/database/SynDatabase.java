package szhzz.sql.database;


import szhzz.Config.CfgProvider;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2008-8-23
 * Time: 0:15:44
 * To change this template use File | Settings | File Templates.
 */
public class SynDatabase {
    public static final int DeleteCopy = 1;
    public static final int InsertIgnal = 2;
    public static final int UpdateOld = 3;

    Database dbSource;
    Database dbTaget;

    public SynDatabase(Database dbSource, Database dbTaget) {
        this.dbSource = dbSource;
        this.dbTaget = dbTaget;
    }

    public static void main(String[] args) throws DBException, SQLException {
        DBProperties mapDist = new DBProperties();
        Database dbSource = Database.getInstance(mapDist, SynDatabase.class);
        dbSource.openDB();

        DBProperties mapFrom = new DBProperties(CfgProvider.getRootFolder() + "/TZ13b.txt");
        Database dbTaget = Database.getInstance(mapFrom, SynDatabase.class);
        dbTaget.openDB();
        new SynDatabase(dbSource, dbTaget).copyDatabase();
    }

    void backUp() {
//        BACKUP TABLE tbl_name [, tbl_name] ... TO '/path/to/backup/directory'

    }

    public void copyDatabase() throws DBException, SQLException {
        ResultSet rs = null;
        LinkedList<String> tables = dbSource.getTables();
        try {
            for (String tableName : tables) {
                if ("cfg".equals(tableName)) {
                    UpdateStatement(tableName);
                    continue;
                }
                int rows = 0;

                LinkedList<String> columns = dbSource.getTableColumns(tableName);

                String query = CreateQueryString(tableName, columns);
                rs = dbSource.getStreamedResultSet(query);

                String update = CreateInsert(tableName, columns);
                PreparedStatement ps = dbTaget.prepareStatement(update);

                System.out.println(" Copied rows From " + tableName);
                while (rs.next()) {
                    for (int i = 1; i <= columns.size(); i++) {
                        ps.setObject(i, rs.getObject(i));
                    }
                    ps.executeUpdate();
                    if (++rows % 5000 == 0f) {
                        System.out.println(" Copied " + rows + " rows From " + tableName);
                    }
                }
            }
        } finally {
            Database.closeResultSet(rs);
        }
    }

    String CreateQueryString(String table, LinkedList<String> columns) {
        StringBuffer sb = new StringBuffer();
        for (String column : columns) {
            if (sb.length() > 0) sb.append(",");
            sb.append(column);
        }
        return "SELECT " + sb.toString() + " FROM " + table;
    }

    String CreateInsert(String table, LinkedList<String> columns) {
        StringBuffer sb = new StringBuffer();
        StringBuffer values = new StringBuffer();
        for (String column : columns) {
            if (sb.length() > 0) {
                sb.append(",");
                values.append(",");
            }
            sb.append(column);
            values.append("?");
        }
        return "INSERT IGNORE INTO  " + table + "(" + sb.toString() + ") VALUES (" + values + ")";
    }

    String CreateInsert_2(String table, LinkedList<String> columns) {
        StringBuffer sb = new StringBuffer();
        for (String column : columns) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(column);
        }
        return "INSERT IGNORE INTO  " + table + "(" + sb.toString() + ") ";
    }


    void UpdateStatement(String tableName) throws SQLException, DBException {
        int rows = 0;


        LinkedList<String> columns = dbSource.getTableColumns(tableName);
        LinkedList<String> types = dbSource.getTableColumnTypes(tableName);

        String query = CreateQueryString(tableName, columns);
        ResultSet st = dbSource.getStreamedResultSet(query);


        String Insert = CreateInsert_2(tableName, columns);

        System.out.println(" Copied rows From " + tableName);
        try {
            while (st.next()) {
                StringBuffer values = new StringBuffer();
                for (int i = 0; i < columns.size(); i++) {
                    if (i > 0) values.append(",");
                    values.append(dataQuotes(types.get(i)));
                    values.append(st.getObject(i + 1));
                    values.append(dataQuotes(types.get(i)));
                }
                String sql = Insert + "VALUES (" + values.toString() + ")";
                dbTaget.executeUpdate(sql);
                if (++rows % 5000 == 0f) {
                    System.out.println(" Copied " + rows + " rows From " + tableName);
                }
            }
        } finally {
            if (st != null) try {
                st.close();
            } catch (SQLException ignored) {

            }
        }
    }


    private String dataQuotes(String colType) {
        //SqlJavaTypeMaper.SqlToJavaName(getColTypeName(col))
        if ("java.lang.String".equals(SqlJavaTypeMaper.SqlToJavaName(colType)))
            return "\'";

        return "";
    }
}
