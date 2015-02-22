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

import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.exception.CompileException;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.Statistics;
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
            Statistics s = new Statistics();
            
            CodeGenerator codeGen = new CodeGenerator(setting);
            codeGen.generate();
            
            //loadResultsFromFiles

            return s;
        } catch (CompileException | IOException e) {
            return null;
        }
    }
}
