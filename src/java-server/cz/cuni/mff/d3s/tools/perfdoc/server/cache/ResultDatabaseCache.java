/*
 Copyright 2014 Jakub Naplava
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cuni.mff.d3s.tools.perfdoc.server.cache;

import cz.cuni.mff.d3s.tools.perfdoc.server.cache.ResultCache;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.ResultAdminCache;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of ResultCache that saves the results in the database.
 *
 * @author Jakub Naplava
 */
public class ResultDatabaseCache implements ResultAdminCache {

    private static final Logger log = Logger.getLogger(ResultDatabaseCache.class.getName());

    private static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String JDBC_URL = "jdbc:derby:cacheDB;create=true";
    private static final String TEST_URL = "jdbc:derby:testDB;create=true";

    protected Connection conn;

    public ResultDatabaseCache() throws SQLException {
        this.conn = createConnection();
    }

    public ResultDatabaseCache(Boolean test) throws SQLException {
        if (test) {
            this.conn = createTestConnection();
        }
    }

    @Override
    public void startDatabase() throws ClassNotFoundException, SQLException {
        try {
            Class.forName(DRIVER);

            checkTablesAndCreate();

        } catch (ClassNotFoundException e) {
            log.log(Level.SEVERE, "Could not find the database driver", e);
            throw e;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "The connection to database could have not been established.", e);
            throw e;
        }
    }

    public void startTestDatabase() throws ClassNotFoundException, SQLException {
        try {
            Class.forName(DRIVER);

            Connection conn = DriverManager.getConnection(TEST_URL);

            if (contains(conn, "results")) {
                String query = "DROP TABLE results";
                conn.createStatement().execute(query);
            }

            checkTestTablesAndCreate();

        } catch (ClassNotFoundException e) {
            log.log(Level.SEVERE, "Could not find the database driver", e);
            throw e;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "The connection to database could have not been established.", e);
            throw e;
        }
    }

    private static Connection createConnection() throws SQLException {
        try {
            return DriverManager.getConnection(JDBC_URL);
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Unable to make new connection to database", e);
            throw e;
        }
    }

    //TODO just debug
    public Connection createTestConnection() throws SQLException {
        return DriverManager.getConnection(TEST_URL);
    }

    private void checkTablesAndCreate() throws SQLException {

        if (!contains(conn, "results")) {
            String query = "CREATE TABLE results (methodName varchar(300), generator varchar(300), data varchar(300), numberOfMeasurements int, time bigint)";
            conn.createStatement().execute(query);
            log.log(Level.INFO, "New table \"results\" was created");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void emptyTable() throws SQLException {

        if (contains(conn, "results")) {
            String query = "DELETE FROM results";
            conn.createStatement().execute(query);
            log.log(Level.INFO, "The table results was emptied.");
        }
    }

    private void checkTestTablesAndCreate() throws SQLException {

        if (!contains(conn, "results")) {
            String query = "CREATE TABLE results (methodName varchar(300), generator varchar(300), data varchar(300), numberOfMeasurements int, time bigint)";
            conn.createStatement().execute(query);
            log.log(Level.INFO, "New table \"results\" was created");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getResult(String methodName, String generatorName, String data, int numberOfMeasurements) {
        try {
            Statement stmt = conn.createStatement();
            String query = "SELECT numberOfMeasurements, time "
                    + "FROM results "
                    + "WHERE (methodName = '" + methodName + "' AND generator='" + generatorName + "' AND data='" + data + "')";
            ResultSet rs = stmt.executeQuery(query);

            if (!rs.next()) {
                //if there is no row in the table containing the measured method with the data
                closeResultSet(rs);
                return -1;
            } else {
                //the first column (numberOfMeasurements) is saved under index 1
                int savedNumberOfMeasurements = rs.getInt(1);
                if (savedNumberOfMeasurements >= numberOfMeasurements) {
                    long savedResult = rs.getLong(2);
                    closeResultSet(rs);
                    return savedResult;
                }

                closeResultSet(rs);
                return -1;
            }
        } catch (SQLException e) {
            log.log(Level.CONFIG, "Unable to look in the database cache for measured result", e);
            return -1;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean insertResult(String methodName, String generatorName, String data, int numberOfMeasurements, long time) {

        Statement stmt;
        ResultSet rs;

        try {
            stmt = conn.createStatement();
            String query = "SELECT numberOfMeasurements "
                    + "FROM results "
                    + "WHERE (methodName = '" + methodName + "' AND generator='" + generatorName + "' AND data='" + data + "')";
            log.log(Level.CONFIG, "Searching the data in database for before insert. Query:  {0}", query);
            rs = stmt.executeQuery(query);

            //we expect one or zero results
            if (!rs.next()) {
                //if there is no row in the table containing the measured method with the data, we insert our results
                insertNewResult(methodName, generatorName, data, numberOfMeasurements, time);
            } else {
                int insertedNumberOfMeasurements = rs.getInt("numberOfMeasurements");

                if (insertedNumberOfMeasurements < numberOfMeasurements) {
                    //if data in database are less accurate, we update them to our value   
                    updateResult(methodName, generatorName, data, numberOfMeasurements, time);
                } else {
                    log.log(Level.CONFIG, "The data in database are better than mine. Not inserting anything.");
                }
            }
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to insert new result in the table", e);
            return false;
        }

        return true;
    }

    private void insertNewResult(String methodName, String generatorName, String data, int numberOfMeasurements, long time) throws SQLException {

        Statement stmt = conn.createStatement();

        String query = "INSERT INTO results (methodName, generator, data, numberOfMeasurements, time) "
                + "VALUES ('" + methodName + "', '" + generatorName + "', '" + data + "', " + numberOfMeasurements + ", " + time + ")";
        log.log(Level.CONFIG, "Inserting new data in database. Query:  {0}", query);
        stmt.executeUpdate(query);

        closeStatement(stmt);
    }

    private void updateResult(String methodName, String generatorName, String data, int numberOfMeasurements, long time) throws SQLException {

        Statement stmt = conn.createStatement();
        String query = "UPDATE results "
                + "SET numberOfMeasurements=" + numberOfMeasurements + ", time=" + time
                + "WHERE (methodName = '" + methodName + "' AND generator='" + generatorName + "' AND data='" + data + "')";
        log.log(Level.CONFIG, "Updating data in database. Query:  {0}", query);
        stmt.executeUpdate(query);

        closeStatement(stmt);
    }
  
    private static void printSQLException(SQLException e) {
        while (e != null) {
            log.log(Level.WARNING, "Some error occured while working with database.", e);

            e = e.getNextException();
        }
    }

    private static boolean contains(Connection con, String tableName) throws SQLException {
        ArrayList<String> tables = getDBTables(con);

        return tables.contains(tableName);
    }

    public ArrayList<String> getDBTables() throws SQLException {
        ArrayList<String> set = new ArrayList<>();
        DatabaseMetaData dbmeta = conn.getMetaData();
        readDBTable(set, dbmeta, "TABLE", null);
        return set;
    }

    private static ArrayList<String> getDBTables(Connection targetDBConn) throws SQLException {
        ArrayList<String> set = new ArrayList<>();
        DatabaseMetaData dbmeta = targetDBConn.getMetaData();
        readDBTable(set, dbmeta, "TABLE", null);
        return set;
    }

    private static void readDBTable(ArrayList<String> set, DatabaseMetaData dbmeta, String searchCriteria, String schema)
            throws SQLException {
        ResultSet rs = dbmeta.getTables(null, schema, null, new String[]{searchCriteria});
        while (rs.next()) {
            set.add(rs.getString("TABLE_NAME").toLowerCase());
        }

        closeResultSet(rs);
    }

    public ResultSet getTable() throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT * FROM results";

        ResultSet result = stmt.executeQuery(query);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeConnection() {
        try {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    private static void closeConnection(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    private static void closeStatement(Statement st) {
        try {
            if (st != null) {
                st.close();
                st = null;
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    private static void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
                rs = null;
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    public void closeDatabase() {
        try {
            closeConnection(conn);
            // the shutdown=true attribute shuts down Derby
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
            log.log(Level.INFO, "Database was shut-down succesfully.");
        } catch (SQLException se) {
            if (((se.getErrorCode() == 50000)
                    && ("XJ015".equals(se.getSQLState())))) {
                // we got the expected exception
                log.log(Level.INFO, "Database was shut-down succesfully.");
            } else {
                // if the error code or SQLState is different, we have
                // an unexpected exception (shutdown failed)                        
                printSQLException(se);
            }
        }
    }
}
