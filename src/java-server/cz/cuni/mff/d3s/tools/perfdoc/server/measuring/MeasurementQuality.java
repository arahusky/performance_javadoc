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

/**
 * This class binds together information about measurement quality for one
 * measure request.
 *
 * Every measure request comes with priority telling us what kind of measurement
 * to perform. Every priority has its specific measurement requirements (e.g.
 * higher priority wants more accurate results, thus longer measurementTime),
 * which are saved in this class instance.
 *
 * Informations are obtained from MeasurementConfiguration.
 *
 * @author Jakub Naplava
 */
public class MeasurementQuality {

    //priority of the request (1-4)    
    private final int priority;

    private final int warmupTime;

    private final int numberOfWarmupCycles;

    private final int measurementTime;

    private final int numberOfMeasurementsCycles;

    //TODO possibly not here
    private final int numberOfPoints;

    public MeasurementQuality(int priority) {
        this.priority = priority;
        this.warmupTime = MeasurementConfiguration.getWarmupTime(priority);
        this.numberOfWarmupCycles = MeasurementConfiguration.getNumberOfWarmupCycles(priority);
        this.measurementTime = MeasurementConfiguration.getMeasurementTime(priority);
        this.numberOfMeasurementsCycles = MeasurementConfiguration.getNumberOfMeasurementCycles(priority);
        this.numberOfPoints = MeasurementConfiguration.getNumberOfPoints(priority);
    }

    public MeasurementQuality(int priority, int warmupTime, int numberOfWarmupCycles, int measurementTime, int numberOfMeasurementsCycles, int numberOfPoints) {
        this.priority = priority;
        this.warmupTime = warmupTime;
        this.numberOfWarmupCycles = numberOfWarmupCycles;
        this.measurementTime = measurementTime;
        this.numberOfMeasurementsCycles = numberOfMeasurementsCycles;
        this.numberOfPoints = numberOfPoints;
    }

    public int getPriority() {
        return priority;
    }

    public int getWarmupTime() {
        return warmupTime;
    }

    public int getNumberOfWarmupCycles() {
        return numberOfWarmupCycles;
    }

    public int getMeasurementTime() {
        return measurementTime;
    }

    public int getNumberOfMeasurementsCycles() {
        return numberOfMeasurementsCycles;
    }

    public int getNumberOfPoints() {
        return numberOfPoints;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.priority;
        hash = 67 * hash + this.warmupTime;
        hash = 67 * hash + this.numberOfWarmupCycles;
        hash = 67 * hash + this.measurementTime;
        hash = 67 * hash + this.numberOfMeasurementsCycles;
        hash = 67 * hash + this.numberOfPoints;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MeasurementQuality other = (MeasurementQuality) obj;
        if (this.priority != other.priority) {
            return false;
        }
        if (this.warmupTime != other.warmupTime) {
            return false;
        }
        if (this.numberOfWarmupCycles != other.numberOfWarmupCycles) {
            return false;
        }
        if (this.measurementTime != other.measurementTime) {
            return false;
        }
        if (this.numberOfMeasurementsCycles != other.numberOfMeasurementsCycles) {
            return false;
        }
        
        return true;
    }
}
