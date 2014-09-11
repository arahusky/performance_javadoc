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
 
package cz.cuni.mff.d3s.tools.perfdoc.server;

import java.util.ArrayList;
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
    
    @Test
    public void testReturnHowManyInInterval()
    {
        MethodMeasurer met = new MethodMeasurer();
        
        int val = met.returnHowManyInInterval(1.0, 2.0, 0.1);
        Assert.assertEquals(9, val);  
        
        val = met.returnHowManyInInterval(1.0, 2.0, 0.01);
        Assert.assertEquals(99, val);  
        
        val = met.returnHowManyInInterval(2.0, 2.0, 0.01);
        Assert.assertEquals(0, val);  
        
        val = met.returnHowManyInInterval(2.5, 2.0, 0.01);
        Assert.assertEquals(0, val);  
    }
    
    @Test
    public void testConvertUnitsIfNeededNoConvert()
    {
        MethodMeasurer met = new MethodMeasurer();
        ArrayList<Object[]> list = new ArrayList<>();
        
        list.add(new Object[] {20.0, (long) 10});
        list.add(new Object[] {100000.0, (long) 100});
        list.add(new Object[] {300.0, (long) 1200});
        list.add(new Object[] {200.0, (long) 100});
        
        ArrayList<Object[]> copy = new ArrayList<>(list);
        
        Assert.assertEquals("ns", met.convertUnitsIfNeeded(copy));
       
        arrayListEquals(list, copy);
    }
    
    @Test
    public void testConvertUnitsIfNeededConvert()
    {
        MethodMeasurer met = new MethodMeasurer();
        ArrayList<Object[]> list = new ArrayList<>();        
        list.add(new Object[] {20.0, (long) 10001});
        list.add(new Object[] {100000.0, (long) 100000});
        list.add(new Object[] {300.0, (long) 12000});
        list.add(new Object[] {200.0, (long) 100000});
        
        ArrayList<Object[]> expected = new ArrayList<>();
        expected.add(new Object[] {20.0, (long) 10});
        expected.add(new Object[] {100000.0, (long) 100});
        expected.add(new Object[] {300.0, (long) 12});
        expected.add(new Object[] {200.0, (long) 100});
        
        Assert.assertEquals("µs", met.convertUnitsIfNeeded(list));
       
        arrayListEquals(expected, list);
    }
    
    @Test
    public void testConvertUnitsIfNeededMoreConverts()
    {
        MethodMeasurer met = new MethodMeasurer();
        ArrayList<Object[]> list = new ArrayList<>();        
        list.add(new Object[] {20.0, 10001000000L});
        list.add(new Object[] {100000.0, 100000000000L});
        list.add(new Object[] {300.0, 12000000000L});
        list.add(new Object[] {200.0, 100000000000L});
        
        ArrayList<Object[]> expected = new ArrayList<>();
        expected.add(new Object[] {20.0, (long) 10});
        expected.add(new Object[] {100000.0, (long) 100});
        expected.add(new Object[] {300.0, (long) 12});
        expected.add(new Object[] {200.0, (long) 100});
        
        Assert.assertEquals("s", met.convertUnitsIfNeeded(list));
       
        arrayListEquals(expected, list);
    }
    
    private void arrayListEquals(ArrayList<Object[]>  first, ArrayList<Object[]> second) {
        Assert.assertEquals(first.size(), second.size());
        
        for (int i = 0; i<first.size(); i++) {
            Assert.assertArrayEquals(first.get(i), second.get(i));
        }
     }
            
}
