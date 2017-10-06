package szhzz.sql.database;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2008-7-21
 * Time: 17:13:46
 * To change this template use File | Settings | File Templates.
 */
public class PagedDAO extends GeneralDAO {
    private int PageRowNumber = 100;
    private int currentPage = 1;

    public PagedDAO(String query, Database db, boolean b) throws SQLException {
        this.readOnly = true;
        this.query = query;
        this.db = db;
        this.map = db.getDBProperties();
        logDBQuery = map.getBooleanProperty("logDB4Query");
        dbMeta = db.getDbMeta();

    }


    /**
     * @return result set for this query.
     * @throws DBException in case of an db error or if query
     *                     is not a selection (i.e. inserts are not allowed).
     */
    ResultSet execute() throws DBException {
        try {
            if (resultSet == null) {
                //resultSet = new CachedRowSetImpl();
                if (!query.trim().toLowerCase().startsWith("select"))
                    throw new DBException("Only select statements are allowed! " +
                            "(" + query + ")", DBException.SQLEXCEPTION);
                try {
//                    String limited = query + " LIMIT " +  currentPage * PageRowNumber + ", "  + PageRowNumber;
                    resultSet = stmt.executeQuery(query);
                    stmt.setFetchSize(Integer.MIN_VALUE);
                    if (logDBQuery) log2Db_(query);
                } catch (SQLException ex) {
                    if (map.propertyHasValue("document", "complete"))
                        throw new DBException("The query:         " + query + "\n"
                                + "could not be executed! Probably "
                                + "you have no permission to access "
                                + "this \n"
                                + "table or you are not connected "
                                + "to this catalog. Change schema or "
                                + "catalog selection!",
                                DBException.UNSUPPORTEDFEATURE);
                    else
                        throw ex;
                }
            }
            return resultSet;
        } catch (SQLException ex) {
            throw new DBException(ex);
        }
    }

    public ResultSet getStreamdResultset(int buffer) throws SQLException, DBException {
//        stmt = db.getStreamedStatement(buffer);
        return execute();
    }
}
