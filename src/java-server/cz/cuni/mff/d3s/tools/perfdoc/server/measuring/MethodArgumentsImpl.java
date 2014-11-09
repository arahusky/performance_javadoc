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

import java.util.Arrays;

/**
 *
 * @author Jakub Naplava
 */
public class MethodArgumentsImpl implements MethodArguments {

    public static final String argumentsDelimiter = ",";
    
    private final Object[] arguments;

    public MethodArgumentsImpl(Object[] arguments) {
        this.arguments = arguments;
    }

    /**
     * Creates new instance of MethodArguments from given string (obtained from
     * database). Attention: the argument array will contain every element as
     * String!
     *
     * @param arguments should be in format: [arg1,arg2,...,argN] or without
     * brackets
     */
    public MethodArgumentsImpl(String arguments) {
        if (arguments.startsWith("[") && arguments.endsWith("]")) {
            arguments = arguments.substring(1, arguments.length() - 1);
        }

        String[] chunks = arguments.split(argumentsDelimiter);
        this.arguments = new Object[chunks.length];

        for (int i = 0; i < chunks.length; i++) {
            this.arguments[i] = chunks[i];
        }
    }

    @Override
    public Object[] getValues() {
        return arguments;
    }

    @Override
    public String getValuesDBFormat(Boolean omit) {
        int itemsToSkip = 0;
        if (omit) {
            itemsToSkip = 2;
        }

        if (arguments == null || arguments.length < (itemsToSkip + 1)) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(arguments[itemsToSkip]);

        for (int i = (itemsToSkip + 1); i < arguments.length; i++) {
            sb.append(argumentsDelimiter).append(arguments[i]);
        }

        sb.append("]");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Arrays.deepHashCode(this.arguments);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof MethodArgumentsImpl)) {
            return false;
        }
        MethodArgumentsImpl ma = (MethodArgumentsImpl) o;
        return Arrays.equals(ma.arguments, this.arguments);
    }
}
