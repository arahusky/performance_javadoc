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

/**
 *
 * @author Jakub Naplava
 */
public class MeasurementConfiguration {
    public static int returnHowManyValuesToMeasure(int priority)
    {
        switch(priority)
        {
            case 1: return 4;
            case 2: return 6;
            case 3: return 8;
            case 4: return 10;
        }
        
        return -1; //some value to indicate error
    }
    
    public static int returnHowManyTimesToMeasure(int priority)
    {
        switch(priority)
        {
            case 1: return 1;
            case 2: return 1;
            case 3: return 1;
            case 4: return 1;
        }
        
        return -1; //some value to indicate error
    }
}
