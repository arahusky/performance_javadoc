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
package cz.cuni.mff.d3s.tools.perfdoc.workloads;

/**
 * Interface to prepare measured method arguments and its class instance.
 *
 * @author Jakub Naplava
 */
public interface Workload {

    /**
     * Creates new tuple containing the object, on which the measurement will be
     * called, and the arguments, for the measured method.
     *
     * @param obj Instance of object, on which the measurement will be
     * performed.
     * @param args Arguments for the measured method.
     */
    public void addCall(Object obj, Object... args);

    /**
     * Set class containing code to be run after measurement and after
     * benchmark.
     *
     * There may be one method having annotation AfterMeasurement, which means
     * that its code will be run just after running one measurement (= one code
     * set by addCall method).
     *
     * There may be one method having annotation AfterBenchmark, which means
     * that its code will be run after running all "calls", which are set by
     * addCall method.
     *
     * When there are multiple methods having same annotation, the last of them
     * is called.
     *
     * @param obj instance of class, which contains specified methods.
     */
    public void setHooks(Object obj);
}
