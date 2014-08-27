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
 * This exception stands for the number (int, float, double) parameter, that lacks any ParamNum annotation
 * @author Jakub Naplava
 */
public class GeneratorParamNumException extends Exception {

    /**
     * Creates a new instance of <code>GeneratorParamNumException</code> without
     * detail message.
     */
    public GeneratorParamNumException() {
    }

    /**
     * Constructs an instance of <code>GeneratorParamNumException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public GeneratorParamNumException(String msg) {
        super(msg);
    }
}
