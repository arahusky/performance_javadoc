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

import java.util.ArrayList;

/**
 * Class that provides basic information about a method that we get from JSON
 * request.
 *
 * @author Jakub Naplava
 */
public class MethodInfo {

    protected String containingClassQualifiedName;
    protected String methodName;
    protected ArrayList<String> params;

    /**
     * @param methodData data either from incoming JSON
     */
    public MethodInfo(String methodData) throws IllegalArgumentException {
        parseMethod(methodData);
    }
    
    public MethodInfo(String containingClassQualifiedName, String methodName, ArrayList<String> parameters) {
        this.containingClassQualifiedName = containingClassQualifiedName;
        this.methodName = methodName;
        this.params = parameters;
    }

    /**
     * Parses the method that we get from incoming JSON.
     *
     * @param method the incoming method name
     * @return String array containing the className, methodName and abbrParams
     */
    private void parseMethod(String method) throws IllegalArgumentException {
        String[] subs = method.split("#");

        //if it is MethodInfo.toString()
        if (subs.length == 3) {
            this.containingClassQualifiedName = subs[0];
            this.methodName = subs[1];
            this.params = getParamNames(subs[2]);
        } else if (subs.length == 5) {
            //it is an incoming JSON
            this.containingClassQualifiedName = subs[0] + "." + subs[1];
            this.methodName = subs[2];
            this.params = getParamNames(subs[3]);
        } else {
            throw new IllegalArgumentException("Given string does not represent correct method");
        }
    }

    /**
     * Saves the params in appropriate format (converts them from String to
     * ArrayList)
     *
     * @param params the String representing params (that we get from incoming
     * JSON)
     * @return an ArrayList<String>, that has on i-th index an i-th method
     * parameter
     */
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
     * @return qualified class name (Format: package.className)
     */
    public String getQualifiedClassName() {
        return containingClassQualifiedName;
    }

    public String getMethodName() {
        return methodName;
    }

    public ArrayList<String> getParams() {
        return params;
    }

    /**
     * @return String in format:
     * package.className#methodName#@param1&param2&...&paramN
     * This format is used for storing data in database.
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(containingClassQualifiedName + "#" + methodName + "#");
        for (String param : params) {
            result.append("@" + param);
        }

        return result.toString();
    }
}
