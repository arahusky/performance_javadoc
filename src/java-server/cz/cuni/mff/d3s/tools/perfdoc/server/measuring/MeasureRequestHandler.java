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
package cz.cuni.mff.d3s.tools.perfdoc.server.measuring;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import cz.cuni.mff.d3s.tools.perfdoc.server.LockBase;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.exception.MeasurementException;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.exception.PropertiesBadFormatException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 * Handler that handles incoming measure request.
 *
 * @author Jakub Naplava
 */
public class MeasureRequestHandler implements HttpHandler {

    private final LockBase lockBase;

    public MeasureRequestHandler(LockBase lockBase) {
        this.lockBase = lockBase;
    }

    private static final Logger log = Logger.getLogger(MeasureRequestHandler.class.getName());

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.log(Level.INFO, "Got new Ajax request. Starting to handle it.");

        //adding the right header
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");

        //getting the JSON request
        InputStream in = exchange.getRequestBody();

        //gets the body of the output
        OutputStream responseBody = exchange.getResponseBody();

        MethodMeasurer measurer = null;
        MeasureRequest measureRequest = null;

        try (BufferedReader rd = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")))) {
            String requestBody = readAll(rd);
            log.log(Level.CONFIG, "The incoming message is: {0}", requestBody);

            //parsing incoming request
            measureRequest = new MeasureRequest(requestBody);

            measurer = new MethodMeasurer(measureRequest, lockBase);

            JSONObject obj = measurer.measure();
            log.log(Level.CONFIG, "The response is: {0}", obj.toString());
            
            try {
                exchange.sendResponseHeaders(200, obj.toString().getBytes().length);
                responseBody.write(obj.toString().getBytes());
            } catch (IOException ex) {
                log.log(Level.INFO, "Unable to send the results to the client", ex);
            }
        } catch (ClassNotFoundException ec) {
            sendErrorMessage("Unable to find a measuredMethod/generator class", exchange, responseBody);
        } catch (NoSuchMethodException ex) {
            sendErrorMessage(ex.getMessage(), exchange, responseBody);
        } catch (IOException ex) {
            sendErrorMessage("There was some problem while reading some file on the server.", exchange, responseBody);
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "There was some problem when connecting to database.", ex);
            sendErrorMessage("There was some problem when connecting to database.", exchange, responseBody);
        } catch (MeasurementException e) {
            sendErrorMessage(e.getMessage(), exchange, responseBody);
        } catch (PropertiesBadFormatException e) {
            sendErrorMessage(e.getMessage(), exchange, responseBody);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Unknown exception occured.", e);
            sendErrorMessage("There was some problem on the server.", exchange, responseBody);
        } finally {
            try {
                in.close();
                responseBody.close();
            } catch (IOException ex) {
                //there is nothing we can do with it
                log.log(Level.INFO, "An exception occured when trying to close comunnication with client", ex);
            }
        }

        //results with highest priority are cached
        if (measurer != null && measureRequest != null && measureRequest.getMeasurementQuality().getPriority() == 4) {
            measurer.saveResultsAndCloseDatabaseConnection();
        }

        log.log(Level.INFO, "Data were succesfully sent to the user ({0}).", measureRequest.getUserID());
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
