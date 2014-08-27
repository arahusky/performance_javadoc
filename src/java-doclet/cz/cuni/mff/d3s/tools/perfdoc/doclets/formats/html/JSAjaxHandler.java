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
                + "   error: function( xhr, status, errorThrown ) { printAjaxError(xhr, status, graphName, errorThrown); }"
                + "});"
                + "  }\n";

        return pattern;
    }

    /**
     * Returns the (global) javascript function, that tells the user about the
     * occured error
     */
    public static String returnErrorFunction() {
        String errorFunction = " function printAjaxError(xhr, status, graphName, errorThrown) {"
                + " alert( \"Sorry, there was a problem! Detailed information can be found in debugger console.\" );"
                + " console.log( \"Error: \" + errorThrown );"
                + " console.log( \"Status: \" + status );"
                + " console.dir( xhr );"
                + " if (xhr.status == 0) { $(\"#\" + graphName + \" .right\").text(\"Server is shut-down, or could not connect to him.\"); }"
                + "else { $(\"#\" + graphName + \" .right\").text(xhr.status + \": \" + xhr.responseText); }"
                + " }\n";

        return errorFunction;
    }

    /**
     * To the local javascript code adds the succesfunction for current
     * generator
     */
    public static void addSuccessFunction() {
        String successFunctionName = returnSuccesFunctionName();

        String success = "function " + successFunctionName + "(json, data, myName, graphName, graph, priority) {"
                + "  	//show in appropriate format to user \n"
                //+ "    alert(json);"
                //+ "    alert(priority);"
                + "    if (priority == 1) {"
                +      returnStartGraphCode(returnGraphName(), "some x-value")
                + "    var jsonData = JSON.parse(data); "
                + "    jsonData.priority++; "
                + "    var newData = JSON.stringify(jsonData, null, 2);"
                + "    callServer(newData, myName, graphName, myGraph, ++priority);" //here must be graph variable passing the graph reference
                + "    } else if (priority < 4) {"
                + "    graph.updateOptions( { 'file': JSON.parse(json).data } );"
                + "    var jsonData = JSON.parse(data); "
                + "    jsonData.priority++; "
                + "    var newData = JSON.stringify(jsonData, null, 2);"
                + "    callServer(newData, myName, graphName, graph, ++priority);"
                + "    } else {"
                + "    graph.updateOptions( { 'file': JSON.parse(json).data } );"
                + "    alert(\"Measurement done\");"
                + "    }"
                + "    }\n ";

        JavascriptCodeBox.addLocalCode(success);
    }

    public static String returnSuccesFunctionName() {
        PerformanceWriterImpl perfWriter = new PerformanceWriterImpl();
        return perfWriter.getUniqueInfo(generator) + "success";
    }

    public static String returnGraphName() {
        PerformanceWriterImpl perfwWriter = new PerformanceWriterImpl();
        return perfwWriter.getUniqueInfo(generator);
    }

    /**
     * Returns the javascript code to call the server with right data
     */
    public static String returnSuccessButtonHandleFunction() {
        String successFunctionName = returnSuccesFunctionName();
        String graphName = returnGraphName();

        //data in JSON format
        String data = returnDataJSON();

        StringBuilder sb = new StringBuilder();
        sb.append("var json = JSON.stringify(" + data + ", null, 2); ");

        //create graph
        //sb.append(returnStartGraphCode(graphName, "Some x-value"));
        
        sb.append("callServer( json," + successFunctionName + ", \"" + graphName + "\", null, 1);");

        return sb.toString();
    }

    private static String returnStartGraphCode(String graphName, String xAxisName) {
        StringBuilder sb = new StringBuilder();
        sb.append(""
                + " var myGraph = new Dygraph("
                + "    document.getElementById(\""+ graphName + "\").getElementsByClassName(\"right\")[0], "
                + "    JSON.parse(json).data," 
                + "    {"
                + "      ylabel: 'Search time (ms)',"
                + "      xlabel: '" + xAxisName + "'"
                + "    }"
                + "  );");

        return sb.toString();
    }

    /**
     * Returns the JSON format data
     */
    private static String returnDataJSON() {
        StringBuilder sb = new StringBuilder();

        sb.append("{");
        sb.append("\"testedMethod\" : \"" + testedMethod + "\",");
        sb.append("\"generator\" : \"" + generator + "\",");
        sb.append("\"rangeValue\" : error[2],");
        sb.append("\"priority\" : 1,");

        //data are in this part stored in an array error on the index 1 (index 0 is reserved for the error message and the index 2 for the range value)
        sb.append("\"data\" :  error[1] ");

        sb.append("}");

        return sb.toString();
    }
}
