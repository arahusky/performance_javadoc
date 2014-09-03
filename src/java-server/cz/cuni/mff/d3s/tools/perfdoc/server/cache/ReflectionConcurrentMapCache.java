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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *Class to cache loaded method and classes
 * @author Jakub Naplava
 */
public class ReflectionConcurrentMapCache implements ReflectionCache{
    private static final Map<String, Method> methodCache = new ConcurrentHashMap<>();
    private static final Map<String, Class<?>> classCache = new ConcurrentHashMap<>();
    
    private static ReflectionConcurrentMapCache instance= null;
    private static final Object mutex= new Object();
    
    private ReflectionConcurrentMapCache(){
    }
 
    public static ReflectionConcurrentMapCache getInstance(){
        if(instance==null){
            synchronized (mutex){
                if(instance==null) instance= new ReflectionConcurrentMapCache();
            }
        }
        return instance;
    }
    
    @Override
    public void addMethod(String name, Method method) {
        methodCache.put(name, method);
    }
    
    @Override
    public void addClass(String name, Class<?> clazz) {
        classCache.put(name, clazz);
    }
    
    @Override
    public Method getMethod(String name) {
        return methodCache.get(name);
    }
    
    @Override
    public Class<?> getClass(String name) {
        return classCache.get(name);
    }
}
