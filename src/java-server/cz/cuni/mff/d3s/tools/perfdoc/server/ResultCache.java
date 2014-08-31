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
 * Interface to communicate with results in cache
 * @author arahusky
 */
public interface ResultCache {
    
    /**
     * Returns time that corresponds to given parameters.
     * @param methodName
     * @param generatorName
     * @param data
     * @param numberOfMeasurements
     * @return the measured time if saved in the cache, otherwise -1
     */
    long getResults(String methodName, String generatorName, String data, int numberOfMeasurements);
    
    /**
     * Inserts the data in cache
     * @param methodName
     * @param generatorName
     * @param data
     * @param numberOfMeasurements
     * @param time
     * @return true if data were inserted succesfully, otherwise false
     */
    boolean insertResult(String methodName, String generatorName, String data, int numberOfMeasurements, long time);
    
    void closeConnection();
}
