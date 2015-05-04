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
import cz.cuni.mff.d3s.tools.perfdoc.blackhole.Blackhole;
import cz.cuni.mff.d3s.tools.perfdoc.server.MethodReflectionInfo;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.Workload;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jakub Naplava
 */
public class MeasuringUtils {

    private static final double delta = 0.000001;
    private static final Logger log = Logger.getLogger(MeasuringUtils.class.getName());

    /**
     * Prepares arguments for the call of generator.
     *
     * @param measureRequest
     * @param workload
     * @param serviceWorkload
     * @param rangeValValue
     * @return the Object[] that is in the proper format for calling
     * workload.invoke(..., args)
     */
    public static Object[] prepareArgsToCall(MeasureRequest measureRequest, Workload workload, ServiceWorkload serviceWorkload, double rangeValValue) {
        Object[] oldArgs = measureRequest.getValues();
        Object[] newArgs = new Object[oldArgs.length + 2];
        newArgs[0] = workload;
        newArgs[1] = serviceWorkload;

        for (int i = 0; i < oldArgs.length; i++) {
            newArgs[i + 2] = oldArgs[i];
        }

        int rangeValPosition = measureRequest.getRangeVal();
        //all values have already good type (method normalize in JSONParser) except for the range value
        String parameter = measureRequest.getGenerator().getParams().get(rangeValPosition + 2);
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
     * Chooses the right data in which the generator will generate data.
     *
     * The data are chosen to divide the interval to the very same pieces (using
     * binary interval dividing).
     *
     * @param rangeArgument the range argument for which the values will be
     * generated (string in format "double to double")
     * @param step the distance between two (not-equal) closest numbers in
     * interval
     * @param howMany how many points should be chosen
     * @return the double array containing chosen values. If there is enough
     * data in interval, the length of this array is howMany, otherwise it
     * contains as many data as possible
     * @throws IllegalArgumentException when the rangeArgument is not in the
     * proper format (as already described)
     * @throws NumberFormatException when the rangeArgument does not contain
     * double numbers next to " to "
     */
    public static double[] getValuesToMeasure(Object rangeArgument, double step, int howMany) throws IllegalArgumentException, NumberFormatException {
        try {
            String[] oarr = ((String) rangeArgument).split(" to ");

            if (oarr.length != 2) {
                log.log(Level.INFO, "Rangeargument in bad format was passed to the method");
                throw new IllegalArgumentException("RangeArgument was in a bad format.");
            }

            double min = Double.parseDouble(oarr[0]);
            double max = Double.parseDouble(oarr[1]);

            double[] possibleValues = returnPossibleValues(min, max, step);

            return getValuesInWhichToMeasure(possibleValues, howMany);
        } catch (NumberFormatException ex) {
            log.log(Level.INFO, "Rangeargument in bad format was passed to the method", ex);
            throw ex;
        }
    }

    /**
     * Generates all possible values less than max that can be reached by adding
     * step to min
     */
    private static double[] returnPossibleValues(double min, double max, double step) {
        List<Double> list = new ArrayList<>();
        list.add(min);
        double actualValue = min;

        //comparing of doubles needs to be handled with some delta
        while (Math.abs(actualValue + step - max) >= delta) {
            actualValue += step;
            list.add(actualValue);
        }

        list.add(max);

        double[] res = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            res[i] = list.get(i);
        }

        return res;
    }

    /**
     * Returns the array containing maximally howMany values chosen from
     * possibleValues. The array is lexicographically sorted.
     *
     * The method supposes the possibleValues array to be lexicographically
     * sorted.
     */
    private static double[] getValuesInWhichToMeasure(double[] possibleValues, int howMany) {
        if (possibleValues.length < 2) {
            log.log(Level.INFO, "Not enough possible values were passed to getValuesInWhichToMeasure." );
            throw new IllegalArgumentException("Not enough possible values were passed to getValuesInWhichToMeasure.");
        }
        List<Double> list = new ArrayList<>();

        //the maximal and minimal value should always be in the result 
        if (--howMany >= 0) {
            list.add(possibleValues[0]);
        }

        if (--howMany >= 0) {
            list.add(possibleValues[possibleValues.length - 1]);
        }

        binaryDivideChoose(1, possibleValues.length - 2, possibleValues, howMany, list);
        Collections.sort(list);

        double[] res = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            res[i] = list.get(i);
        }
        return res;
    }

    /**
     * Recursive implementation of binary dividing.
     *
     * This method should choose the howMany values from possibleVals into list.
     * The basic idea is choosing the index in the middle of maxIndex and
     * minIndex, adding the possibleVals[index] into list and recursive calling
     * on the part lower than index and the part bigger than index.
     *
     * @param minIndex the min index to possibleVals
     * @param maxIndex the max index to possibleVals
     * @param possibleVals possible values
     * @param howMany how many values should the method generate
     * @param list return value. Contains generated values
     */
    private static void binaryDivideChoose(int minIndex, int maxIndex, double[] possibleVals, int howMany, List<Double> list) {
        if (howMany <= 0) {
            return;
        }

        if (minIndex > maxIndex) {
            return;
        }

        int half = minIndex + ((int) Math.floor((maxIndex - minIndex) / 2));
        list.add(possibleVals[half]);

        int numberLower = (int) Math.floor((howMany - 1) / 2);
        int numberUpper = howMany - 1 - numberLower;
        binaryDivideChoose(minIndex, half - 1, possibleVals, numberLower, list);
        binaryDivideChoose(half + 1, maxIndex, possibleVals, numberUpper, list);
    }

    /**
     * Finds the step value in the generator for given rangeValue.
     */
    public static double findStepValue(MethodReflectionInfo generator, int rangeValue) {
        //first two parameters are workload and serviceWorkload
        int numInParams = rangeValue + 2;
        Annotation[][] params = generator.getMethod().getParameterAnnotations();

        Annotation[] annotations = params[numInParams];
        String ParamNumName = ParamNum.class.getName();

        for (Annotation a : annotations) {
            if (ParamNumName.equals(a.annotationType().getName())) {
                return ((ParamNum) a).step();
            }
        }

        return -1; //some value to indicate non-succes
    }
    
    /**
     * Converts measurements in nanoseconds to other units if necessary.
     */
    public static String convertUnits(List<Long> first, List<Long>second) {
        //supported units (may be added more)
        String[] units = new String[]{"s", "ms", "µs", "ns"};
        //pointer to units array showing actual unit
        int index = 3;

        double min = Double.MAX_VALUE;

        //computing minValue
        for (double d : first) {
            if (d < min) {
                min = d;
            }
        }
        
        for (double d : second) {
            if (d < min) {
                min = d;
            }
        }
        
        //10,000 was chosen constant so that the minValue is not bigger than it
        while (min >= 10000 && (index > 0)) {
            index--;
            min = min / 1000;
        }

        int divideBy = 1;

        for (int i = index; i < units.length - 1; i++) {
            divideBy *= 1000;
        }
        
        //first.size() == second.size()
        for (int i = 0; i < first.size(); i++) {
            long newValueFirst = first.get(i) / divideBy;
            first.set(i, newValueFirst);
            long newValueSecond = second.get(i) / divideBy;
            second.set(i, newValueSecond);            
        }

        return units[index];
    }
    
    /**
     * Checks whether first parameter of the method is Blackhole.
     */
    public static boolean hasMeasuredMethodBlackhole(Method method) {
        if (method.getParameterTypes().length == 0) {
            return false;
        }
        
        return method.getParameterTypes()[0].equals(Blackhole.class);
    }
    
    public static Object[] pushBlackholeToBegin(Object[] args) {
        Blackhole bh = BlackholeFactory.getInstance();
        
        Object[] newArgs = new Object[args.length + 1];
        newArgs[0] = bh;
        
        for (int i = 0; i<args.length; i++) {
            newArgs[i+1] = args[i];
        }
        
        return newArgs;
    }
}
