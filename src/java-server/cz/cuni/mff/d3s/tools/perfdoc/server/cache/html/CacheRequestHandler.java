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
package cz.cuni.mff.d3s.tools.perfdoc.server.cache.html;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.resourceCodeHandler.ResourceCodeHandler;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.resourceCodeHandler.ResourceType;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.sitehandlers.ClassSiteHandler;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.sitehandlers.DetailedSiteHandler;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.sitehandlers.FullDebugSiteHandler;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.sitehandlers.MethodGeneratorSiteHandler;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.sitehandlers.MethodSiteHandler;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.sitehandlers.OverviewSiteHandler;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.sitehandlers.SingleResultSiteHandler;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.sitehandlers.SiteHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HttpHandler that handles request to show the actual cache
 *
 * @author Jakub Naplava
 */
public class CacheRequestHandler implements HttpHandler {

    private static final Logger log = Logger.getLogger(CacheRequestHandler.class.getName());

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        SiteHandler handler = null;
        URI uri = exchange.getRequestURI();

        String path = uri.getPath();
        if (path.charAt(path.length() - 1) == '/') {
            path = path.substring(0, path.length() - 1);
        }
        
        switch (path) {
            case "/cache":
                handler = new OverviewSiteHandler();
                break;
            case "/cache/class":
                handler = new ClassSiteHandler();
                break;
            case "/cache/method":
                handler = new MethodSiteHandler();
                break;
            case "/cache/methodgenerator":
                handler = new MethodGeneratorSiteHandler();
                break;
            case "/cache/detailed":
                handler = new DetailedSiteHandler();
                break;
            case "/cache/single":
                handler = new SingleResultSiteHandler();
                break;
            case "/cache/full":
                handler = new FullDebugSiteHandler();
                break;
            case "/cache/js":
                handler = new ResourceCodeHandler(ResourceType.JS);
                break;
            case "/cache/css":
                handler = new ResourceCodeHandler(ResourceType.CSS);
                break;
            default:
                log.log(Level.CONFIG, "User tried to go on a page ({0}), that does not exist", path);
                byte[] message = "Such a page does not exist here.".getBytes();
                exchange.sendResponseHeaders(404, message.length); //0 means Chunked transfer encoding - HTTP 1.1 arbitary amount of data may be sent

                try (OutputStream responseBody = exchange.getResponseBody()) {
                    responseBody.write(message);
                }
                return;
        }

        try {
            ResultCacheForWeb res = new ResultDatabaseCacheForWeb(ResultDatabaseCacheForWeb.JDBC_URL);
            handler.handle(exchange, res);
        } catch (SQLException e) {
            String errorMessage = "Unable to connect to database.";
            exchange.sendResponseHeaders(500, errorMessage.getBytes().length);

            try (OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(errorMessage.getBytes());
            }
        }
    }

}
