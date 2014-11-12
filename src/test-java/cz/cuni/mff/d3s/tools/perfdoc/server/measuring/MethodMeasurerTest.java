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
