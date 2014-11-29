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
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Site handler that shows the content of cache in the form of table
 *
 * @author Jakub Naplava
 */
public class FullDebugSiteHandler extends AbstractSiteHandler {

    private static final Logger log = Logger.getLogger(FullDebugSiteHandler.class.getName());

    @Override
    public void handle(HttpExchange exchange, ResultCacheForWeb res) {
        log.log(Level.INFO, "Got new full=debug-site request. Starting to handle it.");

        if (res != null) {
            Collection<BenchmarkResult> item = res.getMainTableResults();
            Collection<Object[]> detailedResults = res.getDetailedTableResults();
            addCode(addMainTable(item));
            addCode(addDetailedTable(detailedResults));
            String output = getCode();

            HttpExchangeUtils.sentSuccesHeaderAndBodyAndClose(exchange, output.getBytes(), log);
        } else {
            //there is no database connection available
            //sending information about internal server error
            HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "Database not available.", 500, log);
        }

        log.log(Level.INFO, "Data were succesfully sent to the user.");
    }

    private String addMainTable(Collection<BenchmarkResult> output) {
        StringBuilder sb = new StringBuilder("<table border = \"1\">");
        sb.append("<tr>");
        sb.append("<td><b>method</b></td>");
        sb.append("<td><b>workload</b></td>");
        sb.append("<td><b>workloadArgs</b></td>");
        sb.append("<td><b>number Of Measurements</b></td>");
        sb.append("<td><b>time</b></td>");
        sb.append("</tr>");

        for (BenchmarkResult item : output) {
            sb.append("<tr>");
            String methodName = item.getBenchmarkSetting().getTestedMethod().toString();
            sb.append("<td>").append(methodName).append("</td>");

            String generator = item.getBenchmarkSetting().getWorkload().toString();
            sb.append("<td>").append(generator).append("</td>");

            String data = item.getBenchmarkSetting().getWorkloadArguments().getValuesDBFormat(false);
            sb.append("<td>").append(data).append("</td>");
            //TODO when proper statistics table is added, change it to getting from statistics
            //int numberOfMeasurements = item.getStatistics().getNumberOfMeasurements();
            int numberOfMeasurements = item.getBenchmarkSetting().getPriority();
            sb.append("<td>").append(numberOfMeasurements).append("</td>");

            long time = item.getStatistics().compute();
            sb.append("<td>").append(time).append("</td>");

            sb.append("</tr>");
        }

        sb.append("</table><br />");
        return sb.toString();
    }
    
    private String addDetailedTable(Collection<Object[]> list) {
        StringBuilder sb = new StringBuilder("<table border = \"1\">");
        sb.append("<tr>");
        sb.append("<td><b>id</b></td>");
        sb.append("<td><b>time</b></td>");
        sb.append("</tr>");

        for (Object[] item : list) {
            sb.append("<tr>");
            
            sb.append("<td>").append(item[0]).append("</td>");

            sb.append("<td>").append(item[1]).append("</td>");

            sb.append("</tr>");
        }

        sb.append("</table>");
        return sb.toString();
    }
}
