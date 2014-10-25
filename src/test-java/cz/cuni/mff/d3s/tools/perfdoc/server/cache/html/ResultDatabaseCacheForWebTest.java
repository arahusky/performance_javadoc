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
package cz.cuni.mff.d3s.tools.perfdoc.server.cache.html;

import cz.cuni.mff.d3s.tools.perfdoc.server.MethodInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.MeasurementResult;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
public class ResultDatabaseCacheForWebTest {

    private static ResultDatabaseCacheForWeb res;

    @BeforeClass
    public static void testStartAndCreateDB() throws SQLException, ClassNotFoundException {
        res = new ResultDatabaseCacheForWeb(true);
        res.startTestDatabase();
    }

    @Before
    public void makeNewConnection() throws SQLException {
        res = new ResultDatabaseCacheForWeb(true);
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
    public void testGetResults() {
        res.insertResult("package#class1#method#@someParam", "package#class1#generator0#@someParam", "[data]", 10, 1000);
        res.insertResult("package#class1#method#@someParam", "package#class1#generator1#@someParam", "[data2]", 9, 200);
        res.insertResult("package#class1#method2#@someParam", "package#class1#generator2#@someParam", "[data3]", 90, 1200);
        List<MeasurementResult> list = res.getResults();

        Assert.assertNotNull(list);

        Assert.assertEquals(3, list.size());

        MeasurementResult res = new MeasurementResult(new MethodInfo("package#class1#method#@someParam"), new MethodInfo("package#class1#generator0#@someParam"), "[data]", 10, 1000);
        Assert.assertTrue(res.equals(list.get(0)));

        res = new MeasurementResult(new MethodInfo("package#class1#method#@someParam"), new MethodInfo("package#class1#generator1#@someParam"), "[data2]", 9, 200);
        Assert.assertTrue(res.equals(list.get(1)));

        res = new MeasurementResult(new MethodInfo("package#class1#method2#@someParam"), new MethodInfo("package#class1#generator2#@someParam"), "[data3]", 90, 1200);
        Assert.assertTrue(res.equals(list.get(2)));
    }

     @Test
    public void testGetDistinctTestedMethods() {
        res.insertResult("method", "generator", "[data]", 10, 1000);
        res.insertResult("method", "generator1", "[data2]", 9, 200);
        res.insertResult("method2", "generator2", "[data3]", 90, 1200);
        res.insertResult("method2", "someGen", "someData", 10, 300);
        ArrayList<String> list = res.getDistinctTestedMethods();

        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.contains("method"));
        Assert.assertTrue(list.contains("method2"));
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

        Assert.assertNotNull(list);
        Assert.assertEquals(3, list.size());
        Assert.assertTrue(list.contains("package1.class1#method"));
        Assert.assertTrue(list.contains("package1.class1#method2"));
        Assert.assertTrue(list.contains("package1.class1#method3"));
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

        Assert.assertNotNull(list);
        Assert.assertEquals(3, list.size());
        Assert.assertTrue(list.contains("generator"));
        Assert.assertTrue(list.contains("generator1"));
        Assert.assertTrue(list.contains("someGen"));
    }

    @Test
    public void testGetResultsMethodAndGenerator() {
        res.insertResult("package#class1#method#someParam", "package#class1#generator#someParam", "[data]", 10, 1000);
        res.insertResult("package#class1#method#someParam", "package#class1#generator#someParam", "[data2]", 9, 200);
        res.insertResult("package#class1#method#someParam", "package#class1#generator#someParam", "[data3]", 19, 2000);
        res.insertResult("package#class1#method2#someParam", "package#class1#generator2#someParam", "[data3]", 90, 1200);
        res.insertResult("package#class1#method3#someParam", "package#class1#someGen#someParam", "someData", 10, 300);
        res.insertResult("package#class1#method#someParam", "package#class1#someGen#someParam", "someData", 10, 300);
        res.insertResult("package1#class3#method2#someParam", "package#class1#someGen#someParam", "someData", 10, 300);
        res.insertResult("package2#class2#method2#someParam", "package#class1#someGen#someParam", "someData", 10, 300);
        List<MeasurementResult> list = res.getResults("package#class1#method#someParam", "package#class1#generator#someParam");

        Assert.assertNotNull(list);
        Assert.assertEquals(3, list.size());

        MethodInfo testedMethod = new MethodInfo("package#class1#method#someParam");
        MethodInfo generator = new MethodInfo("package#class1#generator#someParam");
        
        MeasurementResult mr = new MeasurementResult(testedMethod, generator, "[data]", 10, 1000);        
        Assert.assertTrue(mr.equals(list.get(0)));

        mr = new MeasurementResult(testedMethod, generator, "[data2]", 9, 200);
        Assert.assertTrue(mr.equals(list.get(1)));

        mr = new MeasurementResult(testedMethod, generator, "[data3]", 19, 2000);
        Assert.assertTrue(mr.equals(list.get(2)));
    }
}
