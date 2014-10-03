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
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.ResultCacheForWeb;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.sitehandlers.AbstractSiteHandler;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that returns requested javascript files
 *
 * @author Jakub Naplava
 */
public class JavascriptCodeHandler extends AbstractSiteHandler {

    private static final Logger log = Logger.getLogger(JavascriptCodeHandler.class.getName());

    //folder containing the javascript files
    private static final String defaultFolder = "lib/js";

    @Override
    public void handle(HttpExchange exchange, ResultCacheForWeb res) {
        String fileName = exchange.getRequestURI().getQuery();

        try (InputStream input = new FileInputStream(defaultFolder + "/" + fileName)) {

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
            sentErrorHeaderAndClose(exchange, "The requested file was not found on the server.", 404, log);
        } catch (IOException ex) {
            log.log(Level.INFO, "Unable to send the results to the client.", ex);
        }
    }
}
