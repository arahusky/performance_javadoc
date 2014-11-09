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

import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum;
import cz.cuni.mff.d3s.tools.perfdoc.server.LockBase;
import cz.cuni.mff.d3s.tools.perfdoc.server.MethodInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.MethodReflectionInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.ResultCache;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.ResultDatabaseCache;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkloadImpl;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.Workload;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.WorkloadImpl;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author Jakub Naplava
 */
public class MethodMeasurer {

    private static final Logger log = Logger.getLogger(MethodMeasurer.class.getName());

    private final MeasureRequest measureRequest;
    private final ResultCache resultCache;
    private final LockBase lockBase;

    public MethodMeasurer(MeasureRequest measureRequest, LockBase lockBase) throws IOException, ClassNotFoundException, SQLException {
        this.lockBase = lockBase;
        this.measureRequest = measureRequest;
        resultCache = new ResultDatabaseCache();
    }

    public JSONObject measure() {
        int priority = measureRequest.getPriority();

        WorkloadImpl workloadImpl = new WorkloadImpl();
        ServiceWorkloadImpl serviceImpl = new ServiceWorkloadImpl();

        //passing the priority to generator
        serviceImpl.setPriority(priority);

        //list to store measured Statistics
        ArrayList<BenchmarkResult> result = new ArrayList<>();

        //values chosen from rangeValue to measure data in
        double[] valuesToMeasure = getValuesToMeasure(measureRequest, MeasurementConfiguration.returnHowManyValuesToMeasure(priority));

        //how many times to measure the method in one cycle
        int howManyTimesToMeasure = MeasurementConfiguration.returnHowManyTimesToMeasure(priority);

        //passing the amount of wanted results to generator
        serviceImpl.setNumberCalls(howManyTimesToMeasure);

        for (int i = 0; i < valuesToMeasure.length; i++) {

            //the arguments for the generator 
            Object[] args = prepareArgsToCall(measureRequest, workloadImpl, serviceImpl, valuesToMeasure[i] );
            BenchmarkSetting benSetting = new BenchmarkSettingImpl(measureRequest, new MethodArgumentsImpl(args));
            
            //checking for results in cache
            BenchmarkResult res = resultCache.getResult(benSetting, howManyTimesToMeasure);
            if (res != null && !res.getStatistics().isEmpty()) {
                result.add(res);
                log.log(Level.CONFIG, "The value for measuring was found in cache.");
                continue;
            }
            BenchmarkRunner runner = null;
            switch (priority) {
                case 1:
                case 2:
                case 3:
                case 4:
                    runner = new MethodReflectionRunner();
                    break;
            }
            
            //wait until we can measure (there is no lock for our hash)
            lockBase.waitUntilFree(measureRequest.getUserID());
            result.add(new BenchmarkResultImpl(runner.measure(benSetting), benSetting));
            lockBase.freeLock(measureRequest.getUserID());     
        }

        log.log(Level.CONFIG, "Measurement succesfully done");

        //String units = convertUnitsIfNeeded(result);

        //create new JSONObject containing measured results
        JSONObject jsonResults = new JSONObject();
        for (int i = 0; i < result.size(); i++) {
            BenchmarkResult benRes = result.get(i);
            long res = benRes.getStatistics().compute();
            resultCache.insertResult(benRes.getBenchmarkSetting(), howManyTimesToMeasure, res);
            jsonResults.accumulate("data", new Object[] {valuesToMeasure[i], res});
        }
        //jsonResults.accumulate("units", units);
        jsonResults.accumulate("units", "ns");
        if (resultCache != null) {
            //we do not need the connection to database anymore
            resultCache.closeConnection();
        }
        return jsonResults;
    }

    /**
     * Creates arguments to pass to .invoke method of generator
     */
    private static Object[] prepareArgsToCall(MeasureRequest measureRequest, Workload workload, ServiceWorkload serviceWorkload, double rangeValValue) {
        Object[] oldArgs = measureRequest.getValues();
        Object[] newArgs = new Object[oldArgs.length + 2];
        newArgs[0] = workload;
        newArgs[1] = serviceWorkload;

        for (int i = 0; i < oldArgs.length; i++) {
            newArgs[i + 2] = oldArgs[i];
        }
        
        int rangeValPosition = measureRequest.getRangeVal();
        //all values have already good type (method normalize in JSONParser) except for the range value
        String parameter = getArgName(measureRequest.getWorkload(), rangeValPosition);
        switch (parameter) {
            case "int":
                newArgs[rangeValPosition + 2] = (int) rangeValValue;
                break;
            case "float":
                newArgs[rangeValPosition + 2] = (float) rangeValValue;
                break;
            case "double":
                newArgs[rangeValPosition + 2] = rangeValValue;
                break;
        }

        return newArgs;
    }

    /**
     * Returns the i+2 argument name of the given method.
     */
    private static String getArgName(MethodInfo mi, int i) {
        return mi.getParams().get(i + 2);
    }

    /**
     * Chooses the right data in which the generator will generate data,
     * the data are chosen to divide the interval to the very same pieces
     *
     * @param howMany how many data will be chosen
     * @return the double array containing chosen values
     */
    static double[] getValuesToMeasure(MeasureRequest measureRequest, int howMany) {
        Object[] allValues = measureRequest.getValues();
        int rangeValue = measureRequest.getRangeVal();
        
        //the endpoints will always be contained in the values 
        String[] oarr = ((String) allValues[rangeValue]).split(" to ");
        double min = Double.parseDouble(oarr[0]);
        double max = Double.parseDouble(oarr[1]);

        //the distance of two measured values
        double step = findStepValue(measureRequest);

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
    static int returnHowManyInInterval(double min, double max, double step) {
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
    static double[] findOtherValues(double step, double minVal, double maxVal, int howMany) {
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
    static double findNearestSmallerPossibleValue(double value, double min, double step) {
        double actualValue = 0;

        while (actualValue + step <= value) {
            actualValue += step;
        }

        return actualValue;
    }

    /**
     * Finds the step value in the generator for given rangeValue
     */
    static double findStepValue(MeasureRequest measureRequest) {
        int rangeValue = measureRequest.getRangeVal();
        MethodReflectionInfo generator = measureRequest.getWorkload();
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
}
