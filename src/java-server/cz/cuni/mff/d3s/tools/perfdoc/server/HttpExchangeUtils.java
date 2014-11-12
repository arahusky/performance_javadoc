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
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that provides basic methods to handle HttpExchange.
 * 
 * @author Jakub Naplava
 */
public class HttpExchangeUtils {
    
    /**
     * Sends the given message to the client with the success
     * headers and closes communication channel.
     *
     * @param exchange stands for client's representation
     * @param message Message to pass to the client
     * @param log Logger to log exceptions
     */
    public static void sentSuccesHeaderAndBodyAndClose(HttpExchange exchange, byte[] message, Logger log) {
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
     * Reports client of the occurred error and closes the communication channel.
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
