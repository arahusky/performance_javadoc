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

package cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jakub Naplava
 */
public class Statistics {
    
    private static final Logger log = Logger.getLogger(Statistics.class.getName());
    
    private final Method testedMethod;
    private final Object[] parameters;
    
    private final ArrayList<Long> measurementResults = new ArrayList<>();
    
    public Statistics(Method testedMethod, Object[] parameters) {
        if (testedMethod == null) {
            throw new IllegalArgumentException("Statistics: Tested method can not be null.");
        } 
        
        if (parameters == null) {
            throw new IllegalArgumentException("Statistics: Parameters can not be null.");
        }
        
        this.testedMethod = testedMethod;
        this.parameters = parameters;
        log.log(Level.CONFIG, "New instance of Statistics created for testedMethod: {0}", testedMethod.getName());
    }
    
    public void addResult(long result) {
        measurementResults.add(result);
    }
    
    public long compute() {
        long totalTime = 0;
        
        for (long res : measurementResults) {
            totalTime += res;
        }
        
        return totalTime/measurementResults.size();
    }
}
