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
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author arahusky
 */
public class DetailedSiteHandler extends AbstractSiteHandler {

    private static final Logger log = Logger.getLogger(DetailedSiteHandler.class.getName());

    @Override
    public void handle(HttpExchange exchange, ResultCacheForWeb res) {
        log.log(Level.INFO, "Got new detailed request. Starting to handle it.");

        String query = exchange.getRequestURI().getQuery();
        String[] data = getData(query);

        if (data.length != 3) {
            try {
                sentErrorHeaderAndClose(exchange, "There was some problem with the URL adress you requested.", 404);
            } catch (IOException ex) {
                //there is nothing we can do with it
                log.log(Level.INFO, "An exception occured when trying to close comunnication with client", ex);
            }
            return;
        }

        if (res != null) {

            String testedMethod = getMethodFromQuery(data[0]);
            String generator = getMethodFromQuery(data[1]);
            String parameters = data[2];

            //adding links to JQquery in order to be able to use sort
            addToHeader("<script src=\"http://code.jquery.com/jquery-1.10.2.js\"></script>");
            addToHeader("<script src=\"http://code.jquery.com/ui/1.10.4/jquery-ui.js\"></script>");
            
            addCode(getBody(testedMethod, generator, parameters, res));
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

    private String[] getData(String query) {
        return query.split("separator=");
    }

    public String getBody(String testedMethod, String generator, String parameters, ResultCacheForWeb res) {
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
        int range = getRangeValue(parameters, genParameters);        

        if (genParametersText == null || (range == -1)) {
            return sb.toString();
        }
        
        double min = Double.parseDouble(parameters.split(",")[range].split("_to_")[0]);
        double max = Double.parseDouble(parameters.split(",")[range].split("_to_")[1]);
        System.out.println("min: " + min);
        System.out.println("max: " + max);

        sb.append("<table border = \"1\"><thead><tr>");

        sb.append("<td>");
        sb.append(genParametersText[range] + " (" + genParameters[range + 2] + ")");
        sb.append("</td>");

        sb.append("<td>number of measurements</td>");
        sb.append("<td>time (ms)</td></tr></thead>");

        List<Map<String, Object>> list = res.getResults(testedMethod, generator);

        sb.append("<tbody>");
        if (list != null) {
            for (Map<String, Object> map : list) {
                
                String data = (String) map.get("data");
                String[] datas = data.split(";");
                double value = Double.parseDouble(datas[range]);
                
                if (value > max || value < min) {
                    continue;
                }
                
                sb.append("<tr>");
                sb.append("<td>" + datas[range] + "</td>");

                int numberOfMeasurements = (int) map.get("numberOfMeasurements");
                sb.append("<td>" + numberOfMeasurements + "</td>");

                long time = (long) map.get("time");
                sb.append("<td>" + time + "</td>");

                sb.append("</tr>");
            }
        }

        sb.append("</tbody></table>");

        return sb.toString();
    }

    int getRangeValue(String parameters, String[] paramTypeNames) {
        for (int i = 2; i < paramTypeNames.length; i++) {
            System.out.println(paramTypeNames[i]);
        }
        String[] arrParams = parameters.split(",");

        //first two paramTypeNames are Workloads
        for (int i = 2; i < paramTypeNames.length; i++) {
            String s = paramTypeNames[i];
            if (s.equals("int") || s.equals("double") || s.equals("float")) {
                String parameter = arrParams[i - 2];

                if (parameter.contains("_to_")) {
                    String[] parts = parameter.split("_to_");
                    if (parts.length == 2) {
                        if (!parts[0].equals(parts[1])) {
                            return i-2;
                        }
                    }
                }
            }
        }

        return -1;
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

    private String[] getDescriptions(String className, String methodName) {
        ClassParser cp;
        try {
            cp = new ClassParser(className);
            Method m = cp.findMethod(new MethodInfo(methodName));

            Parameter[] parameters = m.getParameters();
            String[] result = new String[parameters.length - 2];

            //first two parameters are Workload and ServiceWorkload
            for (int i = 2; i < parameters.length; i++) {
                Parameter p = parameters[i];
                Annotation[] annot = p.getAnnotations();

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
