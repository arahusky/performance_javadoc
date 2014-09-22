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
package cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.javascript;

import com.sun.net.httpserver.HttpExchange;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.CacheRequestHandler;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.ResultCacheForWeb;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.sitehandlers.AbstractSiteHandler;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author arahusky
 */
public class JavascriptCodeHandler extends AbstractSiteHandler {

    private static final Logger log = Logger.getLogger(JavascriptCodeHandler.class.getName());

    @Override
    public void handle(HttpExchange exchange, ResultCacheForWeb res) {
        String fileName = exchange.getRequestURI().getQuery();

        try (InputStream input = new FileInputStream("config/" + fileName)) {

            //sending succesfull headers with length set 0, which means that arbitrary amount of data may be sent
            exchange.sendResponseHeaders(200, 0);

            try (OutputStream responseBody = exchange.getResponseBody()) {
                int i;
                
                //copying the file into output byte per byte
                while ((i = input.read()) != -1) {
                    responseBody.write(i);
                }
            }
        } catch (IOException ex) {
            log.log(Level.INFO, "Unable to send the results to the client", ex);
        }
    }
}
