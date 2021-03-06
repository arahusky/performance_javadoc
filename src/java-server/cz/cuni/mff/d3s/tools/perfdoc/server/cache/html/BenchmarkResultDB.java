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
package cz.cuni.mff.d3s.tools.perfdoc.server.cache.html;

import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkResult;

/**
 * The interface for benchmark results saved in database. This interface is
 * except for the ID of the measurement same as the BenchmarkResult.
 *
 * @author Jakub Naplava
 */
public interface BenchmarkResultDB extends BenchmarkResult{
    
    /**
     * Gets ID, under which this measurement is saved in database.
     */
    public int getIDMeasurement();
}
