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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static cz.cuni.mff.d3s.tools.perfdoc.server.cache.BenchmarkMockups.*;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkResultImpl;
import java.sql.Statement;

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
        res.start();
    }

    @Before
    public void makeNewConnection() throws SQLException {
        res = new ResultDatabaseCache(TEST_URL);
    }

    @After
    public void closeConnection() throws SQLException {
        res.empty();
        res.closeConnection();

    }

    @AfterClass
    public static void endDB() {
        res.close();
    }

    private void checkResultSetFor1(ResultSet rs) throws SQLException {
        Assert.assertEquals(method1.toString(), rs.getString("method"));
        Assert.assertEquals(workload1.toString(), rs.getString("workload"));
        Assert.assertEquals("[arg1]", rs.getString("workload_arguments"));
        Assert.assertEquals(statistics1.getNumberOfMeasurements(), rs.getInt("number_of_measurements"));
        Assert.assertEquals(statistics1.computeMean(), rs.getLong("time"));
    }

    private void checkResultSetFor2(ResultSet rs) throws SQLException {
        Assert.assertEquals(method2.toString(), rs.getString("method"));
        Assert.assertEquals(workload2.toString(), rs.getString("workload"));
        Assert.assertEquals("[1,2.0]", rs.getString("workload_arguments"));
        Assert.assertEquals(statistics2.getNumberOfMeasurements(), rs.getInt("number_of_measurements"));
        Assert.assertEquals(statistics2.computeMean(), rs.getLong("time"));
    }

    private void checkResultSetFor3(ResultSet rs) throws SQLException {
        Assert.assertEquals(method3.toString(), rs.getString("method"));
        Assert.assertEquals(workload3.toString(), rs.getString("workload"));
        Assert.assertEquals("[2.0]", rs.getString("workload_arguments"));
        Assert.assertEquals(statistics3.getNumberOfMeasurements(), rs.getInt("number_of_measurements"));
        Assert.assertEquals(statistics3.computeMean(), rs.getLong("time"));
    }

    @Test
    public void testNumberOfTables() throws SQLException {
        ArrayList<String> set = res.getDBTables();

        //created database contains just two tables
        Assert.assertEquals(2, set.size());

        //which name is results
        Assert.assertTrue(set.contains("measurement_information"));
        Assert.assertTrue(set.contains("measurement_detailed"));
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
    public void insertRowWithMoreMeasurementTimesShallUpdateRecord() throws SQLException {
         //statistics3 contains more measurements, than statistics1/statistics2        
        res.insertResult(new BenchmarkResultImpl(statistics1, benSet3)); 
        res.insertResult(new BenchmarkResultImpl(statistics2, benSet3));
        res.insertResult(benResult3);
        ResultSet rs = getContentsBasicTable();

        Assert.assertTrue(rs.next());        
        checkResultSetFor3(rs);

        Assert.assertFalse(rs.next());
    }

    @Test
    public void insertRowWithSameMeasurementTimesShallDoNothing() throws SQLException {
        //statistics2 and statistics4 contain same amount of measurements
        res.insertResult(benResult2);
        res.insertResult(new BenchmarkResultImpl(statistics4, benSet2));
        ResultSet rs = getContentsBasicTable();

        Assert.assertTrue(rs.next());

        checkResultSetFor2(rs);

        Assert.assertFalse(rs.next());
    }

    @Test
    public void insertRowWithWorseMeasurementTimesShallDoNothing() throws SQLException {
        //statistics3 contains more results than statistics1
        res.insertResult(benResult3);
        res.insertResult(new BenchmarkResultImpl(statistics1, benSet3));
        ResultSet rs = getContentsBasicTable();

        Assert.assertTrue(rs.next());

        checkResultSetFor3(rs);

        Assert.assertFalse(rs.next());
    }

    @Test
    public void testMoreConnections() throws SQLException {
        res.insertResult(benResult3);

        ResultDatabaseCache newRes = new ResultDatabaseCache(TEST_URL);

        //statistics3 contains more results (thus more accurate), therefore next insert will not insert anything at all
        newRes.insertResult(new BenchmarkResultImpl(statistics1, benSet3));
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
    public void testEmptyTable() throws SQLException {
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
}
