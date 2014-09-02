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

import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkloadImpl;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.Workload;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.WorkloadImpl;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Jakub Naplava
 */
public class MethodMeasurer {

    private MethodInfo testedMethod;
    private MethodInfo generator;

    private int rangeValue;
    private int priority;
    private String hash;

    private ArrayList<Object> data = new ArrayList<>();

    private ResultCache resultCache;
    private LockBase lockBase;

    private static final Logger log = Logger.getLogger(MethodMeasurer.class.getName());

    public MethodMeasurer(String data, LockBase lockBase) throws ClassNotFoundException, MalformedURLException, IOException, SQLException {
        JSONParser parser = new JSONParser();
        parser.parseData(data);

        this.resultCache = new ResultDatabaseCache();
        this.lockBase = lockBase;
    }

    /**
     * measures the time duration of the method for given data and rangeValue
     *
     * @return JSONObject containing measured values with their time durations
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public JSONObject measureTime() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        WorkloadImpl workloadImpl = new WorkloadImpl();
        ServiceWorkloadImpl serviceImpl = new ServiceWorkloadImpl();

        //passing the amount of wanted results to generator
        serviceImpl.setNumberResults(1);

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

        //wait until we can measure (there is no lock for our hash)
        lockBase.waitUntilFree(hash);
        
        for (int i = 0; i < valuesToMeasure.length; i++) {

            //the arguments for the generator 
            Object[] args = prepareArgsToCall(valuesToMeasure[i], workloadImpl, serviceImpl);

            //check, whether we do not have already data cached
            long res = resultCache.getResult(testedMethod.toString(), generator.toString(), ("" + args[2] + args[3] + args[4]), howManyTimesToMeasure);
            if (res != -1) {
                result.add(new Object[]{valuesToMeasure[i], res});
                log.log(Level.CONFIG, "The value for measuring was found in cache.");
                continue;
            }

            String msg = "Starting to measure..." + ", tested Method:{0}" + testedMethod.getMethod().getName()
                    + "generator:{0}" + generator.getMethod().getName() + "class Generator:{0}" + generator.getContainingClass().getName();
            log.log(Level.CONFIG, msg);

            try {
                generatorMethod.invoke(generatorClass.newInstance(), args);

                Object[] objs;
                //the generator prepared us the arguments for tested method in workloadImpl
                //we just get one and measure the tested method with it
                while ((objs = workloadImpl.getCall()) != null) {

                    //TODO if is an array, if not ...
                    long before = System.nanoTime();
                    for (int a = 0; a < howManyTimesToMeasure; a++) {
                        method.invoke(objs[0], objs[1]);
                    }
                    long after = System.nanoTime();

                    long duration = ((after - before) / 1000000) / howManyTimesToMeasure;
                    result.add(new Object[]{valuesToMeasure[i], duration});

                    resultCache.insertResult(testedMethod.toString(), generator.toString(), ("" + args[2] + args[3] + args[4]), priority + 10, duration);

                }
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

        //create new JSONObject containing measured results
        JSONObject jsonResults = new JSONObject();
        for (int i = 0; i < result.size(); i++) {
            jsonResults.accumulate("data", result.get(i));
        }

        if (resultCache != null) {
            //we do not need the connection to database anymore
            resultCache.closeConnection();
        }
        return jsonResults;
    }

    /**
     * Chooses the right data from rangeValue which will be passed to generator,
     * the data are chosen to divide the interval to the very same pieces
     *
     * @param howMany how many data will be chosen
     * @return the double array containing chosen values
     */
    private double[] getValuesToMeasure(int howMany) {
        //TODO check whether it is even possible to make so many values - otherwise return as much as you can
        double[] values = new double[howMany];

        //the endpoints will be always contained in the values 
        String[] oarr = ((String) data.get(rangeValue)).split(" to ");
        double min = Double.parseDouble(oarr[0]);
        double max = Double.parseDouble(oarr[1]);

        //the distance of two measured values
        double step = findStepValue();

        values[0] = min;
        values[values.length - 1] = max;

        double[] otherVals = findOtherValues(step, min, max, howMany - 2);

        for (int i = 1; i < values.length - 1; i++) {
            values[i] = otherVals[i - 1];
        }

        return values;
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
    private double[] findOtherValues(double step, double minVal, double maxVal, int howMany) {
        if (howMany < 1) {
            return new double[0];
        }

        double distance = maxVal - minVal;
        //how many units are between the endpoints
        double numberOfPossibleSteps = Math.floor(distance / step);

        //the candidate for the step
        double possibleStep = (numberOfPossibleSteps / (howMany + 1)) * step;
        //the candidate for the step must be normalized = we need to find the highest smaller (or equal) value that can be reached be adding the step to the min
        double myStep = findNearestSmallerPossibleValue(possibleStep, minVal, step);

        //TODO check whether is it possible to find so many values in the interval
        double[] arr = new double[howMany];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = roundToNextPossibleValue(minVal + ((i + 1) * myStep));
        }

        return arr;
    }

    /**
     * Finds the highest number smaller than the value that can be achieved by
     * step
     */
    private double findNearestSmallerPossibleValue(double value, double min, double step) {
        double actualValue = 0;

        while (actualValue + step <= value) {
            actualValue += step;
        }

        return roundToNextPossibleValue(actualValue);
    }

    /**
     * Rounds the value (slices inaccurate data)
     */
    private double roundToNextPossibleValue(double value) {
        //TODO FIXME to be more precise according to step (which would be better in string format - it seems impossible)
        DecimalFormat df = new DecimalFormat("0.#####");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return Double.parseDouble(df.format(value));
    }

    /**
     * Finds the step value in the generator for given rangeValue
     */
    private double findStepValue() {
        //first two parameters are workload and serviceWorkload
        int numInParams = rangeValue + 2;
        Parameter[] params = generator.getMethod().getParameters();

        Annotation[] annotations = params[numInParams].getAnnotations();

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

            testedMethod = new MethodInfo(methodName);
            generator = new MethodInfo(generatorName);

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
                        data.set(i, Enum.valueOf((Class<? extends Enum>) new ClassParser(parameter).clazz, (String) item));
                    }
                }
            }
        }
    }
}
