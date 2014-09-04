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
package testing.server.cache;

import cz.cuni.mff.d3s.tools.perfdoc.server.cache.ResultDatabaseCache;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.ResultCacheForWeb;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.ResultDatabaseCacheForWeb;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
public class TestResultDatabaseCacheForWeb {

    private static ResultDatabaseCacheForWeb res;

    @BeforeClass
    public static void testStartAndCreateDB() {
        try {
            res = new ResultDatabaseCacheForWeb(true);
            res.startTestDatabase();
        } catch (SQLException | ClassNotFoundException e) {
            Assert.assertTrue(false);
        }
    }

    @Before
    public void makeNewConnection() {
        try {
            res = new ResultDatabaseCacheForWeb(true);
        } catch (SQLException e) {
            Assert.assertTrue(false);
        }
    }

    @After
    public void closeConnection() {
        try {
            res.emptyTable();
            res.closeConnection();
        } catch (SQLException ex) {
            Assert.assertTrue(false);
        }

    }

    @AfterClass
    public static void endDB() {
        res.closeDatabase();
    }

    @Test
    public void testGetResults() {
        res.insertResult("method", "generator", "[data]", 10, 1000);
        res.insertResult("method", "generator1", "[data2]", 9, 200);
        res.insertResult("method2", "generator2", "[data3]", 90, 1200);
        List<Map<String, Object>> list = res.getResults();

        if (list == null) {
            Assert.assertTrue(false);
        }

        Assert.assertEquals(3, list.size());

        ArrayList<Object> alist = new ArrayList<>();
        alist.add("method");
        alist.add("generator");
        alist.add("[data]");
        alist.add(10);
        alist.add((long) 1000);
        rowEquals(alist, list.get(0));

        alist = new ArrayList<>();
        alist.add("method");
        alist.add("generator1");
        alist.add("[data2]");
        alist.add(9);
        alist.add((long) 200);
        rowEquals(alist, list.get(1));

        alist = new ArrayList<>();
        alist.add("method2");
        alist.add("generator2");
        alist.add("[data3]");
        alist.add(90);
        alist.add((long) 1200);
        rowEquals(alist, list.get(2));
    }

    @Test
    public void testGetDistinctTestedMethods() {
        res.insertResult("method", "generator", "[data]", 10, 1000);
        res.insertResult("method", "generator1", "[data2]", 9, 200);
        res.insertResult("method2", "generator2", "[data3]", 90, 1200);
        res.insertResult("method2", "someGen", "someData", 10, 300);
        ArrayList<String> list = res.getDistinctTestedMethods();

        if (list == null) {
            Assert.assertTrue(false);
        }

        if (list.size() != 2) {
            Assert.assertTrue(false);
        }

        if (!list.contains("method")) {
            Assert.assertTrue(false);
        }

        if (!list.contains("method2")) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testGetDistinctClassMethods() {
        res.insertResult("package1.class1#method", "generator", "[data]", 10, 1000);
        res.insertResult("package1.class1#method", "generator1", "[data2]", 9, 200);
        res.insertResult("package1.class1#method2", "generator2", "[data3]", 90, 1200);
        res.insertResult("package1.class1#method3", "someGen", "someData", 10, 300);
        res.insertResult("package1.class2#method3", "someGen", "someData", 10, 300);
        res.insertResult("package1.class3#method2", "someGen", "someData", 10, 300);
        res.insertResult("package2.class2#method2", "someGen", "someData", 10, 300);
        ArrayList<String> list = res.getDistinctClassMethods("package1.class1");

        if (list == null) {
            Assert.assertTrue(false);
        }

        if (list.size() != 3) {
            Assert.assertTrue(false);
        }

        if (!list.contains("package1.class1#method")) {
            Assert.assertTrue(false);
        }

        if (!list.contains("package1.class1#method2")) {
            Assert.assertTrue(false);
        }

        if (!list.contains("package1.class1#method3")) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testGetDistinctGenerators() {
        res.insertResult("package1.class1#method", "generator", "[data]", 10, 1000);
        res.insertResult("package1.class1#method", "generator1", "[data2]", 9, 200);
        res.insertResult("package1.class1#method2", "generator2", "[data3]", 90, 1200);
        res.insertResult("package1.class1#method3", "someGen", "someData", 10, 300);
        res.insertResult("package1.class1#method", "someGen", "someData", 10, 300);
        res.insertResult("package1.class3#method2", "someGen", "someData", 10, 300);
        res.insertResult("package2.class2#method2", "someGen", "someData", 10, 300);
        ArrayList<String> list = res.getDistinctGenerators("package1.class1#method");

        if (list == null) {
            Assert.assertTrue(false);
        }

        if (list.size() != 3) {
            Assert.assertTrue(false);
        }

        if (!list.contains("generator")) {
            Assert.assertTrue(false);
        }

        if (!list.contains("generator1")) {
            Assert.assertTrue(false);
        }

        if (!list.contains("someGen")) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testGetResultsMethodAndGenerator() {
        res.insertResult("package1.class1#method", "generator", "[data]", 10, 1000);
        res.insertResult("package1.class1#method", "generator", "[data2]", 9, 200);
        res.insertResult("package1.class1#method", "generator", "[data3]", 19, 2000);
        res.insertResult("package1.class1#method2", "generator", "[data3]", 90, 1200);
        res.insertResult("package1.class1#method3", "someGen", "someData", 10, 300);
        res.insertResult("package1.class1#method", "someGen", "someData", 10, 300);
        res.insertResult("package1.class3#method2", "someGen", "someData", 10, 300);
        res.insertResult("package2.class2#method2", "someGen", "someData", 10, 300);
        List<Map<String, Object>> list = res.getResults("package1.class1#method", "generator");

        if (list == null) {
            Assert.assertTrue(false);
        }

        Assert.assertEquals(3, list.size());

        ArrayList<Object> alist = new ArrayList<>();
        alist.add("[data]");
        alist.add(10);
        alist.add((long) 1000);
        rowEquals2(alist, list.get(0));

        alist = new ArrayList<>();
        alist.add("[data2]");
        alist.add(9);
        alist.add((long) 200);
        rowEquals2(alist, list.get(1));

        alist = new ArrayList<>();
        alist.add("[data3]");
        alist.add(19);
        alist.add((long) 2000);
        rowEquals2(alist, list.get(2));
    }
    
    private void rowEquals2(ArrayList<Object> row, Map<String, Object> map) {
        Assert.assertEquals(row.get(0), map.get("data"));
        Assert.assertEquals(row.get(1), map.get("numberOfMeasurements"));
        Assert.assertEquals(row.get(2), map.get("time"));
    }

    private void rowEquals(ArrayList<Object> row, Map<String, Object> map) {
        Assert.assertEquals(row.get(0), map.get("methodName"));
        Assert.assertEquals(row.get(1), map.get("generator"));
        Assert.assertEquals(row.get(2), map.get("data"));
        Assert.assertEquals(row.get(3), map.get("numberOfMeasurements"));
        Assert.assertEquals(row.get(4), map.get("time"));
    }
}
