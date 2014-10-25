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

import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.Statistics;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Jakub Naplava
 */
public class StatisticsTest {
    
    @Test(expected= IllegalArgumentException.class)
    public void testNullCreation() {
        new Statistics(null, new Object[5]);
    }
    
    @Test
    public void testSimpleCompute() throws SecurityException, NoSuchMethodException {
        Statistics s = new Statistics(StatisticsTest.class.getMethod("testSimpleCompute", null), new Object[0]);
        
        s.addResult(10);
        s.addResult(12);
        s.addResult(14);
        s.addResult(16);
        
        Assert.assertEquals(13, s.compute());
    }
}
