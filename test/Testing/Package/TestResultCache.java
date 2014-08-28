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
package Testing.Package;

import cz.cuni.mff.d3s.tools.perfdoc.server.ResultCache;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author arahusky
 */
public class TestResultCache {

    private static Connection conn;

    @BeforeClass
    public static void testStartAndCreateDB() {
        try {
            ResultCache.startTestDatabase();
            conn = ResultCache.createTestConnection();
        } catch (ClassNotFoundException | SQLException e) {
            Assert.assertTrue(false);
        }
    }

    @Before
    public void makeNewConnection() {
        try {
            conn = ResultCache.createTestConnection();
        } catch (SQLException e) {
            Assert.assertTrue(false);
        }
    }

    @After
    public void closeConnection() {
        try {
            ResultCache.dropTable(conn);
            ResultCache.closeConnection(conn);
        } catch (SQLException ex) {
            Assert.assertTrue(false);
        }

    }

    @AfterClass
    public static void endDB() {
        ResultCache.closeDatabase();
    }

    @Test
    public void testNumberOfTables() {
        try {
            ArrayList<String> set = ResultCache.getDBTables(conn);

            //created database contains just one table
            Assert.assertTrue(set.size() == 1);

            //which name is results
            Assert.assertTrue(set.get(0).equals("results"));

        } catch (SQLException ex) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testSimpleEmptyTableTest() {
        try {
            ResultSet rs = ResultCache.getTable(conn);

            if (rs.next()) {
                Assert.assertTrue(false);
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testSimpleInsertRow() {
        try {
            ResultCache.insertResult(conn, "method1", "[data]", 10, 1000);
            ResultSet rs = ResultCache.getTable(conn);

            if (!rs.next()) {
                Assert.assertTrue(false);
            } else {
                Assert.assertEquals("method1", rs.getString("methodName"));
                Assert.assertEquals("[data]", rs.getString("data"));
                Assert.assertEquals(10, rs.getInt("numberOfMeasurements"));
                Assert.assertEquals(1000, rs.getInt("time"));

                if (rs.next()) {
                    Assert.assertTrue(false);
                }
            }
        } catch (SQLException ex) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testMultipleInsertDifferentRow() {
        try {
            ResultCache.insertResult(conn, "method1", "[data]", 10, 1000);
            ResultCache.insertResult(conn, "method2", "[data1]", 0, 20);
            ResultCache.insertResult(conn, "method3", "[data2]", 10, 1000);
            ResultSet rs = ResultCache.getTable(conn);

            if (!rs.next()) {
                Assert.assertTrue(false);
            }

            Assert.assertEquals("method1", rs.getString("methodName"));
            Assert.assertEquals("[data]", rs.getString("data"));
            Assert.assertEquals(10, rs.getInt("numberOfMeasurements"));
            Assert.assertEquals(1000, rs.getInt("time"));

            if (!rs.next()) {
                Assert.assertTrue(false);
            }
            
            Assert.assertEquals("method2", rs.getString("methodName"));
            Assert.assertEquals("[data1]", rs.getString("data"));
            Assert.assertEquals(0, rs.getInt("numberOfMeasurements"));
            Assert.assertEquals(20, rs.getInt("time"));
            
            if (!rs.next()) {
                Assert.assertTrue(false);
            }
            
            Assert.assertEquals("method3", rs.getString("methodName"));
            Assert.assertEquals("[data2]", rs.getString("data"));
            Assert.assertEquals(10, rs.getInt("numberOfMeasurements"));
            Assert.assertEquals(1000, rs.getInt("time"));
            
            if (rs.next()) {
                Assert.assertTrue(false);
            }

        } catch (SQLException ex) {
            Assert.assertTrue(false);
        }
    }
    
    @Test
    public void testMultipleUpdateSametRowBetterResults() {
        try {
            ResultCache.insertResult(conn, "method", "[data]", 10, 1000);
            ResultCache.insertResult(conn, "method", "[data]", 100, 20);
            ResultSet rs = ResultCache.getTable(conn);

            if (!rs.next()) {
                Assert.assertTrue(false);
            }

            Assert.assertEquals("method", rs.getString("methodName"));
            Assert.assertEquals("[data]", rs.getString("data"));
            Assert.assertEquals(100, rs.getInt("numberOfMeasurements"));
            Assert.assertEquals(20, rs.getInt("time"));

            if (rs.next()) {
                Assert.assertTrue(false);
            }
        } catch (SQLException ex) {
            Assert.assertTrue(false);
        }
    }
    
    @Test
    public void testMultipleUpdateSametRowEqualResults() {
        try {
            ResultCache.insertResult(conn, "method", "[data]", 10, 1000);
            ResultCache.insertResult(conn, "method", "[data]", 10, 200);
            ResultSet rs = ResultCache.getTable(conn);

            if (!rs.next()) {
                Assert.assertTrue(false);
            }

            Assert.assertEquals("method", rs.getString("methodName"));
            Assert.assertEquals("[data]", rs.getString("data"));
            Assert.assertEquals(10, rs.getInt("numberOfMeasurements"));
            Assert.assertEquals(1000, rs.getInt("time"));

            if (rs.next()) {
                Assert.assertTrue(false);
            }
        } catch (SQLException ex) {
            Assert.assertTrue(false);
        }
    }
    
    @Test
    public void testMultipleUpdateSametRowWorseResults() {
        try {
            ResultCache.insertResult(conn, "method", "[data]", 10, 1000);
            ResultCache.insertResult(conn, "method", "[data]", 9, 200);
            ResultSet rs = ResultCache.getTable(conn);

            if (!rs.next()) {
                Assert.assertTrue(false);
            }

            Assert.assertEquals("method", rs.getString("methodName"));
            Assert.assertEquals("[data]", rs.getString("data"));
            Assert.assertEquals(10, rs.getInt("numberOfMeasurements"));
            Assert.assertEquals(1000, rs.getInt("time"));

            if (rs.next()) {
                Assert.assertTrue(false);
            }
        } catch (SQLException ex) {
            Assert.assertTrue(false);
        }
    }
    
    @Test
    public void testMulitpleUpdateSametRowWorseResults() {
        try {
            ResultCache.insertResult(conn, "method", "[data]", 10, 1000);
            ResultCache.insertResult(conn, "method", "[data]", 9, 200);
            ResultSet rs = ResultCache.getTable(conn);

            if (!rs.next()) {
                Assert.assertTrue(false);
            }

            Assert.assertEquals("method", rs.getString("methodName"));
            Assert.assertEquals("[data]", rs.getString("data"));
            Assert.assertEquals(10, rs.getInt("numberOfMeasurements"));
            Assert.assertEquals(1000, rs.getInt("time"));

            if (rs.next()) {
                Assert.assertTrue(false);
            }
        } catch (SQLException ex) {
            Assert.assertTrue(false);
        }
    }
    
    @Test
    public void testMoreConnections() {
        try {
            ResultCache.insertResult(conn, "method", "[data]", 10, 1000);            
            
            Connection pomConn = ResultCache.createTestConnection();
            ResultCache.insertResult(pomConn, "method", "[data]", 9, 200);
            ResultCache.insertResult(pomConn, "method1", "[data]", 9, 200);
            
            ResultSet rs = ResultCache.getTable(pomConn);

            if (!rs.next()) {
                Assert.assertTrue(false);
            }

            Assert.assertEquals("method", rs.getString("methodName"));
            Assert.assertEquals("[data]", rs.getString("data"));
            Assert.assertEquals(10, rs.getInt("numberOfMeasurements"));
            Assert.assertEquals(1000, rs.getInt("time"));

            if (!rs.next()) {
                Assert.assertTrue(false);
            }
            
            Assert.assertEquals("method1", rs.getString("methodName"));
            Assert.assertEquals("[data]", rs.getString("data"));
            Assert.assertEquals(9, rs.getInt("numberOfMeasurements"));
            Assert.assertEquals(200, rs.getInt("time"));
            
            if (rs.next()) {
                Assert.assertTrue(false);
            }
            
            ResultCache.closeConnection(pomConn);
        } catch (SQLException ex) {
            Assert.assertTrue(false);
        } 
    }
}
