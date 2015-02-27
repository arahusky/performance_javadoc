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
 * Site handler that is shows specific results for given method, workload and
 * some of the workload argument values.
 *
 * @author Jakub Naplava
 */
public class DetailedSiteHandler extends AbstractSiteHandler {

    private static final Logger log = Logger.getLogger(DetailedSiteHandler.class.getName());

    @Override
    public void handle(HttpExchange exchange, ResultCacheForWeb res) {
        log.log(Level.INFO, "Got new detailed request. Starting to handle it.");
        
        //URL adress should be in format: .../detailed?methodseparator=workloadseparator=workloadArgs
        String query = exchange.getRequestURI().getQuery();
        String[] data = getData(query);

        //the data array should contain: method, workload, workloadArguments
        if (data.length != 3) {
            HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "There was some problem with the URL adress you requested.", 404, log);
            return;
        }

        if (res != null) {
            try {
                MethodInfo testedMethod;
                MethodInfo generator;
                
                try {
                    testedMethod = getMethodFromQuery(data[0]);
                    generator = getMethodFromQuery(data[1]);
                } catch (IllegalArgumentException e) {
                    HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "There was some problem with the URL adress you requested.", 404, log);
                    return;
                }
                String parameters = data[2];
                
                //adding links to JQquery in order to be able to use sort
                addToHeader("<script src=\"http://code.jquery.com/jquery-1.10.2.js\"></script>");
                addToHeader("<script src=\"http://code.jquery.com/ui/1.10.4/jquery-ui.js\"></script>");
                addToHeader("<script src=\"js?tablesorter.js\"></script>");
                addToHeader("<script>$(document).ready(function()  { "
                        + "        $(\"#myTable\").tablesorter(); } ); </script> ");
                
                addCode(getBody(testedMethod, generator, parameters, res));
                String output = getCode();
                
                HttpExchangeUtils.sentSuccesHeaderAndBodyAndClose(exchange, output.getBytes(), log);
            } catch (ClassNotFoundException | IOException | NoSuchMethodException ex) {
                HttpExchangeUtils.sentErrorHeaderAndClose(exchange, ex.getMessage(), 500, log);
                return;
            }
        } else {
            //there is no database connection available
            //sending information about internal server error
            HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "Database not available.", 500, log);
        }

        log.log(Level.INFO, "Data were succesfully sent to the user.");
    }

    private String[] getData(String query) {
        return query.split("separator=");
    }

    public String getBody(MethodInfo testedMethod, MethodInfo generator, String parameters, ResultCacheForWeb res) throws ClassNotFoundException, IOException, NoSuchMethodException {
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
            log.log(Level.INFO, "User obtained method that is not actually present on the server.");
            sb.append("Sorry, but the requested method is not actually present on the server.");
            return sb.toString();
        }
        String[] genParametersText = AnnotationWorker.geParameterDescriptions(generatorMethod);
        int range = getRangeValue(parameters, generator.getParams());
        
        if (genParametersText == null || (range == -1)) {
            return sb.toString();
        }
        
        String[] normalizedParameters = normalizeParameters(parameters, range, generator.getParams());
        
        if (normalizedParameters == null) {
            return sb.toString();
        }
        
        double min = Double.parseDouble(parameters.split(",")[range].split("_to_")[0]);
        double max = Double.parseDouble(parameters.split(",")[range].split("_to_")[1]);

        sb.append("<table border = \"1\" class=\"tablesorter\" id = \"myTable\"><thead><tr>");

        sb.append("<th>");
        sb.append(genParametersText[range] + " (" + generator.getParams().get(range + 2) + ")");
        sb.append("</th>");

        sb.append("<th>time (ns)</th></tr></thead>");
        
        Collection<BenchmarkResult> list = res.getResults(testedMethod, generator);
        
        sb.append("<tbody>");
        if (list != null) {
            for (BenchmarkResult item : list) {
                sb.append(getRowIfPass(normalizedParameters, item, min, max, range));
            }
        }
        
        sb.append("</tbody></table>");

        return sb.toString();
    }

    String getRowIfPass(String[] normalizedData, BenchmarkResult resultItem, double min, double max, int rangeValue) {
        StringBuilder sb = new StringBuilder();
        Object[] data = resultItem.getBenchmarkSetting().getWorkloadArguments().getValues();
        for (int i = 0; i < data.length; i++) {
            if (i != rangeValue) {
                if (!data[i].equals(normalizedData[i])) {
                    return "";
                }
            }
        }
        double value = Double.parseDouble(data[rangeValue].toString());
        if (value > max || value < min) {
            return "";
        }
        sb.append("<tr>");
        sb.append("<td>").append(data[rangeValue]).append("</td>");

        long time = resultItem.getStatistics().computeMean();
        sb.append("<td>").append(time).append("</td>");

        sb.append("</tr>");
        return sb.toString();
    }

    int getRangeValue(String parameters, List<String> paramTypeNames) {
        String[] arrParams = parameters.split(",");

        //first two paramTypeNames are Workloads
        for (int i = 2; i < paramTypeNames.size(); i++) {
            String s = paramTypeNames.get(i);
            if (s.equals("int") || s.equals("double") || s.equals("float")) {
                String parameter = arrParams[i - 2];

                if (parameter.contains("_to_")) {
                    String[] parts = parameter.split("_to_");
                    if (parts.length == 2) {
                        if (!parts[0].equals(parts[1])) {
                            return i - 2;
                        }
                    }
                }
            }
        }

        return -1;
    }

    String[] normalizeParameters(String params, int rangeValue, List<String> paramTypeNames) {
        String[] paramsArr = params.split(",");
        String[] res = new String[paramsArr.length];

        //first two paramTypeNames are Workloads
        for (int i = 2; i < paramTypeNames.size(); i++) {
            if (i == rangeValue + 2) {
                res[i - 2] = paramsArr[i - 2];
                continue;
            }

            String s = paramTypeNames.get(i);
            String parameter = paramsArr[i - 2];

            if (s.equals("int") || s.equals("double") || s.equals("float")) {

                if (parameter.contains("_to_")) {
                    String[] parts = parameter.split("_to_");
                    if (parts.length == 2) {
                        if (!parts[0].equals(parts[1])) {
                            return null;
                        }

                        switch (s) {
                            case "int":
                                res[i - 2] = Integer.parseInt(parts[0]) + "";
                                break;
                            case "float":
                                res[i - 2] = Float.parseFloat(parts[0]) + "";
                                break;
                            case "double":
                                res[i - 2] = Double.parseDouble(parts[0]) + "";
                                break;
                        }
                    }
                } else {
                    res[i - 2] = parameter;
                }
            } else {
                res[i - 2] = parameter;
            }
        }
        return res;
    }
}
