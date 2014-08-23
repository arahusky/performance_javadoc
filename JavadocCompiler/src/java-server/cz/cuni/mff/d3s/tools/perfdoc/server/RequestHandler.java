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
import java.nio.charset.Charset;
import org.json.JSONObject;


/**
 *
 * @author arahusky
 */
class RequestHandler implements HttpHandler {

    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Got new request. Starting to handle it.");

        //adding the right header
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");

        //getting the json request
        InputStream in = exchange.getRequestBody();

        //gets the body of the output
        OutputStream responseBody = exchange.getResponseBody();

        try (BufferedReader rd = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")))) {
            String requestBody = readAll(rd);
            
            MethodMeasurer m = new MethodMeasurer(requestBody); 
            JSONObject obj = m.measureTime();
            
            exchange.sendResponseHeaders(200, 0); //0 means Chunked transfer encoding - HTTP 1.1 arbitary amount of data may be sent
            responseBody.write(requestBody.getBytes());
            

        } catch (Exception e) {
            //TODO possibly data in bad format - sent him some message
            System.out.println(e);
        } finally {
            in.close();
            
            //closing to indicate that the response is complete
            responseBody.close();
        }
        
        System.out.println("all data sent");
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

