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

    public static String addCallServerFunction(String serverAddress) {               
        String pattern = "function callServer(data, successFunc, graphName) {"
                + "   $.ajax({"
                + "   url: \"" + serverAddress +  "\","
                + "   data: data,"
                + "   type: \"POST\","
                + "   success: function(json) {successFunc(json)},"
                + "   error: function( xhr, status, errorThrown ) { printAjaxError(xhr, status, errorThrown); }"
                + "});"
                + "  }\n";

        return pattern;
    }

    public static String addErrorFunction() {
        String errorFunction = " function printAjaxError(xhr, status, errorThrown) {"
                + " alert( \"Sorry, there was a problem! (More informations in debugger console.)\" );"
                + " console.log( \"Error: \" + errorThrown );"
                + " console.log( \"Status: \" + status );"
                + " console.dir( xhr ); }\n";

        return errorFunction;
    }

    public static void addSuccessFunction() {
        String successFunctionName = returnSuccesFunctionName();
                
        String success = "function " + successFunctionName + "(json, data, myName, graphName) {"
                + "  	//show in appropriate format to user \n"
                + "    alert(data);"
                + "    //if not enough data\n"
                + "    //callServer(data, myName, graphName);}\n "
                + " }\n";
        
        JavascriptCodeBox.addLocalCode(success);
    }
    
    public static String returnSuccesFunctionName()
    {
        return generator + "success";
    }
    
    public static String returnGraphName()
    {
        return generator + "graph";
    }
    
    public static String returnSuccessButtonHandleFunction()
    {
        String successFunctionName = returnSuccesFunctionName();
        
        //TODO possibly change to something get from some function globally
        String graphName = returnGraphName();
        
        //data in JSON format
        String data = returnDataJSON();
        
        return ("callServer( " + data + "," + successFunctionName + ", \"" + graphName + "\");");
    }
    
    private static String returnDataJSON()
    {        
        StringBuilder sb = new StringBuilder();
        
        sb.append("{");
        sb.append("\"testedMethod\" : \"" + returnSuccesFunctionName() + "\",");
        sb.append("\"generator\" : \"" + generator + "\",");
        sb.append("\"data\" : [ error[1] ]");
        
        sb.append("}");
        
        return sb.toString();
    }    
}
