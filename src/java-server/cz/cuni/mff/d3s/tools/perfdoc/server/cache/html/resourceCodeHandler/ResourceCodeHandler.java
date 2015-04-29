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
package cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.resourceCodeHandler;

import com.sun.net.httpserver.HttpExchange;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.ResultCacheForWeb;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import cz.cuni.mff.d3s.tools.perfdoc.server.HttpExchangeUtils;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.sitehandlers.SiteHandler;

/**
 * Class that returns requested javascript/css files.
 *
 * @author Jakub Naplava
 */
public class ResourceCodeHandler implements SiteHandler {

    private static final Logger log = Logger.getLogger(ResourceCodeHandler.class.getName());

    //folder containing the resource code files
    private static final String defaultFolder = "/cz/cuni/mff/d3s/tools/perfdoc/server/cache/resources/";
    
    public ResourceCodeHandler(ResourceType type) {
        switch (type) {
            case CSS:
                myFolder = defaultFolder + "css/";
                break;
            case JS:
                myFolder = defaultFolder + "js/";
                break;
        }
    }
    
    private  String myFolder = "";
    
    @Override
    public void handle(HttpExchange exchange, ResultCacheForWeb res) {
        String fileName = exchange.getRequestURI().getQuery();
        
        try (InputStream input = HttpExchangeUtils.class.getResourceAsStream(myFolder + fileName)) {

            //sending succesfull headers with length set 0, which means that arbitrary amount of data may be sent
            exchange.sendResponseHeaders(200, 0);

            try (OutputStream responseBody = exchange.getResponseBody()) {
                int i;
                //copying the file into output stream byte per byte
                while ((i = input.read()) != -1) {
                    responseBody.write(i);
                }
            }
        } catch (FileNotFoundException ex) {
            log.log(Level.INFO, "Unable to find class" + fileName, ex);
            HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "The requested file was not found on the server.", 404, log);
        } catch (IOException ex) {
            log.log(Level.INFO, "Unable to send the results to the client.", ex);
        }
    }
}
