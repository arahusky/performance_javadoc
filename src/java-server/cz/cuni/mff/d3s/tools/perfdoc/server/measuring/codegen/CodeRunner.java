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
package cz.cuni.mff.d3s.tools.perfdoc.server.measuring.codegen;

import cz.cuni.mff.d3s.tools.perfdoc.server.HttpMeasureServer;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.ClassParser;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class runs the measurement in a new JVM.
 *
 * @author Jakub Naplava
 */
public class CodeRunner {

    private static final Logger log = Logger.getLogger(CodeRunner.class.getName());

    //path to standard java
    private static final String path = System.getProperty("java.home")
            + File.separator + "bin" + File.separator + "java";

    //name of the class to be measured
    private static final String measurementClassName = "TMeasurement";

    //relative path to directory, where the class to be run is saved
    private final String measurementDirPath;

    /**
     * Creates new instance of CodeRunner that will be measuring class located
     * in measurementDirPath folder.
     *
     * @param measurementDirPath relative path to directory, where the class
     * performing measurement is saved
     */
    public CodeRunner(String measurementDirPath) {
        this.measurementDirPath = measurementDirPath;
    }

    /**
     * Runs the class (measurement).
     *
     * @return if no error occurred than true; otherwise false
     * @throws InterruptedException
     * @throws IOException
     */
    public boolean run() throws IOException, InterruptedException {
        //whether no error occurred during the measurement
        boolean noError = true;
        
        //class path for the new JVM
        String classpath = getClassPath();

        ProcessBuilder processBuilder = new ProcessBuilder(path, "-cp", classpath, measurementClassName);

        Process process = processBuilder.start();

        //output the process must be processed (otherwise process never ends - deadlock)
        final InputStream stdIn = process.getInputStream();

        //reading stdIn must be done in separate thread (otherwise, when there's no input, but just errors, deadlock occurs)
        new Thread(new Runnable() {

            @Override
            public void run() {
                int c = 0;
                try {
                    while ((c = stdIn.read()) != -1) {
                        //doNothing (we do not care about what other code produces
                        //System.out.print((char) c);
                    }
                } catch (IOException ex) {
                    //doNothing
                }
            }
        }).start();

        //catching error messages
        InputStream in = process.getErrorStream();
        StringBuilder errorMsg = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            errorMsg.append((char) c);
        }

        if (errorMsg.length() != 0) {
            log.log(Level.SEVERE, "An error occured when trying to run new JVM to perform measurement. ", new Exception(errorMsg.toString()));
            noError = false;
        }

        process.waitFor();
        
        return noError;
    }

    /**
     * Returns class paths that are used for call of the new JVM.
     *
     * Main class to be run is saved in 'measurementDirPath', then all workload
     * with their implementations are in the compiled server classes
     * (HttpMeasureServer.getApplicationRootDir()) and other classes, that may
     * be needed are obtained from ClassParser.
     *
     * @return String that may be used as an argument for -cp
     */
    private String getClassPath() {
        List<String> workloadPaths = ClassParser.getClassPaths();

        StringBuilder classPathBuilder = new StringBuilder(measurementDirPath);

        classPathBuilder.append(File.pathSeparator + HttpMeasureServer.getApplicationRootDir());

        for (String classPath : workloadPaths) {
            classPathBuilder.append(File.pathSeparator + classPath);
        }

        return classPathBuilder.toString();
    }
}
