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
import cz.cuni.mff.d3s.tools.perfdoc.annotations.AnnotationUtils;
import cz.cuni.mff.d3s.tools.perfdoc.server.HttpExchangeUtils;
import cz.cuni.mff.d3s.tools.perfdoc.server.MethodInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.MethodReflectionInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.ResultCacheForWeb;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkResult;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.MeasurementQuality;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.velocity.VelocityContext;

/**
 * Site handler that is shows specific results for given measuredMethod, generator and
 * some of the generator argument values.
 *
 * @author Jakub Naplava
 */
public class DetailedSiteHandler implements SiteHandler {

    private static final Logger log = Logger.getLogger(DetailedSiteHandler.class.getName());
    
    private static final String templateName = "detailed";

    @Override
    public void handle(HttpExchange exchange, ResultCacheForWeb res) {
        log.log(Level.INFO, "Got new detailed request. Starting to handle it.");
        
        //URL adress should be in format: .../detailed?methodseparator=generatorseparator=generatorArgs
        String query = exchange.getRequestURI().getQuery();
        String[] data = getData(query);

        //the data array should contain: measuredMethod, generator, generatorArguments
        if (data.length != 3) {
            HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "There was some problem with the URL adress you requested.", 404, log);
            return;
        }

        if (res != null) {
            try {
                MethodInfo testedMethod;
                MethodInfo generator;
                
                try {
                    testedMethod = SiteHandlingUtils.getMethodFromQuery(data[0]);
                    generator = SiteHandlingUtils.getMethodFromQuery(data[1]);
                } catch (IllegalArgumentException e) {
                    HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "There was some problem with the URL adress you requested.", 404, log);
                    return;
                }
                String parameters = data[2];
                
                VelocityContext context = new VelocityContext();

                context.put("methodName", testedMethod.getMethodName());
                context.put("methodClassName", testedMethod.getQualifiedClassName());
                context.put("methodParameters", SiteHandlingUtils.chainParameters(testedMethod.getParams()));

                context.put("generatorName", generator.getMethodName());
                context.put("generatorClass", generator.getQualifiedClassName());
                context.put("generatorParameters", SiteHandlingUtils.chainParameters(generator.getParams()));
                
                List<String> tHeads = getTHeads(generator, parameters);
                context.put("theads",tHeads);
                
                List<List<Object>> measurements = getMeasurements(testedMethod, generator, parameters, res);
                context.put("measurements", measurements);
                
                HttpExchangeUtils.mergeTemplateAndSentPositiveResponseAndClose(exchange, templateName, context);
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

    private List<String> getTHeads(MethodInfo generator, String parameters) throws NoSuchMethodException {
        List<String> tHeads = new ArrayList<>();
        
        StringBuilder sb = new StringBuilder();

        Method generatorMethod;
        try {
            generatorMethod = new MethodReflectionInfo(generator.toString()).getMethod();
        } catch (ClassNotFoundException | IOException ex) {
            log.log(Level.INFO, "User obtained method that is not actually present on the server.");
            sb.append("Sorry, but the requested method is not actually present on the server.");
            return null;
        }
        String[] genParametersText = AnnotationUtils.geParameterDescriptions(generatorMethod);
        int range = getRangeValue(parameters, generator.getParams());
        
        if (genParametersText == null || (range == -1)) {
            return null;
        }
        
        tHeads.add(genParametersText[range] + " (" + generator.getParams().get(range + 2) + ")");
        
        tHeads.add("time (ns)");
        tHeads.add("warmupTime");
        tHeads.add("warmupCycles");
        tHeads.add("measurementTime");        
        tHeads.add("measurementCycles");
        
        return tHeads;
    }
    
    private List<List<Object>> getMeasurements(MethodInfo testedMethod, MethodInfo generator, String parameters, ResultCacheForWeb res) throws ClassNotFoundException, IOException, NoSuchMethodException {
        List<List<Object>> list = new ArrayList<>();
        
        int range = getRangeValue(parameters, generator.getParams());
        String[] normalizedParameters = normalizeParameters(parameters, range, generator.getParams());
        
        if (normalizedParameters == null) {
            return null;
        }
        
        double min = Double.parseDouble(parameters.split(",")[range].split("_to_")[0]);
        double max = Double.parseDouble(parameters.split(",")[range].split("_to_")[1]);
        
        Collection<BenchmarkResult> benchmarkResults = res.getResults(testedMethod, generator);
        
        if (benchmarkResults != null) {
            for (BenchmarkResult item : benchmarkResults) {
                list.add(getRowIfPass(normalizedParameters, item, min, max, range));
            }
        }
        
        return list;
    }

    List<Object> getRowIfPass(String[] normalizedData, BenchmarkResult resultItem, double min, double max, int rangeValue) {
        List<Object> list = new ArrayList<>();
        
        Object[] data = resultItem.getBenchmarkSetting().getGeneratorArguments().getValues();
        for (int i = 0; i < data.length; i++) {
            if (i != rangeValue) {
                if (!data[i].equals(normalizedData[i])) {
                    //TODO
                    return null;
                }
            }
        }
        double value = Double.parseDouble(data[rangeValue].toString());
        if (value > max || value < min) {
            //TODO
            return null;
        }
        
        list.add(data[rangeValue]);

        double time = resultItem.getStatistics().getMean();
        list.add(time);
        
        MeasurementQuality mq = resultItem.getBenchmarkSetting().getMeasurementQuality();
        int warmupTime = mq.getWarmupTime();
        int warmupCycles = mq.getNumberOfWarmupCycles();
        int measurementTime = mq.getMeasurementTime();
        int measurementCycles = mq.getNumberOfMeasurementsCycles();
        
        list.add(warmupTime);
        list.add(warmupCycles);
        list.add(measurementTime);
        list.add(measurementCycles);        
        
        return list;
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
