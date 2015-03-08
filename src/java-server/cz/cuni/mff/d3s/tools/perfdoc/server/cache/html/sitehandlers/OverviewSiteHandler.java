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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.velocity.VelocityContext;

/**
 * Site handler, that shows all classes, that have any measured method
 *
 * @author Jakub Naplava
 */
public class OverviewSiteHandler implements SiteHandler {

    private static final Logger log = Logger.getLogger(OverviewSiteHandler.class.getName());
    
    private static final String templateName = "overview";

    @Override
    public void handle(HttpExchange exchange, ResultCacheForWeb res) {        
        log.log(Level.INFO, "Got new overview-site request. Starting to handle it.");

        if (res != null) {
            Collection<MethodInfo> testedMethod = res.getDistinctTestedMethods();
            if (testedMethod == null) {
                HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "An error occured when trying to connect to DB.", 500, log);
            }
            
            List<PairNameUrl> list = getClassNames(testedMethod);
            
            VelocityContext context = new VelocityContext();
            context.put("measuredClasses", list);
                                 
            HttpExchangeUtils.mergeTemplateAndSentPositiveResponseAndClose(exchange, templateName, context);
        } else {
            //there is no database connection available
            //sending information about internal server error
            HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "Database not available.", 500, log);
        }

        log.log(Level.INFO, "Data were succesfully sent to the user.");
    }    
    
    private List<PairNameUrl> getClassNames(Collection<MethodInfo> output) {
        List<PairNameUrl> list = new ArrayList<>();
        Set<String> distinctClasses = getDistinctClasses(output);
        
        for (String className : distinctClasses) {
            String URL = "cache/class?" + className;
            list.add(new PairNameUrl(className, URL));            
        }
        
        return list;
    }
    
    private Set<String> getDistinctClasses(Collection<MethodInfo> testedMethods) {
        Set<String> set = new HashSet<>();
        for (MethodInfo method : testedMethods) {            
            set.add(method.getQualifiedClassName());
        }
        return set;
    }
}
