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
import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamDesc;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum;
import cz.cuni.mff.d3s.tools.perfdoc.server.ClassParser;
import cz.cuni.mff.d3s.tools.perfdoc.server.MethodInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.ResultCacheForWeb;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            try {
                sentErrorHeaderAndClose(exchange, "The URL adress you passes seems not be correct.", 404);                
            } catch (IOException ex) {
                Logger.getLogger(MethodGeneratorSiteHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }

        String testedMethod = getMethodFromQuery(methods[0]);
        String generator = getMethodFromQuery(methods[1]);

        if (res != null) {

            addCode(returnHeading(methods[0], testedMethod, generator));
            addCode(getBody(testedMethod, generator, res));
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

    private String returnHeading(String testedMethodNet, String testedMethod, String generator) {
        String[] testedMethodChunks = testedMethod.split("#");
        String[] generatorChunks = generator.split("#");

        if ((testedMethodChunks.length < 2) || (generatorChunks.length < 2)) {            
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<p><a href = \"http://localhost:8080/cache\"><-- Back to classes overview </a></p>");
        sb.append("<p><a href = \"class?" + testedMethodChunks[0] + "\"><-- Back to class " + testedMethodChunks[0] + "</a></p>");
        sb.append("<p><a href = \"method?" + testedMethodNet + "\"><-- Back to method " + testedMethodChunks[1] + "</a></p>");
        sb.append("<h1>Method <i>" + testedMethodChunks[1] + "</i> with generator <i>" + generatorChunks[1] + "</i></h1>");

        return sb.toString();
    }

    public String getBody(String testedMethod, String generator, ResultCacheForWeb res) {
        String[] testedMethodChunks = testedMethod.split("#");
        String[] generatorChunks = generator.split("#");
        
        if (testedMethodChunks.length < 2 || generatorChunks.length < 2) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("<h3>Tested method:</h3>"
                + "<ul>"
                + "<li>Method name: " + testedMethodChunks[1] + "</li>"
                + "<li>Containing class: " + testedMethodChunks[0] + "</li>"
                + "<li>Parameters: " + getParameterInfo(testedMethod) + "</li>"
                + "</ul>");

        String genParams = getParameterInfo(generator);
        sb.append("<h3>Generator:</h3>"
                + "<ul>"
                + "<li>Method name: " + generatorChunks[1] + "</li>"
                + "<li>Containing class: " + generatorChunks[0] + "</li>"
                + "<li>Parameters: " + genParams + "</li>"
                + "</ul>");
        sb.append("<h3>Measurements:<h3>");

        String[] genParameters = genParams.split(",");
        String[] genParametersText = getDescriptions(generatorChunks[0], generator);

        if (genParametersText == null) {
            //TODO return some error
            return sb.toString();
        }
        
        sb.append("<table border = \"1\"><tr>");
        
        for (int i = 0; i < genParametersText.length; i++) {
            sb.append("<td>");
            sb.append(genParametersText[i] + " (" + genParameters[i+2] + ")");
            sb.append("</td>");
        }
        
        System.out.println("sitlll");
        sb.append("<td>number of measurements</td>");
        sb.append("<td>time (ms)</td></tr>");

        List<Map<String, Object>> list = res.getResults(testedMethod, generator);

        if (list != null) {
            for (Map<String, Object> map : list) {
                sb.append("<tr>");

                String data = (String) map.get("data");
                String[] datas = data.split(";");
                for (String datum : datas) {
                    sb.append("<td>" + datum + "</td>");
                }

                int numberOfMeasurements = (int) map.get("numberOfMeasurements");
                sb.append("<td>" + numberOfMeasurements + "</td>");

                long time = (long) map.get("time");
                sb.append("<td>" + time + "</td>");

                sb.append("</tr>");
            }
        }

        sb.append("</table>");

        return sb.toString();
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

    private String[] getMethods(String query) {
        return query.split("separator=");
    }

    private String[] getDescriptions(String className, String methodName) {
        ClassParser cp;
        try {
            cp = new ClassParser(className);
            Method m = cp.findMethod(new MethodInfo(methodName));

            Annotation[][] annotations = m.getParameterAnnotations();
            String[] result = new String[annotations.length - 2];

            //first two parameters are Workload and ServiceWorkload
            for (int i = 2; i < annotations.length; i++) {;
                Annotation[] annot = annotations[i];

                for (Annotation a : annot) {
                    if ("cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum".equals(a.annotationType().getName())) {
                        result[i - 2] = ((ParamNum) a).description();
                    } else if ("cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamDesc".equals(a.annotationType().getName())) {
                        result[i - 2] = ((ParamDesc) a).description();
                        System.out.println(((ParamDesc) a).description());
                    }
                }
            }

            return result;
        } catch (ClassNotFoundException | IOException ex) {
            log.log(Level.INFO, "Unable to find some class", ex);
        }

        return null;
    }
}
