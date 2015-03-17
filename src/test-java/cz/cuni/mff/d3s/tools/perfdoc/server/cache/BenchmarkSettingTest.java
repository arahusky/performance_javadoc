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
import static cz.cuni.mff.d3s.tools.perfdoc.server.cache.BenchmarkMockups.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Jakub Naplava
 */
public class BenchmarkSettingTest {
    @Test
    public void testEquals() {
        Assert.assertThat(benSet1, not(equalTo(benSet2)));
        Assert.assertThat(benSet2, not(equalTo(benSet3)));
        Assert.assertThat(benSet3, not(equalTo(benSet4)));
        Assert.assertThat(benSet4, not(equalTo(benSet1)));
        
        BenchmarkSettingImpl benSet5 = new BenchmarkSettingImpl(measuredMethod1, generator1, generatorArguments1, measurementQuality2);
        Assert.assertThat(benSet1, not(equalTo(benSet5)));
        
        benSet5 = new BenchmarkSettingImpl(measuredMethod2,generator1, generatorArguments1, measurementQuality1);
        Assert.assertThat(benSet1, not(equalTo(benSet5)));
        
        benSet5 = new BenchmarkSettingImpl(measuredMethod1,generator2, generatorArguments1, measurementQuality1);
        Assert.assertThat(benSet1, not(equalTo(benSet5)));
        
        benSet5 = new BenchmarkSettingImpl(measuredMethod1,generator1, generatorArguments2, measurementQuality1);
        Assert.assertThat(benSet1, not(equalTo(benSet5)));
        
        benSet5 = new BenchmarkSettingImpl(measuredMethod1,generator1, generatorArguments1, measurementQuality1);
        Assert.assertEquals(benSet1, benSet5);
    }
}
