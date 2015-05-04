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

package cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Jakub Naplava
 */
public class StatisticsUtilsTest {
    
    private static final double delta = 0.0001;
    public void testAll(List<Long> values, double mean, double median, double stdDeviation, double firstQuartile, double thirdQuartile) {
        Assert.assertEquals(mean, StatisticsUtils.getMean(values), delta);
        Assert.assertEquals(median, StatisticsUtils.getMedian(values), delta);
        Assert.assertEquals(stdDeviation, StatisticsUtils.getStandardDeviation(values), delta);
        Assert.assertEquals(firstQuartile, StatisticsUtils.getFirstQuartile(values), delta);
        Assert.assertEquals(thirdQuartile, StatisticsUtils.getThirdQuartile(values), delta);
    }
    
    @Test
    public void testEmptyList() {
        List<Long> values = new ArrayList<>();
        
        testAll(values, -1, -1, -1, -1, -1);
    }
    
    @Test
    public void testBasic() {
        List<Long> values = new ArrayList<>();
        values.add(1L);
        
        testAll(values, 1, 1, 0, 1, 1);
    }
    
    @Test
    public void testComplex1() {
        List<Long> values = new ArrayList<>();
        for (int i = 1; i<=4; i++) {
            values.add((long) i);
        }
        
        
        testAll(values, 2.5, 2.5, Math.sqrt(5D/3), 1.25, 3.75);
    }
    
    @Test
    public void testComplex2() {
        List<Long> values = new ArrayList<>();
        for (int i = 0; i<=10;i++) {
            values.add((long) i);
        }
        
        testAll(values, 5, 5, Math.sqrt(11), 2, 8);
    }
    
    @Test
    public void testRemoveSuspicious() {
        List<Long> values = new ArrayList<>();
        
        for (int i = 0; i<50; i++) {
            values.add((long) i);
        }
        
        //no outlier
        List<Long> newList = StatisticsUtils.excludeOutliers(values);
        Assert.assertEquals(values, newList);
        
        //adding outliers
        values.add(500L);
        values.add(600L);
        newList = StatisticsUtils.excludeOutliers(values);
        
        values.remove(500L);
        values.remove(600L);
        Assert.assertEquals(values, newList);        
    }
    
    @Test
    public void testGetRepresentativeSubset() {
        MeasurementStatistics ms = new MeasurementStatistics();        
        Assert.assertEquals(ms, StatisticsUtils.getRepresentativeSubset(ms, 100));
        
        ms.addResult(100);        
        Assert.assertEquals(ms, StatisticsUtils.getRepresentativeSubset(ms, 100));
        
        ms = new MeasurementStatistics();        
        for (int i = 1; i<100; i++) {
            ms.addResult(i);
        }
        
        MeasurementStatistics expected = new MeasurementStatistics();
        for (int i = 1; i<=50; i++) {
            expected.addResult(1 + (i-1)*2);
        }
        Assert.assertEquals(expected, StatisticsUtils.getRepresentativeSubset(ms, 50));
    }
}
