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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Class that stores informations about one method
 *
 * @author Jakub Naplava
 */
public class MethodInfo {

    private String className;
    private String methodName;
    private ArrayList<String> params;

    private Class<?> containingClass;
    private Method method;

    /**
     * creates new instance of MethodInfo
     *
     * @param methodData data either from incoming json or another
     * MethodInfo.toString()
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public MethodInfo(String methodData) throws ClassNotFoundException, IOException {
        parseMethod(methodData);

        ClassParser cp = new ClassParser(className);
        this.containingClass = cp.clazz;
        this.method = cp.findMethod(this);
    }

    /**
     * Saves the params in appropriate format (converts them from String to
     * ArrayList)
     *
     * @param params the String representing params (that we get from incoming
     * JSON)
     * @return an arraylist, that has on i-th index an i-th method parameter
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
     * Parses the method that we get from incoming JSON.
     *
     * @param method the incoming method name
     * @return String array containing the className, methodName and abbrParams
     */
    private void parseMethod(String method) {
        String[] subs = method.split("#");

        //if it is MethodInfo.toString()
        if (subs.length == 3) {
            this.className = subs[0];
            this.methodName = subs[1];
            this.params = getParamNames(subs[2]);
        } else {
            //it is an incoming JSON
            this.className = subs[0] + "." + subs[1];
            this.methodName = subs[2];
            this.params = getParamNames(subs[3]);
        }
    }

    public String getContainingClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public ArrayList<String> getParams() {
        return params;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getContainingClass() {
        return containingClass;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(className + "#" + methodName + "#");
        for (String param : params) {
            result.append("@" + param);
        }

        return result.toString();
    }
}
