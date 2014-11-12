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
import cz.cuni.mff.d3s.tools.perfdoc.server.MethodInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.ResultCacheForWeb;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import cz.cuni.mff.d3s.tools.perfdoc.server.HttpExchangeUtils;
import java.util.Collection;

/**
 * Site handler that shows all generators, that have any measured result for the
 * given (tested) method
 *
 * @author Jakub Naplava
 */
public class MethodSiteHandler extends AbstractSiteHandler {

    private static final Logger log = Logger.getLogger(MethodSiteHandler.class.getName());

    @Override
    public void handle(HttpExchange exchange, ResultCacheForWeb res) {
        log.log(Level.INFO, "Got new method-site request. Starting to handle it.");

        String query = exchange.getRequestURI().getQuery();
        MethodInfo methodName;

        try {
            methodName = getMethodFromQuery(query);
        } catch (IllegalArgumentException e) {
            //there is a problem with URL (probably own written URL)
            HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "There was some problem with the URL adress you requested.", 404, log);
            return;
        }

        if (methodName == null) {
            //there is a problem with URL (probably own written URL)
            HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "There was some problem with the URL adress you requested.", 404, log);
            return;
        }

        if (res != null) {
            Collection<MethodInfo> availableGenerators = res.getDistinctGenerators(methodName);

            addCode(returnHeading(methodName));

            String classOutput = formatGenerators(methodName.toString(), availableGenerators);
            addCode(classOutput);
            String output = getCode();

            HttpExchangeUtils.sentSuccesHeaderAndBodyAndClose(exchange, output.getBytes(), log);
        } else {
            //there is no database connection available
            //sending information about internal server error
            HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "Database not available.", 500, log);
        }

        log.log(Level.INFO, "Data were succesfully sent to the user.");
    }

    private String returnHeading(MethodInfo method) {
        String className = method.getQualifiedClassName();
        String methodName = method.getMethodName();

        StringBuilder sb = new StringBuilder();
        sb.append("<p><a href = \"http://localhost:8080/cache\"><-- Back to classes overview </a></p>");
        sb.append("<p><a href = \"class?" + className + "\"><-- Back to class " + className + "</a></p>");
        sb.append("<h1>Method <i>" + methodName + "</i> in class <i>" + className + "</i></h1>");
        sb.append("<h2>with parameters</h2>");

        sb.append("<ul>");
        for (String param : method.getParams()) {
            sb.append("<li>");
            sb.append(param);
            sb.append("</li>");
        }
        sb.append("</ul>");

        sb.append("<h2>has this possible saved generators</h2>");

        return sb.toString();
    }

    private String formatGenerators(String methodName, Collection<MethodInfo> generators) {

        //unable to retrieve data from database
        if (generators == null) {
            return "<p>Sorry, but there was an error when trying to connect to database.</p>";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("<ul>");
        for (MethodInfo generator : generators) {
            String generatorInfo = formatGenerator(generator);
            String methodgeneratorURL = getMethodGeneratorURL(methodName, generator);
            sb.append("<li><a href= \"methodgenerator?" + methodgeneratorURL + "\">" + generatorInfo + "</a></li>");
        }
        sb.append("</ul>");

        return sb.toString();
    }

    private String formatGenerator(MethodInfo generator) {
       
        String methodName = generator.getMethodName();
        String parameterInfo = generator.getParams().get(2);

        return (methodName + "(" + parameterInfo + ")");
    }

    private String getMethodGeneratorURL(String method, MethodInfo generator) {
        String methodQuery = getQueryURL(method);
        String generatorQuery = getQueryURL(generator.toString());

        return (methodQuery + "separator=" + generatorQuery);
    }
}
