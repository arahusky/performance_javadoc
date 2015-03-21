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

import cz.cuni.mff.d3s.tools.perfdoc.annotations.Workload;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 
 * @author Jakub Naplava
 */
public class FileRead {

    /**
     * Reads from input stream, until EOF reached.
     *
     * @param stream
     * @return against DCE
     */
    @Workload("example005.FileGenerator#prepareStream")
    public static int read(FileInputStream stream) {
        int character;
        int sum = 0;

        try {
            while ((character = stream.read()) != -1) {
                sum += character;
            }
        } catch (IOException ex) {
            System.err.println("Stream already closed");
        }

        return sum;
    }
}
