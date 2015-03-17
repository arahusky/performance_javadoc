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
import static cz.cuni.mff.d3s.tools.perfdoc.server.cache.BenchmarkMockups.*;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkResult;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkResultImpl;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkSetting;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkSettingImpl;
import java.sql.SQLException;
import java.util.Collection;
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
    private static final String TEST_URL = "jdbc:derby:test_database/cacheDB;create=true";

    @BeforeClass
    public static void testStartAndCreateDB() throws SQLException, ClassNotFoundException {
        res = new ResultDatabaseCacheForWeb(TEST_URL);
        res.start();
    }

    @Before
    public void makeNewConnection() throws SQLException {
        res = new ResultDatabaseCacheForWeb(TEST_URL);
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

    /*the database data contain all parameters as String and do not contain the first two of them (workload, serviceWorkload). Therefore just partial test is possible*/
    @Test
    public void testGetResults() {
        res.insertResult(benResult1);
        res.insertResult(benResult2);
        res.insertResult(benResult3);
        List<BenchmarkResult> list = res.getMainTableResults();

        Assert.assertNotNull(list);
        Assert.assertEquals(3, list.size());
        
        Assert.assertEquals(benResult1.getStatistics().computeMean(), list.get(0).getStatistics().computeMean());
        Assert.assertEquals(benResult1.getBenchmarkSetting().getMeasuredMethod(), list.get(0).getBenchmarkSetting().getMeasuredMethod());
        Assert.assertEquals(benResult1.getBenchmarkSetting().getGenerator(), list.get(0).getBenchmarkSetting().getGenerator());
       
        System.out.println("MeasureTime:" + benResult1.getBenchmarkSetting().getMeasurementQuality().getMeasurementTime());
        System.out.println("Num meaurements:" + benResult1.getBenchmarkSetting().getMeasurementQuality().getNumberOfMeasurementsCycles());
        System.out.println("warmupTime:" + benResult1.getBenchmarkSetting().getMeasurementQuality().getWarmupTime());
        System.out.println("Num warmup:" + benResult1.getBenchmarkSetting().getMeasurementQuality().getNumberOfWarmupCycles());
        System.out.println("Priority:" + benResult1.getBenchmarkSetting().getMeasurementQuality().getPriority());
        System.out.println("Num points:" + benResult1.getBenchmarkSetting().getMeasurementQuality().getNumberOfPoints());
        
         System.out.println("MeasureTime:" + list.get(0).getBenchmarkSetting().getMeasurementQuality().getMeasurementTime());
         System.out.println("Num meaurements:" + list.get(0).getBenchmarkSetting().getMeasurementQuality().getNumberOfMeasurementsCycles());
         System.out.println("warmupTime:" + list.get(0).getBenchmarkSetting().getMeasurementQuality().getWarmupTime());
         System.out.println("Num warmup:" + list.get(0).getBenchmarkSetting().getMeasurementQuality().getNumberOfWarmupCycles());
        System.out.println("Priority:" + list.get(0).getBenchmarkSetting().getMeasurementQuality().getPriority());
        System.out.println("Num points:" + list.get(0).getBenchmarkSetting().getMeasurementQuality().getNumberOfPoints());
         
        Assert.assertEquals(benResult1.getBenchmarkSetting().getMeasurementQuality(), list.get(0).getBenchmarkSetting().getMeasurementQuality());
        
        Assert.assertEquals(benResult2.getStatistics().computeMean(), list.get(1).getStatistics().computeMean());
        Assert.assertEquals(benResult2.getBenchmarkSetting().getMeasuredMethod(), list.get(1).getBenchmarkSetting().getMeasuredMethod());
        Assert.assertEquals(benResult2.getBenchmarkSetting().getGenerator(), list.get(1).getBenchmarkSetting().getGenerator());
        Assert.assertEquals(benResult2.getBenchmarkSetting().getMeasurementQuality(), list.get(1).getBenchmarkSetting().getMeasurementQuality());
        
        Assert.assertEquals(benResult3.getStatistics().computeMean(), list.get(2).getStatistics().computeMean());
        Assert.assertEquals(benResult3.getBenchmarkSetting().getMeasuredMethod(), list.get(2).getBenchmarkSetting().getMeasuredMethod());
        Assert.assertEquals(benResult3.getBenchmarkSetting().getGenerator(), list.get(2).getBenchmarkSetting().getGenerator());
        Assert.assertEquals(benResult3.getBenchmarkSetting().getMeasurementQuality(), list.get(2).getBenchmarkSetting().getMeasurementQuality());
    }

    @Test
    public void testGetDistinctTestedMethods() {
        res.insertResult(benResult1);
        res.insertResult(benResult2);
        res.insertResult(benResult3);
        res.insertResult(benResult4);
        Collection<MethodInfo> list = res.getDistinctMeasuredMethods();

        Assert.assertNotNull(list);
        Assert.assertEquals(4, list.size());
        Assert.assertTrue(list.contains(measuredMethod1));
        Assert.assertTrue(list.contains(measuredMethod2));
        Assert.assertTrue(list.contains(measuredMethod3));
        Assert.assertTrue(list.contains(measuredMethod4));
    }

    @Test
    public void testGetDistinctClassMethods() {
        res.insertResult(benResult1);
        res.insertResult(benResult2);
        res.insertResult(benResult3);
        res.insertResult(benResult4);

        Collection<MethodInfo> list = res.getDistinctClassMethods(measuredMethod1.getQualifiedClassName());

        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.contains(measuredMethod1));
        Assert.assertTrue(list.contains(measuredMethod2));
    }

    @Test
    public void testGetDistinctGenerators() {
        res.insertResult(benResult1);

        BenchmarkSetting benSet5 = new BenchmarkSettingImpl(measuredMethod1, generator1, generatorArguments3, measurementQuality2);
        BenchmarkSetting benSet6 = new BenchmarkSettingImpl(measuredMethod1, generator2, generatorArguments4, measurementQuality1);
        res.insertResult(new BenchmarkResultImpl(statistics1, benSet5));
        res.insertResult(new BenchmarkResultImpl(statistics1, benSet6));

        Collection<MethodInfo> list = res.getDistinctGenerators(measuredMethod1);

        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.contains(generator1));
        Assert.assertTrue(list.contains(generator2));
    }

    /*the database data contain all parameters as String and do not contain the first two of them (workload, serviceWorkload). Therefore just partial test is possible*/
    @Test
    public void testGetResultsMethodAndGenerator() {
        res.insertResult(benResult1);
        res.insertResult(benResult2);
        res.insertResult(benResult3);
        res.insertResult(benResult4);

        BenchmarkSetting benSet5 = new BenchmarkSettingImpl(measuredMethod1, generator1, generatorArguments2, measurementQuality1);
        BenchmarkSetting benSet6 = new BenchmarkSettingImpl(measuredMethod1, generator1, generatorArguments3, measurementQuality2);
        BenchmarkSetting benSet7 = new BenchmarkSettingImpl(measuredMethod1, generator1, generatorArguments4, measurementQuality4);
        res.insertResult(new BenchmarkResultImpl(statistics1, benSet5));
        res.insertResult(new BenchmarkResultImpl(statistics2, benSet6));
        res.insertResult(new BenchmarkResultImpl(statistics3, benSet7));

        List<BenchmarkResult> list = res.getResults(measuredMethod1, generator1);

        Assert.assertNotNull(list);
        Assert.assertEquals(4, list.size());

        Assert.assertEquals(benResult1.getStatistics(), list.get(0).getStatistics());
        Assert.assertEquals(benSet1.getMeasuredMethod(), list.get(0).getBenchmarkSetting().getMeasuredMethod());
        Assert.assertEquals(benSet1.getGenerator(), list.get(0).getBenchmarkSetting().getGenerator());
        
        Assert.assertEquals(statistics1.computeMean(), list.get(1).getStatistics().computeMean());
        Assert.assertEquals(measuredMethod1, list.get(1).getBenchmarkSetting().getMeasuredMethod());
        Assert.assertEquals(generator1, list.get(1).getBenchmarkSetting().getGenerator());
        
        Assert.assertEquals(statistics2.computeMean(), list.get(2).getStatistics().computeMean());
        Assert.assertEquals(measuredMethod1, list.get(2).getBenchmarkSetting().getMeasuredMethod());
        Assert.assertEquals(generator1, list.get(2).getBenchmarkSetting().getGenerator());
        
        Assert.assertEquals(statistics3.computeMean(), list.get(3).getStatistics().computeMean());
        Assert.assertEquals(measuredMethod1, list.get(3).getBenchmarkSetting().getMeasuredMethod());
        Assert.assertEquals(generator1, list.get(3).getBenchmarkSetting().getGenerator());
    }
}