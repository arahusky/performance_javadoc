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
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.ResultCacheForWeb;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkResult;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.MeasurementQuality;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.velocity.VelocityContext;

/**
 * Site handler that shows the content of cache in the form of table
 *
 * @author Jakub Naplava
 */
public class FullDebugSiteHandler implements SiteHandler {

    private static final Logger log = Logger.getLogger(FullDebugSiteHandler.class.getName());
    
    private static final String templateName = "full";

    @Override
    public void handle(HttpExchange exchange, ResultCacheForWeb res) {
        log.log(Level.INFO, "Got new full=debug-site request. Starting to handle it.");

        if (res != null) {
            Collection<BenchmarkResult> item = res.getMainTableResults();
            Collection<Object[]> detailedResults = res.getDetailedTableResults();
            Collection<Object[]> qualityResults = res.getQualityResults();
            
            VelocityContext context = new VelocityContext();            
            addInfoTable(item, context);
            addQualityTable(qualityResults, context);
            addDetailedTable(detailedResults, context);            

            HttpExchangeUtils.mergeTemplateAndSentPositiveResponseAndClose(exchange, templateName, context);
        } else {
            //there is no database connection available
            //sending information about internal server error
            HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "Database not available.", 500, log);
        }

        log.log(Level.INFO, "Data were succesfully sent to the user.");
    }

    /**
     * Adds measurement_info table contents into context.
     * @param output
     * @param context 
     */
    private void addInfoTable(Collection<BenchmarkResult> output, VelocityContext context) {
        
        List<String> theadsInfo = new ArrayList<>();
        theadsInfo.add("method");
        theadsInfo.add("generator");
        theadsInfo.add("generatorArgs");
        theadsInfo.add("time");
        context.put("theadsInfo",theadsInfo);
        
        List<List<Object>> measurementsInfo = new ArrayList<>();

        for (BenchmarkResult item : output) {
            List<Object> measurement = new ArrayList<>();
            
            String methodName = item.getBenchmarkSetting().getMeasuredMethod().toString();
            measurement.add(methodName);

            String generator = item.getBenchmarkSetting().getGenerator().toString();
            measurement.add(generator);

            String data = item.getBenchmarkSetting().getGeneratorArguments().getValuesDBFormat(false);
            measurement.add(data);
           
            double time = item.getStatistics().getMean();
            measurement.add(time);
            
            measurementsInfo.add(measurement);
        }

        context.put("measurementsInfo",measurementsInfo);
    }
    
    /**
     * Adds measurement_quality into context.
     * @param list
     * @return 
     */
    private void addQualityTable(Collection<Object[]> list, VelocityContext context) {
        
        List<String> theadsQuality = new ArrayList<>();
        theadsQuality.add("idQuality");
        theadsQuality.add("warmupTime");
        theadsQuality.add("warmupCycles");
        theadsQuality.add("measurementTime");
        theadsQuality.add("measurementCycles");
        theadsQuality.add("priority");
        context.put("theadsQuality",theadsQuality);

        List<List<Object>> measurementsQuality = new ArrayList<>();
        
        for (Object[] item : list) {
            List<Object> measurement = new ArrayList<>();       
            //idQuality
            measurement.add(item[0]);
            
            MeasurementQuality mq = (MeasurementQuality) item[1];
            measurement.add(mq.getWarmupTime());
            measurement.add(mq.getNumberOfWarmupCycles());
            measurement.add(mq.getMeasurementTime());
            measurement.add(mq.getNumberOfMeasurementsCycles());
            measurement.add(mq.getPriority());
            
            measurementsQuality.add(measurement);            
        }

        context.put("measurementsQuality",measurementsQuality);
    }
    
    /**
     * Adds measurement_detailed into context.
     * @param list
     * @return 
     */
    private void addDetailedTable(Collection<Object[]> list, VelocityContext context) {
        
        List<String> theadsDetailed = new ArrayList<>();
        theadsDetailed.add("id");
        theadsDetailed.add("time");
        context.put("theadsDetailed",theadsDetailed);

        List<List<Object>> measurementsDetailed = new ArrayList<>();
        
        for (Object[] item : list) {
            List<Object> measurement = new ArrayList<>();            
            measurement.add(item[0]);
            measurement.add(item[1]);
            
            measurementsDetailed.add(measurement);            
        }

        context.put("measurementsDetailed",measurementsDetailed);
    }
}
