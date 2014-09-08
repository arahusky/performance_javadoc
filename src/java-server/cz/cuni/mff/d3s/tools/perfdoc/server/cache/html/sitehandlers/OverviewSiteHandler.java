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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jakub Naplava
 */
public class OverviewSiteHandler extends AbstractSiteHandler{

    private static final Logger log = Logger.getLogger(OverviewSiteHandler.class.getName());
    
    @Override
    public void handle(HttpExchange exchange, ResultCacheForWeb res) {
        log.log(Level.INFO, "Got new overview-site request. Starting to handle it.");
        
        if (res != null) {

            ArrayList<String> testedMethod = res.getDistinctTestedMethods();
            
            addCode(returnHeading());
            String classOutput = formatClasses(testedMethod);
            addCode(classOutput);
            String output = getCode();

            try {
                sentSuccesHeaderAndBodyAndClose(exchange, output.getBytes());
            } catch (IOException ex) {
                log.log(Level.INFO, "Unable to send the results to the client", ex);
            } 
        } else {
            //there is no database connection available
            //sending information about internal server error
            try {
                   sentErrorHeaderAndClose(exchange, "Database not available.", 500);
                } catch (IOException ex) {
                    //there is nothing we can do with it
                    log.log(Level.INFO, "An exception occured when trying to close comunnication with client", ex);
                }            
        }
        
        log.log(Level.INFO, "Data were succesfully sent to the user.");
    }
    
     private String returnHeading()
     {
        String heading = "<h1>Measured classes:</h1>";
        
        return heading;
     }
     
     private String formatClasses(ArrayList<String> output) {
        
        //unable to retrieve data from database
        if (output == null) {
            return "<p>Sorry, but there was an error when trying to connect to database.</p>";
        }
        
        StringBuilder sb = new StringBuilder();
        
         Set<String> classes = parseClasess(output);
         
         sb.append("<ul>");
         for (String className : classes) {
             sb.append("<li><a href= \"cache/class?" + className + "\">" + className + "</a></li>" );
         }
         sb.append("</ul>");        
         
        return sb.toString();
    }
     
     private Set<String> parseClasess(ArrayList<String> testedMethods) {
         
         Set<String> set = new HashSet<>();
         
         for (String method : testedMethods) {
             String className = method.split("#")[0];
             set.add(className);
         }
         
         return set;
     }
    
}
