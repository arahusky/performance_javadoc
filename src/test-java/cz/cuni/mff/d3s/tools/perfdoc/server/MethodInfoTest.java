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
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Jakub Naplava
 */
public class MethodInfoTest {
    
    @Test
    public void testSimpleGetters1()
    {
        String methodJSON = "package1#class1#method1#@param1#0";
        
        MethodInfo mi = new MethodInfo(methodJSON);
        
        Assert.assertEquals("package1.class1", mi.getQualifiedClassName());
        Assert.assertEquals("method1", mi.getMethodName());
        
        ArrayList<String> expectedParams = new ArrayList<>();
        expectedParams.add("param1");        
        Assert.assertEquals(expectedParams, mi.getParams());
        
        Assert.assertEquals("package1.class1#method1#@param1", mi.toString());
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
    public void testIlegalArg1() {
        //too many hashes
        String methodJSON = "example001#MyListGenerator#prepareDataBad#@cz.cuni.mff.d3s.tools.perfdoc.workloads.Wo" +
"rkload@cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload@int#1#2";
        
        new MethodInfo(methodJSON);
    }
    
    @Test
    public void testEqualsSimple() {
        String methodJSON = "example001#MyArrayList#contains#@java.lang.Object#0";        
        MethodInfo mi = new MethodInfo(methodJSON);
        
        Assert.assertThat(mi, not(equalTo(new Object())));
        
        String almostSameJSON = "example001#MyArrayList#contains1#@java.lang.Object#0";  
        Assert.assertThat(mi, not(equalTo(new MethodInfo(almostSameJSON))));
                
        Assert.assertEquals(mi, new MethodInfo(methodJSON));
    }
    
    @Test
    public void testEqualsMore() {
        String moreParamsString = "example001#MyArrayList#contains#@java.lang.Object@int@String";     
        String anotherMoreParamsString = "example001#MyArrayList#contains#@java.lang.Object@int@float";   
        MethodInfo mi = new MethodInfo(moreParamsString);
        
        Assert.assertThat(mi, not(equalTo(new Object())));
        Assert.assertThat(mi, not(equalTo(new MethodInfo(anotherMoreParamsString))));
        Assert.assertEquals(mi, new MethodInfo(moreParamsString));
        
        String almostSameJSON = "example001#MyArrayList#contains1#@java.lang.Object#0";  
        Assert.assertThat(mi, not(equalTo(new MethodInfo(almostSameJSON)))); 
        
        String another = "example002#MyArrayList#contains#@java.lang.Object@int@String";        
        MethodInfo miNoParams = new MethodInfo(another); 
        Assert.assertThat(mi, not(equalTo(miNoParams)));
        Assert.assertEquals(miNoParams, new MethodInfo(another));
    }
    
    @Test
    public void testGetParamsNullParams() {
        String method = "example001#MyArrayList#contains#";   
        MethodInfo mi = new MethodInfo(method);
        Assert.assertArrayEquals(new String[0], mi.getParams().toArray());
    }
    
    @Test
    public void testGetParams() {
        String method = "example001#MyArrayList#contains#@java.lang.Object@int@float";   
        MethodInfo mi = new MethodInfo(method);
        
        Assert.assertArrayEquals(new String[]{"java.lang.Object","int","float"}, mi.getParams().toArray());
    }
}
