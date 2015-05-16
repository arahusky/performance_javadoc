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

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * This class provides basic methods to handle HttpExchange.
 *
 * @author Jakub Naplava
 */
public class HttpExchangeUtils {

    private static final Logger log = Logger.getLogger(HttpExchangeUtils.class.getName());

    public static final String templatesFolder = "/cz/cuni/mff/d3s/tools/perfdoc/server/cache/html/resources/";

    /**
     * Merges template with context, then sends given message to the client with
     * the success headers and closes communication channel.
     *
     * @param exchange stands for client's representation
     * @param templateName
     * @param context
     */
    public static void mergeTemplateAndSentPositiveResponseAndClose(HttpExchange exchange, String templateName, VelocityContext context) {

        InputStream template = HttpExchangeUtils.class.getResourceAsStream(templatesFolder + templateName + ".vm");
        InputStreamReader input = new InputStreamReader(template);

        StringWriter w = new StringWriter();
        Velocity.evaluate(context, w, "", input);
        //Velocity.mergeTemplate(templateLocation, "UTF-8", context, w);
        byte[] message = w.getBuffer().toString().getBytes();

        try {
            exchange.sendResponseHeaders(200, message.length);

            //autoclosable handles closing
            try (OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(message);
            }
        } catch (IOException e) {
            log.log(Level.INFO, "Unable to send the results to the client", e);
        }
    }

    /**
     * Reports client of the occurred error and closes the communication
     * channel.
     *
     * @param exchange stands for client's representation
     * @param errorMessage
     * @param errorCode the ErrorCode to add to the header
     * @param log Logger to log exceptions
     */
    public static void sentErrorHeaderAndClose(HttpExchange exchange, String errorMessage, int errorCode, Logger log) {
        try {
            exchange.sendResponseHeaders(errorCode, errorMessage.getBytes().length);

            //autoclosable handles closing
            try (OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(errorMessage.getBytes());
            }
        } catch (IOException e) {
            log.log(Level.INFO, "Unable to send the results to the client", e);
        }
    }
}
