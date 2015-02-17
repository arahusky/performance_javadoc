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

import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.MeasureRequestHandler;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.ResultDatabaseCache;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.CacheRequestHandler;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.ResultAdminCache;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpMeasureServer {

    private static final Logger log = Logger.getLogger(HttpMeasureServer.class.getName());

    //port, on which the server runs (may be changed by command-line arguments)
    private static int port = 8080;

    //flag whether to empty tables (may be changed by command-line arguments)
    private static boolean emptyTable = false;

    /**
     * The main method, which starts the measuring server.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        //process command-line arguments and if any error occured, return
        if (!processArgs(args)) {
            return;
        }

        //the port on which the server will run
        InetSocketAddress addr = new InetSocketAddress(port);

        //creates server with backlog (=the maximum queue length for incoming connection indications) set to 0 (system default value)
        HttpServer server = HttpServer.create(addr, 0);

        LockBase lockBase = new LockHashMapBase();

        //handler to handle request for measuring
        server.createContext("/measure", new MeasureRequestHandler(lockBase));

        //handler to handle request for cache
        server.createContext("/cache", new CacheRequestHandler());

        server.setExecutor(Executors.newCachedThreadPool());

        try {
            ResultAdminCache res = new ResultDatabaseCache(ResultDatabaseCache.JDBC_URL);
            res.start();

            //empty tables if requested
            if (emptyTable) {
                res.empty();
            }
        } catch (ClassNotFoundException ex) {
            //Could not find the database driver
            return;
        } catch (SQLException ex) {
            //The connection to database could have not been established
            return;
        }

        server.start();
        log.log(Level.INFO, "Server started and is listening on port {0}", port);
    }

    /**
     * Processes command-line arguments. If any error occurred, then false is
     * returned. Otherwise true is returned
     *
     * @param args the command line arguments
     */
    private static boolean processArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-empty":
                    emptyTable = true;
                    break;
                case "-port":
                    if (i >= args.length - 1) {
                        System.out.println("Expected port number, but end of arguments found");
                        return false;
                    }
                    try {
                        port = Integer.parseInt(args[++i]);
                    } catch (NumberFormatException e) {
                        System.out.println("Given port number is not a number.");
                        return false;
                    }

                    break;
                default:
                    System.out.println("Unexpected argument: " + args[i]);
                    return false;
            }
        }

        return true;
    }
    
    /**
     * Returns port number of port, on which the server runs.
     * @return 
     */
    public static int getPort() {
        return port;
    }
}
