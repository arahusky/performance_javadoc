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
package cz.cuni.mff.d3s.tools.perfdoc.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class, that loads measurement configuration
 *
 * @author Jakub Naplava
 */
public class MeasurementConfiguration {

    private static final Logger log = Logger.getLogger(MeasurementConfiguration.class.getName());

    static Properties prop = new Properties();

    //whether we already tried to load properties file
    private static boolean propertiesLoaded = false;

    //whether there was an error when trying to load properties and the properties file should not be loaded again
    private static boolean propertiesError = false;

    public static int returnHowManyValuesToMeasure(int priority) {
        
        if (!propertiesLoaded && !propertiesError) {
            loadProperties();
        }

        switch (priority) {
            case 1:
                return returnValues(prop.getProperty("howManyValuesOne"), priority);
            case 2:
                return returnValues(prop.getProperty("howManyValuesTwo"), priority);
            case 3:
                return returnValues(prop.getProperty("howManyValuesThree"), priority);
            case 4:
                return returnValues(prop.getProperty("howManyValuesFour"), priority);
            default:
                return -1;
        }
    }

    public static int returnHowManyTimesToMeasure(int priority) {
        
        if (!propertiesLoaded && !propertiesError) {
            loadProperties();
        }

        switch (priority) {
            case 1:
                return returnTimes(prop.getProperty("howManyTimesOne"), priority);
            case 2:
                return returnTimes(prop.getProperty("howManyTimesTwo"), priority);
            case 3:
                return returnTimes(prop.getProperty("howManyTimesThree"), priority);
            case 4:
                return returnTimes(prop.getProperty("howManyTimesFour"), priority);
            default:
                return -1;
        }
    }

    private static int returnValues(String propertyName, int priority) {
        if (propertyName == null) {
            return defaultReturnHowManyValuesToMeasure(priority);
        }

        try {
            int value = Integer.parseInt(propertyName);
            return value;
        } catch (NumberFormatException e) {
            log.log(Level.INFO, "The properties file is in a bad format.", e);
            return defaultReturnHowManyValuesToMeasure(priority);
        }
    }

    private static int returnTimes(String propertyName, int priority) {
        if (propertyName == null) {
            return defaultReturnHowManyTimesToMeasure(priority);
        }

        try {
            int value = Integer.parseInt(propertyName);
            return value;
        } catch (NumberFormatException e) {
            log.log(Level.INFO, "The properties file is in a bad format.", e);
            return defaultReturnHowManyTimesToMeasure(priority);
        }
    }

    private static void loadProperties() {
        InputStream input = null;

        try {
            input = new FileInputStream("config/measure.properties");

            prop.load(input);

        } catch (IOException ex) {
            log.log(Level.INFO, "Unable to find configuration file for measurement. The default values will be used.", ex);
            propertiesError = true;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    log.log(Level.INFO, "Unable to close file with measurement configuration properly.", e);
                }
            }

            propertiesLoaded = true;
        }
    }

    /**
     * Method, that will be used in case, that no configuration file will be
     * found.
     *
     * @param priority
     * @return
     */
    public static int defaultReturnHowManyValuesToMeasure(int priority) {
        switch (priority) {
            case 1:
                return 4;
            case 2:
                return 6;
            case 3:
                return 8;
            case 4:
                return 10;
        }

        return -1; //some value to indicate error
    }

    /**
     * Method, that will be used in case, that no configuration file will be
     * found.
     *
     * @param priority
     * @return
     */
    public static int defaultReturnHowManyTimesToMeasure(int priority) {
        switch (priority) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
        }

        return -1; //some value to indicate error
    }
}
