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

import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkResult;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkSetting;

/**
 * Interface to communicate with results in cache
 *
 * @author Jakub Naplava
 */
public interface ResultCache {

    /**
     * Returns the BenchmarkResult corresponding to the given BenchmarkSetting.
     *
     * It means, that the cache is searched for record having same methodName,
     * workloadName, workloadArguments and was measured at least as precisely as
     * we need to (measured warmupTime, warmupCycles, measurementTime,
     * measurementCycles are at least as big as our requirements saved in
     * setting).
     *
     * @param setting
     * @return the BenchmarkResult with Statistics obtained from the cache. If
     * no record with such setting is found, null is returned.
     */
    BenchmarkResult getResult(BenchmarkSetting setting);

    /**
     * Inserts the measured result in cache.
     *
     * @param benResult
     * @return true if data were inserted successfully, otherwise false
     */
    boolean insertResult(BenchmarkResult benResult);

    /**
     * Closes the current connection to database
     */
    void closeConnection();
}
