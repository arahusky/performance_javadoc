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
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.BenchmarkResultDB;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.ResultCacheForWeb;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.Statistics;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.velocity.VelocityContext;

/**
 * Site handler that shows all results for given tested method and its generator
 *
 * @author Jakub Naplava
 */
public class MethodGeneratorSiteHandler implements SiteHandler {

    private static final Logger log = Logger.getLogger(MethodGeneratorSiteHandler.class.getName());
    
    private static final String templateName = "methodGenerator";

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
            try {
                MethodInfo testedMethod;
                MethodInfo generator;

                try {
                    testedMethod = SiteHandlingUtils.getMethodFromQuery(methods[0]);
                    generator = SiteHandlingUtils.getMethodFromQuery(methods[1]);
                } catch (IllegalArgumentException e) {
                    HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "The URL adress you passed seems to be incorrect.", 404, log);
                    return;
                }

                String remoteAddress = SiteHandlingUtils.getRemoteAddress(exchange);
                
                VelocityContext context = new VelocityContext();

                String overviewSite = remoteAddress + "/cache";
                context.put("overviewSite", overviewSite);

                String classSite = "class?" + testedMethod.getQualifiedClassName();
                context.put("classSite", classSite);
                
                String methodSite = "method?" + methods[0];
                context.put("methodSite", methodSite);

                context.put("methodName", testedMethod.getMethodName());
                context.put("methodClassName", testedMethod.getQualifiedClassName());
                context.put("methodParameters", SiteHandlingUtils.chainParameters(testedMethod.getParams()));

                context.put("generatorName", generator.getMethodName());
                context.put("generatorClass", generator.getQualifiedClassName());
                context.put("generatorParameters", SiteHandlingUtils.chainParameters(generator.getParams()));

                List<String> tHeads = getTHeads(generator);
                
                if (tHeads == null) {
                    HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "There was problem when trying to find generator in database.", 500, log);
                }
                context.put("theads", tHeads);
                context.put("measurements", getMeasurements(testedMethod, generator, res));

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
    
    private List<String> getTHeads(MethodInfo generator) throws ClassNotFoundException, IOException, NoSuchMethodException {
        
        List<String> list = new ArrayList<>();
        
        Method generatorMethod = null;
        try {
            generatorMethod = new MethodReflectionInfo(generator.toString()).getMethod();
        } catch (ClassNotFoundException | IOException ex) {
            log.log(Level.INFO, "User obtained method that does not exist.");
            return null;
        }
        String[] genParametersText = AnnotationUtils.geParameterDescriptions(generatorMethod);
        if (genParametersText == null) {
            return null;
        }

        for (int i = 0; i < genParametersText.length; i++) {
            list.add(genParametersText[i] + " (" + generator.getParams().get(i + 2) + ")");
        }
        list.add("mean (ns)");
        list.add("std. deviation (ns)");
        list.add("Q1, Q2, Q3 (ns)");
        list.add("Detailed results");
        
        return list;
    }
    
    private List<List<Object>> getMeasurements(MethodInfo testedMethod, MethodInfo generator, ResultCacheForWeb res) throws ClassNotFoundException, IOException, NoSuchMethodException {
        
        List<List<Object>> list = new ArrayList<>();
        Collection<BenchmarkResultDB> benchmarkResults = res.getResults(testedMethod, generator);

        if (benchmarkResults != null) {
            for (BenchmarkResultDB resultItem : benchmarkResults) {
                List<Object> pomList = new ArrayList<>();
                
                Object[] data = resultItem.getBenchmarkSetting().getGeneratorArguments().getValues();
                for (Object datum : data) {
                    pomList.add(datum);
                }

                Statistics stat = resultItem.getStatistics();
                
                pomList.add(stat.getMean());
                pomList.add(stat.getStandardDeviation());
                
                String quartiles = stat.getFirstQuartile() + ", " + stat.getMedian() + ", " + stat.getThirdQuartile();
                pomList.add(quartiles);
                
                int idOfMeasurement = resultItem.getIDMeasurement();
                pomList.add(idOfMeasurement);
                
                list.add(pomList);
            }
        }

        return list;
    }

    private String[] getMethods(String query) {
        return query.split("separator=");
    }
}
