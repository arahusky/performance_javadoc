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
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author arahusky
 */
public class MethodMeasurer {

    private Method method;

    private Class<?> generatorClass;
    private Method generatorMethod;
    private ArrayList<String> generatorParamTypes;

    private int rangeValue;

    private ArrayList<Object> data = new ArrayList<>();

    public MethodMeasurer(String data) throws ClassNotFoundException, MalformedURLException {
        JSONParser parser = new JSONParser();
        parser.parseData(data);

        for (int i = 0; i < this.data.size(); i++) {
            System.out.println(this.data.get(i));
        }
    }

    public MethodMeasurer(Method method, Method generator, int rangeValue, ArrayList<Object> data) {
        this.method = method;
        this.generatorMethod = generator;
        this.rangeValue = rangeValue;
        this.data = data;
    }

    /**
     * measures the time duration of the method for given data and rangeValue
     *
     * @return JSONObject containg measured values with their time durations
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public JSONObject measureTime() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        WorkloadImpl workloadImpl = new WorkloadImpl();
        ServiceWorkloadImpl serviceImpl = new ServiceWorkloadImpl();
        serviceImpl.setNumberResults(1);

        ArrayList<Object[]> result = new ArrayList<>();

        double[] valuesToMeasure = getValuesToMeasure(5);

        for (int i = 0; i < valuesToMeasure.length; i++) {
            Object[] args = prepareArgsToCall(valuesToMeasure[i], workloadImpl, serviceImpl);
            System.out.println("heree");
            
            generatorMethod.invoke(generatorClass.newInstance(), args);

            Object[] objs;
            while ((objs = workloadImpl.getCall()) != null) {
                long before = System.nanoTime();
                method.invoke(objs[0], objs[1]);
                long after = System.nanoTime();

                result.add(new Object[]{valuesToMeasure[i], ((after - before) / 1000000)});
            }
        }

        //create new JSONObject containing measured results
        JSONObject jsonResults = new JSONObject();
        for (int i = 0; i < result.size(); i++) {
            jsonResults.accumulate("data", result.get(i));
            System.out.println(result.get(i)[0] + ":" + result.get(i)[1]);
        }

        return jsonResults;
    }

    private void debugInfo(Object[] args) {
        System.out.println("test:" + method.getName());
        System.out.println("generator:" + generatorMethod.getName());
        System.out.println("class Generator:" + generatorClass.getName());

        for (int u = 0; u < args.length; u++) {
            System.out.println(args[u] + ":" + args[u].getClass().getName());
        }
    }

    /**
     * Chooses the right data from rangeValue which will be passed to generator,
     * the data are chosen to divide the interval to the very same pieces
     *
     * @param howMany how many data will be chosen
     * @return the double array containing chosen values
     */
    private double[] getValuesToMeasure(int howMany) {
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
        System.out.println("---------------");
        System.out.println("Poss step:" + possibleStep);

        //the candidate for the step must be normalized = we need to find the highest smaller (or equal) value that can be reached be adding the step to the min
        double myStep = findNearestSmallerPossibleValue(possibleStep, minVal, step);
        System.out.println("------------");
        System.out.println("Mystep:" + myStep);

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

        //System.out.println("calue:" + value + ", min:" + min + ", step: " + step);
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
        Parameter[] params = generatorMethod.getParameters();

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
        return generatorParamTypes.get(i + 2);
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
        private void parseData(String parseData) throws ClassNotFoundException, MalformedURLException {
            JSONObject obj = new JSONObject(parseData);

            String methodName = obj.getString("testedMethod");
            String generatorName = obj.getString("generator");

            findAndSaveMethodsAndClassses(methodName, generatorName);

            rangeValue = obj.getInt("rangeValue");

            JSONArray dataArray = obj.getJSONArray("data");

            for (int i = 0; i < dataArray.length(); i++) {
                data.add(dataArray.get(i));
            }

            normalize(generatorName);
        }

        /**
         * Finds and saves (in MethodMeasurer variables) tested method and
         * generator class and generator method
         *
         * @param testedMethodString
         * @param generatorMethodString
         * @throws ClassNotFoundException when tested method or generator method
         * were not found
         * @throws MalformedURLException when files in which to search the files
         * are in a bad format
         */
        private void findAndSaveMethodsAndClassses(String testedMethodString, String generatorMethodString) throws MalformedURLException, ClassNotFoundException {
            String[] testedMethodInfo = parseMethod(testedMethodString);
            ArrayList<String> testedParamNames = getParamNames(testedMethodInfo[2]);

            method = new ClassParser(testedMethodInfo[0]).findMethod(testedMethodInfo[1], testedParamNames);

            String[] generatorMethodInfo = parseMethod(generatorMethodString);

            ClassParser generatorClassParser = new ClassParser(generatorMethodInfo[0]);
            generatorClass = generatorClassParser.clazz;

            generatorParamTypes = getParamNames(generatorMethodInfo[2]);
            generatorMethod = generatorClassParser.findMethod(generatorMethodInfo[1], generatorParamTypes);
        }

        private ArrayList<String> getParamNames(String params) {
            String[] paramNames = params.split("@");
            ArrayList<String> res = new ArrayList<>();

            for (String s : paramNames) {
                if (!s.isEmpty()) {
                res.add(s);
                }
            }

            return res;
        }

        /**
         * Parses the method that we get from incoming JSON.
         *
         *
         * @param method the incoming method name
         * @return String array containing the className, methodName and
         * abbrParams
         */
        private String[] parseMethod(String method) {
            String[] subs = method.split("#");

            String className = subs[0] + "." + subs[1];
            String methodName = subs[2];
            String params = subs[3];
            
            System.out.println(params);

            return new String[]{className.toString(), methodName, params};
        }

        /**
         * incoming data may contain stuff like "0 to 0", which should be
         * converted to "0" and all the incoming numbers must be converted to
         * the corresponding types (int, float, double)
         */
        private void normalize(String generatorName) {

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
                    }
                }
                //TODO Strings are implicitly solved, but we need to convert strings to enums
            }
        }
    }
}
