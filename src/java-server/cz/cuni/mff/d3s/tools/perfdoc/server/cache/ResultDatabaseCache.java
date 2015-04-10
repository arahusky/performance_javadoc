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
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.MeasurementQuality;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.MeasurementStatistics;
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
    public static String JDBC_URL = "jdbc:derby:database/cacheDB;create=true";

    protected Connection conn;

    public ResultDatabaseCache(String connection_url) throws SQLException {
        this.conn = createConnection(connection_url);
    }

    public static void setUrl(String url) {
        ResultDatabaseCache.JDBC_URL = url;
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
     * creates them.
     *
     * @throws SQLException
     */
    private void checkTablesAndCreate() throws SQLException {

        if (!contains(conn, "measurement_quality")) {
            String query = "CREATE TABLE measurement_quality ("
                    + " idQuality INTEGER PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                    + " warmup_time INTEGER,"
                    + " warmup_cycles INTEGER,"
                    + " measurement_time INTEGER,"
                    + " measurement_cycles INTEGER,"
                    + " priority INTEGER,"
                    + " number_uses INTEGER DEFAULT 0,"
                    + "UNIQUE (warmup_time, warmup_cycles, measurement_time, measurement_cycles)"
                    + ")";
            conn.createStatement().execute(query);
            log.log(Level.INFO, "New table \"measurement_quality\" was created");
        }

        //TODO index pres trojici (method, workload, workloadArgs)
        //derby creates indexes automatically for columns declared as primary key or foreign key, therefore there is no need to create any other
        if (!contains(conn, "measurement_information")) {
            String query = "CREATE TABLE measurement_information ("
                    + " id INTEGER PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                    + " measured_method VARCHAR(500),"
                    + " generator VARCHAR(500),"
                    + " generator_arguments VARCHAR(300),"
                    + " mean BIGINT,"
                    + " median BIGINT,"
                    + " deviation BIGINT,"
                    + " firstQuartile BIGINT,"
                    + " thirdQuartile BIGINT,"
                    + " idQuality INTEGER,"
                    + " FOREIGN KEY (idQuality) REFERENCES measurement_quality(idQuality)"
                    + ")";
            conn.createStatement().execute(query);
            log.log(Level.INFO, "New table \"measurement_information\" was created");
        }

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
    @Override
    public void empty() throws SQLException, ClassNotFoundException {

        Class.forName(DRIVER);

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

        if (contains(conn, "measurement_quality")) {
            String query = "DELETE FROM measurement_quality";
            conn.createStatement().execute(query);
            log.log(Level.INFO, "The table measurement_quality was emptied.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BenchmarkResult getResult(BenchmarkSetting setting) {
        String measuredMethodName = setting.getMeasuredMethod().toString();
        String generatorName = setting.getGenerator().toString();
        String generatorArguments = setting.getGeneratorArguments().getValuesDBFormat(true);

        int warmupTime = setting.getMeasurementQuality().getWarmupTime();
        int warmupCycles = setting.getMeasurementQuality().getNumberOfWarmupCycles();
        int measurementTime = setting.getMeasurementQuality().getMeasurementTime();
        int measurementCycles = setting.getMeasurementQuality().getNumberOfMeasurementsCycles();
        
        try {
            Statement stmt = conn.createStatement();
            //we want to get results, that have same measuredMethodName, generatorName and generatorArguments and were measured at least as precisely as we need to
            String query = "SELECT time"
                    + " FROM measurement_information"
                    + " NATURAL JOIN measurement_detailed"
                    + " NATURAL JOIN measurement_quality"
                    + " WHERE (measured_method = '" + measuredMethodName + "'"
                    + " AND generator='" + generatorName + "'"
                    + " AND generator_arguments='" + generatorArguments + "'"
                    + " AND warmup_time>=" + warmupTime
                    + " AND warmup_cycles>=" + warmupCycles
                    + " AND measurement_time>=" + measurementTime
                    + " AND measurement_cycles>=" + measurementCycles
                    + ")";
            ResultSet rs = stmt.executeQuery(query);
            if (!rs.next()) {
                //if there is no row in the table containing the measured method with the data
                closeResultSet(rs);
                return null;
            } else {
                //there may be more results saved in database (thus in rs), but it's enough for us to get any of them
                Statistics statistics = returnStatisticsFromResultsSet(rs);
                closeResultSet(rs);
                return new BenchmarkResultImpl(statistics, setting);
            }
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to look in the database cache for measured result", e);
            return null;
        }
    }

    private Statistics returnStatisticsFromResultsSet(ResultSet rs) throws SQLException {
        MeasurementStatistics statistics = new MeasurementStatistics();
        statistics.addResult(rs.getLong("time"));

        while (rs.next()) {
            statistics.addResult(rs.getLong("time"));
        }

        return statistics;
    }

    /**
     * {@inheritDoc}
     *
     * Inserting new results contains also deleting of no longer needed cache
     * results. This means, that when there are results having same
     * measuredMethodName, generatorName and generatorArguments and theirs
     * measurement quality is lower than ours, than such results can be deleted
     * (replaced by better ones).
     *
     * Before calling this method, you should better call getResults first to
     * ensure, that there are no results better than this one which means.
     * Otherwise can be newly inserted data 'useless'.
     */
    @Override
    public boolean insertResult(BenchmarkResult benResult) {
        BenchmarkSetting setting = benResult.getBenchmarkSetting();
        String measuredMethodName = setting.getMeasuredMethod().toString();
        String generatorName = setting.getGenerator().toString();
        String data = setting.getGeneratorArguments().getValuesDBFormat(true);

        MeasurementQuality mQuality = setting.getMeasurementQuality();
        int warmupTime = mQuality.getWarmupTime();
        int warmupCycles = mQuality.getNumberOfWarmupCycles();
        int measurementTime = mQuality.getMeasurementTime();
        int measurementCycles = mQuality.getNumberOfMeasurementsCycles();

        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.createStatement();
            //at first, we check, whether our data can replace any other, which means, whether our measurement quality is better for some data with same measuredMethod, generator and generatorArguments
            String query = "SELECT id, idQuality"
                    + " FROM measurement_information "
                    + " NATURAL JOIN measurement_detailed "
                    + " NATURAL JOIN measurement_quality "
                    + " WHERE ( measured_method = '" + measuredMethodName
                    + "' AND generator='" + generatorName
                    + "' AND generator_arguments='" + data + "'"
                    + " AND warmup_time<=" + warmupTime
                    + " AND warmup_cycles<=" + warmupCycles
                    + " AND measurement_time<=" + measurementTime
                    + " AND measurement_cycles<=" + measurementCycles
                    + ")";

            rs = stmt.executeQuery(query);
            log.log(Level.CONFIG, "Searching for the data in database before insert. Query:  {0}", query);

            StringBuilder sbId = new StringBuilder();
            StringBuilder sbIdQuality = new StringBuilder();
            while (rs.next()) {
                int id = rs.getInt("id");
                int idQuality = rs.getInt("idQuality");
                sbId.append(id + ",");
                sbIdQuality.append(idQuality + ",");
            }

            if (sbId.length() != 0) {
                sbId.deleteCharAt(sbId.length() - 1);
            }

            if (sbIdQuality.length() != 0) {
                sbIdQuality.deleteCharAt(sbIdQuality.length() - 1);
            }

            if (sbId.length() > 0) {
                query = "DELETE FROM measurement_detailed"
                        + " WHERE id IN (" + sbId.toString() + ")";
                stmt.executeUpdate(query);
                //as well as from measurement_information

                query = "DELETE FROM measurement_information"
                        + " WHERE id IN (" + sbId.toString() + ")";
                stmt.executeUpdate(query);
                //decrementing number of use in measurement_quality
                query = "UPDATE measurement_quality"
                        + " SET number_uses = number_uses - 1"
                        + " WHERE idQuality IN (" + sbIdQuality.toString() + ")";
                stmt.executeUpdate(query);
            }

            //and finally inserting record
            insertNewResult(benResult);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to insert new result in the table", e);
            return false;
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
        }

        return true;
    }

    private void insertNewResult(BenchmarkResult benResult) throws SQLException {
        String measuredMethodName = benResult.getBenchmarkSetting().getMeasuredMethod().toString();
        String generatorName = benResult.getBenchmarkSetting().getGenerator().toString();
        String generatorArguments = benResult.getBenchmarkSetting().getGeneratorArguments().getValuesDBFormat(true);
                
        BenchmarkSetting setting = benResult.getBenchmarkSetting();
        MeasurementQuality mq = setting.getMeasurementQuality();
        
        Statement stmt = conn.createStatement();
        //inserting measurement quality record
        insertMeasurementQuality(mq);
        
        //inserting record into measurement_information
        int idQuality = getIDQualityForGivenRecord(mq);
        insertMeasurementMain(measuredMethodName, generatorName, generatorArguments,benResult.getStatistics(), idQuality);
       
        //and all times into measurement_detailed
        int id = getIDForGivenRecord(measuredMethodName, generatorName, generatorArguments);
        insertDetailedResults(id, ((MeasurementStatistics) benResult.getStatistics()).getValues());
        closeStatement(stmt);
    }

    /**
     * Returns the unique ID for the row measuredMethodName by given arguments.
     *
     * @param measuredMethodName
     * @param generatorName
     * @param generatorArguments
     * @return
     * @throws SQLException if there's no such record in database
     */
    private int getIDForGivenRecord(String measuredMethodName, String generatorName, String generatorArguments) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT id "
                + "FROM measurement_information"
                + " WHERE (measured_method = '" + measuredMethodName + "'"
                + " AND generator='" + generatorName + "'"
                + " AND generator_arguments='" + generatorArguments + "')";

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
     * Returns the unique ID for the row identified by given arguments.
     *
     * @param mq
     * @return
     * @throws SQLException if there's no such record in database
     */
    private int getIDQualityForGivenRecord(MeasurementQuality mq) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT idQuality"
                + " FROM measurement_quality"
                + " WHERE ("
                + " warmup_time=" + mq.getWarmupTime()
                + " AND warmup_cycles=" + mq.getNumberOfWarmupCycles()
                + " AND measurement_time=" + mq.getMeasurementTime()
                + " AND measurement_cycles=" + mq.getNumberOfMeasurementsCycles()
                + ")";
        ResultSet rs = stmt.executeQuery(query);

        if (!rs.next()) {
            throw new SQLException("Data were inserted badly.");
        }
        int idQuality = rs.getInt("idQuality");
        closeResultSet(rs);
        closeStatement(stmt);
        return idQuality;
    }

    /**
     * Inserts or update number of use of given MeasurementQuality in
     * measurement_quality table.
     *
     * At first tries an update. If an update fails, than tries to create new
     * record and when also this fails (due to concurrency), then the other
     * thread must have already create record, so performing update is now OK.
     *
     * Update is considered to be much more frequent operation than the insert,
     * which upholds for (not so natural) process.
     *
     * @param mq
     */
    private void insertMeasurementQuality(MeasurementQuality mq) throws SQLException {

        Statement stmt = conn.createStatement();

        String queryUpdateQuality = "UPDATE measurement_quality"
                + " SET number_uses = number_uses + 1"
                + " WHERE ("
                + " warmup_time=" + mq.getWarmupTime()
                + " AND warmup_cycles=" + mq.getNumberOfWarmupCycles()
                + " AND measurement_time=" + mq.getMeasurementTime()
                + " AND measurement_cycles=" + mq.getNumberOfMeasurementsCycles()
                + ")";

        if (stmt.executeUpdate(queryUpdateQuality) > 0) {
            return; //number of affected rows is bigger than 0
        }

        try {
            String queryInsertNew = "INSERT INTO measurement_quality (warmup_time,warmup_cycles,measurement_time,measurement_cycles,priority,number_uses)"
                    + "VALUES (" + mq.getWarmupTime() + " , " + mq.getNumberOfWarmupCycles() + " , "
                    + mq.getMeasurementTime() + " , " + mq.getNumberOfMeasurementsCycles() + " , "
                    + mq.getPriority() + " , " + "1"
                    + ")";

            log.log(Level.CONFIG, "Updating data in database. Script for measurement_quality:  {0}", queryInsertNew);
            stmt.executeUpdate(queryInsertNew);
        } catch (SQLException ex) {
            if (ex.getSQLState().equals("23505")) {
                //if trying to insert duplicate key (value is already there)
                stmt.executeUpdate(queryUpdateQuality);
            } else {
                log.log(Level.WARNING, "An exception occurred while inserting new meaurementQuality into cache", ex);
            }
        }
    }

    /**
     * Inserts the given results into measurement_detailed table under given ID.
     *
     * @param id the identifier, under which all results will be saved.
     * @param resultsToInsert
     * @throws SQLException
     */
    private void insertDetailedResults(int id, Long[] resultsToInsert) throws SQLException {
        //in order to increase performance of inserting multiple records, we turn the autocommit mode to false and commit transaction in the end
        String query = "INSERT INTO measurement_detailed (id, time) "
                + "VALUES (" + id + ",?)";

        conn.setAutoCommit(false);
        PreparedStatement preparedStmt = conn.prepareStatement(query);

        for (long val : resultsToInsert) {
            preparedStmt.setLong(1, val);
            preparedStmt.addBatch();
        }

        preparedStmt.executeBatch();
        conn.commit();
        
        conn.setAutoCommit(true);

        closeStatement(preparedStmt);
    }
    
    private void insertMeasurementMain(String measuredMethodName, String generatorName, String generatorArguments, Statistics statistics, int idQuality) throws SQLException {
        Statement stmt = conn.createStatement();
        
        long mean = statistics.getMean();
        long median = statistics.getMedian();
        long deviation = statistics.getStandardDeviation();
        long firstQuartile = statistics.getFirstQuartile();
        long thirdQuartile = statistics.getThirdQuartile();
        
        String queryInsertInfo = "INSERT INTO measurement_information "
                + "(measured_method,"
                + " generator,"
                + " generator_arguments,"
                + " mean,"
                + " median,"
                + " deviation,"
                + " firstQuartile,"
                + " thirdQuartile,"
                + " idQuality) "
                + "VALUES ('" + measuredMethodName + "',"
                + " '" + generatorName + "',"
                + " '" + generatorArguments + "',"
                + mean + ","
                + median + ","
                + deviation + ","
                + firstQuartile + ","
                + thirdQuartile + ","
                + " " + idQuality + ")";
        log.log(Level.CONFIG, "Inserting new data into database. Script for measurement_information:  {0}", queryInsertInfo);
        
        stmt.executeUpdate(queryInsertInfo);
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
