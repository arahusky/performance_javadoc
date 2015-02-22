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

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Jakub Naplava
 */
public class StatisticsTest {
        
    @Test
    public void testSimpleCompute() throws SecurityException, NoSuchMethodException {
        Statistics s = new Statistics();
        
        s.addResult(10);
        s.addResult(12);
        s.addResult(14);
        s.addResult(16);
        
        Assert.assertEquals(13, s.computeMean());
    }
    
    @Test
    public void testToString() throws NoSuchMethodException
    {
        Statistics s = new Statistics();
        
        Assert.assertEquals("{}", s.toString());
        
        s.addResult(10);
        s.addResult(12);
        s.addResult(14);
        
        Assert.assertEquals("{10,12,14}", s.toString());
    }
    
    @Test
    public void testGetValues() throws NoSuchMethodException
    {
        Statistics s = new Statistics();  
        
        Assert.assertArrayEquals(new Long[0], s.getValues());
        
        s.addResult(10);
        s.addResult(12);
        s.addResult(14);
        
        Assert.assertArrayEquals(new Long[]{10L,12L,14L}, s.getValues());
    }
    
    @Test
    public void testNewStatisticsFromToString() {
        Statistics s = new Statistics();     
        s.addResult(10);
        s.addResult(12);
        s.addResult(14);
        String toStringStatistics = s.toString();
        Statistics second = new Statistics(toStringStatistics);
        Assert.assertEquals("{10,12,14}", second.toString());
    }
    
    @Test
    public void testEquals() {
        Statistics s1 = new Statistics();
        Statistics s2 = new Statistics("{1,2,3,4,5}");
        Statistics s3 = new Statistics("{1,2,3,4}");
        Statistics s4 = new Statistics("{1,2,3,4,5}");
        
        Assert.assertEquals(s1, new Statistics());
        Assert.assertEquals(s2, s4);
        Assert.assertFalse(s3.equals(s4));
    }
}
