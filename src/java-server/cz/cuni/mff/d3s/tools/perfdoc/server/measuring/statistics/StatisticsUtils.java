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
import java.util.Collections;
import java.util.List;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * This class provides basic statistics methods which are mainly used by
 * Statistics class.
 *
 * @author Jakub Naplava
 */
public class StatisticsUtils {

    public static double getMean(List<Long> values) {
        if (values.isEmpty()) {
            return -1;
        }
        
        DescriptiveStatistics ds = getDescriptiveStatisticsFromList(values);
        
        return ds.getMean();
    }

    public static double getMedian(List<Long> values) {
        if (values.isEmpty()) {
            return -1;
        }
        
        DescriptiveStatistics ds = getDescriptiveStatisticsFromList(values);
        
        return ds.getPercentile(50);
    }
    
    private static DescriptiveStatistics getDescriptiveStatisticsFromList(List<Long> values) {
        DescriptiveStatistics ds = new DescriptiveStatistics();
        
        for (long l : values) {
            ds.addValue(l);
        }
        
        return ds;
    }

    public static double getStandardDeviation(List<Long> values) {
        if (values.isEmpty()) {
            return -1;
        }
        
        DescriptiveStatistics ds = getDescriptiveStatisticsFromList(values);
        
        return ds.getStandardDeviation();
    }

    /**
     * Returns first quartile of an array, which is a value from the array,
     * under which (together with this number) 25% of all number lies.
     *
     * Supposes 'values' to be sorted.
     *
     * @param values
     * @return
     */
    public static double getFirstQuartile(List<Long> values) {
        if (values.isEmpty()) {
            return -1;
        }
        
        DescriptiveStatistics ds = getDescriptiveStatisticsFromList(values);
        
        return ds.getPercentile(25);
    }

    /**
     * Returns first quartile of an array, which is a value from the array,
     * above which (together with this number) 25% of all number lies.
     *
     * Supposes 'values' to be sorted.
     *
     * @param values
     * @return
     */
    public static double getThirdQuartile(List<Long> values) {
        if (values.isEmpty()) {
            return -1;
        }
        
        DescriptiveStatistics ds = getDescriptiveStatisticsFromList(values);
        
        return ds.getPercentile(75);
    }

    /**
     * Returns new list that comes from 'values' by removing outliers (values
     * that seem to be corrupted).
     *
     * @param values
     * @return
     */
    public static List<Long> excludeOutliers(List<Long> values) {

        List<Long> newList = new ArrayList<>(values);
        Collections.sort(newList);

        int outliersIndex = getSuspicious(newList);
        if (outliersIndex == -1) {
            return newList;
        }

        int numberOfOutliers = values.size() - outliersIndex;
        if (numberOfOutliers <= ((5D / 100) * values.size())) {
            return newList.subList(0, outliersIndex);
        }

        return newList;
    }

    /**
     * Returns suspicious index, from which the measurements that seems to be
     * corrupted start.
     *
     * According to statistics, major outliers our value that do not belong to
     * an interval of [Q1 - 1,5 * IQR; Q3 + 1,5*IQR], where Q1 and Q3 is first,
     * respectively third quartile and IQR is interquantile range (Q3 - Q1).
     *
     * However in our case, we do not want to exclude values from the lower
     * interval, but only results from higher interval (measurement can be only
     * too long, not too short).
     *
     * @param values <b>sorted list</b> from in which the suspicious items will
     * be searched
     * @return
     */
    private static int getSuspicious(List<Long> values) {
        double firstQuartile = getFirstQuartile(values);
        double thirdQuartile = getThirdQuartile(values);

        double iqr = thirdQuartile - firstQuartile;

        double outerFence = thirdQuartile + 3 * iqr;

        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) > outerFence) {
                return i;
            }
        }

        //some value to indicate that an array does not contain any suspicious values
        return -1;
    }

}
