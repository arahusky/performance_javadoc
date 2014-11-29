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

import cz.cuni.mff.d3s.tools.perfdoc.server.MethodInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.ResultCache;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkResult;
import java.util.Collection;

/**
 * Interface, that extends ResultCache with methods for site handlers
 *
 * @author Jakub Naplava
 */
public interface ResultCacheForWeb extends ResultCache {

    /**
     * Returns faked BenchmarkResults (with the Statistics containing only
     * computed results) obtained from the main table. .
     *
     * @return
     */
    Collection<BenchmarkResult> getMainTableResults();

    Collection<Object[]> getDetailedTableResults();

    /**
     * Returns faked BenchmarkResults (with the Statistics containing only
     * computed results) for given testedMethod and generator
     *
     * @param testedMethod
     * @param generator
     * @return BenchmarkResult if found, otherwise null
     */
    Collection<BenchmarkResult> getResults(MethodInfo testedMethod, MethodInfo generator);

    /**
     * Returns all methods, that have already been tested and have some result
     * in cache
     *
     * @return ArrayList containing the names of searched methods; if there was
     * an error, then null
     */
    Collection<MethodInfo> getDistinctTestedMethods();

    /**
     * Returns all method, that are in specified class and have some result in
     * cache
     *
     * @param className (format: package.className)
     * @return ArrayList containing the names of searched methods; if there was
     * an error, then null
     */
    Collection<MethodInfo> getDistinctClassMethods(String className);

    /**
     * Returns all generators for given methodName, that have some result in
     * cache
     *
     * @param methodName the Method for which the generator is declared
     * @return ArrayList containing the names of searched generators; if there
     * was an error, then null
     */
    Collection<MethodInfo> getDistinctGenerators(MethodInfo methodName);
}
