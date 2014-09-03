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
 
package testing.server;

import cz.cuni.mff.d3s.tools.perfdoc.server.MethodMeasurer;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Jakub Naplava
 */
public class TestMethodMeasurer {
    
    
    @Test
    public void testFindOtherValuesSimple()
    {
        MethodMeasurer met = new MethodMeasurer();
        
        double step = 2;
        double minVal = 12;
        double maxVal = 20;
        int howMany = 3;
        double[] arr = met.findOtherValues(step, minVal, maxVal, howMany);
        double[] expected = new double[] {14,16,18};
        
        arrayEquals(expected, arr);        
    }
    
    @Test
    public void testFindOtherValuesMore()
    {
        MethodMeasurer met = new MethodMeasurer();
        
        double step = 0.5;
        double minVal = 12;
        double maxVal = 37.5;
        int howMany = 5;
        double[] arr = met.findOtherValues(step, minVal, maxVal, howMany);
        double[] expected = new double[] {16,20,24,28,32};
        
        arrayEquals(expected, arr);        
    }
    
    private void arrayEquals(double[] arr, double[] arr1) {
        Assert.assertEquals(arr.length, arr1.length);
        
        for (int i = 0; i<arr.length; i++) {
            Assert.assertEquals(arr[i], arr1[i], 0.0001);
        }
    }
    
    @Test
    public void testFindNearestPossibleValue()
    {
        MethodMeasurer met = new MethodMeasurer();
        
        double val = met.findNearestSmallerPossibleValue(37.999, 15, 1);
        Assert.assertEquals(37, val, 0.0001);
        
        val = met.findNearestSmallerPossibleValue(37.999, 15, 0.1);
        Assert.assertEquals(37.9, val, 0.0001);
        
        val = met.findNearestSmallerPossibleValue(38.01, 15, 0.1);
        Assert.assertEquals(38, val, 0.0001);
    }
            
}
