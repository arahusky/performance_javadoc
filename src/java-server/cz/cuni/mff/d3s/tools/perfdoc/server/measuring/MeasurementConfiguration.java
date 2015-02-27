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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides additional measurement information (such as max time for
 * measurement with priority one) that can be changed by changing configuration
 * file.
 *
 * When configuration file can not be found, or user specifies default values
 * (-1) , values from default measurement properties are used. Default
 * measurement properties file contains same keys with prefix 'def'.
 *
 * @author Jakub Naplava
 */
public class MeasurementConfiguration {

    private static final Logger log = Logger.getLogger(MeasurementConfiguration.class.getName());

    //path to the configuration file
    private static final String configurationFileLocation = "config/measure.properties";

    //path to the default configuration file
    private static final String defConfigurationFileLocation = "src/java-server/cz/cuni/mff/d3s/tools/perfdoc/server/measuring/resources/default_measure.properties";

    //properties containing user-measurement configuration  
    private static final Properties measurementProperties = new Properties();

    //properties containing default values  
    private static final Properties defMeasurementProperties = new Properties();

    //public variable telling other classes, how many priorities does server handle
    public static final int numberOfPriorities = 4;

    static {
        loadProperties();
    }

    /**
     * Loads property files into program
     */
    private static void loadProperties() {

        try (InputStream input = new FileInputStream(configurationFileLocation)) {
            measurementProperties.load(input);
        } catch (IOException ex) {
            log.log(Level.WARNING, "Unable to find configuration file for measurement. The default values will be used.", ex);
        }

        try (InputStream input = new FileInputStream(defConfigurationFileLocation)) {
            defMeasurementProperties.load(input);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Unable to find default measurement properties. Program will probably wont work properly.", e);
        }
    }

    /**
     * Returns the number of values, in which the measurement should be
     * performed.
     *
     * @param priority priority for which we want to get number of points
     * @return
     */
    public static int getNumberOfPoints(int priority) {
        switch (priority) {
            case 1:
                return getProperty("priorityOneNumberOfPoints");
            case 2:
                return getProperty("priorityTwoNumberOfPoints");
            case 3:
                return getProperty("priorityThreeNumberOfPoints");
            case 4:
                return getProperty("priorityFourNumberOfPoints");
            default:
                return -1;
        }
    }

    /**
     * Returns number of cycles determining maximal number of cycles used for
     * warmup measuring of one point.
     *
     * @param priority priority for which we want to get number of warmup-cycles
     * @return
     */
    public static int getNumberOfWarmupCycles(int priority) {

        switch (priority) {
            case 1:
                return getProperty("priorityOneNumberOfCyclesWarmup");
            case 2:
                return getProperty("priorityTwoNumberOfCyclesWarmup");
            case 3:
                return getProperty("priorityThreeNumberOfCyclesWarmup");
            case 4:
                return getProperty("priorityFourNumberOfCyclesWarmup");
            default:
                return -1;
        }
    }

    /**
     * Returns time, that when exceeded while warmup-measuring one point, should
     * end warmup-measuring this point and move to another.
     *
     * @param priority priority for which we want to get warmup-time
     * @return
     */
    public static int getWarmupTime(int priority) {

        switch (priority) {
            case 1:
                return getProperty("priorityOneElapsedTimeWarmup");
            case 2:
                return getProperty("priorityTwoElapsedTimeWarmup");
            case 3:
                return getProperty("priorityThreeElapsedTimeWarmup");
            case 4:
                return getProperty("priorityFourElapsedTimeWarmup");
            default:
                return -1;
        }
    }

    /**
     * Returns number of cycles determining maximal number of cycles used for
     * measuring of one point.
     *
     * @param priority priority for which we want to get number of cycles
     * @return
     */
    public static int getNumberOfMeasurementCycles(int priority) {

        switch (priority) {
            case 1:
                return getProperty("priorityOneNumberOfCyclesMeasurement");
            case 2:
                return getProperty("priorityTwoNumberOfCyclesMeasurement");
            case 3:
                return getProperty("priorityThreeNumberOfCyclesMeasurement");
            case 4:
                return getProperty("priorityFourNumberOfCyclesMeasurement");
            default:
                return -1;
        }
    }

    /**
     * Returns time, that when exceeded while measuring one point, should end
     * measuring this point and move to another.
     *
     * @param priority priority for which we want to get time
     * @return
     */
    public static int getMeasurementTime(int priority) {

        switch (priority) {
            case 1:
                return getProperty("priorityOneElapsedTimeMeasurement");
            case 2:
                return getProperty("priorityTwoElapsedTimeMeasurement");
            case 3:
                return getProperty("priorityThreeElapsedTimeMeasurement");
            case 4:
                return getProperty("priorityFourElapsedTimeMeasurement");
            default:
                return -1;
        }
    }

    private static int getProperty(String propertyName) {

        String propertyValue = measurementProperties.getProperty(propertyName);
        //if such a property does not exist or has a value of -1, we use default property value
        if (propertyValue == null || propertyValue.equals("-1")) {
            return getDefaultProperty(propertyName);
        }

        try {
            int value = Integer.parseInt(propertyValue);
            return value;
        } catch (NumberFormatException e) {
            log.log(Level.WARNING, "The measurement properties file contains non-integer value. Using default one.", e);
            return getDefaultProperty(propertyName);
        }
    }

    private static int getDefaultProperty(String propertyName) {
        String defPropertyName = "def" + propertyName;
        String defPropertyValue = defMeasurementProperties.getProperty(defPropertyName);

        try {
            int value = Integer.parseInt(defPropertyValue);
            return value;
        } catch (NumberFormatException e) {
            log.log(Level.SEVERE, "The default measurement properties file contains non-integer value.", e);
            return -1;
        }
    }
}
