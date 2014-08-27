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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *Class to cache loaded method and classes
 * @author Jakub Naplava
 */
public class ReflectionCache {
    private static Map<String, Method> methodCache = new ConcurrentHashMap<>();
    private static Map<String, Class<?>> classCache = new ConcurrentHashMap<>();
    
    public static void addMethod(String name, Method method) {
        methodCache.put(name, method);
    }
    
    public static void addClass(String name, Class<?> clazz) {
        classCache.put(name, clazz);
    }
    
    public static Method getMethod(String name) {
        return methodCache.get(name);
    }
    
    public static Class<?> getClass(String name) {
        return classCache.get(name);
    }
}