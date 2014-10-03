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

package cz.cuni.mff.d3s.tools.perfdoc.server;

import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Jakub Naplava
 */
public class TestMethodInfo {
    
    @Test
    public void testSimpleGetters1()
    {
        String methodJSON = "example001#MyArrayList#contains#@java.lang.Object#0";
        
        MethodInfo mi = new MethodInfo(methodJSON);
        
        Assert.assertEquals("example001.MyArrayList", mi.getQualifiedClassName());
        Assert.assertEquals("contains", mi.getMethodName());
        
        ArrayList<String> expectedParams = new ArrayList<>();
        expectedParams.add("java.lang.Object");        
        Assert.assertEquals(expectedParams, mi.getParams());
        
        Assert.assertEquals("example001.MyArrayList#contains#@java.lang.Object", mi.toString());
    }
    
    @Test
    public void testSimpleGetters2() {
        String methodJSON = "example001#MyListGenerator#prepareDataBad#@cz.cuni.mff.d3s.tools.perfdoc.workloads.Wo" +
"rkload@cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload@int#0";
        
        MethodInfo mi = new MethodInfo(methodJSON);
        
        Assert.assertEquals("example001.MyListGenerator", mi.getQualifiedClassName());
        Assert.assertEquals("prepareDataBad", mi.getMethodName());
        
        ArrayList<String> expectedParams = new ArrayList<>();
        expectedParams.add("cz.cuni.mff.d3s.tools.perfdoc.workloads.Workload");
        expectedParams.add("cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload");
        expectedParams.add("int");        
        Assert.assertEquals(expectedParams, mi.getParams());
        
        Assert.assertEquals("example001.MyListGenerator#prepareDataBad#@cz.cuni.mff.d3s.tools.perfdoc.workloads.Wo" +
"rkload@cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload@int", mi.toString());
    }
    
    @Test(expected= IllegalArgumentException.class)
    public void testBadFormat1() {
        //given String does not contain last hash with number
        String methodJSON = "example001#MyListGenerator#prepareDataBad#@cz.cuni.mff.d3s.tools.perfdoc.workloads.Wo" +
"rkload@cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload@int";
        
        new MethodInfo(methodJSON);
    }
    
    @Test(expected= IllegalArgumentException.class)
    public void testIlegalArg1() {
        //too many hashes
        String methodJSON = "example001#MyListGenerator#prepareDataBad#@cz.cuni.mff.d3s.tools.perfdoc.workloads.Wo" +
"rkload@cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload@int#1#2";
        
        new MethodInfo(methodJSON);
    }
}
