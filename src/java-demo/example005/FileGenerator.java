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
package example005;

import cz.cuni.mff.d3s.tools.perfdoc.annotations.AfterBenchmark;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.AfterMeasurement;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.Generator;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.Workload;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 *
 * @author Jakub Naplava
 */
public class FileGenerator {

    @Generator(description = "Prepares file stream of given file size.", genName = "File stream preparer")
    public void prepareStream(
            Workload workload,
            ServiceWorkload service,
            @ParamNum(description = "Size of file (bytes)", min = 1, max = 100000, step = 1) int file_size
    ) throws IOException {
        final File file = new File("fileThatDoesNotExist.txt");
        file.createNewFile();

        Random r = new Random();

        byte[] array = new byte[file_size];
        r.nextBytes(array);

        try (FileWriter fw = new FileWriter(file)) {
            for (byte b : array) {
                fw.write(b);
            }
        }

        FileInputStream stream = new FileInputStream(file);
        workload.addCall(null, stream);

        workload.setHooks(new MyHooks(file));
    }

    /**
     * Hooks implemented as inner class.
     *
     * This class must be public so that we can find and run it.
     */
    public class MyHooks {

        private final File file;

        public MyHooks(File file) {
            this.file = file;
        }

        @AfterMeasurement
        public void destroy(Object instance, Object[] objs) {
            try {
                ((FileInputStream) objs[0]).close();
            } catch (IOException ex) {
                System.err.println("Unable to close file stream.");
            }
        }

        @AfterBenchmark
        public void destroy() {
            file.delete();
        }
    }
}
