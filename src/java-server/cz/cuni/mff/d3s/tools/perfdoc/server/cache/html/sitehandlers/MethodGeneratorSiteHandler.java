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
import cz.cuni.mff.d3s.tools.perfdoc.annotations.workers.AnnotationWorker;
import cz.cuni.mff.d3s.tools.perfdoc.server.MethodInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.MethodReflectionInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.ResultCacheForWeb;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkResult;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import cz.cuni.mff.d3s.tools.perfdoc.server.HttpExchangeUtils;
import java.util.Collection;

/**
 * Site handler that shows all results for given tested method and its generator
 *
 * @author Jakub Naplava
 */
public class MethodGeneratorSiteHandler extends AbstractSiteHandler {

    private static final Logger log = Logger.getLogger(MethodGeneratorSiteHandler.class.getName());

    @Override
    public void handle(HttpExchange exchange, ResultCacheForWeb res) {
        log.log(Level.INFO, "Got new method-generator-site request. Starting to handle it.");

        String query = exchange.getRequestURI().getQuery();
        String[] methods = getMethods(query);

        if ((methods == null) || (methods.length != 2)) {
            HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "The URL adress you passed seems to be incorrect.", 404, log);
            return;
        }
        
        if (res != null) {
            MethodInfo testedMethod;
            MethodInfo generator;

            try {
                testedMethod = getMethodFromQuery(methods[0]);
                generator = getMethodFromQuery(methods[1]);
            } catch (IllegalArgumentException e) {
                HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "The URL adress you passed seems to be incorrect.", 404, log);
                return;
            }

            //adding links to JQquery in order to be able to use sort
            addToHeader("<script src=\"http://code.jquery.com/jquery-1.10.2.js\"></script>");
            addToHeader("<script src=\"http://code.jquery.com/ui/1.10.4/jquery-ui.js\"></script>");
            addToHeader("<script src=\"js?tablesorter.js\"></script>");
            addToHeader("<script>$(document).ready(function()  { "
                    + "        $(\"#myTable\").tablesorter(); } ); </script> ");

            addCode(returnHeading(methods[0], testedMethod, generator));
            addCode(getBody(testedMethod, generator, res));
            String output = getCode();

            HttpExchangeUtils.sentSuccesHeaderAndBodyAndClose(exchange, output.getBytes(), log);
        } else {
            //there is no database connection available
            //sending information about internal server error
            HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "Database not available.", 500, log);
        }

        log.log(Level.INFO, "Data were succesfully sent to the user.");
    }

    private String returnHeading(String testedMethodNet, MethodInfo testedMethod, MethodInfo generator) {
        StringBuilder sb = new StringBuilder();
        sb.append("<p><a href = \"http://localhost:8080/cache\"><-- Back to classes overview </a></p>");
        sb.append("<p><a href = \"class?" + testedMethod.getQualifiedClassName() + "\"><-- Back to class " + testedMethod.getQualifiedClassName() + "</a></p>");
        sb.append("<p><a href = \"method?" + testedMethodNet + "\"><-- Back to method " + testedMethod.getMethodName() + "</a></p>");
        sb.append("<h1>Method <i>" + testedMethod.getMethodName() + "</i> with generator <i>" + generator.getMethodName() + "</i></h1>");

        return sb.toString();
    }

    public String getBody(MethodInfo testedMethod, MethodInfo generator, ResultCacheForWeb res) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h3>Tested method:</h3>"
                + "<ul>"
                + "<li>Method name: " + testedMethod.getMethodName() + "</li>"
                + "<li>Containing class: " + testedMethod.getQualifiedClassName() + "</li>"
                + "<li>Parameters: " + chainParameters(testedMethod.getParams()) + "</li>"
                + "</ul>");
        sb.append("<h3>Generator:</h3>"
                + "<ul>"
                + "<li>Method name: " + generator.getMethodName() + "</li>"
                + "<li>Containing class: " + generator.getQualifiedClassName() + "</li>"
                + "<li>Parameters: " + chainParameters(generator.getParams()) + "</li>"
                + "</ul>");
        sb.append("<h3>Measurements:<h3>");
        
        Method generatorMethod;
        try {
            generatorMethod = new MethodReflectionInfo(generator.toString()).getMethod();
        } catch (ClassNotFoundException | IOException ex) {
            log.log(Level.INFO, "User obtained method that does not exist.");
            return sb.toString();
        }
        String[] genParametersText = AnnotationWorker.geParameterDescriptions(generatorMethod);
        if (genParametersText == null) {
            return sb.toString();
        }
        
        sb.append("<table border = \"1\" class=\"tablesorter\" id = \"myTable\"><thead><tr>");

        for (int i = 0; i < genParametersText.length; i++) {
            sb.append("<th>");
            sb.append(genParametersText[i] + " (" + generator.getParams().get(i + 2) + ")");
            sb.append("</th>");
        }
        sb.append("<th>number of measurements</th>");
        sb.append("<th>time (ns)</th></tr></thead><tbody>");

        Collection<BenchmarkResult> list = res.getResults(testedMethod, generator);
       
        if (list != null) {
            for (BenchmarkResult resultItem : list) {
                sb.append("<tr>");

                Object[] data = resultItem.getBenchmarkSetting().getWorkloadArguments().getValues();
                for (Object datum : data) {
                    sb.append("<td>").append(datum).append("</td>");
                }

                //TODO proper statistics -> uncomment and delete
                //int numberOfMeasurements = resultItem.getStatistics().getNumberOfMeasurements();
                int numberOfMeasurements = resultItem.getBenchmarkSetting().getPriority();
                sb.append("<td>").append(numberOfMeasurements).append("</td>");

                long time = resultItem.getStatistics().compute();
                sb.append("<td>").append(time).append("</td>");

                sb.append("</tr>");
            }
        }

        sb.append("</tbody></table>");

        return sb.toString();
    }

    private String[] getMethods(String query) {
        return query.split("separator=");
    }
}
