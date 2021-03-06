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
package cz.cuni.mff.d3s.tools.perfdoc.server.cache;

import java.lang.reflect.Method;

/**
 * Interface to communicate with things (methods and classes) saved via
 * reflection.
 *
 * @author Jakub Naplava
 */
public interface ReflectionCache {

    void addMethod(String name, Method method);

    void addClass(String name, Class<?> clazz);

    Method getMethod(String name);

    Class<?> getClass(String name);
}
