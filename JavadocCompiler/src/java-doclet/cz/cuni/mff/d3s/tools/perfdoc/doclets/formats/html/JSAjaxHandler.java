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
 * @author arahusky
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
        String pattern = "function callServer(data, successFunc, graphName) {"
                + "   $.ajax({"
                + "   url: \"" + serverAddress + "\","
                + "   data: data,"
                + "   type: \"POST\","
                + "   success: function(json) {successFunc(json, data, successFunc, graphName)},"
                + "   error: function( xhr, status, errorThrown ) { printAjaxError(xhr, status, errorThrown); }"
                + "});"
                + "  }\n";

        return pattern;
    }

    /**
     * Returns the (global) javascript function, that tells the user about the
     * occured error
     */
    public static String returnErrorFunction() {
        String errorFunction = " function printAjaxError(xhr, status, errorThrown) {"
                + " alert( \"Sorry, there was a problem! (More informations in debugger console.)\" );"
                + " console.log( \"Error: \" + errorThrown );"
                + " console.log( \"Status: \" + status );"
                + " console.dir( xhr ); }\n";

        return errorFunction;
    }

    /**
     * To the local javascript code adds the succesfunction for current
     * generator
     */
    public static void addSuccessFunction() {
        String successFunctionName = returnSuccesFunctionName();

        String success = "function " + successFunctionName + "(json, data, myName, graphName) {"
                + "  	//show in appropriate format to user \n"
                + "    alert(json);"
                + "    //if not enough data\n"
                + "    //callServer(data, myName, graphName);}\n "
                + " }\n";

        JavascriptCodeBox.addLocalCode(success);
    }

    public static String returnSuccesFunctionName() {
        return generator + "success";
    }

    public static String returnGraphName() {
        return generator + "graph";
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
        sb.append("callServer( json," + successFunctionName + ", \"" + graphName + "\");");

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

        //data are in this part stored in an array error on the index 1 (index 0 is reserved for the error message and the index 2 for the range value)
        sb.append("\"data\" :  error[1] ");

        sb.append("}");

        return sb.toString();
    }
}
