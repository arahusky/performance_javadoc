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

import static cz.cuni.mff.d3s.tools.perfdoc.server.cache.BenchmarkMockups.*;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkResult;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkResultImpl;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkSetting;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkSettingImpl;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.MeasurementQuality;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jakub Naplava
 */
public class ResultCacheTest {

    private static ResultDatabaseCache res;
    private static final String TEST_URL = "jdbc:derby:test_database/cacheDB;create=true";

    @BeforeClass
    public static void testStartAndCreateDB() throws SQLException, ClassNotFoundException {
        res = new ResultDatabaseCache(TEST_URL);
        //dropAllTables();
    }

    @Before
    public void makeNewConnection() throws SQLException, ClassNotFoundException {
        res = new ResultDatabaseCache(TEST_URL);
        res.start();
    }

    @After
    public void closeConnection() throws SQLException, ClassNotFoundException {
        res.empty();
        res.closeConnection();

    }

    @AfterClass
    public static void endDB() {
        res.close();
    }

    private void checkResultSetFor1(ResultSet rs) throws SQLException {
        Assert.assertEquals(measuredMethod1.toString(), rs.getString("measured_method"));
        Assert.assertEquals(generator1.toString(), rs.getString("generator"));
        Assert.assertEquals("[arg1]", rs.getString("generator_arguments"));
        Assert.assertEquals(statistics1.getMean(), rs.getLong("mean"));
    }

    private void checkResultSetFor2(ResultSet rs) throws SQLException {
        Assert.assertEquals(measuredMethod2.toString(), rs.getString("measured_method"));
        Assert.assertEquals(generator2.toString(), rs.getString("generator"));
        Assert.assertEquals("[1,2.0]", rs.getString("generator_arguments"));
        Assert.assertEquals(statistics2.getMean(), rs.getLong("mean"));
    }

    private void checkResultSetFor3(ResultSet rs) throws SQLException {
        Assert.assertEquals(measuredMethod3.toString(), rs.getString("measured_method"));
        Assert.assertEquals(generator3.toString(), rs.getString("generator"));
        Assert.assertEquals("[2.0]", rs.getString("generator_arguments"));
        Assert.assertEquals(statistics3.getMean(), rs.getLong("mean"));
    }

    @Test
    public void testNumberOfTables() throws SQLException {
        ArrayList<String> set = res.getDBTables();

        //created database contains three tables
        Assert.assertEquals(3, set.size());

        //with following names
        Assert.assertTrue(set.contains("measurement_information"));
        Assert.assertTrue(set.contains("measurement_detailed"));
        Assert.assertTrue(set.contains("measurement_quality"));
    }

    @Test
    public void testSimpleEmptyTableTest() throws SQLException {
        ResultSet rs = getContentsBasicTable();

        Assert.assertFalse(rs.next());
    }

    @Test
    public void testSimpleInsertRow() throws SQLException {
        res.insertResult(benResult1);
        ResultSet rs = getContentsBasicTable();

        Assert.assertTrue(rs.next());

        checkResultSetFor1(rs);

        Assert.assertFalse(rs.next());
    }

    @Test
    public void testMultipleInsertDifferentRecords() throws SQLException {
        res.insertResult(benResult1);
        res.insertResult(benResult2);
        res.insertResult(benResult3);
        ResultSet rs = getContentsBasicTable();

        Assert.assertTrue(rs.next());

        checkResultSetFor1(rs);

        Assert.assertTrue(rs.next());

        checkResultSetFor2(rs);

        Assert.assertTrue(rs.next());

        checkResultSetFor3(rs);

        Assert.assertFalse(rs.next());
    }

    @Test
    public void insertRowWithBetterMeasurementQualityShallUpdateRecord() throws SQLException {
        
        BenchmarkResult brFirst = benResult2;

        //creating second BenchmarkResult, that have exactly same methodName, worloadName, workloadArguments and Statistics, but have better measurement quality
        MeasurementQuality mqSecond = new MeasurementQuality(measurementQuality2.getPriority() + 1,
                measurementQuality2.getWarmupTime() + 1, measurementQuality2.getNumberOfWarmupCycles() + 1, 
                measurementQuality2.getMeasurementTime() + 1, measurementQuality2.getNumberOfMeasurementsCycles() + 1,
                measurementQuality2.getNumberOfPoints());
        BenchmarkSetting bsSecond = new BenchmarkSettingImpl(measuredMethod2, generator2, generatorArguments2, mqSecond);
        BenchmarkResult brSecond = new BenchmarkResultImpl(statistics2, bsSecond);

        res.insertResult(brFirst);
        res.insertResult(brSecond);
        ResultSet rs = getContentsBasicTable();

        Assert.assertTrue(rs.next());
        int idQuality = rs.getInt("idQuality");
        String queryToObtainQuality = "SELECT * FROM measurement_quality WHERE idQuality=" + idQuality;
        Statement stmt = res.conn.createStatement();
        ResultSet rsQuality = stmt.executeQuery(queryToObtainQuality);

        Assert.assertTrue(rsQuality.next());
        Assert.assertEquals(mqSecond.getWarmupTime() , rsQuality.getInt("warmup_time"));
        Assert.assertEquals(mqSecond.getNumberOfWarmupCycles() , rsQuality.getInt("warmup_cycles"));
        Assert.assertEquals(mqSecond.getMeasurementTime(), rsQuality.getInt("measurement_time"));
        Assert.assertEquals(mqSecond.getNumberOfMeasurementsCycles(), rsQuality.getInt("measurement_cycles"));
        Assert.assertFalse(rsQuality.next());
    }
    
    @Test
    public void insertRowWithEqualMeasurementQualityAsOtherRecord() throws SQLException {
        
        BenchmarkResult brFirst = benResult1;

        //create new BenchmarkResult, that has same measurementQuality as previous
        BenchmarkSetting bsSecond = new BenchmarkSettingImpl(measuredMethod2, generator2, generatorArguments2, measurementQuality1);
        BenchmarkResult brSecond = new BenchmarkResultImpl(statistics2, bsSecond);

        res.insertResult(brFirst);
        res.insertResult(brSecond);
        ResultSet rs = getContentsBasicTable();

        Assert.assertTrue(rs.next());
        int idQuality = rs.getInt("idQuality");
        
        Assert.assertTrue(rs.next());
        //test whether both records point to the same measurementQuality
        Assert.assertEquals(idQuality, rs.getInt("idQuality"));
        
        //test, whether pointed measurement quality is right (especcially number_uses)
        String queryToObtainQuality = "SELECT * FROM measurement_quality WHERE idQuality=" + idQuality;
        Statement stmt = res.conn.createStatement();
        ResultSet rsQuality = stmt.executeQuery(queryToObtainQuality);

        Assert.assertTrue(rsQuality.next());
        Assert.assertEquals(measurementQuality1.getWarmupTime() , rsQuality.getInt("warmup_time"));
        Assert.assertEquals(measurementQuality1.getNumberOfWarmupCycles() , rsQuality.getInt("warmup_cycles"));
        Assert.assertEquals(measurementQuality1.getMeasurementTime(), rsQuality.getInt("measurement_time"));
        Assert.assertEquals(measurementQuality1.getNumberOfMeasurementsCycles(), rsQuality.getInt("measurement_cycles"));
        Assert.assertEquals(2, rsQuality.getInt("number_uses"));
        Assert.assertFalse(rsQuality.next());
        
        Assert.assertFalse(rs.next());        
    }

    @Test
    public void insertRowWithWorseMeasurementQualityShallNotUpdateRecord() throws SQLException {
               
        //creating BenchmarkResult, that have exactly same methodName, worloadName, workloadArguments and Statistics, but have worse measurement quality
        MeasurementQuality mqFirst = new MeasurementQuality(measurementQuality1.getPriority() + 1,
                measurementQuality1.getWarmupTime() + 1, measurementQuality1.getNumberOfWarmupCycles() + 1, 
                measurementQuality1.getMeasurementTime() + 1, measurementQuality1.getNumberOfMeasurementsCycles() + 1,
                measurementQuality1.getNumberOfPoints());
        BenchmarkSetting bsFirst = new BenchmarkSettingImpl(measuredMethod1, generator1, generatorArguments1, mqFirst);
        BenchmarkResult brFirst = new BenchmarkResultImpl(statistics1, bsFirst);

        BenchmarkResult brSecond = benResult1;
        
        res.insertResult(brFirst);
        res.insertResult(brSecond);
        ResultSet rs = getContentsBasicTable();

        Assert.assertTrue(rs.next());
        int idQuality = rs.getInt("idQuality");
        String queryToObtainQuality = "SELECT * FROM measurement_quality WHERE idQuality=" + idQuality;
        Statement stmt = res.conn.createStatement();
        ResultSet rsQuality = stmt.executeQuery(queryToObtainQuality);

        Assert.assertTrue(rsQuality.next());
        Assert.assertEquals(mqFirst.getWarmupTime() , rsQuality.getInt("warmup_time"));
        Assert.assertEquals(mqFirst.getNumberOfWarmupCycles() , rsQuality.getInt("warmup_cycles"));
        Assert.assertEquals(mqFirst.getMeasurementTime(), rsQuality.getInt("measurement_time"));
        Assert.assertEquals(mqFirst.getNumberOfMeasurementsCycles(), rsQuality.getInt("measurement_cycles"));
        
        Assert.assertTrue(rs.next());
        
        idQuality = rs.getInt("idQuality");
        queryToObtainQuality = "SELECT * FROM measurement_quality WHERE idQuality=" + idQuality;
        
        rsQuality = stmt.executeQuery(queryToObtainQuality);

        Assert.assertTrue(rsQuality.next());
        Assert.assertEquals(measurementQuality1.getWarmupTime() , rsQuality.getInt("warmup_time"));
        Assert.assertEquals(measurementQuality1.getNumberOfWarmupCycles() , rsQuality.getInt("warmup_cycles"));
        Assert.assertEquals(measurementQuality1.getMeasurementTime(), rsQuality.getInt("measurement_time"));
        Assert.assertEquals(measurementQuality1.getNumberOfMeasurementsCycles(), rsQuality.getInt("measurement_cycles"));
        
        Assert.assertFalse(rs.next());
    }

    @Test
    public void testMoreConnections() throws SQLException {
        res.insertResult(benResult3);

        ResultDatabaseCache newRes = new ResultDatabaseCache(TEST_URL);

        newRes.insertResult(benResult2);

        ResultSet rs = getContentsBasicTable();

        Assert.assertTrue(rs.next());
        checkResultSetFor3(rs);

        Assert.assertTrue(rs.next());
        checkResultSetFor2(rs);

        Assert.assertFalse(rs.next());
        newRes.closeConnection();
    }

    @Test
    public void testGetResultSimple() {
        res.insertResult(benResult1);

        BenchmarkResult br1 = res.getResult(benSet1);
        Assert.assertEquals(benResult1.getStatistics(), br1.getStatistics());

    }

    @Test
    public void testGetResultComplex() {
        res.insertResult(benResult1);
        res.insertResult(benResult2);
        res.insertResult(benResult3);

        BenchmarkResult br = res.getResult(benSet4);
        Assert.assertNull(br);

        br = res.getResult(benSet1);
        Assert.assertEquals(benResult1.getStatistics(), br.getStatistics());

        br = res.getResult(benSet2);
        Assert.assertEquals(benResult2.getStatistics(), br.getStatistics());

        br = res.getResult(benSet3);
        Assert.assertEquals(benResult3.getStatistics(), br.getStatistics());
    }

    @Test
    public void testEmptyTable() throws SQLException, ClassNotFoundException {
        res.insertResult(benResult1);
        res.insertResult(benResult2);
        res.empty();

        Statement stmt = res.conn.createStatement();

        String query = "SELECT * FROM measurement_information";
        ResultSet rs = stmt.executeQuery(query);
        Assert.assertFalse(rs.next());

        query = "SELECT * FROM measurement_detailed";
        rs = stmt.executeQuery(query);
        Assert.assertFalse(rs.next());
    }

    private ResultSet getContentsBasicTable() throws SQLException {
        Statement stmt = res.conn.createStatement();
        String query = "SELECT * FROM measurement_information";

        ResultSet result = stmt.executeQuery(query);
        return result;
    }

    /**
     * Drops all tables in database.
     *
     * Useful when modifying structure of tables.
     *
     * @throws SQLException
     */
    private static void dropAllTables() throws SQLException {
        String query = "DROP TABLE measurement_detailed";
        Statement statement = res.conn.createStatement();

        statement.executeUpdate(query);

        query = "DROP TABLE measurement_information";
        statement.execute(query);

        query = "DROP TABLE measurement_quality";
        statement.execute(query);
    }
}
