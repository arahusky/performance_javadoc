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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jakub Naplava
 */
public final class Statistics {

    private static final Logger log = Logger.getLogger(Statistics.class.getName());

    private List<Long> measurementResults = new ArrayList<>();    

    public Statistics() {
        log.log(Level.CONFIG, "New instance of Statistics created.");
    }

    /**
     * Creates new instance of Statistics containing values from the given
     * parameter. The parameter is in format: {value1, ..., valueN} - as stored
     * in database
     *
     * @param values
     */
    public Statistics(String values) {
        String[] items = values.substring(1, values.length() - 1).split(",");
        for (String item : items) {
            try {
                long value = Long.parseLong(item);
                addResult(value);
            } catch (NumberFormatException e) {
                log.log(Level.WARNING, "There was a not-long number passed to Statistics.");
            }
        }
    }

    public void addResult(long result) {
        measurementResults.add(result);
        
    }

    public long getMean() {        
        return (long) StatisticsUtils.getMean(measurementResults);
    }
    
    public long getMedian() {
        return (long) StatisticsUtils.getMedian(measurementResults);
    }
    
    public long getStandardDeviation() {
        return (long) StatisticsUtils.getStandardDeviation(measurementResults);
    }
    
    public long getFirstQuartile() {
        return (long) StatisticsUtils.getFirstQuartile(measurementResults);
    }
    
    public long getThirdQuartile() {
        return (long) StatisticsUtils.getThirdQuartile(measurementResults);
    }
    
    public void removeOutliers() {
        this.measurementResults = StatisticsUtils.excludeOutliers(measurementResults);
    }

    public int getNumberOfMeasurements() {
        return measurementResults.size();
    }

    public boolean isEmpty() {
        return measurementResults.isEmpty();
    }

    /**
     * Returns all values from the Statistics.
     *
     * @return
     */
    public Long[] getValues() {
        return measurementResults.toArray(new Long[measurementResults.size()]);
    }
    

    /**
     * *
     * @return String representation of Statistic results in format: {result1,
     * ..., resultN}
     */
    @Override
    public String toString() {
        if (measurementResults.isEmpty()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(measurementResults.get(0));

        for (int i = 1; i < measurementResults.size(); i++) {
            sb.append(",").append(measurementResults.get(i));
        }

        sb.append("}");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.measurementResults);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Statistics)) {
            return false;
        }
        Statistics s = (Statistics) o;
        return s.measurementResults.equals(this.measurementResults);
    }
}
