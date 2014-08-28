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

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

class HttpMeasureServer {

    private static final Logger log = Logger.getLogger( HttpMeasureServer.class.getName() );
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        InetSocketAddress addr = new InetSocketAddress(8080);

        //creates server with backlog (=the maximum queue length for incoming connection indications) set to 0 (system default value)
        HttpServer server = HttpServer.create(addr, 0);
        server.createContext("/", new RequestHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        
        try {
            ResultCache.startDatabase();
            //ResultCache.run();
        } catch (ClassNotFoundException ex) {
            //Could not find the database driver
            return;
        } catch (SQLException ex) {
            //The connection to database could have not been established
            return;
        }
        
        server.start();
        log.log(Level.INFO, "Server started and is listening on port 8080");
        
        //TODO close server on exit                
    }
}
