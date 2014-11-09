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

package cz.cuni.mff.d3s.tools.perfdoc.server.cache;

import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkSettingImpl;
import org.junit.Assert;
import org.junit.Test;
import static cz.cuni.mff.d3s.tools.perfdoc.server.cache.BenchmarkSettingMockups.*;

/**
 *
 * @author Jakub Naplava
 */
public class BenchmarkSettingTest {
    @Test
    public void testEquals() {
        Assert.assertFalse(benSet1.equals(benSet2));
        Assert.assertFalse(benSet2.equals(benSet3));
        Assert.assertFalse(benSet3.equals(benSet4));
        Assert.assertFalse(benSet4.equals(benSet1));
        
        BenchmarkSettingImpl benSet5 = new BenchmarkSettingImpl(method1, workload1, methodArguments1, 2);
        Assert.assertFalse(benSet1.equals(benSet5));
        
        benSet5 = new BenchmarkSettingImpl(method2,workload1, methodArguments1, 1);
        Assert.assertFalse(benSet1.equals(benSet5));
        
        benSet5 = new BenchmarkSettingImpl(method1,workload2, methodArguments1, 1);
        Assert.assertFalse(benSet1.equals(benSet5));
        
        benSet5 = new BenchmarkSettingImpl(method1,workload1, methodArguments2, 1);
        Assert.assertFalse(benSet1.equals(benSet5));
        
        benSet5 = new BenchmarkSettingImpl(method1,workload1, methodArguments1, 1);
        Assert.assertEquals(benSet1, benSet5);
    }
}
