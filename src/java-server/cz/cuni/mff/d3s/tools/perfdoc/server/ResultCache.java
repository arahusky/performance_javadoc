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
package cz.cuni.mff.d3s.tools.perfdoc.server;

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
 *
 * @author Jakub Naplava
 */
public class ResultCache {

    private static final Logger log = Logger.getLogger(ResultCache.class.getName());

    private static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String JDBC_URL = "jdbc:derby:cacheDB;create=true";
    private static final String TEST_URL = "jdbc:derby:testDB;create=true";

    public static void startDatabase() throws ClassNotFoundException, SQLException {
        try {
            Class.forName(DRIVER);

            Connection conn = DriverManager.getConnection(JDBC_URL);

            checkTablesAndCreate();

        } catch (ClassNotFoundException e) {
            log.log(Level.SEVERE, "Could not find the database driver", e);
            throw e;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "The connection to database could have not been established.", e);
            throw e;
        }
    }

    public static void startTestDatabase() throws ClassNotFoundException, SQLException {
        try {
            Class.forName(DRIVER);

            Connection conn = DriverManager.getConnection(TEST_URL);

            if (contains(conn, "results")) {
                String query = "DROP TABLE results";
                conn.createStatement().execute(query);
                System.out.println("Table results dropped");
            }

            checkTablesAndCreate();

        } catch (ClassNotFoundException e) {
            log.log(Level.SEVERE, "Could not find the database driver", e);
            throw e;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "The connection to database could have not been established.", e);
            throw e;
        }
    }

    public static Connection createConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL);
    }

    //TODO just debug
    public static Connection createTestConnection() throws SQLException {
        return DriverManager.getConnection(TEST_URL);
    }

    private static void checkTablesAndCreate() throws SQLException {

        Connection conn = createConnection();
        
        if (!contains(conn, "results")) {
            String query = "CREATE TABLE results (methodName varchar(300), data varchar(300), numberOfMeasurements int, time int)";
            conn.createStatement().execute(query);
            log.log(Level.INFO, "New table \"results\" was created");
        }

        closeConnection(conn);
    }

    public static int getResults(Connection conn, String methodName, String data, int numberOfMeasurements) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT numberOfMeasurements, time "
                + "FROM results "
                + "WHERE (methodName = '" + methodName + "' && data='" + data + "')";
        ResultSet rs = stmt.executeQuery(query);

        if (!rs.next()) {
            //if there is no row in the table containing the measured method with the data
            return -1;
        } else {
            //the first column (numberOfMeasurements) is saved under index 1
            int savedNumberOfMeasurements = rs.getInt(1);
            if (savedNumberOfMeasurements >= numberOfMeasurements) {
                int savedResult = rs.getInt(2);
                return savedResult;
            }

            return -1;
        }
    }

    public static void insertResult(Connection conn, String methodName, String data, int numberOfMeasurements, int time) {
        try {
            Statement stmt = conn.createStatement();
            String query = "SELECT numberOfMeasurements "
                    + "FROM results"
                    + " WHERE (methodName = '" + methodName + "' AND data='" + data + "')";
            ResultSet rs = stmt.executeQuery(query);

            //we expect one or zero results
            if (!rs.next()) {
                //if there is no row in the table containing the measured method with the data, we insert our results
                insertNewResult(conn, methodName, data, numberOfMeasurements, time);
            } else {
                int insertedNumberOfMeasurements = rs.getInt("numberOfMeasurements");

                if (insertedNumberOfMeasurements < numberOfMeasurements) {
                    //if data in database are less accurate, we update them to our value
                    updateResult(conn, methodName, data, numberOfMeasurements, time);
                }
            }
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to insert new result in the table", e);
        }
    }

    private static void insertNewResult(Connection conn, String methodName, String data, int numberOfMeasurements, int time) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "INSERT INTO "
                + "results (methodName, data, numberOfMeasurements, time) "
                + "VALUES"
                + "('" + methodName + "', '" + data + "', " + numberOfMeasurements + ", " + time + ")";
        System.out.println(query);
        System.out.println("==================");
        stmt.executeUpdate(query);
    }

    private static void updateResult(Connection conn, String methodName, String data, int numberOfMeasurements, int time) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "UPDATE results "
                + "SET numberOfMeasurements='" + numberOfMeasurements + "', time='" + time + "'"
                + "WHERE (methodName='" + methodName + "' && data='" + data + "')";

        stmt.executeUpdate(query);
    }

    public static void printSQLException(SQLException e) {
        while (e != null) {
            log.log(Level.WARNING, "Some error occured while working with database.", e);

            e = e.getNextException();
        }
    }

    private static boolean contains(Connection con, String tableName) throws SQLException {
        ArrayList<String> tables = getDBTables(con);

        return tables.contains(tableName);
    }

    public static ArrayList<String> getDBTables(Connection targetDBConn) throws SQLException {
        ArrayList<String> set = new ArrayList<>();
        DatabaseMetaData dbmeta = targetDBConn.getMetaData();
        readDBTable(set, dbmeta, "TABLE", null);
        return set;
    }

    public static void readDBTable(ArrayList<String> set, DatabaseMetaData dbmeta, String searchCriteria, String schema)
            throws SQLException {
        ResultSet rs = dbmeta.getTables(null, schema, null, new String[]{searchCriteria});
        while (rs.next()) {
            set.add(rs.getString("TABLE_NAME").toLowerCase());
        }

        closeResultSet(rs);
    }

    public static ResultSet getTable(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT * FROM results";

        ResultSet result = stmt.executeQuery(query);
        closeStatement(stmt);
        return result;
    }

    public static void closeConnection(Connection conn) {
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

    public static void closeDatabase() {
        try {
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
