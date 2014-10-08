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

import cz.cuni.mff.d3s.tools.perfdoc.server.MethodInfo;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Jakub Naplava
 */
public class TestMeasurementResult {
    
    @Test
    public void testEquals() {
        String testedMethod = "example001#MyArrayList#contains#@java.lang.Object";
        String generator = "example001#MyArrayList#generator#@java.lang.Object";
        MeasurementResult res = new MeasurementResult(new MethodInfo(testedMethod), new MethodInfo(generator), "data", 5, 4);
        MeasurementResult swappedRes = new MeasurementResult(new MethodInfo(generator), new MethodInfo(testedMethod), "data", 5, 4);
        
        Assert.assertFalse(res.equals(new Object()));
        Assert.assertFalse(res.equals(swappedRes));
        Assert.assertTrue(res.equals(new MeasurementResult(new MethodInfo(testedMethod), new MethodInfo(generator), "data", 5, 4)));
        
    }
}
