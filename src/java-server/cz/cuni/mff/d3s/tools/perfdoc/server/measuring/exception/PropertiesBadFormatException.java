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
 * This exception describes situation when user defined bad values in any of the
 * properties files.
 *
 * @author Jakub Naplava
 */
public class PropertiesBadFormatException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of <code>MeasurementException</code> without
     * detail message.
     */
    public PropertiesBadFormatException() {
    }

    /**
     * Constructs an instance of <code>MeasurementException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public PropertiesBadFormatException(String msg) {
        super(msg);
    }
}
