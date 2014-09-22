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

package cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.sitehandlers;

import com.sun.net.httpserver.HttpExchange;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.ResultCacheForWeb;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Site handler that shows all measured methods for the given class 
 * 
 * @author Jakub Naplava
 */
public class ClassSiteHandler extends AbstractSiteHandler {

private static final Logger log = Logger.getLogger(ClassSiteHandler.class.getName());
    
    @Override
    public void handle(HttpExchange exchange, ResultCacheForWeb res) {
        log.log(Level.INFO, "Got new class-site request. Starting to handle it.");
        
        String className = exchange.getRequestURI().getQuery();
        
        if (res != null) {

            ArrayList<String> testedMethod = res.getDistinctClassMethods(className);
                        
            addCode(returnHeading(className));
            String classOutput = formatMethods(testedMethod);
            addCode(classOutput);
            String output = getCode();

            try {
                sentSuccesHeaderAndBodyAndClose(exchange, output.getBytes());
            } catch (IOException ex) {
                log.log(Level.INFO, "Unable to send the results to the client", ex);
            } 
        } else {
            //there is no database connection available
            //sending information about internal server error
            try {
                   sentErrorHeaderAndClose(exchange, "Database not available.", 500);
                } catch (IOException ex) {
                    //there is nothing we can do with it
                    log.log(Level.INFO, "An exception occured when trying to close comunnication with client", ex);
                }            
        }
        
        log.log(Level.INFO, "Data were succesfully sent to the user.");
    }
    
    private String returnHeading(String className)
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("<p><a href = \"http://localhost:8080/cache\"><-- Back to classes overview </a></p>");
        sb.append("<h1>Class <i>" + className + "</i></h1>");
        sb.append("<h2>Methods</h2>");
        
        return sb.toString();
    } 
    
    private String formatMethods(ArrayList<String> methods) {
        
        //unable to retrieve data from database
        if (methods == null) {
            return "<p>Sorry, but there was an error when trying to connect to database.</p>";
        }
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("<ul>");
         for (String method : methods) {
             String methodInfo = formatMethod(method);
             String url = getQueryURL(method);
             sb.append("<li><a href= \"method?" + url + "\">" + methodInfo + "</a></li>" );
         }
         sb.append("</ul>");
         
         return sb.toString();
    }
    
    private String formatMethod(String method) {
        String[] chunks = method.split("#");
        
        if (chunks.length < 3) {
            //situation, where we are forcing either some mistake input or method with no parameters
            //if is it method with no params
            if (chunks.length == 2 && method.endsWith("#")) {
                String[] newChunks = new String[] {chunks[0], chunks[1], ""};
                chunks = newChunks;
            }
            else return "";
        }
        String methodName = chunks[1];
        String parameterInfo = getParameterInfo(chunks[2]);
        
        return (methodName + "(" + parameterInfo + ")");
    }
    
    private String getParameterInfo(String parameter) {
        String[] parameters = parameter.split("@");
        
        StringBuilder sb = new StringBuilder();
        
        for (int i = 1; i<parameters.length-1; i++) {
            sb.append(parameters[i] + ",");
        }
        
        sb.append(parameters[parameters.length - 1]);
        
        return sb.toString();
    }
}
