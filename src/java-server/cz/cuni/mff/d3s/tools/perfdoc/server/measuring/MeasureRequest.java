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

import cz.cuni.mff.d3s.tools.perfdoc.server.MethodInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.MethodReflectionInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Container for incoming JSON request. It contains all information from the
 * request in easy-to-work representation (e.g. methods represented via
 * MethodReflectionInfo, all arguments of generator in proper types, ...).
 *
 * While creating inner representation, it also checks, whether measuredMethod
 * and generator exists (via MethodReflectionInfo)
 *
 * @author Jakub Naplava
 */
public class MeasureRequest {

    private final MethodReflectionInfo measuredMethod;
    private final MethodReflectionInfo generator;
    private final int rangeVal;
    private final MeasurementQuality measurementQuality;
    private final String userID;

    //the arguments for generator with proper type. The only exception is the range-argument, which is being hold as String.
    private final Object[] values;

    /**
     * Creates new instance of MeasureRequest from the JSON request.
     *
     * @param JSONRequest
     * @throws ClassNotFoundException when any of the classes mentioned in JSON
     * can not be found.
     * @throws IOException when the configuration file containing classpath can
     * not be handled
     */
    public MeasureRequest(String JSONRequest) throws ClassNotFoundException, IOException, NoSuchMethodException {
        JSONObject obj = new JSONObject(JSONRequest);

        String measuredMethodName = obj.getString("testedMethod");
        String generatorName = obj.getString("generator");

        this.measuredMethod = new MethodReflectionInfo(measuredMethodName);
        this.generator = new MethodReflectionInfo(generatorName);

        this.rangeVal = obj.getInt("rangeValue");

        int priority = obj.getInt("priority");
        this.measurementQuality = new MeasurementQuality(priority);

        this.userID = obj.getString("id");

        JSONArray dataArray = obj.getJSONArray("data");
        List<Object> valuesList = new ArrayList<>();

        for (int i = 0; i < dataArray.length(); i++) {
            valuesList.add(dataArray.get(i));
        }

        values = normalize(valuesList, rangeVal);
    }

    /**
     * Normalize incoming values. The normalizing includes converting to proper
     * types (e.g. integers will be saved as integers, enums like enums) and
     * shortening numeric types (e.g. integer value sent in format "0 to 0",
     * will be converted to integer with value 0).
     *
     * @param valuesList the List containing values to normalize.
     * @param rangeValue the number of the rangeValue. valueList[item] will be
     * left as it was.
     */
    private Object[] normalize(List<Object> valuesList, int rangeValue) throws ClassNotFoundException, IOException {

        Object[] normalizedValues = valuesList.toArray();
        for (int i = 0; i < valuesList.size(); i++) {
            //rangeValue shall stay in the incoming format
            if (i != rangeValue) {
                Object item = valuesList.get(i);

                String parameter = getArgName(generator, i);
                //if it is a number, it must be on it converted
                if (parameter.equals("int") || parameter.equals("float") || parameter.equals("double")) {
                    if (((String) item).contains(" to ")) {
                        String[] chunks = ((String) item).split(" to ");
                        if (chunks.length == 2 && (chunks[0].equals(chunks[1]))) {
                            switch (parameter) {
                                case "int":
                                    normalizedValues[i] = Integer.parseInt(chunks[0]);
                                    break;
                                case "float":
                                    normalizedValues[i] = Float.parseFloat(chunks[0]);
                                    break;
                                case "double":
                                    normalizedValues[i] = Double.parseDouble(chunks[0]);
                                    break;
                            }
                        }
                    } else {
                        switch (parameter) {
                            case "int":
                                normalizedValues[i] = Integer.parseInt((String) item);
                                break;
                            case "float":
                                normalizedValues[i] = Float.parseFloat((String) item);
                                break;
                            case "double":
                                normalizedValues[i] = Double.parseDouble((String) item);
                                break;
                        }
                    }
                } else if (!parameter.equals("java.lang.String") && !parameter.equals("String")) {
                    //enum
                    //enum can be of any type, therefore Enum<?>, however this format is not accepted by valueof
                    @SuppressWarnings({"unchecked", "rawtypes"})
                    Object pom = Enum.valueOf((Class<? extends Enum>) new ClassParser(parameter).getLoadedClass(), (String) item);
                    normalizedValues[i] = pom;
                }
            }
        }

        return normalizedValues;
    }

    /**
     * Returns the i+2 argument name of the given method (omitting generator and
     * serviceWorkload arguments)
     */
    private static String getArgName(MethodInfo mi, int i) {
        return mi.getParams().get(i + 2);
    }

    public MethodReflectionInfo getMeasuredMethod() {
        return measuredMethod;
    }

    public MethodReflectionInfo getGenerator() {
        return generator;
    }

    public int getRangeVal() {
        return rangeVal;
    }

    public MeasurementQuality getMeasurementQuality() {
        return measurementQuality;
    }

    public String getUserID() {
        return userID;
    }

    public Object[] getValues() {
        return values;
    }
}
