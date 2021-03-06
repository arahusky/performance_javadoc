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

import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.exception.PropertiesBadFormatException;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Jakub Naplava
 */
public class MeasurementConfigurationTest {
    
    /**
     * Tests, whether all measure properties are properly defined.
     */
    @Test
    public void testWhetherEveryPropertyIsCorrectlyDefined() throws PropertiesBadFormatException {
        
        int numberOfPriorities = MeasurementConfiguration.numberOfPriorities;
        
        for (int i = 1; i<=numberOfPriorities; i++) {
            int t = MeasurementConfiguration.getMeasurementTime(i);
            Assert.assertNotSame(-1, t);
        }
        
        for (int i = 1; i<=numberOfPriorities; i++) {
            int t = MeasurementConfiguration.getNumberOfMeasurementMeasurements(i);
            Assert.assertNotSame(-1, t);
        }
        
        for (int i = 1; i<=numberOfPriorities; i++) {
            int t = MeasurementConfiguration.getNumberOfPoints(i);
            Assert.assertNotSame(-1, t);
        }
        
        for (int i = 1; i<=numberOfPriorities; i++) {
            int t = MeasurementConfiguration.getNumberOfWarmupMeasurements(i);
            Assert.assertNotSame(-1, t);
        }
        
        for (int i = 1; i<numberOfPriorities; i++) {
            int t = MeasurementConfiguration.getWarmupTime(i);
            Assert.assertNotSame(-1, t);
        }
    }
}
