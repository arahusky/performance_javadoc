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
 *
 * @author arahusky
 */
public class MethodSiteHandler extends AbstractSiteHandler {

    private static final Logger log = Logger.getLogger(MethodSiteHandler.class.getName());

    @Override
    public void handle(HttpExchange exchange, ResultCacheForWeb res) {
        log.log(Level.INFO, "Got new method-site request. Starting to handle it.");

        String query = exchange.getRequestURI().getQuery();
        String methodName = getMethodFromQuery(query);
        
        if (methodName == null) {
            //there was some problem with URL (probably own written URL)
            try {
                sentErrorHeaderAndClose(exchange, "There was some problem with the URL adress you requested.", 404);
            } catch (IOException ex) {
                //there is nothing we can do with it
                log.log(Level.INFO, "An exception occured when trying to close comunnication with client", ex);
            }            
            return;
        }        

        if (res != null) {

            ArrayList<String> availableGenerators = res.getDistinctGenerators(methodName);

            addCode(returnHeading(methodName));
            String classOutput = formatGenerators(methodName, availableGenerators);
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

    private String returnHeading(String method) {

        String[] chunks = method.split("#");
        
        if (chunks.length < 2) {
            return "";
        }
        
        String className = chunks[0];
        String methodName = chunks[1];

        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Method <i>" + methodName + "</i> in class <i>" + className + "</i></h1>");
        sb.append("<h2>with parameters</h2>");
        
        sb.append("<ul>");
        for (int i = 2; i<chunks.length; i++) {
            sb.append("<li>");
            sb.append(chunks[i].substring(1));
            sb.append("</li>");
        }
        sb.append("</ul>");
        
        sb.append("<h2>has this possible saved generators</h2>");

        return sb.toString();
    }

    private String formatGenerators(String methodName, ArrayList<String> generators) {

        //unable to retrieve data from database
        if (generators == null) {
            return "<p>Sorry, but there was an error when trying to connect to database.</p>";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("<ul>");
        for (String generator : generators) {
            String generatorInfo = formatGenerator(generator);
            String methodgeneratorURL = getMethodGeneratorURL(methodName, generator);
            sb.append("<li><a href= \"methodgenerator?" + methodgeneratorURL + "\">" + generatorInfo + "</a></li>");
        }
        sb.append("</ul>");

        return sb.toString();
    }

    private String formatGenerator(String generator) {
        String[] chunks = generator.split("#");
        
        if (chunks.length < 3) {
            return "";
        }

        String methodName = chunks[1];
        String parameterInfo = getParameterInfo(chunks[2]);

        return (methodName + "(" + parameterInfo + ")");
    }

    private String getParameterInfo(String parameter) {
        String[] parameters = parameter.split("@");

        StringBuilder sb = new StringBuilder();

        for (int i = 1; i < parameters.length - 1; i++) {
            sb.append(parameters[i] + ",");
        }

        sb.append(parameters[parameters.length - 1]);

        return sb.toString();
    }
    
    private String getMethodGeneratorURL(String method, String generator) {
        String methodQuery = getQueryURL(method);
        String generatorQuery = getQueryURL(generator);
        
        return (methodQuery + "separator=" + generatorQuery);
    }
}
