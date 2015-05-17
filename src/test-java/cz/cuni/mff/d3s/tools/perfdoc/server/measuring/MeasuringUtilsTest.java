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
package cz.cuni.mff.d3s.tools.perfdoc.server.measuring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Jakub Naplava
 */
public class MeasuringUtilsTest {

    //the delta used for double comparison
    private static final double delta = 0.00001;

    @Test
    public void getValuesToMeasureZeroResultsShallReturnNoResult() {
        double[] res = MeasuringUtils.getValuesToMeasure("0 to 9", 1, 0);

        Assert.assertArrayEquals(new double[0], res, delta);
    }

    @Test
    public void getValuesToMeasureMoreResultsThanAvailableShallReturnAllValues() {
        double[] res = MeasuringUtils.getValuesToMeasure("0 to 9", 1, 100);
        Assert.assertArrayEquals(new double[]{0., 1., 2., 3., 4., 5., 6., 7., 8., 9.}, res, delta);
    }

    @Test
    public void getValuesToMeasureThreeResultsShallReturnMinMaxAndHalf() {
        double[] res = MeasuringUtils.getValuesToMeasure("0 to 9", 1, 3);
        Assert.assertArrayEquals(new double[]{0.0, 4.0, 9.0}, res, delta);

        res = MeasuringUtils.getValuesToMeasure("0 to 10", 1, 3);
        Assert.assertArrayEquals(new double[]{0.0, 5.0, 10.0}, res, delta);
    }

    @Test
    public void testGetValuesInWhichToMeasureMoreResults() {
        double[] res = MeasuringUtils.getValuesToMeasure("0 to 9", 1, 6);
        Assert.assertArrayEquals(new double[]{0.0, 2.0, 4.0, 6.0, 7.0, 9.0}, res, delta);

        res = MeasuringUtils.getValuesToMeasure("0 to 9", 1, 5);
        Assert.assertArrayEquals(new double[]{0.0, 2.0, 4.0, 6.0, 9.0}, res, delta);

        res = MeasuringUtils.getValuesToMeasure("0 to 10", 1, 5);
        Assert.assertArrayEquals(new double[]{0.0, 2.0, 5.0, 7.0, 10.0}, res, delta);
    }

    @Test
    public void testGetValuesInWhichToMeasureMoreResultsBinaryBadStep() {
        double[] res = MeasuringUtils.getValuesToMeasure("0 to 1", 0.1, 6);
        Assert.assertArrayEquals(new double[]{0.0, 0.2, 0.5, 0.7, 0.8, 1.0}, res, delta);

        res = MeasuringUtils.getValuesToMeasure("0 to 1", 0.1, 7);
        Assert.assertArrayEquals(new double[]{0.0, 0.2, 0.3, 0.5, 0.7, 0.8, 1.0}, res, delta);
    }

    @Test
    public void testConvertUnitsNoConversion() {
        Assert.assertEquals("ns", MeasuringUtils.convertUnits(Arrays.asList(new Long[]{1000L, 10000L, 20000L}), Arrays.asList(new Long[]{1000L, 10000L, 20000L}), new boolean[3]));
    }

    @Test
    public void testConvertUnitsOneConversion() {
        List<Long> list = Arrays.asList(new Long[]{10000L, 20000L, 30000L});
        Assert.assertEquals("µs", MeasuringUtils.convertUnits(list, new ArrayList<>(list), new boolean[3]));
        Assert.assertArrayEquals(new Object[]{10L, 20L, 30L}, list.toArray());
    }

    @Test
    public void testConvertUnitsMultipleConversions() {
        List<Long> list = Arrays.asList(new Long[]{10000000L, 20000000L, 30000000L});
        Assert.assertEquals("ms", MeasuringUtils.convertUnits(list, new ArrayList<>(list), new boolean[3]));
        Assert.assertArrayEquals(new Object[]{10L, 20L, 30L}, list.toArray());
    }
    
    @Test
    public void testConvertUnitsOmit() {
        List<Long> list = Arrays.asList(new Long[]{10000000L, -1L, 30000000L});
        
        Assert.assertEquals("ns", MeasuringUtils.convertUnits(list, new ArrayList<>(list), new boolean[3]));
        Assert.assertArrayEquals(new Object[]{10000000L, -1L, 30000000L}, list.toArray());
        
        boolean[] omit = new boolean[3];
        omit[1] = true;
        Assert.assertEquals("ms", MeasuringUtils.convertUnits(list, new ArrayList<>(list), omit));
        Assert.assertArrayEquals(new Object[]{10L, 0L, 30L}, list.toArray());
    }

    @Test
    public void testPushBlackholeToBegin() {
        Object[] args = new Object[0];

        args = MeasuringUtils.pushBlackholeToBegin(args);
        Assert.assertArrayEquals(new Object[]{BlackholeFactory.getInstance()}, args);
        
        args = new Object[] { 1, 2D, "hello"};
        args = MeasuringUtils.pushBlackholeToBegin(args);
        Assert.assertArrayEquals(new Object[]{BlackholeFactory.getInstance(), 1, 2D, "hello"}, args);
    }
}
