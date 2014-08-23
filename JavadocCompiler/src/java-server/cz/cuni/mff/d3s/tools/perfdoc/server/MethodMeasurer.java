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

import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkloadImpl;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.WorkloadImpl;
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
        
        for (int i = 0; i<this.data.size(); i++)
        {
            System.out.println(this.data.get(i));
        }
    }

    public MethodMeasurer(Method method, Method generator, int rangeValue, ArrayList<Object> data) {
        this.method = method;
        this.generatorMethod = generator;
        this.rangeValue = rangeValue;
        this.data = data;
    }

    public JSONObject measureTime()
    {
        WorkloadImpl workloadImpl = new WorkloadImpl();
        ServiceWorkloadImpl serviceImpl = new ServiceWorkloadImpl();        
        serviceImpl.setNumberResults(3);
        
        return new JSONObject();
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
            
            normalize();
        }
        
        private void findMethods(String testedMethodString, String generatorMethodString) throws MalformedURLException, ClassNotFoundException
        {
            String[] testedMethodInfo = parseMethod(testedMethodString);
            method = new ClassParser(testedMethodInfo[0]).findMethod(testedMethodInfo[1], testedMethodInfo[2]);
            
            String[] generatorMethodInfo = parseMethod(generatorMethodString);
            
            ClassParser generatorClassParser = new ClassParser(generatorMethodInfo[0]);
            generatorClass = generatorClassParser.clazz;
            generatorMethod = generatorClassParser.findMethod(generatorMethodInfo[1], generatorMethodInfo[2]);
        }
        
        private String[] parseMethod(String method)
        {
            String[] subs = method.split("_");
            
            String abbrParams = subs[subs.length - 2];
            String methodName = subs[subs.length - 3];
            
            StringBuilder className = new StringBuilder();
            for (int i = 0; i<subs.length - 4; i++) {
                className.append(subs[i] + ".");
            }            
            className.append(subs[subs.length - 4]);
           
            return new String[] {className.toString(), methodName, abbrParams};
        }
        
        /**
         * incoming data may contain stuff like "0 to 0", which should be converted to "0"
         */
        private void normalize()
        {
            for (int i = 0; i<data.size(); i++)
            {
                if (i != rangeValue)
                {
                    String item = (String) data.get(i);
                    
                    if (item.contains(" to ")) {
                        String[] chunks = item.split(" to ");
                        if (chunks.length == 2 && (chunks[0].equals(chunks[1]))) {                        
                        data.set(i, chunks[0]);
                        }
                    }
                }
            }
        }
    }
}
