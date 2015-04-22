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
package cz.cuni.mff.d3s.tools.perfdoc.doclets.formats.html.js;

import cz.cuni.mff.d3s.tools.perfdoc.doclets.formats.html.PerformanceBodyWriter;
import cz.cuni.mff.d3s.tools.perfdoc.doclets.formats.html.PerformanceNamingUtils;
import java.io.IOException;

/**
 * Class that maintains Javascript code associated with an AJAX communication
 *
 * @author Jakub Naplava
 */
public class JSAjaxHandler {

    private static String testedMethod;

    public static String generator;

    /**
     * Returns the (global) javascript code, that calls the measuring server and
     * passes the data to be measured
     *
     * @param serverAddress The address of the measuring server
     */
    public static String returnCallServerFunction(String serverAddress) throws IOException {
        String pattern = JavascriptLoader.getFileContent("callserver.js");

        //placing right serverAddress in the pattern by replacing $serverAddress
        return pattern.replace("$serverAddress", serverAddress);
    }

    /**
     * Returns the (global) javascript function, that tells the user about the
     * occured error
     */
    public static String returnErrorFunction() throws IOException {
        return JavascriptLoader.getFileContent("printajaxerror.js");
    }

    /**
     * To the local javascript code adds the succesfunction for current
     * generator, this function handles incoming JSON results (puts them in the
     * right graph, changes graph color, ...)
     */
    public static String returnSuccessFunction() throws IOException {
        return JavascriptLoader.getFileContent("successfunction.js");
    }

    public static String returnIdentifierFunction() throws IOException {
        return JavascriptLoader.getFileContent("identifier.js");
    }

    public static String returnSuccesFunctionName() {
        PerformanceBodyWriter perfWriter = new PerformanceBodyWriter();
        return PerformanceNamingUtils.getUniqueInfo(generator) + "success";
    }

    public static String returnDivName() {
        PerformanceBodyWriter perfwWriter = new PerformanceBodyWriter();
        return PerformanceNamingUtils.getUniqueInfo(generator);
    }

    /**
     * Returns the javascript code to call the server with right data
     */
    public static String returnSuccessButtonHandleFunction() {
        String graphName = returnDivName();

        //data in JSON format
        String data = returnDataJSON();

        StringBuilder sb = new StringBuilder();
        sb.append("var json = JSON.stringify(" + data + ", null, 2); ");

        sb.append("var graphInfo = {"
                + "divLocation : \"" + graphName + "\","
                + "xAxisLabel : paramResult.rangeValueName,"
                + "graph : null"
                + "};");

        sb.append("callServer( json, graphInfo, 1);");

        return sb.toString();
    }

    /**
     * Returns the data in JSON format
     */
    private static String returnDataJSON() {
        StringBuilder sb = new StringBuilder();

        sb.append("{");
        sb.append("\"testedMethod\" : \"" + testedMethod + "\",");
        sb.append("\"generator\" : \"" + generator + "\",");
        sb.append("\"rangeValue\" : paramResult.rangeValue,");
        sb.append("\"priority\" : 1,");
        sb.append("\"id\" : globalIdentifier, ");

        //data are in this part stored in an paramResult.values
        sb.append("\"data\" :  paramResult.values ");

        sb.append("}");

        return sb.toString();
    }

    public static String getTestedMethod() {
        return testedMethod;
    }

    public static void setTestedMethod(String testedMethod) {
        JSAjaxHandler.testedMethod = testedMethod;
    }
}
