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

import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.Statistics;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum;
import cz.cuni.mff.d3s.tools.perfdoc.server.LockBase;
import cz.cuni.mff.d3s.tools.perfdoc.server.MethodReflectionInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.ResultCache;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.ResultDatabaseCache;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkloadImpl;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.Workload;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.WorkloadImpl;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Class that parses an incoming JSON request and measures the time duration
 *
 * @author Jakub Naplava
 */
public class MethodMeasurer {

    private MethodReflectionInfo testedMethod;
    private MethodReflectionInfo generator;

    //which generator parameter is the range parameter
    private int rangeValue;

    private int priority;

    //user identifier
    public String hash;

    private ArrayList<Object> data = new ArrayList<>();

    private ResultCache resultCache;
    private LockBase lockBase;

    private static final Logger log = Logger.getLogger(MethodMeasurer.class.getName());

    /**
     * Creates new instance of MethodMeasurer.
     *
     * @param data JSON request
     * @param lockBase instance of LockBase
     */
    public MethodMeasurer(String data, LockBase lockBase) throws ClassNotFoundException, MalformedURLException, IOException, SQLException {
        JSONParser parser = new JSONParser();
        parser.parseData(data);

        this.resultCache = new ResultDatabaseCache();
        this.lockBase = lockBase;
    }

    MethodMeasurer() {
        //for testing purposes only
    }

    /**
     * measures the time duration of the method for given data and rangeValue
     *
     * @return JSONObject containing measured values with their time durations
     * and units, in which the values are returned
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public JSONObject measureTime() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        WorkloadImpl workloadImpl = new WorkloadImpl();
        ServiceWorkloadImpl serviceImpl = new ServiceWorkloadImpl();

        //passing the priority to generator
        serviceImpl.setPriority(priority);

        //list to store measured results
        ArrayList<Object[]> result = new ArrayList<>();

        Method method = testedMethod.getMethod();
        Method generatorMethod = generator.getMethod();
        Class<?> generatorClass = generator.getContainingClass();

        //values chosen from rangeValue to measure data in
        double[] valuesToMeasure = getValuesToMeasure(MeasurementConfiguration.returnHowManyValuesToMeasure(priority));

        //how many times to measure the method in one cycle
        int howManyTimesToMeasure = MeasurementConfiguration.returnHowManyTimesToMeasure(priority);

        //passing the amount of wanted results to generator
        serviceImpl.setNumberCalls(howManyTimesToMeasure);

        Statistics statistics;

        //wait until we can measure (there is no lock for our hash)
        lockBase.waitUntilFree(hash);

        for (int i = 0; i < valuesToMeasure.length; i++) {

            //the arguments for the generator 
            Object[] args = prepareArgsToCall(valuesToMeasure[i], workloadImpl, serviceImpl);
            statistics = new Statistics(testedMethod.getMethod(), args);

            //check, whether we do not have already data cached
            String dataCache = prepareDataForCache(args);
            long res = resultCache.getResult(testedMethod.toString(), generator.toString(), dataCache, howManyTimesToMeasure);
            if (res != -1) {
                result.add(new Object[]{valuesToMeasure[i], res});
                log.log(Level.CONFIG, "The value for measuring was found in cache.");
                continue;
            }

            String msg = "Starting to measure..." + ", tested Method:{0}" + testedMethod.getMethod().getName()
                    + "generator:{0}" + generator.getMethod().getName() + "class Generator:{0}" + generator.getContainingClass().getName();
            log.log(Level.CONFIG, msg);

            try {
                //invoking generator in order to prepare the calls of tested method
                generatorMethod.invoke(generatorClass.newInstance(), args);

                Object[] objs;

                //the generator prepared us the arguments for tested method in workloadImpl
                //we just get one and measure the tested method with it
                while ((objs = workloadImpl.getCall()) != null) {
                    Object[] o = (Object[]) objs[1];

                    long before = System.nanoTime();
                    method.invoke(objs[0], o);
                    long after = System.nanoTime();

                    statistics.addResult(after - before);
                }
                long countedResult = statistics.compute();
                result.add(new Object[]{valuesToMeasure[i], countedResult});

                resultCache.insertResult(testedMethod.toString(), generator.toString(), dataCache, howManyTimesToMeasure, countedResult);
            } catch (IllegalAccessException ex) {
                log.log(Level.SEVERE, "An IllegalAccessException occured", ex);
                lockBase.freeLock(hash);
                throw ex;
            } catch (IllegalArgumentException ex) {
                log.log(Level.SEVERE, "Some bad arguments were passed to tested/generator method", ex);
                lockBase.freeLock(hash);
                throw ex;
            } catch (InstantiationException ex) {
                log.log(Level.SEVERE, "Could not istantiate generator class", ex);
                lockBase.freeLock(hash);
                throw ex;
            } catch (InvocationTargetException ex) {
                log.log(Level.SEVERE, "An InvocationTargetException occured when trying to invoke generator/tested method", ex);
                lockBase.freeLock(hash);
                throw ex;
            }
        }

        log.log(Level.CONFIG, "Measurement succesfully done");
        lockBase.freeLock(hash);

        String units = convertUnitsIfNeeded(result);

        //create new JSONObject containing measured results
        JSONObject jsonResults = new JSONObject();
        for (int i = 0; i < result.size(); i++) {
            jsonResults.accumulate("data", result.get(i));
        }

        jsonResults.accumulate("units", units);

        if (resultCache != null) {
            //we do not need the connection to database anymore
            resultCache.closeConnection();
        }
        return jsonResults;
    }

    String convertUnitsIfNeeded(ArrayList<Object[]> list) {
        //supported units (may be added more)
        String[] units = new String[]{"s", "ms", "µs", "ns"};
        //pointer to units array showing actual unit
        int index = 3;

        long min = Long.MAX_VALUE;

        for (Object[] obj : list) {
            long value = (long) obj[1];

            if (value < min) {
                min = value;
            }
        }

        //10,000 was chosen constant so that the minValue is not bigger than it
        while (min > 10000 && (index > 0)) {
            index--;
            min = min / 1000;
        }

        int divideBy = 1;

        for (int i = index; i < units.length - 1; i++) {
            divideBy *= 1000;
        }

        for (int i = 0; i < list.size(); i++) {
            Object[] o = list.get(i);
            long val = ((long) o[1]) / divideBy;
            o[1] = val;
            list.set(i, o);
        }

        return units[index];
    }

    /**
     * Chooses the right data from rangeValue which will be passed to generator,
     * the data are chosen to divide the interval to the very same pieces
     *
     * @param howMany how many data will be chosen
     * @return the double array containing chosen values
     */
    double[] getValuesToMeasure(int howMany) {
        //the endpoints will always be contained in the values 
        String[] oarr = ((String) data.get(rangeValue)).split(" to ");
        double min = Double.parseDouble(oarr[0]);
        double max = Double.parseDouble(oarr[1]);

        //the distance of two measured values
        double step = findStepValue();

        //calling method to get values between min and max
        double[] otherVals = findOtherValues(step, min, max, howMany - 2);
        double[] values = new double[otherVals.length + 2];

        values[0] = min;
        values[values.length - 1] = max;

        for (int i = 1; i < values.length - 1; i++) {
            values[i] = otherVals[i - 1];
        }

        return values;
    }

    /**
     * Returns how many numbers, that are made by adding step to min, are bigger
     * than min and smaller than max
     *
     * @param min
     * @param max
     * @param step
     * @return total number of such values
     */
    int returnHowManyInInterval(double min, double max, double step) {
        int number = 0;

        while ((min + step * (number + 1)) < max) {
            number++;
        }

        return number;
    }

    /**
     * Method that helps method getValuesToMeasure to find the right data in
     * interval
     *
     * @param step
     * @param minVal
     * @param maxVal
     * @param howMany
     * @return
     */
    double[] findOtherValues(double step, double minVal, double maxVal, int howMany) {
        int howManyIsPossible = returnHowManyInInterval(minVal, maxVal, step);

        //if there is not enough point in the interval, we add as many as we can
        if (howManyIsPossible < howMany) {
            howMany = howManyIsPossible;
        }

        if (howMany < 1) {
            return new double[0];
        }

        double distance = maxVal - minVal;

        //how many units are between the endpoints
        double numberOfPossibleSteps = Math.floor(distance / step);

        //the step to be used
        double myStep;
        if (numberOfPossibleSteps == howMany) {
            myStep = step;
        } else {
            double possibleStep = (numberOfPossibleSteps / (howMany + 1)) * step;
            //the step must be normalized = we need to find the highest smaller (or equal) value that can be reached be adding the step to the min
            myStep = findNearestSmallerPossibleValue(possibleStep, minVal, step);
        }

        double[] arr = new double[howMany];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (minVal + ((i + 1) * myStep));
        }
        return arr;
    }

    /**
     * Finds the highest number smaller than the value that can be achieved by
     * adding step to min
     */
    double findNearestSmallerPossibleValue(double value, double min, double step) {
        double actualValue = 0;

        while (actualValue + step <= value) {
            actualValue += step;
        }

        return actualValue;
    }

    /**
     * Finds the step value in the generator for given rangeValue
     */
    private double findStepValue() {
        //first two parameters are workload and serviceWorkload
        int numInParams = rangeValue + 2;
        Annotation[][] params = generator.getMethod().getParameterAnnotations();

        Annotation[] annotations = params[numInParams];

        for (Annotation a : annotations) {
            if ("cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum".equals(a.annotationType().getName())) {
                return ((ParamNum) a).step();
            }
        }

        return -1; //some value to indicate non-succes
    }

    /**
     * Creates object[] to pass to .invoke method of generator
     */
    private Object[] prepareArgsToCall(double rangeVal, Workload workload, ServiceWorkload serviceWorkload) {
        Object[] args = new Object[data.size() + 2];
        args[0] = workload;
        args[1] = serviceWorkload;

        for (int i = 0; i < data.size(); i++) {
            args[i + 2] = data.get(i);
        }

        //all values have already good type (method normalize in JSONParser) except for the range value
        String parameter = getGenParameterName(rangeValue);
        switch (parameter) {
            case "int":
                args[rangeValue + 2] = (int) rangeVal;
                break;
            case "float":
                args[rangeValue + 2] = (float) rangeVal;
                break;
            case "double":
                args[rangeValue + 2] = rangeVal;
                break;
        }

        return args;
    }

    private String getGenParameterName(int i) {
        return generator.getParams().get(i + 2);
    }

    public String prepareDataForCache(Object[] obj) {
        StringBuilder sb = new StringBuilder();

        for (int i = 2; i < obj.length; i++) {
            sb.append(obj[i] + ";");
        }

        return sb.toString();
    }

    /**
     * private class that parses incoming JSON and the result saves in the
     * MethodMeasure instance
     */
    private class JSONParser {

        /**
         * Parses data and the result saves in the MethodMeasurer variables
         *
         * @param parseData
         * @throws ClassNotFoundException when tested method or generator method
         * were not found
         * @throws MalformedURLException when files in which to search the files
         * are in a bad format
         */
        private void parseData(String parseData) throws ClassNotFoundException, IOException {
            JSONObject obj = new JSONObject(parseData);

            String methodName = obj.getString("testedMethod");
            String generatorName = obj.getString("generator");

            testedMethod = new MethodReflectionInfo(methodName);
            generator = new MethodReflectionInfo(generatorName);

            rangeValue = obj.getInt("rangeValue");
            priority = obj.getInt("priority");
            hash = obj.getString("id");

            JSONArray dataArray = obj.getJSONArray("data");

            for (int i = 0; i < dataArray.length(); i++) {
                data.add(dataArray.get(i));
            }

            normalize(generatorName);
        }

        /**
         * incoming data may contain stuff like "0 to 0", which should be
         * converted to "0" and all the incoming numbers must be converted to
         * the corresponding types (int, float, double)
         */
        private void normalize(String generatorName) throws ClassNotFoundException, IOException {

            for (int i = 0; i < data.size(); i++) {
                if (i != rangeValue) {
                    Object item = data.get(i);

                    String parameter = getGenParameterName(i);
                    //if it is a number, it must be on it converted
                    if (parameter.equals("int") || parameter.equals("float") || parameter.equals("double")) {
                        if (((String) item).contains(" to ")) {
                            String[] chunks = ((String) item).split(" to ");
                            if (chunks.length == 2 && (chunks[0].equals(chunks[1]))) {
                                switch (parameter) {
                                    case "int":
                                        data.set(i, Integer.parseInt(chunks[0]));
                                        break;
                                    case "float":
                                        data.set(i, Float.parseFloat(chunks[0]));
                                        break;
                                    case "double":
                                        data.set(i, Double.parseDouble(chunks[0]));
                                        break;
                                }
                            }
                        } else {
                            switch (parameter) {
                                case "int":
                                    data.set(i, Integer.parseInt((String) item));
                                    break;
                                case "float":
                                    data.set(i, Float.parseFloat((String) item));
                                    break;
                                case "double":
                                    data.set(i, Double.parseDouble((String) item));
                                    break;
                            }
                        }
                    } else if (!parameter.equals("java.lang.String") && !parameter.equals("String")) {
                        //enum
                        data.set(i, Enum.valueOf((Class<? extends Enum>) new ClassParser(parameter).getLoadedClass(), (String) item));
                    }
                }
            }
        }
    }
}
