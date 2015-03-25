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
package cz.cuni.mff.d3s.tools.perfdoc.server;

import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.ClassParser;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Extension of MethodInfo adding reflection information about method and its
 * containing class.
 *
 * @author Jakub Naplava
 */
public class MethodReflectionInfo extends MethodInfo {

    private final Class<?> containingClass;
    private final Method method;

    /**     *
     * @param methodData data either from incoming json or another
     * MethodInfo.toString()
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws java.lang.NoSuchMethodException
     */
    public MethodReflectionInfo(String methodData) throws ClassNotFoundException, IOException, NoSuchMethodException {
        super(methodData);

        ClassParser cp = new ClassParser(containingClassQualifiedName);
        this.containingClass = cp.getLoadedClass();
        this.method = cp.findMethod(this);
        
        if (this.method == null) {
            throw new NoSuchMethodException("The requested method: " + this.getMethodName() + " with requested parameters does not exist .");
        }
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getContainingClass() {
        return containingClass;
    }
}
