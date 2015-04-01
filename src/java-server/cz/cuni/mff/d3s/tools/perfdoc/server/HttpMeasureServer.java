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
import cz.cuni.mff.d3s.tools.perfdoc.blackhole.Blackhole;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BlackholeFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.velocity.app.Velocity;

public class HttpMeasureServer {

    private static final Logger log = Logger.getLogger(HttpMeasureServer.class.getName());

    //port, on which the server runs 
    private static int port;

    //file storing server properties
    private static String serverConfigurationFileLocation = "config/server.properties";

    //flag whether to empty tables (may be changed by command-line arguments)
    private static boolean emptyTable = false;

    /**
     * The main method, which starts the measuring server.
     *
     * @param args the command line arguments
     * @throws java.io.IOException when the server could not been started on
     * given port number
     */
    public static void main(String[] args) throws IOException {

        if (!processArgs(args) || !loadConfigurationFileAndConfigure()) {
            printUsage();
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

            //empty tables if requested
            if (emptyTable) {
                res.empty();
            }

            res.start();

            Velocity.init();
        } catch (ClassNotFoundException ex) {
            log.log(Level.SEVERE, "Could not find the database driver", ex);
            return;
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "The connection to database could have not been established.", ex);
            return;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Velocity could have not been started.", e);
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
                case "-configuration":
                    if (i >= args.length - 1) {
                        System.out.println("Expected configuration file location, but end of arguments found");
                        return false;
                    }

                    serverConfigurationFileLocation = args[++i];

                    break;
                default:
                    System.out.println("Unexpected argument: " + args[i]);
                    return false;
            }
        }

        return true;
    }

    /**
     * Loads configuration file and sets properties saved in it.
     *
     * @return false if anything went wrong
     */
    private static boolean loadConfigurationFileAndConfigure() {
        Properties serverProperties = new Properties();

        try (InputStream input = new FileInputStream(serverConfigurationFileLocation)) {
            serverProperties.load(input);
            //if port was not set by command-line arguments
            if (port == 0) {
                try {
                    port = Integer.parseInt(serverProperties.getProperty("port"));
                } catch (NumberFormatException e) {
                    System.out.println("Given port number is not a number.");
                    return false;
                }
            }

            String databaseUrl = serverProperties.getProperty("databaseUrlString");
            ResultDatabaseCache.setUrl(databaseUrl);

        } catch (IOException ex) {
            log.log(Level.WARNING, "Unable to find configuration file for server. Try to specify -configuration argument.", ex);
            return false;
        }

        return true;
    }

    /**
     * Prints usage of the program to the standard application output.
     */
    private static void printUsage() {
        System.out.println("Supported arguments:");
        System.out.println(" -configuration <fileLocation>      Specifies the path of file, where the configuration info is located.");
        System.out.println(" -empty                             Flag to empty all caches.");
        System.out.println(" -port <portNumber>                 Sets the number of port, on which the server runs (listens).");
    }

    /**
     * Returns number of port, on which the server runs.
     *
     * @return
     */
    public static int getPort() {
        return port;
    }
}
