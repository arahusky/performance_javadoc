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

import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author arahusky
 */
public class TestSiteHandlers {
    
    @Test
    public void testGetRangeValue()
    {
        String parameters = "0_to_0,0_to_20,0_to_0";
        String[] paramTypeNames = new String[] {"Workload", "ServiceWorkload", "float", "String", "int"};
        
        Assert.assertEquals(-1, new DetailedSiteHandler().getRangeValue(parameters, paramTypeNames));
        
        
        paramTypeNames = new String[] {"Workload", "ServiceWorkload","float", "int", "int"};
        Assert.assertEquals(1, new DetailedSiteHandler().getRangeValue(parameters, paramTypeNames));
        
        parameters = "0_to_0,autobus,0_to_0";
        paramTypeNames = new String[] {"Workload", "ServiceWorkload", "float", "int", "int"};
        Assert.assertEquals(-1, new DetailedSiteHandler().getRangeValue(parameters, paramTypeNames));
    }
}
