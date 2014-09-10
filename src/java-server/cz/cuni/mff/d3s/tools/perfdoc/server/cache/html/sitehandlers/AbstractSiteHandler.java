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
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.ResultCacheForWeb;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Abstract class that defines method that are common for all site handlers
 * 
 * @author Jakub Naplava
 */
public abstract class AbstractSiteHandler implements SiteHandler {

    protected StringBuilder head = new StringBuilder();
    protected StringBuilder code = new StringBuilder();

    @Override
    public abstract void handle(HttpExchange exchange, ResultCacheForWeb res);

    protected void sentSuccesHeaderAndBodyAndClose(HttpExchange exchange, byte[] message) throws IOException {
        exchange.sendResponseHeaders(200, message.length);

        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(message);
        }
    }

    protected void sentErrorHeaderAndClose(HttpExchange exchange, String errorMessage, int errorCode) throws IOException {
        exchange.sendResponseHeaders(errorCode, errorMessage.getBytes().length);

        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(errorMessage.getBytes());
        }
    }    

    /**
     * Gets current html code
     * @return 
     */
    protected String getCode() {
        if (code.length() == 0) {
            startNewCode();
            endCode();
        }

        return code.toString();
    }

    /**
     * Adds the code to the current code
     * @param code 
     */
    protected void addCode(String code) {
        if (this.code.length() == 0) {
            startNewCode();
        }

        this.code.append(code);
    }
    
    protected void addToHeader(String code) {
        head.append(code);
    }

    private void startNewCode() {
        code.append("<html>");
        addHeader();
    }

    private void addHeader() {
        code.append("<head>");
        code.append(head);
        code.append("</head>");
    }

    private void endCode() {
        code.append("</body>");
        code.append("</html>");
    }

    /**
     * Encodes method (in database format), so that it can be easily passes
     * as an URL query Format: className&methodName&param1&param2&...&paramN
     *
     * @param method
     * @return
     */
    protected String getQueryURL(String method) {
        
        String result = method.replaceAll("#@", "&").replaceAll("#", "&").replaceAll("@", "&");
        
        return result;
    }

    /**
     * Inverse method to getQueryURL, decodes given query to the format of
     * results in database Format:
     * className#methodName#@param1&param2&...&paramN
     *
     * @param query
     * @return as described; else if there is anything wrong null
     */
    protected String getMethodFromQuery(String query) {
        String[] chunks = query.split("&");

        if (chunks.length < 2) {
            return null;
        }
        String className = chunks[0];
        String methodName = chunks[1];

        StringBuilder method = new StringBuilder(className + "#" + methodName + "#");

        for (int i = 2; i < chunks.length; i++) {
            method.append("@" + chunks[i]);
        }

        return method.toString();
    }
    
}
