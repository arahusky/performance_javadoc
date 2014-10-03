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

import cz.cuni.mff.d3s.tools.perfdoc.server.MethodInfo;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Jakub Naplava
 */
public class TestAbstractSiteHandler {

    @Test
    public void testGetMethodFromQuery1() {
        //implementation does not matter
        AbstractSiteHandler ash = new ClassSiteHandler();
        
        MethodInfo mi = ash.getMethodFromQuery("example002.SimpleWaiting&simpleWait&int&int");
        
        Assert.assertEquals("example002.SimpleWaiting", mi.getQualifiedClassName());
        Assert.assertEquals("simpleWait", mi.getMethodName());
        
        ArrayList<String> params = new ArrayList<>();
        params.add("int");
        params.add("int");
        
        Assert.assertEquals(params, mi.getParams());
    }
    
    @Test
    public void testGetMethodFromQuery2() {
        //implementation does not matter
        AbstractSiteHandler ash = new ClassSiteHandler();
        
        MethodInfo mi = ash.getMethodFromQuery("example001.MyArrayList&contains&java.lang.Object");
        
        Assert.assertEquals("example001.MyArrayList", mi.getQualifiedClassName());
        Assert.assertEquals("contains", mi.getMethodName());
        
        ArrayList<String> params = new ArrayList<>();
        params.add("java.lang.Object");
        
        Assert.assertEquals(params, mi.getParams());
    }
    
    @Test
    public void testGetParameterInfo() {
        AbstractSiteHandler ash = new DetailedSiteHandler();
        ArrayList<String> params = new ArrayList<>();

        Assert.assertEquals("", ash.chainParameters(params));
        
        params.add("int");
        Assert.assertEquals("int", ash.chainParameters(params));
        
        params.add("String");
        params.add("float");
        Assert.assertEquals("int,String,float", ash.chainParameters(params));        
    }
}
