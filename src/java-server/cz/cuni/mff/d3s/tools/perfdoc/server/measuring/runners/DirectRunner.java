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
package cz.cuni.mff.d3s.tools.perfdoc.server.measuring.runners;

import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkSetting;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.MethodRunner;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.codegen.CodeGenerator;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.codegen.CodeRunner;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.exception.CompileException;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.exception.MeasurementException;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.Statistics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of BenchmarkRunner that uses code generation to generate code
 * to be measured directly.
 *
 * @author Jakub Naplava
 */
public class DirectRunner extends MethodRunner {

    private static final Logger log = Logger.getLogger(DirectRunner.class.getName());

    @Override
    public Statistics measure(BenchmarkSetting setting) throws Throwable {
        try {
            //generating code for measurement
            log.log(Level.FINE, "Starting to generate benchmark code.");
            CodeGenerator codeGen = new CodeGenerator(setting);
            codeGen.generate();
            log.log(Level.FINE, "Benchmark code generated.");

            //running generated code
            CodeRunner codeRunner = new CodeRunner(codeGen.getDirectory());
            log.log(Level.FINE, "Running benchmark code.");
            codeRunner.run();
            log.log(Level.FINE, "Benchmark code running done.");

            //collecting generated results
            Statistics s = collectResults(codeGen.getDirectory());

            //if no results were generated, some exception must have occured
            if (s.isEmpty()) {
                throw new MeasurementException("An exception occured while trying to measure results by direct call.");
            }

            log.log(Level.FINE, "Deleting folder containing generated code.");
            //CodeGenerator created new folder, which should be (with all its content) deleted
            codeGen.deleteGeneratedContent();

            return s;
        } catch (CompileException | IOException e) {
            throw new MeasurementException("An exception occured while performing most precise measuring by direct call. Check, whether server has access to write new files, and whether there is no collision due to starting new JVM.");
        } catch (InterruptedException ex) {
            log.log(Level.SEVERE, "Thread was interupted while waiting for another JVM to measure results.", ex);
            return null;
        }
    }

    /**
     * Collects results from generated file.
     *
     * @param fileName Name of the file containing measured results
     * @return Statistics containing measured results
     */
    private Statistics collectResults(String fileName) throws MeasurementException {
        Statistics s = new Statistics();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName + File.separator + "results.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                long time = Long.parseLong(line);
                s.addResult(time);
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, "Unable to find measured results file", e);
            throw new MeasurementException("An exception occured while performing most precise measuring by direct call. Check, whether server has access to write new files, and whether there is no collision due to starting new JVM.");
        }

        return s;
    }
}
