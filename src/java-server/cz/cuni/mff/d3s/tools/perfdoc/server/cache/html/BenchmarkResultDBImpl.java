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

import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkResultImpl;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkSetting;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.Statistics;

/**
 *
 * @author Jakub Naplava
 */
public class BenchmarkResultDBImpl extends BenchmarkResultImpl implements BenchmarkResultDB{

    private final int id;
    
    public BenchmarkResultDBImpl(Statistics statistics, BenchmarkSetting benchmarkSetting, int id) {
        super(statistics, benchmarkSetting);
        this.id = id;
    }

    @Override
    public int getIDMeasurement() {
        return id;
    }
}
