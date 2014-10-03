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
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.MeasurementResult;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.ResultCacheForWeb;
import java.io.IOException;
import java.util.ArrayList;
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

        String className = exchange.getRequestURI().getQuery();

        if (res != null) {

            ArrayList<String> testedMethod = res.getDistinctClassMethods(className);

            List<MeasurementResult> item = res.getResults();
            addCode(formatOutput(item));
            String output = getCode();

            sentSuccesHeaderAndBodyAndClose(exchange, output.getBytes(), log);
        } else {
            //there is no database connection available
            //sending information about internal server error
            sentErrorHeaderAndClose(exchange, "Database not available.", 500, log);
        }

        log.log(Level.INFO, "Data were succesfully sent to the user.");
    }

    private String formatOutput(List<MeasurementResult> output) {
        StringBuilder sb = new StringBuilder("<table border = \"1\">");
        sb.append("<tr>");
        sb.append("<td><b>methodName</b></td>");
        sb.append("<td><b>generator</b></td>");
        sb.append("<td><b>data</b></td>");
        sb.append("<td><b>number Of Measurements</b></td>");
        sb.append("<td><b>time</b></td>");
        sb.append("</tr>");

        for (MeasurementResult item : output) {
            sb.append("<tr>");
            String methodName = item.getTestedMethod().toString();
            sb.append("<td>" + methodName + "</td>");

            String generator = item.getGenerator().toString();
            sb.append("<td>" + generator + "</td>");

            String data = item.getData();
            sb.append("<td>" + data + "</td>");

            int numberOfMeasurements = item.getNumberOfMeasurements();
            sb.append("<td>" + numberOfMeasurements + "</td>");

            long time = item.getTime();
            sb.append("<td>" + time + "</td>");

            sb.append("</tr>");
        }

        sb.append("</table>");
        return sb.toString();
    }
}
