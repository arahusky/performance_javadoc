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
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.MeasurementStatistics;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.Statistics;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.velocity.VelocityContext;

/**
 * This site handler shows the page containing results for single point of the
 * measurement.
 *
 * @author Jakub Naplava
 */
public class SingleResultSiteHandler implements SiteHandler {

    private static final Logger log = Logger.getLogger(SingleResultSiteHandler.class.getName());

    private final String templateName = "singlemeasurement";

    @Override
    public void handle(HttpExchange exchange, ResultCacheForWeb res) {
        log.log(Level.INFO, "Got new singleResult-site request. Starting to handle it.");

        //the id is saved in the request URI
        String idString = exchange.getRequestURI().getQuery();
        int id = getID(idString);

        if ((res != null) && (id != -1)) {
            //the methods of the class that have been measured 
            Statistics statistics = res.getResults(id);
            if ((statistics == null) || !(statistics instanceof MeasurementStatistics)) {
                HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "Sorry, but there was an error when trying to connect to database..", 500, log);
                log.log(Level.INFO, "Data were succesfully sent to the user.");
                return;
            }

            MeasurementStatistics mstatistics = (MeasurementStatistics) statistics;

            VelocityContext context = new VelocityContext();
            context.put("results", mstatistics.getValues());

            HttpExchangeUtils.mergeTemplateAndSentPositiveResponseAndClose(exchange, templateName, context);
        } else {
            //there is no database connection available
            //sending information about internal server error
            HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "Problem with requested URL or database not available.", 500, log);
        }

        log.log(Level.INFO, "Data were succesfully sent to the user.");
    }

    /**
     * Parses the incoming idString in format id=XXX, where XXX should be a
     * number to be returned..
     * 
     * @return if no error occurred the parsed number, otherwise -1
     */
    private int getID(String idString) {
        String[] arr = idString.split("=");
        if (arr.length != 2) {
            //error
            return -1;
        }

        return Integer.parseInt(arr[1]);
    }
}
