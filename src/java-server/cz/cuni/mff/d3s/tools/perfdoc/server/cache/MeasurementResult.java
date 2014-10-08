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

import cz.cuni.mff.d3s.tools.perfdoc.server.MethodInfo;
import java.util.Objects;

/**
 * Class that represents one measurement
 * 
 * @author Jakub Naplava
 */
public class MeasurementResult {

    private final MethodInfo testedMethod;
    private final MethodInfo generator;
    private final String data;
    private final int numberOfMeasurements;
    private final long time;
    
    public MeasurementResult(MethodInfo testedMethod, MethodInfo generator, String data, int numberOfMeasurements, long time) {
        this.testedMethod = testedMethod;
        this.generator = generator;
        this.data = data;
        this.numberOfMeasurements = numberOfMeasurements;
        this.time = time;
    }

    public MethodInfo getTestedMethod() {
        return testedMethod;
    }

    public MethodInfo getGenerator() {
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
        
    @Override
    public boolean equals(Object o) {
        if (o == this) { 
            return true;
        }        
        if (!(o instanceof MeasurementResult)) {
            return false;
        }        
        MeasurementResult res = (MeasurementResult) o;
        return res.data.equals(data) 
                && res.generator.equals(generator) 
                && res.numberOfMeasurements == numberOfMeasurements
                && res.testedMethod.equals(testedMethod)
                && res.time == time;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.testedMethod);
        hash = 47 * hash + Objects.hashCode(this.generator);
        hash = 47 * hash + Objects.hashCode(this.data);
        hash = 47 * hash + this.numberOfMeasurements;
        hash = 47 * hash + (int) (this.time ^ (this.time >>> 32));
        return hash;
    }
}
