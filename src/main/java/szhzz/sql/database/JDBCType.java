package szhzz.sql.database;


import szhzz.Utils.DawLogger;

import javax.mail.internet.MailDateFormat;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
 * TODO TBD
 * 把数据库数据类型映射成为 java 数据类型(非原始类型)
 * <p/>
 * Created by Jhon.
 * User: szhzz
 * Date: 2006-9-27
 * Time: 17:52:36
 * <p/>
 * Database to Java data type map
 */
public class JDBCType {
    public static final String isNULL = "NULL";      //null
    static NumberFormat nf = (DecimalFormat) NumberFormat.getInstance();
    static DateFormat df = MailDateFormat.getDateInstance(DateFormat.LONG);
    private static DawLogger logger = DawLogger.getLogger(JDBCType.class);

    public static Object getColumnDataObject(int col, ResultSet rs) {
        Object obj = null;
        int type = 0;
        try {
            type = rs.getMetaData().getColumnType(col);
            switch (type) {
                case Types.INTEGER:
                case Types.SMALLINT:
                case Types.TINYINT:
                    obj = rs.getObject(col);
                    break;
                case Types.DOUBLE:
                case Types.DECIMAL:
                case Types.NUMERIC:
                case Types.FLOAT:
                case Types.REAL:
                case Types.BIGINT:

                    //nf.setMaximumFractionDigits(3);
                    //obj = nf.format(rs.getDouble(col));
                    obj = rs.getObject(col);
                    break;
                case Types.BIT:
                    if (rs.getObject(col) != null)
                        obj = rs.getBoolean(col);
                    break;
                case Types.DATE:
                    obj = rs.getObject(col);
                    //obj = java.szhzz.sql.Date.valueOf(rs.getObject(col).toString());
                    break;
                case Types.TIMESTAMP:
                    //cdate
                    //obj = java.szhzz.sql.Timestamp.valueOf(rs.getObject(col).toString());
                    //TODO Data & DatTime format error in datawindow, sou changge type as String
                    // check this error
                    if (rs.getObject(col) != null)
                        obj = rs.getObject(col).toString();
                    break;
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                    if (rs.getObject(col) != null)
                        obj = rs.getObject(col).toString().trim();
                    break;
                case Types.BLOB:
                case Types.CLOB:
                    obj = "<BLOB>";
                    break;
                case Types.NULL:
                    obj = isNULL;  // Map to blank string
                    break;
                default:
//                    obj = rs.getObject(col);
                    if (rs.getObject(col) != null)
                        obj = rs.getObject(col).toString();
                    break;
            }
        } catch (SQLException e) {
            logger.error("SQL Exception in getColumnData.", e);
        }
        return obj;
    }

}