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

package cz.cuni.mff.d3s.tools.perfdoc.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author arahusky
 */
class CacheRequestHandler implements HttpHandler {
    
    private static final Logger log = Logger.getLogger(CacheRequestHandler.class.getName());
    private static ResultCache res;
    
    public CacheRequestHandler() {
        try {
            res = new ResultDatabaseCache();
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to connect to database, the cache will not work.", e);
        }
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.log(Level.INFO, "Got new Cache request. Starting to handle it.");

        if (res != null) {
        //adding the right header
        Headers responseHeaders = exchange.getResponseHeaders();

        //getting the json request
        InputStream in = exchange.getRequestBody();

        //gets the body of the output
        OutputStream responseBody = exchange.getResponseBody();
        
        exchange.sendResponseHeaders(200, 0); //0 means Chunked transfer encoding - HTTP 1.1 arbitary amount of data may be sent
        
        List<Map<String, Object>> cacheContent = res.getResults();
        
            System.out.println("wait");
        String output = formatOutput(cacheContent);
        
            System.out.println("prepared");
        responseBody.write(output.getBytes());
        responseBody.close();
        } else {
            //there is no database connection available
            //sending information about internal server error
            exchange.sendResponseHeaders(500, 0); 
        } 
        
    }
    
    private String formatOutput(List<Map<String, Object>> output) {
        StringBuilder sb = new StringBuilder("<table border = \"1\">");
        sb.append("<tr>");
        sb.append("<td><b>methodName</b></td>");
        sb.append("<td><b>generator</b></td>");
        sb.append("<td><b>data</b></td>");
        sb.append("<td><b>number Of Measurements</b></td>");
        sb.append("<td><b>time</b></td>");
        sb.append("</tr>");
        
        for (Map<String, Object> map : output) {
            System.out.println("started");
            sb.append("<tr>");
            String methodName = (String) map.get("methodName");
            sb.append("<td>" + methodName + "</td>");
            System.out.println("m");
            
            String generator = (String) map.get("generator");
            sb.append("<td>" + generator + "</td>");
            System.out.println("gen");
            
            String data = (String) map.get("data");
            sb.append("<td>" + data + "</td>");
            System.out.println("data");
            
            int numberOfMeasurements = (int) map.get("numberOfMeasurements");
            sb.append("<td>" + numberOfMeasurements + "</td>");
            System.out.println("nums");
            
            long time = (long) map.get("time");
            sb.append("<td>" + time + "</td>");
            System.out.println("time");
            
            sb.append("</tr>");
            System.out.println("appended");
        }
        
        sb.append("</table>");
        return sb.toString();
    }
}
