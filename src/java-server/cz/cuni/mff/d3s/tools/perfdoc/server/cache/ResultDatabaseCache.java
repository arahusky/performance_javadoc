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

import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkResult;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkResultImpl;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkSetting;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.Statistics;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of ResultAdminCache that saves the results in the database.
 *
 * @author Jakub Naplava
 */
public class ResultDatabaseCache implements ResultAdminCache {

    private static final Logger log = Logger.getLogger(ResultDatabaseCache.class.getName());

    private static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    public static final String JDBC_URL = "jdbc:derby:database/cacheDB;create=true";

    protected Connection conn;

    public ResultDatabaseCache(String connection_url) throws SQLException {
        this.conn = createConnection(connection_url);
    }

    @Override
    public void start() throws ClassNotFoundException, SQLException {
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

    private static Connection createConnection(String connection_url) throws SQLException {
        try {
            return DriverManager.getConnection(connection_url);
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Unable to make new connection to database", e);
            throw e;
        }
    }

    /**
     * Checks whether database contains all needed tables and if not than
     * creates them
     *
     * @throws SQLException
     */
    private void checkTablesAndCreate() throws SQLException {

        if (!contains(conn, "measurement_information")) {
            String query = "CREATE TABLE measurement_information ("
                    + " id INTEGER PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                    + " method VARCHAR(500),"
                    + " workload VARCHAR(500),"
                    + " workload_arguments VARCHAR(300),"
                    + " number_of_measurements INTEGER, "
                    + " time BIGINT)";
            conn.createStatement().execute(query);
            log.log(Level.INFO, "New table \"measurement_information\" was created");
        }

        //derby creates indexes automatically for columns declared as primary key or foreign key, therefore there is no need to create any other
        if (!contains(conn, "measurement_detailed")) {
            String query = "CREATE TABLE measurement_detailed ("
                    + " id INTEGER, "
                    + " time BIGINT,"
                    + " FOREIGN KEY (id) REFERENCES measurement_information(id)"
                    + ")";
            conn.createStatement().execute(query);
            log.log(Level.INFO, "New table \"measurement_detailed\" was created");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void empty() throws SQLException {

        if (contains(conn, "measurement_detailed")) {
            String query = "DELETE FROM measurement_detailed";
            conn.createStatement().execute(query);
            log.log(Level.INFO, "The table measurement_detailed was emptied.");
        }
        
        if (contains(conn, "measurement_information")) {
            String query = "DELETE FROM measurement_information";
            conn.createStatement().execute(query);
            log.log(Level.INFO, "The table measurement_information was emptied.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BenchmarkResult getResult(BenchmarkSetting setting) {
        String methodName = setting.getTestedMethod().toString();
        String workloadName = setting.getWorkload().toString();
        String workloadArguments = setting.getWorkloadArguments().getValuesDBFormat(true);

        try {
            Statement stmt = conn.createStatement();
            String query = "SELECT D.time"
                    + " FROM measurement_information M"
                    + " INNER JOIN measurement_detailed D"
                    + " ON M.id = D.id"
                    + " WHERE (M.method = '" + methodName + "'"
                    + " AND M.workload='" + workloadName + "'"
                    + " AND M.workload_arguments='" + workloadArguments + "')";
            ResultSet rs = stmt.executeQuery(query);
            if (!rs.next()) {
                //if there is no row in the table containing the measured method with the data
                closeResultSet(rs);
                return null;
            } else {
                Statistics statistics = returnStatisticsFromResultsSet(rs);
                closeResultSet(rs);
                return new BenchmarkResultImpl(statistics, setting);
            }
        } catch (SQLException e) {
            log.log(Level.CONFIG, "Unable to look in the database cache for measured result", e);
            return null;
        }
    }
    
    private Statistics returnStatisticsFromResultsSet(ResultSet rs) throws SQLException {
        Statistics statistics = new Statistics();
        statistics.addResult(rs.getLong("time"));
        
        while (rs.next()) {
            statistics.addResult(rs.getLong("time"));
        }
        
        return statistics;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean insertResult(BenchmarkResult benResult) {

        BenchmarkSetting setting = benResult.getBenchmarkSetting();
        String methodName = setting.getTestedMethod().toString();
        String generatorName = setting.getWorkload().toString();
        String data = setting.getWorkloadArguments().getValuesDBFormat(true);
        int numberOfMeasurements = benResult.getStatistics().getNumberOfMeasurements();

        Statement stmt;
        ResultSet rs;

        try {
            stmt = conn.createStatement();
            String query = "SELECT number_of_measurements "
                    + "FROM measurement_information"
                    + " WHERE (method = '" + methodName
                    + "' AND workload='" + generatorName
                    + "' AND workload_arguments='" + data + "')";
            log.log(Level.CONFIG, "Searching the data in database for before insert. Query:  {0}", query);
            rs = stmt.executeQuery(query);

            //we expect one or zero results
            if (!rs.next()) {
                //if there is no row in the table containing the measured method with the data, we insert our results
                insertNewResult(benResult);
            } else {
                int insertedNumberOfMeasurements = rs.getInt("number_of_measurements");

                if (insertedNumberOfMeasurements < numberOfMeasurements) {
                    //if data in database are less accurate, we update them to our value  
                    updateResult(benResult);
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

    private void insertNewResult(BenchmarkResult benResult) throws SQLException {
        String methodName = benResult.getBenchmarkSetting().getTestedMethod().toString();
        String workloadName = benResult.getBenchmarkSetting().getWorkload().toString();
        String workloadArguments = benResult.getBenchmarkSetting().getWorkloadArguments().getValuesDBFormat(true);
        int numberOfMeasurements = benResult.getStatistics().getNumberOfMeasurements();
        long time = benResult.getStatistics().compute();

        Statement stmt = conn.createStatement();

        String query = "INSERT INTO measurement_information (method, workload, workload_arguments, number_of_measurements, time) "
                + "VALUES ('" + methodName + "', '" + workloadName + "', '" + workloadArguments + "', " + numberOfMeasurements + ", " + time + ")";
        log.log(Level.CONFIG, "Inserting new data into database. Script for measurement_information:  {0}", query);
        stmt.executeUpdate(query);

        //unique identifier of record that will be updated
        int id = getIDForGivenRecord(methodName, workloadName, workloadArguments);

        insertDetailedResults(id, benResult.getStatistics().getValues());

        closeStatement(stmt);
    }

    private void updateResult(BenchmarkResult benResult) throws SQLException {
        String methodName = benResult.getBenchmarkSetting().getTestedMethod().toString();
        String workloadName = benResult.getBenchmarkSetting().getWorkload().toString();
        String workloadArguments = benResult.getBenchmarkSetting().getWorkloadArguments().getValuesDBFormat(true);
        int numberOfMeasurements = benResult.getStatistics().getNumberOfMeasurements();
        long time = benResult.getStatistics().compute();

        Statement stmt = conn.createStatement();
        
        //unique identifier of record that will be updated
        int id = getIDForGivenRecord(methodName, workloadName, workloadArguments);
        
        String query = "UPDATE measurement_information"
                + " SET number_of_measurements=" + numberOfMeasurements + ", time=" + time
                + " WHERE (id = " + id + ")";

        log.log(Level.CONFIG, "Updating data in database. Query:  {0}", query);
        stmt.executeUpdate(query);
                
        //deleting all old results
        query = "DELETE FROM measurement_detailed"
                + " WHERE (id = " + id + ")";
        
        log.log(Level.CONFIG, "Deleting records from measurement_detailed:  {0}", query);
        stmt.executeUpdate(query);
        
        //and replacing them by the new ones
        insertDetailedResults(id, benResult.getStatistics().getValues());

        closeStatement(stmt);
    }
    
    /**
     * Returns the unique ID for the row identified by given arguments.
     * @param method
     * @param workload
     * @param workloadArgs
     * @return
     * @throws SQLException if there's no such record in database
     */
    private int getIDForGivenRecord(String method, String workload, String workloadArgs) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT id "
                + "FROM measurement_information"
                + " WHERE (method = '" + method + "'"
                + " AND workload='" + workload + "'"
                + " AND workload_arguments='" + workloadArgs + "')";
        
        ResultSet rs = stmt.executeQuery(query);
        
        if (!rs.next()) {
            throw new SQLException("Data were inserted badly.");
        }        
        int id = rs.getInt("id");
        closeResultSet(rs);
        closeStatement(stmt);
        return id;
    } 
    
    /**
     * Inserts the given results into measurement_detailed table under given ID.
     * @param id the identifier, under which all results will be saved.
     * @param resultsToInsert
     * @throws SQLException 
     */
    private void insertDetailedResults(int id, Long[] resultsToInsert) throws SQLException {
        String query = "INSERT INTO measurement_detailed (id, time) "
                + "VALUES (" + id + ",?)";

        PreparedStatement preparedStmt = conn.prepareStatement(query);

        for (long val : resultsToInsert) {
            preparedStmt.setLong(1, val);
            preparedStmt.executeUpdate();
        }

        closeStatement(preparedStmt);
    }

    private static void printSQLException(SQLException e) {
        while (e != null) {
            log.log(Level.WARNING, "Some error occured while working with database.", e);

            e = e.getNextException();
        }
    }

    /**
     * Determines, whether database already contains given table.
     */
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

    @Override
    public void close() {
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
