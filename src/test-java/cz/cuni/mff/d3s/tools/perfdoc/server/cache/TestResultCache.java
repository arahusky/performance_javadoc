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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jakub Naplava
 */
public class TestResultCache {

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
        res.insertResult("method1", "generator", "[data]", 10, 1000);
        ResultSet rs = res.getTable();

        Assert.assertTrue(rs.next());

        Assert.assertEquals("method1", rs.getString("methodName"));
        Assert.assertEquals("[data]", rs.getString("data"));
        Assert.assertEquals(10, rs.getInt("numberOfMeasurements"));
        Assert.assertEquals(1000, rs.getInt("time"));

        Assert.assertFalse(rs.next());
    }

    @Test
    public void testMultipleInsertDifferentRow() throws SQLException {
        res.insertResult("method1", "generator", "[data]", 10, 1000);
        res.insertResult("method2", "generator", "[data1]", 0, 20);
        res.insertResult("method3", "generator", "[data2]", 10, 1000);
        ResultSet rs = res.getTable();

        Assert.assertTrue(rs.next());

        Assert.assertEquals("method1", rs.getString("methodName"));
        Assert.assertEquals("[data]", rs.getString("data"));
        Assert.assertEquals(10, rs.getInt("numberOfMeasurements"));
        Assert.assertEquals(1000, rs.getInt("time"));

        Assert.assertTrue(rs.next());

        Assert.assertEquals("method2", rs.getString("methodName"));
        Assert.assertEquals("[data1]", rs.getString("data"));
        Assert.assertEquals(0, rs.getInt("numberOfMeasurements"));
        Assert.assertEquals(20, rs.getInt("time"));

        Assert.assertTrue(rs.next());

        Assert.assertEquals("method3", rs.getString("methodName"));
        Assert.assertEquals("[data2]", rs.getString("data"));
        Assert.assertEquals(10, rs.getInt("numberOfMeasurements"));
        Assert.assertEquals(1000, rs.getInt("time"));

        Assert.assertFalse(rs.next());
    }

    @Test
    public void testMultipleUpdateSametRowBetterResults() throws SQLException {
        res.insertResult("method", "generator", "[data]", 10, 1000);
        res.insertResult("method", "generator", "[data]", 100, 20);
        ResultSet rs = res.getTable();

        Assert.assertTrue(rs.next());

        Assert.assertEquals("method", rs.getString("methodName"));
        Assert.assertEquals("[data]", rs.getString("data"));
        Assert.assertEquals(100, rs.getInt("numberOfMeasurements"));
        Assert.assertEquals(20, rs.getInt("time"));

        Assert.assertFalse(rs.next());
    }

    @Test
    public void testMultipleUpdateSametRowEqualResults() throws SQLException {
        res.insertResult("method", "generator", "[data]", 10, 1000);
        res.insertResult("method", "generator", "[data]", 10, 200);
        ResultSet rs = res.getTable();

        Assert.assertTrue(rs.next());

        Assert.assertEquals("method", rs.getString("methodName"));
        Assert.assertEquals("[data]", rs.getString("data"));
        Assert.assertEquals(10, rs.getInt("numberOfMeasurements"));
        Assert.assertEquals(1000, rs.getInt("time"));

        Assert.assertFalse(rs.next());
    }

    @Test
    public void testMultipleUpdateSametRowWorseResults() throws SQLException {
        res.insertResult("method", "generator", "[data]", 10, 1000);
        res.insertResult("method", "generator", "[data]", 9, 200);
        ResultSet rs = res.getTable();

        Assert.assertTrue(rs.next());

        Assert.assertEquals("method", rs.getString("methodName"));
        Assert.assertEquals("[data]", rs.getString("data"));
        Assert.assertEquals(10, rs.getInt("numberOfMeasurements"));
        Assert.assertEquals(1000, rs.getInt("time"));

        Assert.assertFalse(rs.next());
    }

    @Test
    public void testMulitpleUpdateSametRowWorseResults() throws SQLException {
        res.insertResult("method", "generator", "[data]", 10, 1000);
        res.insertResult("method", "generator", "[data]", 9, 200);
        ResultSet rs = res.getTable();

        Assert.assertTrue(rs.next());

        Assert.assertEquals("method", rs.getString("methodName"));
        Assert.assertEquals("[data]", rs.getString("data"));
        Assert.assertEquals(10, rs.getInt("numberOfMeasurements"));
        Assert.assertEquals(1000, rs.getInt("time"));

        Assert.assertFalse(rs.next());
    }

    @Test
    public void testMoreConnections() throws SQLException {
        res.insertResult("method", "generator", "[data]", 10, 1000);

        ResultDatabaseCache newRes = new ResultDatabaseCache(true);

        newRes.insertResult("method", "generator", "[data]", 9, 200);
        newRes.insertResult("method1", "generator", "[data]", 9, 200);

        ResultSet rs = res.getTable();

        Assert.assertTrue(rs.next());

        Assert.assertEquals("method", rs.getString("methodName"));
        Assert.assertEquals("[data]", rs.getString("data"));
        Assert.assertEquals(10, rs.getInt("numberOfMeasurements"));
        Assert.assertEquals(1000, rs.getInt("time"));

        Assert.assertTrue(rs.next());

        Assert.assertEquals("method1", rs.getString("methodName"));
        Assert.assertEquals("[data]", rs.getString("data"));
        Assert.assertEquals(9, rs.getInt("numberOfMeasurements"));
        Assert.assertEquals(200, rs.getInt("time"));

        Assert.assertFalse(rs.next());

        newRes.closeConnection();
    }

    @Test
    public void testGetResultSimple() {
        res.insertResult("method", "generator", "[0;0;0;0]", 10, 1000);

        long time = res.getResult("method", "generator", "[0;0;0;0]", 9);
        Assert.assertEquals(1000, time);

        time = res.getResult("method", "generator", "[0;0;0;0]", 10);
        Assert.assertEquals(1000, time);

        time = res.getResult("method", "generator", "[0;0;0;0]", 11);
        Assert.assertEquals(-1, time);

    }

    @Test
    public void testGetResultComplex() {
        res.insertResult("method", "generator", "[data]", 10, 1000);
        res.insertResult("method", "generator", "[data2]", 9, 200);
        res.insertResult("method1", "generator", "[data]", 19, 2500);

        long time = res.getResult("method2", "generator", "[data]", 9);
        Assert.assertEquals(-1, time);

        time = res.getResult("method", "generator", "[data1]", 9);
        Assert.assertEquals(-1, time);

        time = res.getResult("method", "generator", "[data]", 10);
        Assert.assertEquals(1000, time);

        time = res.getResult("method", "generator", "[data]", 11);
        Assert.assertEquals(-1, time);

        time = res.getResult("method", "generator", "[data2]", 9);
        Assert.assertEquals(200, time);

        time = res.getResult("method1", "generator", "[data]", 15);
        Assert.assertEquals(2500, time);

    }

    @Test
    public void testEmptyTable() throws SQLException {
        res.insertResult("method", "generator", "[data]", 10, 1000);
        res.insertResult("method", "generator", "[data2]", 9, 200);

        res.empty();
        ResultSet rs = res.getTable();

        Assert.assertFalse(rs.next());
    }
}
