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
import static cz.cuni.mff.d3s.tools.perfdoc.server.cache.BenchmarkSettingMockups.*;

/**
 *
 * @author Jakub Naplava
 */
public class ResultCacheTest {

    private static ResultDatabaseCache res;
   
    @BeforeClass
    public static void testStartAndCreateDB() throws SQLException, ClassNotFoundException {
        res = new ResultDatabaseCache(true);
        res.startTestDatabase();
    }

    @Before
    public void makeNewConnection() throws SQLException {
        res = new ResultDatabaseCache(true);
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

    private void checkResultSetFor1(ResultSet rs, int numberOfMeasurements, long time) throws SQLException {
        Assert.assertEquals(method1.toString(), rs.getString("methodName"));
        Assert.assertEquals(workload1.toString(), rs.getString("generator"));
        Assert.assertEquals("[arg1]", rs.getString("data"));
        Assert.assertEquals(numberOfMeasurements, rs.getInt("numberOfMeasurements"));
        Assert.assertEquals(time, rs.getInt("time"));
    }

    private void checkResultSetFor2(ResultSet rs, int numberOfMeasurements, long time) throws SQLException {
        Assert.assertEquals(method2.toString(), rs.getString("methodName"));
        Assert.assertEquals(workload2.toString(), rs.getString("generator"));
        Assert.assertEquals("[1,2.0]", rs.getString("data"));
        Assert.assertEquals(numberOfMeasurements, rs.getInt("numberOfMeasurements"));
        Assert.assertEquals(time, rs.getInt("time"));
    }

    private void checkResultSetFor3(ResultSet rs, int numberOfMeasurements, long time) throws SQLException {
        Assert.assertEquals(method3.toString(), rs.getString("methodName"));
        Assert.assertEquals(workload3.toString(), rs.getString("generator"));
        Assert.assertEquals("[2.0]", rs.getString("data"));
        Assert.assertEquals(numberOfMeasurements, rs.getInt("numberOfMeasurements"));
        Assert.assertEquals(time, rs.getInt("time"));
    }

    @Test
    public void testNumberOfTables() throws SQLException {
        ArrayList<String> set = res.getDBTables();

        //created database contains just one table
        Assert.assertEquals(1, set.size());

        //which name is results
        Assert.assertEquals("results", set.get(0));
    }

    @Test
    public void testSimpleEmptyTableTest() throws SQLException {
        ResultSet rs = res.getTable();

        Assert.assertFalse(rs.next());
    }

    @Test
    public void testSimpleInsertRow() throws SQLException {
        res.insertResult(benSet1, 10, 1000);
        ResultSet rs = res.getTable();

        Assert.assertTrue(rs.next());

        checkResultSetFor1(rs, 10, 1000);

        Assert.assertFalse(rs.next());
    }

    @Test
    public void testMultipleInsertDifferentRecords() throws SQLException {
        res.insertResult(benSet1, 10, 1000);
        res.insertResult(benSet2, 0, 20);
        res.insertResult(benSet3, 10, 1000);
        ResultSet rs = res.getTable();

        Assert.assertTrue(rs.next());

        checkResultSetFor1(rs, 10, 1000);

        Assert.assertTrue(rs.next());

        checkResultSetFor2(rs, 0, 20);

        Assert.assertTrue(rs.next());

        checkResultSetFor3(rs, 10, 1000);

        Assert.assertFalse(rs.next());
    }

    @Test
    public void insertRowWithMoreMeasurementTimesShallUpdateRecord() throws SQLException {
        res.insertResult(benSet1, 10, 1000);
        res.insertResult(benSet1, 100, 20);
        ResultSet rs = res.getTable();

        Assert.assertTrue(rs.next());

        checkResultSetFor1(rs, 100, 20);

        Assert.assertFalse(rs.next());
    }

    @Test
    public void insertRowWithSameMeasurementTimesShallDoNothing() throws SQLException {
        res.insertResult(benSet1, 10, 1000);
        res.insertResult(benSet1, 10, 200);
        ResultSet rs = res.getTable();

        Assert.assertTrue(rs.next());

        checkResultSetFor1(rs, 10, 1000);

        Assert.assertFalse(rs.next());
    }

    @Test
    public void insertRowWithWorseMeasurementTimesShallDoNothing() throws SQLException {
        res.insertResult(benSet1, 10, 1000);
        res.insertResult(benSet1, 9, 200);
        ResultSet rs = res.getTable();

        Assert.assertTrue(rs.next());

        checkResultSetFor1(rs, 10, 1000);

        Assert.assertFalse(rs.next());
    }

    @Test
    public void testMoreConnections() throws SQLException {
        res.insertResult(benSet1, 10, 1000);

        ResultDatabaseCache newRes = new ResultDatabaseCache(true);

        newRes.insertResult(benSet1, 9, 200);
        newRes.insertResult(benSet2, 9, 200);

        ResultSet rs = res.getTable();

        Assert.assertTrue(rs.next());

        checkResultSetFor1(rs, 10, 1000);

        Assert.assertTrue(rs.next());

        checkResultSetFor2(rs, 9, 200);

        Assert.assertFalse(rs.next());

        newRes.closeConnection();
    }

    @Test
    public void testGetResultSimple() {
        res.insertResult(benSet1, 10, 1000);

        BenchmarkResult br1 = res.getResult(benSet1, 9);
        Assert.assertEquals(1000, br1.getStatistics().compute());

        br1 = res.getResult(benSet1, 10);
        Assert.assertEquals(1000, br1.getStatistics().compute());

        br1 = res.getResult(benSet1, 11);
        Assert.assertEquals(-1, br1.getStatistics().compute());

    }

    @Test
    public void testGetResultComplex() {
        res.insertResult(benSet1, 10, 1000);
        res.insertResult(benSet2, 9, 200);
        res.insertResult(benSet3, 19, 2500);

        BenchmarkResult br = res.getResult(benSet4, 9);
        Assert.assertEquals(-1, br.getStatistics().compute());

        br = res.getResult(benSet1, 10);
        Assert.assertEquals(1000, br.getStatistics().compute());

        br = res.getResult(benSet2, 9);
        Assert.assertEquals(200, br.getStatistics().compute());

        br = res.getResult(benSet3, 15);
        Assert.assertEquals(2500, br.getStatistics().compute());
    }

    @Test
    public void testEmptyTable() throws SQLException {
        res.insertResult(benSet1, 10, 1000);
        res.insertResult(benSet2, 9, 200);

        res.empty();
        ResultSet rs = res.getTable();

        Assert.assertFalse(rs.next());
    }
}
