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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author Jakub Naplava
 */
class RequestHandler implements HttpHandler {

    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.log(Level.INFO, "Got new Ajax request. Starting to handle it.");

        //adding the right header
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");

        //getting the json request
        InputStream in = exchange.getRequestBody();

        //gets the body of the output
        OutputStream responseBody = exchange.getResponseBody();

        try (BufferedReader rd = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")))) {
            String requestBody = readAll(rd);
            
            log.log(Level.FINE, "The incoming message is: {0}", requestBody);

            MethodMeasurer m = new MethodMeasurer(requestBody);
            JSONObject obj = m.measureTime();
            try {
                exchange.sendResponseHeaders(200, 0); //0 means Chunked transfer encoding - HTTP 1.1 arbitary amount of data may be sent
                System.out.println("heree");
                responseBody.write(obj.toString().getBytes());
                System.out.println("jeee");
            } catch (IOException ex) {
                log.log(Level.INFO, "Unable to send the results to the client", ex);
            }
        } catch (ClassNotFoundException ec) {
            sendErrorMessage("Unable to find a generator class", exchange, responseBody);
        } catch (InstantiationException ex) {
            sendErrorMessage("Unable to make an instance of generator class", exchange, responseBody);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            sendErrorMessage("Some other error", exchange, responseBody);
        } catch (IllegalArgumentException ex) {
            sendErrorMessage("The bad parameters were sent to server", exchange, responseBody);
        } catch (IOException ex) {
            sendErrorMessage("There was some problem while reading some file on the server", exchange, responseBody);
        } finally {
            try {
                in.close();
                responseBody.close();
            } catch (IOException ex) {
                //there is nothing we can do with it
                log.log(Level.INFO, "An exception occured when trying to close comunnication with client", ex);
            }
        }

        //TODO az nam uzivatel posle unikatni identifikator, tak ho sem taky zapsat
        log.log(Level.INFO, "Data were succesfully sent to the user.");
    }
    
    private void sendErrorMessage(String msg, HttpExchange exchange, OutputStream out) {
        try {
            exchange.sendResponseHeaders(500, 0);
            out.write(msg.getBytes());
            log.log(Level.INFO, msg);
        } catch (IOException ex) {
           log.log(Level.INFO, "Unable to send the error message to the client", ex);
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
