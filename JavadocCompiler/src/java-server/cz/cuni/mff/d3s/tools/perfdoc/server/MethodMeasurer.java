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

import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkloadImpl;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.Workload;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.WorkloadImpl;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
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

    private int rangeValue;

    private ArrayList<Object> data = new ArrayList<Object>();

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

    public JSONObject measureTime() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        WorkloadImpl workloadImpl = new WorkloadImpl();
        ServiceWorkloadImpl serviceImpl = new ServiceWorkloadImpl();
        serviceImpl.setNumberResults(3);
        
        ArrayList<Object []> result = new ArrayList<>();

        int[] valuesToMeasure = getRangeValues(1);

        for (int i = 0; i < valuesToMeasure.length; i++) {
            Object[] args = getArgsToCall(valuesToMeasure[i], workloadImpl, serviceImpl);

            generatorMethod.invoke(generatorClass.newInstance(), args);

            Object[] objs = workloadImpl.getCall();
            long before = System.nanoTime();
            method.invoke(objs[0], objs[1]);
            long after = System.nanoTime();
            
            result.add(new Object[] {valuesToMeasure[i], ((after - before) / 1000000)});
        }

        JSONObject obj = new JSONObject();
        
        for (int i = 0; i<result.size(); i++) {
            obj.accumulate("data", result.get(i));
            System.out.println(result.get(i)[0] + ":" + result.get(i)[1]);
        }
        
        return obj;
    }

    private void debugInfo(Object[] args) {
        System.out.println("test:" + method.getName());
        System.out.println("generator:" + generatorMethod.getName());
        System.out.println("class Generator:" + generatorClass.getName());

        for (int u = 0; u < args.length; u++) {
            System.out.println(args[u] + ":" + args[u].getClass().getName());
        }
    }

    private int[] getRangeValues(int howMany) {
        int[] arr = new int[2];
        String[] oarr = ((String) data.get(rangeValue)).split(" to ");
        arr[0] = Integer.parseInt(oarr[0]);
        arr[1] = Integer.parseInt(oarr[1]);

        return arr;
    }

    private Object[] getArgsToCall(int rangeVal, Workload workload, ServiceWorkload serviceWorkload) {
        Object[] args = new Object[data.size() + 2];
        args[0] = workload;
        args[1] = serviceWorkload;

        for (int i = 0; i < data.size(); i++) {
            args[i + 2] = data.get(i);
        }

        args[rangeValue + 2] = rangeVal;

        return args;
    }

    /**
     * private class that parses incoming JSON and the result saves in the
     * MethodMeasure instance
     */
    private class JSONParser {

        private void parseData(String parseData) throws ClassNotFoundException, MalformedURLException {
            JSONObject obj = new JSONObject(parseData);

            String methodName = obj.getString("testedMethod");
            String generatorName = obj.getString("generator");

            findMethods(methodName, generatorName);

            rangeValue = obj.getInt("rangeValue");

            JSONArray dataArray = obj.getJSONArray("data");

            for (int i = 0; i < dataArray.length(); i++) {
                data.add(dataArray.get(i));
            }

            normalize(generatorName);
        }

        private void findMethods(String testedMethodString, String generatorMethodString) throws MalformedURLException, ClassNotFoundException {
            String[] testedMethodInfo = parseMethod(testedMethodString);
            method = new ClassParser(testedMethodInfo[0]).findMethod(testedMethodInfo[1], testedMethodInfo[2]);

            String[] generatorMethodInfo = parseMethod(generatorMethodString);

            ClassParser generatorClassParser = new ClassParser(generatorMethodInfo[0]);
            generatorClass = generatorClassParser.clazz;
            generatorMethod = generatorClassParser.findMethod(generatorMethodInfo[1], generatorMethodInfo[2]);
        }

        private String[] parseMethod(String method) {
            String[] subs = method.split("#");

            String className = subs[0] + "." + subs[1];
            String methodName = subs[2];
            String abbrParams = subs[3];

            return new String[]{className.toString(), methodName, abbrParams};
        }

        /**
         * incoming data may contain stuff like "0 to 0", which should be
         * converted to "0" and all the incoming numbers must be converted to
         * the corresponding types (int, float, double)
         */
        private void normalize(String generatorName) {
            String abbrParams = parseMethod(generatorName)[2];

            for (int i = 0; i < data.size(); i++) {
                if (i != rangeValue) {
                    Object item = data.get(i);

                    //if it is a number, it must be on it converted
                    if (abbrParams.charAt(i + 2) == 'i' || abbrParams.charAt(i + 2) == 'd' || abbrParams.charAt(i + 2) == 'f') {
                        if (((String) item).contains(" to ")) {
                            String[] chunks = ((String) item).split(" to ");
                            if (chunks.length == 2 && (chunks[0].equals(chunks[1]))) {
                                System.out.println(abbrParams.charAt(i + 2));
                                switch (abbrParams.charAt(i + 2)) {
                                    case 'i':
                                        data.set(i, Integer.parseInt(chunks[0]));
                                        break;
                                    case 'f':
                                        data.set(i, Float.parseFloat(chunks[0]));
                                        break;
                                    case 'd':
                                        data.set(i, Double.parseDouble(chunks[0]));
                                        break;
                                }
                            }
                        } else {
                            switch (abbrParams.charAt(i + 2)) {
                                case 'i':
                                    data.set(i, Integer.parseInt((String) item));
                                    break;
                                case 'f':
                                    data.set(i, Float.parseFloat((String) item));
                                    break;
                                case 'd':
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
