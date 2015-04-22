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
import cz.cuni.mff.d3s.tools.perfdoc.server.HttpExchangeUtils;
import cz.cuni.mff.d3s.tools.perfdoc.server.MethodInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.ResultCacheForWeb;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.velocity.VelocityContext;

/**
 * Site handler that shows all generators, that have any measured result for the
 * given (tested) method
 *
 * @author Jakub Naplava
 */
public class MethodSiteHandler implements SiteHandler {

    private static final Logger log = Logger.getLogger(MethodSiteHandler.class.getName());
    
    private static final String templateName = "method";

    @Override
    public void handle(HttpExchange exchange, ResultCacheForWeb res) {
        log.log(Level.INFO, "Got new method-site request. Starting to handle it.");

        String query = exchange.getRequestURI().getQuery();
        MethodInfo method;

        try {
            method = SiteHandlingUtils.getMethodFromQuery(query);
        } catch (IllegalArgumentException e) {
            //there is a problem with URL (probably own written URL)
            HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "There was some problem with the URL adress you requested.", 404, log);
            return;
        }

        if (method == null) {
            //there is a problem with URL (probably own written URL)
            HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "There was some problem with the URL adress you requested.", 404, log);
            return;
        }

        if (res != null) {
            Collection<MethodInfo> availableGenerators = res.getDistinctGenerators(method);
            if (availableGenerators == null) {
                HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "Sorry, but there was an error when trying to connect to database.", 500, log);
            }
            
            String className = method.getQualifiedClassName();
            String methodName = method.getMethodName();
            String remoteAddress = SiteHandlingUtils.getRemoteAddress(exchange);
            
            VelocityContext context = new VelocityContext();
            context.put("className", className);
            context.put("methodName", methodName);
            context.put("methodParameters", method.getParams());
            
            String overviewSite = remoteAddress + "/cache";
            context.put("overviewSite",overviewSite);
            
            String classSite = "class?" + className;
            context.put("classSite", classSite);

            List<PairNameUrl> generators = getGenerators(method.toString(), availableGenerators);
            context.put("generators", generators);

            HttpExchangeUtils.mergeTemplateAndSentPositiveResponseAndClose(exchange, templateName, context);
        } else {
            //there is no database connection available
            //sending information about internal server error
            HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "Database not available.", 500, log);
        }

        log.log(Level.INFO, "Data were succesfully sent to the user.");
    }

    private List<PairNameUrl> getGenerators(String methodName, Collection<MethodInfo> generators) {
        List<PairNameUrl> list = new ArrayList<>();
        
        for (MethodInfo generator : generators) {
            String generatorInfo = formatGenerator(generator);
            String methodgeneratorURL = "methodgenerator?" + getMethodGeneratorURL(methodName, generator);
            list.add(new PairNameUrl(generatorInfo, methodgeneratorURL));
        }
        
        return list;
    }

    private String formatGenerator(MethodInfo generator) {
       
        String methodName = generator.getMethodName();
        String parameterInfo = generator.getParams().get(2);

        return (methodName + "(" + parameterInfo + ")");
    }

    private String getMethodGeneratorURL(String method, MethodInfo generator) {
        String methodQuery = SiteHandlingUtils.getQueryURL(method);
        String generatorQuery = SiteHandlingUtils.getQueryURL(generator.toString());

        return (methodQuery + "separator=" + generatorQuery);
    }
}
