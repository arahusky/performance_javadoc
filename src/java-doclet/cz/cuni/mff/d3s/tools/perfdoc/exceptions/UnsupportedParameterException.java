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
 * This exception stands for the parameter in generator, that is not supported (the list of supported parameters can be find in the documentation).
 * @author Jakub Naplava
 */
public class UnsupportedParameterException extends Exception {

    /**
     * Creates a new instance of <code>UnsoportedParameterException</code>
     * without detail message.
     */
    public UnsupportedParameterException() {
    }

    /**
     * Constructs an instance of <code>UnsoportedParameterException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public UnsupportedParameterException(String msg) {
        super(msg);
    }
}
