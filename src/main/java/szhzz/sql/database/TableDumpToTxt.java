package szhzz.sql.database;

import szhzz.Files.FileZiper;
import szhzz.Utils.DawLogger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

/**
 * Created by HuangFang on 2015/4/17.
 * 21:58
 */
public class TableDumpToTxt {
    private static DawLogger logger = DawLogger.getLogger(TableDumpToTxt.class);

    LinkedList<String> columns = null;
    Database db;
    String table;
    private String beforeInsertDelete = null;
    private String whereClause = null;
    private int batchRows = 1000;
    private String queryString;
    private ResultSet rs = null;
    private long rows = 0;
    private String verb = "INSERT IGNORE INTO ";

    public TableDumpToTxt(Database db, String table, LinkedList<String> columns) {
        this.db = db;
        this.table = table;
        this.columns = columns;
    }

    public void setBeforeInsertDelete(String beforeInsertDelete) {
        this.beforeInsertDelete = beforeInsertDelete;
    }

    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    public StringBuffer getResultText() {
        try {
            if ((rs == null) || rs.isAfterLast())
                return null;
        } catch (SQLException e) {
            logger.error(e);
        }

        int rowCount = 0;

        StringBuffer sb = new StringBuffer();
        try {
            if (rs.isBeforeFirst()) {
                sb.append(beforeInsertDelete).append(";\n");
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        int lines = 0;
        try {
            sb.append(createInsert());
            while (rs.next()) {
                if (lines > 0) {
                    sb.append(",");
                }
                sb.append("(");
                for (int c = 1; c <= columns.size(); c++) {
                    if (c > 1) {
                        sb.append(",");
                    }
                    Object o = rs.getObject(c);
                    sb.append("'").append(o).append("'");
                }
                sb.append(")");

                rows++;
                lines++;

                if (sb.length() > FileZiper.dataLen) {
                    break;
                }
                if (++rowCount > batchRows) {
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sb.append(";\n");
        return sb;
    }

    public void close() {
        Database.closeResultSet(rs);
    }

    String createInsert() {
        StringBuffer sb = new StringBuffer();
        for (String column : columns) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(column);
        }
        return verb + " " + table + "(" + sb.toString() + ") VALUES ";
    }

    public boolean prepare() {
        if (columns == null) {
            try {
                columns = db.getTableColumns(table);
            } catch (SQLException e) {
                logger.error(e);
            }
        }
        queryString = createQueryString();
        try {
            rs = db.dynamicSQL(queryString);
        } catch (DBException e) {
            logger.error(e);
        }
        return rs != null;
    }

    private String createQueryString() {
        StringBuffer sb = new StringBuffer();
        for (String column : columns) {
            if (sb.length() > 0)
                sb.append(",");
            sb.append(column);
        }
        return "SELECT " + sb.toString() + " FROM " + table + " " + whereClause;
    }

    public void setBatchRows(int batchRows) {
        this.batchRows = batchRows;
    }

    public long getRows() {
        return rows;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }
}
