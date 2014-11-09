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

import cz.cuni.mff.d3s.tools.perfdoc.server.MethodInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.Statistics;
import java.util.Objects;

/**
 *
 * @author Jakub Naplava
 */
public class BenchmarkResultImpl implements BenchmarkResult {

    private final Statistics statistics;
    private final BenchmarkSetting benchmarkSetting;

    public BenchmarkResultImpl(Statistics statistics, BenchmarkSetting benchmarkSetting) {
        this.statistics = statistics;
        this.benchmarkSetting = benchmarkSetting;
    }
    
    @Override
    public BenchmarkSetting getBenchmarkSetting() {
        return benchmarkSetting;
    }

    @Override
    public Statistics getStatistics() {
        return statistics;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof BenchmarkResultImpl)) {
            return false;
        }
        BenchmarkResultImpl br = (BenchmarkResultImpl) o;
        
        return br.benchmarkSetting.equals(this.benchmarkSetting) &&
                br.statistics.equals(this.statistics);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.statistics);
        hash = 29 * hash + Objects.hashCode(this.benchmarkSetting);
        return hash;
    }
}
