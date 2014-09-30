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
package cz.cuni.mff.d3s.tools.perfdoc.server.cache;

/**
 * Class that represents one measurement
 * 
 * @author Jakub Naplava
 */
public class MeasurementResult {

    private final String testedMethod;
    private final String generator;
    private final String data;
    private final int numberOfMeasurements;
    private final long time;
    
    public MeasurementResult(String testedMethod, String generator, String data, int numberOfMeasurements, long time) {
        this.testedMethod = testedMethod;
        this.generator = generator;
        this.data = data;
        this.numberOfMeasurements = numberOfMeasurements;
        this.time = time;
    }

    public String getTestedMethod() {
        return testedMethod;
    }

    public String getGenerator() {
        return generator;
    }

    public String getData() {
        return data;
    }

    public int getNumberOfMeasurements() {
        return numberOfMeasurements;
    }

    public long getTime() {
        return time;
    }
}
