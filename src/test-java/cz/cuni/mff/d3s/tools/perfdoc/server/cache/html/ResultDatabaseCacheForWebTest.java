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
import static cz.cuni.mff.d3s.tools.perfdoc.server.cache.BenchmarkSettingMockups.*;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkResult;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkResultImpl;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkSetting;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkSettingImpl;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.Statistics;
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

    /*the database data contain all parameters as String and do not contain the first two of them (workload, serviceWorkload) and also priority is set malformed. Therefore just partial test is possible*/
    @Test
    public void testGetResults() {
        res.insertResult(benSet1, 10, 1000);
        res.insertResult(benSet2, 9, 200);
        res.insertResult(benSet3, 90, 1200);
        List<BenchmarkResult> list = res.getResults();

        Assert.assertNotNull(list);
        Assert.assertEquals(3, list.size());
        
        Assert.assertEquals(new Statistics("{" + 1000 + "}"), list.get(0).getStatistics());
        Assert.assertEquals(benSet1.getTestedMethod(), list.get(0).getBenchmarkSetting().getTestedMethod());
        Assert.assertEquals(benSet1.getWorkload(), list.get(0).getBenchmarkSetting().getWorkload());

        Assert.assertEquals(new Statistics("{" + 200 + "}"), list.get(1).getStatistics());
        Assert.assertEquals(benSet2.getTestedMethod(), list.get(1).getBenchmarkSetting().getTestedMethod());
        Assert.assertEquals(benSet2.getWorkload(), list.get(1).getBenchmarkSetting().getWorkload());

        Assert.assertEquals(new Statistics("{" + 1200 + "}"), list.get(2).getStatistics());
        Assert.assertEquals(benSet3.getTestedMethod(), list.get(2).getBenchmarkSetting().getTestedMethod());
        Assert.assertEquals(benSet3.getWorkload(), list.get(2).getBenchmarkSetting().getWorkload());
    }

    @Test
    public void testGetDistinctTestedMethods() {
        res.insertResult(benSet1, 10, 1000);
        res.insertResult(benSet2, 9, 200);
        res.insertResult(benSet3, 90, 1200);
        res.insertResult(benSet4, 10, 300);
        List<MethodInfo> list = res.getDistinctTestedMethods();

        Assert.assertNotNull(list);
        Assert.assertEquals(4, list.size());
        Assert.assertTrue(list.contains(method1));
        Assert.assertTrue(list.contains(method2));
        Assert.assertTrue(list.contains(method3));
        Assert.assertTrue(list.contains(method4));
    }

    @Test
    public void testGetDistinctClassMethods() {
        res.insertResult(benSet1, 10, 1000);
        res.insertResult(benSet2, 9, 200);
        res.insertResult(benSet3, 90, 1200);
        res.insertResult(benSet4, 10, 300);

        List<MethodInfo> list = res.getDistinctClassMethods(method1.getQualifiedClassName());

        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.contains(method1));
        Assert.assertTrue(list.contains(method2));
    }

    @Test
    public void testGetDistinctGenerators() {
        res.insertResult(benSet1, 10, 100);

        BenchmarkSetting benSet5 = new BenchmarkSettingImpl(method1, workload1, methodArguments3, 2);
        BenchmarkSetting benSet6 = new BenchmarkSettingImpl(method1, workload2, methodArguments4, 4);
        res.insertResult(benSet5, 10, 100);
        res.insertResult(benSet6, 10, 100);

        ArrayList<MethodInfo> list = res.getDistinctGenerators(method1);

        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.contains(workload1));
        Assert.assertTrue(list.contains(workload2));
    }

    /*the database data contain all parameters as String and do not contain the first two of them (workload, serviceWorkload) and also priority is set malformed. Therefore just partial test is possible*/
    @Test
    public void testGetResultsMethodAndGenerator() {
        res.insertResult(benSet1, 10, 1000);
        res.insertResult(benSet2, 20, 20);
        res.insertResult(benSet3, 1, 1000);
        res.insertResult(benSet4, 5, 5);

        BenchmarkSetting benSet5 = new BenchmarkSettingImpl(method1, workload1, methodArguments2, 1);
        BenchmarkSetting benSet6 = new BenchmarkSettingImpl(method1, workload1, methodArguments3, 2);
        BenchmarkSetting benSet7 = new BenchmarkSettingImpl(method1, workload1, methodArguments4, 4);
        res.insertResult(benSet5, 10, 200);
        res.insertResult(benSet6, 210, 2);
        res.insertResult(benSet7, 710, 70);

        List<BenchmarkResult> list = res.getResults(method1, workload1);

        Assert.assertNotNull(list);
        Assert.assertEquals(4, list.size());

        Assert.assertTrue(new Statistics("{" + 1000 + "}").equals(list.get(0).getStatistics()));
        Assert.assertEquals(benSet1.getTestedMethod(), list.get(0).getBenchmarkSetting().getTestedMethod());
        Assert.assertEquals(benSet1.getWorkload(), list.get(0).getBenchmarkSetting().getWorkload());
        
        Assert.assertTrue(new Statistics("{" + 200 + "}").equals(list.get(1).getStatistics()));
        Assert.assertEquals(method1, list.get(1).getBenchmarkSetting().getTestedMethod());
        Assert.assertEquals(workload1, list.get(1).getBenchmarkSetting().getWorkload());
        
        Assert.assertTrue(new Statistics("{" + 2 + "}").equals(list.get(2).getStatistics()));
        Assert.assertEquals(method1, list.get(2).getBenchmarkSetting().getTestedMethod());
        Assert.assertEquals(workload1, list.get(2).getBenchmarkSetting().getWorkload());
        
        Assert.assertTrue(new Statistics("{" + 70 + "}").equals(list.get(3).getStatistics()));
        Assert.assertEquals(method1, list.get(3).getBenchmarkSetting().getTestedMethod());
        Assert.assertEquals(workload1, list.get(3).getBenchmarkSetting().getWorkload());
    }
}
