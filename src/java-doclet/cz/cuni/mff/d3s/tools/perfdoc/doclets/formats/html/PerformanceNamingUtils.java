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
package cz.cuni.mff.d3s.tools.perfdoc.doclets.formats.html;

import com.sun.javadoc.MethodDoc;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;

/**
 *
 * @author Jakub Naplava
 */
public class PerformanceNamingUtils {

    /**
     * Method to count the unique identifier (through all .html document), that
     * represents the concrete method, this identifier however contains also
     * characters like dot or hash, which can not be used as a variable (see
     * getUniqueInfo)
     *
     * @param doc the methodDoc of method, for which the unique ID will be
     * counted
     * @return the unique info (packageName#className#method#Params#Number)
     */
    public static String getUniqueFullInfo(MethodDoc doc) {
        String containingPackage = doc.containingPackage().name();
        String className = doc.containingClass().name();
        String methodName = doc.name();
        String abbrParams = getAbbrParams(doc);

        String fullMethodName = (containingPackage + "#" + className + "#" + methodName);
        String number = GeneratorBase.getNewGeneratorID(fullMethodName) + "";

        return (fullMethodName + "#" + abbrParams + "#" + number);
    }

    /**
     * Method to count the unique identifier (through all .html document), that
     * represents the concrete method, this identifier however contains also
     * characters like dot or hash, which can not be used as a variable (see
     * getUniqueInfo).
     *
     * @param doc the (reflection) method, for which the unique ID will be
     * counted
     * @return the unique info (packageName#className#method#Params#Number)
     */
    public static String getUniqueFullInfoReflection(Method doc) {
        String containingPackage = doc.getDeclaringClass().getPackage().getName();
        String className = doc.getDeclaringClass().getSimpleName();
        String methodName = doc.getName();
        String abbrParams = getAbbrParamsReflection(doc);

        String fullMethodName = (containingPackage + "#" + className + "#" + methodName);
        String number = GeneratorBase.getNewGeneratorID(fullMethodName) + "";

        return (fullMethodName + "#" + abbrParams + "#" + number);
    }

    public static String getUniqueInfo(String fullMethodInfo) {
        String[] chunks = fullMethodInfo.split("#");
        String result = chunks[0] + "#" + chunks[1] + "#" + chunks[2] + "#" + chunks[4];

        return result.replaceAll("\\.", "_").replaceAll("#", "_");
    }

    /**
     * Gets the abbreviated form of the parameters type of given method.
     *
     * @param doc
     * @return In the parameter declared order returns the begin letter of the
     * parameters types. For example for method foo(String, int, float) the
     * result would be "sif"
     */
    private static String getAbbrParams(MethodDoc doc) {
        //the following method returns parameters in the declared order (otherwise, there would be no chance to have it unique)
        com.sun.javadoc.Parameter[] params = doc.parameters();
        String abbrParams = "";

        for (int i = 0; i < params.length; i++) {
            switch (params[i].typeName()) {
                case "int":
                    abbrParams += "@int";
                    break;
                case "double":
                    abbrParams += "@double";
                    break;
                case "float":
                    abbrParams += "@float";
                    break;
                case "String":
                    abbrParams += "@java.lang.String";
                    break;
                default:
                    //enum situation
                    abbrParams += "@" + params[i].type().toString();
                    break;
            }
        }

        return abbrParams;
    }

    /**
     * Gets the abbreviated form of the parameters type of given method.
     *
     * @param doc
     * @return In the parameter declared order returns the begin letter of the
     * parameters types. For example for method foo(String, int, float) the
     * result would be "sif"
     */
    private static String getAbbrParamsReflection(Method doc) {
        //the following method returns parameters in the declared order (otherwise, there would be no chance to have it unique)
        Parameter[] params = doc.getParameters();
        String abbrParams = "";

        for (int i = 0; i < params.length; i++) {
            switch (params[i].getClass().getTypeName()) {
                case "int":
                    abbrParams += "@int";
                    break;
                case "double":
                    abbrParams += "@double";
                    break;
                case "float":
                    abbrParams += "@float";
                    break;
                case "String":
                    abbrParams += "@java.lang.String";
                    break;
                default:
                    //enum situation
                    abbrParams += "@" + params[i].getParameterizedType().getTypeName();
                    break;
            }
        }

        return abbrParams;
    }

    /**
     * inner class, that adds the ending to every generator so that the
     * generator ID is unique
     */
    private static class GeneratorBase {

        private static HashMap<String, Integer> map = new HashMap<>();

        /**
         * Gets a workloadName and returns the appropriate ending, so that the
         * result workloadName is unique. The class has s static hashmap, that
         * contains all the workloadNames, that has already been asked for. If
         * the workload is not there yet, it adds it there (with number of use
         * set to 0) and returns 0, otherwise gets the number of usage, add one
         * to it and returns
         *
         * @param workload
         * @return
         */
        public static int getNewGeneratorID(String workload) {

            //the default value (when there's no such a key) is 0
            int value = 0;

            //if the key is contained, we get its value and add one to it 
            if (map.containsKey(workload)) {
                value = map.get(workload);
                value++;
            }

            map.put(workload, value);
            return value;
        }
    }
}
