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

import cz.cuni.mff.d3s.tools.perfdoc.doclets.formats.html.PerformanceWriterImpl;

/**
 *
 * @author Jakub Naplava
 */
public class JSAjaxHandler {

    public static String testedMethod;

    public static String generator;

    /**
     * Returns the (global) javascript code, that calls the measuring server and
     * passes the data to be measured
     *
     * @param serverAddress The address of the measuring server
     */
    public static String returnCallServerFunction(String serverAddress) {
        String pattern = "function callServer(data, successFunc, graphName, graph, priority) {"
                + "   $.ajax({"
                + "   url: \"" + serverAddress + "\","
                + "   data: data,"
                + "   type: \"POST\","
                + "   success: function(json) {successFunc(json, data, successFunc, graphName, graph, priority)},"
                + "   error: function( xhr, status, errorThrown ) { printAjaxError(xhr, status, graphName, errorThrown, priority); }"
                + "});"
                + "  }\n";

        return pattern;
    }

    /**
     * Returns the (global) javascript function, that tells the user about the
     * occured error
     */
    public static String returnErrorFunction() {
        return JavascriptLoader.getFileContent("printajaxerror.js");
    }

    /**
     * To the local javascript code adds the succesfunction for current
     * generator, this function handles incoming JSON results (puts them in the
     * right graph, changes graph color, ...)
     */
    public static void addSuccessFunction() {
        String successFunctionName = returnSuccesFunctionName();

        String success = "function " + successFunctionName + "(json, data, myName, graphName, graph, priority) {"
                + "    if (priority == 1) {"
                + returnStartGraphCode(returnDivName())
                + "    var jsonData = JSON.parse(data); "
                + "    jsonData.priority++; "
                + "    var newData = JSON.stringify(jsonData, null, 2);"
                + "    callServer(newData, myName, graphName, myGraph, ++priority);" //here must be graph variable passing the graph reference
                + "    } else if (priority < 4) {"
                + "    graph.updateOptions( { 'file': JSON.parse(json).data } );"
                + "    if (priority == 2) {"
                + "    graph.updateOptions( { 'colors': ['#B0171F'] }); graph.updateOptions( { 'strokeWidth': 0.75 });"
                + "    } else {"
                + "    graph.updateOptions( { 'colors': ['#9400D3'] }); graph.updateOptions( { 'strokeWidth': 1.0 }); } "
                + "    var jsonData = JSON.parse(data); "
                + "    jsonData.priority++; "
                + "    var newData = JSON.stringify(jsonData, null, 2);"
                + "    callServer(newData, myName, graphName, graph, ++priority);"
                + "    } else {"
                + "    graph.updateOptions( { 'file': JSON.parse(json).data } );"
                + "    graph.updateOptions( { 'colors': ['#0000FF'] });"
                + "    graph.updateOptions( { 'strokeWidth': 1.25 });"
                + "    graph.updateOptions( { 'strokePattern': null })"
                + "    }"
                + "    }\n ";

        JavascriptCodeBox.addLocalCode(success);
    }

    public static String returnSuccesFunctionName() {
        PerformanceWriterImpl perfWriter = new PerformanceWriterImpl();
        return perfWriter.getUniqueInfo(generator) + "success";
    }

    public static String returnDivName() {
        PerformanceWriterImpl perfwWriter = new PerformanceWriterImpl();
        return perfwWriter.getUniqueInfo(generator);
    }

    /**
     * Returns the javascript code to call the server with right data
     */
    public static String returnSuccessButtonHandleFunction() {
        String successFunctionName = returnSuccesFunctionName();
        String graphName = returnDivName();

        //data in JSON format
        String data = returnDataJSON();

        StringBuilder sb = new StringBuilder();
        sb.append("var json = JSON.stringify(" + data + ", null, 2); ");

        //create graph
        sb.append("callServer( json," + successFunctionName + ", \"" + graphName + "\", paramResult.rangeValueName, 1);");

        return sb.toString();
    }
    
    /**
     * Returns code, that creates new Dygraph in given divName
     * @param divName the div, where the graph will be shown
     * @return 
     */
    private static String returnStartGraphCode(String divName) {
        StringBuilder sb = new StringBuilder();
        sb.append(""
                + " var myGraph = new Dygraph("
                + "    document.getElementById(\"" + divName + "\").getElementsByClassName(\"right\")[0].getElementsByClassName(\"graph\")[0], "
                + "    JSON.parse(json).data,"
                + "    {"
                + "      ylabel: 'Elapsed time (' + JSON.parse(json).units + ')',"
                + "      xlabel: graph,"
                + "      strokeWidth: 0.5,"
                + "      colors: ['#FF82AB'],"
                + "      strokePattern: Dygraph.DASHED_LINE,"
                + "      drawPoints : true," 
                + "      pointSize : 2,"
                + "    }"
                + "  );");

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

        //data are in this part stored in an array error on the index 1 (index 0 is reserved for the error message and the index 2 for the range value)
        sb.append("\"data\" :  paramResult.values ");

        sb.append("}");

        return sb.toString();
    }
}
