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
package cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics;

/**
 * This class does not contain measured results, but just their counted
 * characteristics.
 *
 * @author Jakub Naplava
 */
public class BasicStatistics implements Statistics {

    private final long mean;
    private final long median;
    private final long deviation;
    private final long firstQuartile;
    private final long thirdQuartile;
    
    public BasicStatistics(long mean, long median, long deviation, long firstQuartile, long thirdQuartile) {
        this.mean = mean;
        this.median = median;
        this.deviation = deviation;
        this.firstQuartile = firstQuartile;
        this.thirdQuartile = thirdQuartile;
    }
    
    @Override
    public long getMean() {
        return mean;
    }

    @Override
    public long getMedian() {
        return median;
    }

    @Override
    public long getStandardDeviation() {
        return deviation;
    }

    @Override
    public long getFirstQuartile() {
        return firstQuartile;
    }

    @Override
    public long getThirdQuartile() {
        return thirdQuartile;
    }
}
