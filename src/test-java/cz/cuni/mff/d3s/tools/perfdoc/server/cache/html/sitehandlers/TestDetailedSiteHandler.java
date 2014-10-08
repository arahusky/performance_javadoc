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
package cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.sitehandlers;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Jakub Naplava
 */
public class TestDetailedSiteHandler {

    @Test
    public void testGetRangeValue() {
        String parameters = "0_to_0,0_to_20,0_to_0";
        
        String[] paramTypeNamesString = new String[]{"Workload", "ServiceWorkload", "float", "String", "int"};
        List<String> paramTypeNames = Arrays.asList(paramTypeNamesString);
        Assert.assertEquals(-1, new DetailedSiteHandler().getRangeValue(parameters, paramTypeNames));

        paramTypeNamesString = new String[]{"Workload", "ServiceWorkload", "float", "int", "int"};
        paramTypeNames = Arrays.asList(paramTypeNamesString);
        Assert.assertEquals(1, new DetailedSiteHandler().getRangeValue(parameters, paramTypeNames));

        parameters = "0_to_0,autobus,0_to_0";
        paramTypeNamesString = new String[]{"Workload", "ServiceWorkload", "float", "int", "int"};
        paramTypeNames = Arrays.asList(paramTypeNamesString);
        
        Assert.assertEquals(-1, new DetailedSiteHandler().getRangeValue(parameters, paramTypeNames));
    }

    @Test
    public void testNormalize() {
        String parameters = "0_to_5,0_to_20,0_to_0,Ahoj";
        String[] paramTypeNamesString = new String[]{"Workload", "ServiceWorkload", "float", "int", "int", "String"};
        List<String> paramTypeNames = Arrays.asList(paramTypeNamesString);

        Assert.assertArrayEquals(null, new DetailedSiteHandler().normalizeParameters(parameters, 1, paramTypeNames));

        parameters = "0_to_0,0_to_20,0_to_0,Ahoj";

        String[] expectedRes = new String[] {"0.0", "0_to_20", "0", "Ahoj"};
        Assert.assertArrayEquals(expectedRes, new DetailedSiteHandler().normalizeParameters(parameters, 1, paramTypeNames));
        
        paramTypeNamesString = new String[]{"Workload", "ServiceWorkload", "float", "int", "double", "String"};
        paramTypeNames = Arrays.asList(paramTypeNamesString);
        expectedRes = new String[] {"0.0", "0_to_20", "0.0", "Ahoj"};
        Assert.assertArrayEquals(expectedRes, new DetailedSiteHandler().normalizeParameters(parameters, 1, paramTypeNames));
    }
}
