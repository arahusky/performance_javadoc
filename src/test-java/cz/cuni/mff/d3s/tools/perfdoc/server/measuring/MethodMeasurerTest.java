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

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Jakub Naplava
 */
public class MethodMeasurerTest {   
    
//    //the delta used for double comparison
    private static final double delta = 0.00001;
    
    @Test
    public void testFindOtherValuesSimple()
    {
        Assert.assertArrayEquals(new double[] {14,16,18}, MethodMeasurer.findOtherValues(2, 12, 20, 3), delta);
    }
    
    @Test
    public void testFindOtherValuesDecimalStep()
    {        
        Assert.assertArrayEquals(new double[] {16,20,24,28,32}, MethodMeasurer.findOtherValues(0.5, 12, 37.5, 5), delta);
    }
    
    @Test
    public void testFindOtherValuesDecimalNumbers()
    {
        Assert.assertArrayEquals(new double[] {0.7,0.9,1.1,1.3,1.5}, MethodMeasurer.findOtherValues(0.2, 0.5, 1.7, 10), delta);      
    }
    
    @Test
    public void testFindNearestPossibleValue()
    {        
        double val = MethodMeasurer.findNearestSmallerPossibleValue(37.999, 15, 1);
        Assert.assertEquals(37, val, delta);
        
        val = MethodMeasurer.findNearestSmallerPossibleValue(37.999, 15, 0.1);
        Assert.assertEquals(37.9, val, delta);
        
        val = MethodMeasurer.findNearestSmallerPossibleValue(38.01, 15, 0.1);
        Assert.assertEquals(38, val, delta);
    }
    
    @Test
    public void testReturnHowManyInInterval()
    {        
        int val = MethodMeasurer.returnHowManyInInterval(1.0, 2.0, 0.1);
        Assert.assertEquals(9, val);  
        
        val = MethodMeasurer.returnHowManyInInterval(1.0, 2.0, 0.01);
        Assert.assertEquals(99, val);  
        
        val = MethodMeasurer.returnHowManyInInterval(2.0, 2.0, 0.01);
        Assert.assertEquals(0, val);  
        
        val = MethodMeasurer.returnHowManyInInterval(2.5, 2.0, 0.01);
        Assert.assertEquals(0, val);  
    }
    
//    @Test
//    public void testConvertUnitsIfNeededNoConvert()
//    {
//        ArrayList<Object[]> list = new ArrayList<>();
//        
//        list.add(new Object[] {20.0, (long) 10});
//        list.add(new Object[] {100000.0, (long) 100});
//        list.add(new Object[] {300.0, (long) 1200});
//        list.add(new Object[] {200.0, (long) 100});
//        
//        ArrayList<Object[]> copy = new ArrayList<>(list);
//        
//        Assert.assertEquals("ns", MethodMeasurer.convertUnitsIfNeeded(copy));
//       
//        Assert.assertArrayEquals(list.toArray(), copy.toArray());
//    }
    
//    @Test
//    public void testConvertUnitsIfNeededConvert()
//    {
//        MethodMeasurer met = new MethodMeasurer();
//        ArrayList<Object[]> list = new ArrayList<>();        
//        list.add(new Object[] {20.0, (long) 10001});
//        list.add(new Object[] {100000.0, (long) 100000});
//        list.add(new Object[] {300.0, (long) 12000});
//        list.add(new Object[] {200.0, (long) 100000});
//        
//        ArrayList<Object[]> expected = new ArrayList<>();
//        expected.add(new Object[] {20.0, (long) 10});
//        expected.add(new Object[] {100000.0, (long) 100});
//        expected.add(new Object[] {300.0, (long) 12});
//        expected.add(new Object[] {200.0, (long) 100});
//        
//        Assert.assertEquals("µs", met.convertUnitsIfNeeded(list));
//       
//        Assert.assertArrayEquals(expected.toArray(), list.toArray());
//    }
    
//    @Test
//    public void testConvertUnitsIfNeededMoreConverts()
//    {
//        MethodMeasurer met = new MethodMeasurer();
//        ArrayList<Object[]> list = new ArrayList<>();        
//        list.add(new Object[] {20.0, 10001000000L});
//        list.add(new Object[] {100000.0, 100000000000L});
//        list.add(new Object[] {300.0, 12000000000L});
//        list.add(new Object[] {200.0, 100000000000L});
//        
//        ArrayList<Object[]> expected = new ArrayList<>();
//        expected.add(new Object[] {20.0, (long) 10});
//        expected.add(new Object[] {100000.0, (long) 100});
//        expected.add(new Object[] {300.0, (long) 12});
//        expected.add(new Object[] {200.0, (long) 100});
//        
//        Assert.assertEquals("s", met.convertUnitsIfNeeded(list));
//       
//        Assert.assertArrayEquals(expected.toArray(), list.toArray());
//    }            
}
