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
package cz.cuni.mff.d3s.tools.perfdoc.server.measuring.exception;

/**
 * This exception associates all exceptions (such as IllegalAccessException or
 * InstantiationException) reporting that none of the measurement could have
 * been made properly.
 *
 * @author Jakub Naplava
 */
public class MeasurementException extends Exception {

    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance of <code>MeasurementException</code> without
     * detail message.
     */
    public MeasurementException() {
    }

    /**
     * Constructs an instance of <code>MeasurementException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public MeasurementException(String msg) {
        super(msg);
    }
}
