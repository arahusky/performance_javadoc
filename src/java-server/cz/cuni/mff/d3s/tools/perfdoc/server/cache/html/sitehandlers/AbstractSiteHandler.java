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
import cz.cuni.mff.d3s.tools.perfdoc.server.MethodInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.ResultCacheForWeb;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    /**
     * Sends the given message to the client with the success
     * headers
     *
     * @param exchange stands for client's representation
     * @param message Message to pass to the client
     * @param log Logger to log exceptions
     */
    protected void sentSuccesHeaderAndBodyAndClose(HttpExchange exchange, byte[] message, Logger log) {
        try {
            exchange.sendResponseHeaders(200, message.length);

            try (OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(message);
            }
        } catch (IOException e) {
            log.log(Level.INFO, "Unable to send the results to the client", e);
        }
    }

    /**
     * Reports client of the occurred error
     *
     * @param exchange stands for client's representation
     * @param errorMessage
     * @param errorCode the ErrorCode to add to the header
     * @param log Logger to log exceptions
     */
    protected void sentErrorHeaderAndClose(HttpExchange exchange, String errorMessage, int errorCode, Logger log) {
        try {
        exchange.sendResponseHeaders(errorCode, errorMessage.getBytes().length);

        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(errorMessage.getBytes());
        }
        } catch (IOException e) {
            log.log(Level.INFO, "Unable to send the results to the client", e);
        }
    }

    /**
     * Gets the html code
     *
     * @return
     */
    protected String getCode() {
        if (code.length() == 0) {
            startNewCode();
            endCode(code);
            return code.toString();
        }

        StringBuilder sb = new StringBuilder(code.toString());
        endCode(sb);
        return sb.toString();
    }

    /**
     * Adds the code to the current code
     *
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
        code.append("</head> <body>");
    }

    private void endCode(StringBuilder code) {
        code.append("</body>");
        code.append("</html>");
    }

    /**
     * Encodes method (in database format), so that it can be easily passes as
     * an URL query Format: className&methodName&param1&param2&...&paramN
     *
     * @param method
     * @return
     */
    protected String getQueryURL(String method) {
        String result = method.replaceAll("#@", "&").replaceAll("#", "&").replaceAll("@", "&");

        return result;
    }

    /**
     * Inverse method to getQueryURL, decodes given query to MethodInfo instance
     *
     * @param query
     * @return as described; else if there is anything wrong null
     */
    protected MethodInfo getMethodFromQuery(String query) {
        String[] chunks = query.split("&");

        if (chunks.length < 2) {
            return null;
        }
        String className = chunks[0];
        String methodName = chunks[1];

        ArrayList<String> params = new ArrayList<>();

        for (int i = 2; i < chunks.length; i++) {
            params.add(chunks[i]);
        }

        return new MethodInfo(className, methodName, params);
    }
    
    /**
     * Chains the parameters in List.
     * @param parameters
     * @return chained parameters in format: param1,param2,...,paramN
     */
    protected String chainParameters(List<String> parameters) {
        StringBuilder sb = new StringBuilder();

        if (parameters == null || parameters.isEmpty()) {
            return "";
        }
        
        for (int i = 0; i < parameters.size() - 1; i++) {
            sb.append(parameters.get(i) + ",");
        }

        sb.append(parameters.get(parameters.size() - 1));
        return sb.toString();
    }
}
