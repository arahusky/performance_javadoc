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

import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkRunner;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkSetting;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.codegen.CodeGenerator;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.codegen.CodeRunner;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.exception.CompileException;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.Statistics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jakub Naplava
 */
public class DirectRunner implements BenchmarkRunner {

    private static final Logger log = Logger.getLogger(DirectRunner.class.getName());

    @Override
    public Statistics measure(BenchmarkSetting setting) {
        try {
            CodeGenerator codeGen = new CodeGenerator(setting);
            codeGen.generate();

            CodeRunner codeRunner = new CodeRunner(codeGen.getDirectory());
            codeRunner.run();

            Statistics s = collectResults(codeGen.getDirectory());

            //TODO if not enough results, need to make measurement one more time
            deleteDir(codeGen.getDirectory());

            return s;
        } catch (CompileException | IOException e) {
            return null;
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
    private Statistics collectResults(String fileName) {
        Statistics s = new Statistics();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName + File.separator + "results.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                long time = Long.parseLong(line);
                s.addResult(time);
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, "Unable to find measured results", e);
            return null;
        }

        return s;
    }

    private void deleteDir(String directoryName) {
        File dir = new File(directoryName);

        String[] entries = dir.list();
        for (String s : entries) {
            File currentFile = new File(dir.getPath(), s);
            currentFile.delete();
        }

        dir.delete();
    }
}
