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
package cz.cuni.mff.d3s.tools.perfdoc.exceptions;

/**
 * This is more then an exception a warning denoting that a particular method
 * may be a generator (there exists measured method having this method in
 * Workload annotation), but has no Generator annotation.
 *
 * @author Jakub Naplava
 */
public class NoGeneratorAnnotation extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of <code>NoGeneratorAnnotation</code> without
     * detail message.
     */
    public NoGeneratorAnnotation() {
    }

    /**
     * Constructs an instance of <code>NoGeneratorAnnotation</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public NoGeneratorAnnotation(String msg) {
        super(msg);
    }
}
