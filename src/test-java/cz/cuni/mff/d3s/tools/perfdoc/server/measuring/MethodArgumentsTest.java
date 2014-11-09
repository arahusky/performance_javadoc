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

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Jakub Naplava
 */
public class MethodArgumentsTest {
        
    @Test
    public void testEmptyArguments() {
        MethodArguments met = new MethodArgumentsImpl(new Object[0]);
        
        Assert.assertArrayEquals(new Object[0], met.getValues());
        Assert.assertEquals("[]", met.getValuesDBFormat(false));
    }
    
    @Test
    public void testOneArgumentNoOmit() {
        MethodArguments met = new MethodArgumentsImpl(new Object[] {1});
        
        Assert.assertArrayEquals(new Object[] {1}, met.getValues());
        Assert.assertEquals("[1]", met.getValuesDBFormat(false));
    }
    
    @Test
    public void testTwoArgumentOmit() {
        MethodArguments met = new MethodArgumentsImpl(new Object[] {1,2});
        
        Assert.assertArrayEquals(new Object[] {1,2}, met.getValues());
        Assert.assertEquals("[]", met.getValuesDBFormat(true));
    }
    
    @Test
    public void testMoreArgumentOmit() {
        MethodArguments met = new MethodArgumentsImpl(new Object[] {1,2,3,"jahoda"});
        
        Assert.assertArrayEquals(new Object[] {1,2,3,"jahoda"}, met.getValues());
        Assert.assertEquals("[3,jahoda]", met.getValuesDBFormat(true));
    }
    
    @Test
    public void testMoreArgumentNoOmit() {
        MethodArguments met = new MethodArgumentsImpl(new Object[] {1,2,3,"jahoda"});
        
        Assert.assertArrayEquals(new Object[] {1,2,3,"jahoda"}, met.getValues());
        Assert.assertEquals("[1,2,3,jahoda]", met.getValuesDBFormat(false));
    }
    
    @Test
    public void testConstructorFromDB() {
        MethodArguments met = new MethodArgumentsImpl("[64868,1,1,1,1]");
        
        Assert.assertArrayEquals(new Object[] {"64868", "1", "1", "1", "1"}, met.getValues());
        
        met = new MethodArgumentsImpl("64868,1,1,1");
        
        Assert.assertArrayEquals(new Object[] {"64868", "1", "1", "1"}, met.getValues());
    }
    
    @Test
    public void testEquals() {
        MethodArguments met0 = new MethodArgumentsImpl(new Object[] {1,2,3,4,5});
        MethodArguments met1 = new MethodArgumentsImpl(new Object[] {1,3,4.0,5});
        MethodArguments met2 = new MethodArgumentsImpl(new Object[] {1,2,3,4,5});
        MethodArguments met3 = new MethodArgumentsImpl("[1,2,3,4,5]");        
        MethodArguments met4 = new MethodArgumentsImpl("[1,2,3,4,5]");
        MethodArguments met5 = new MethodArgumentsImpl("[1,3,4.0,5]");
        
        Assert.assertEquals(met0, met2);
        Assert.assertFalse(met0.equals(met1));
        
        Assert.assertFalse(met0.equals(met3));
        Assert.assertFalse(met1.equals(met4));
        
        Assert.assertEquals(met3, met4);
        Assert.assertFalse(met3.equals(met5));
    }
}
