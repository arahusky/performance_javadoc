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

import cz.cuni.mff.d3s.tools.perfdoc.server.cache.ReflectionConcurrentMapCache;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.ReflectionCache;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jakub Naplava
 */
public class ClassParser {

    private static final Logger log = Logger.getLogger(ClassParser.class.getName());
    
    private ReflectionCache refCache;
     
    public Class<?> clazz;

    static ClassLoader cl;

    /**
     * Creates new ClassParser instance for the specified class, which is
     * determined by a className
     *
     * @param className
     * @throws MalformedURLException when there was a problem when parsing class
     * location
     * @throws ClassNotFoundException when the class was not found
     */
    public ClassParser(String className) throws MalformedURLException, ClassNotFoundException, IOException {
        loadClass(className);        
    }

    /**
     * Loads and saves the specified class
     *
     * @param className
     * @throws ClassNotFoundException when tested method or generator method
     * were not found
     * @throws MalformedURLException when files in which to search the files are
     * in a bad format
     */
    private void loadClass(String className) throws ClassNotFoundException, MalformedURLException, IOException {
        try {
            refCache = ReflectionConcurrentMapCache.getInstance();
            
            if (cl == null) {
                URL[] urls = findClassClassPaths();
                cl = new URLClassLoader(urls);
                clazz = cl.loadClass(className);
                refCache.addClass(className, clazz);
                log.log(Level.CONFIG, "ClassssName, clazz); {0} was found and saved.", className);
                return;
            }

            if ((clazz = refCache.getClass(className)) == null) {
                clazz = cl.loadClass(className);
                refCache.addClass(className, clazz);
                log.log(Level.CONFIG, "ClassssName, clazz); {0} was found and saved.", className);
            }
        } catch (ClassNotFoundException e) {
            log.log(Level.SEVERE, "Class was not found", e);
            throw e;
        } catch (IOException ei) {
            log.log(Level.SEVERE, "Unable to read from file containing class directories, or its bad format", ei);
            throw ei;
        }
    }

    private URL[] findClassClassPaths() throws IOException {
        ArrayList<URL> urls = new ArrayList<>();

        //TODO copy somewhere or what
        try (BufferedReader reader = new BufferedReader(new FileReader("Class_classPath.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                File file = new File(line);
                URL url = file.toURI().toURL();
                urls.add(url);
            }
        }

        return urls.toArray(new URL[urls.size()]);
    }

    /**
     * Finds specified method
     *
     * @param methodInfo
     * @return the Method instance if found, otherwise null
     */
    public Method findMethod(MethodInfo methodInfo) {

        String methodName = methodInfo.getMethodName();
        ArrayList<String> params = methodInfo.getParams();

        Method met;
        if ((met = searchMethodInCache(methodInfo)) != null) {
            return met;
        }

        Method[] methods = clazz.getMethods();

        for (Method m : methods) {
            if (m.getName().equals(methodName) && (m.getParameterCount() == params.size())) {
                Parameter[] parameters = m.getParameters();

                boolean isCorrect = true;

                for (int i = 0; i < parameters.length; i++) {
                    if (!parameters[i].getType().getCanonicalName().equals(params.get(i))) {
                        isCorrect = false;
                        break;
                    }
                }

                if (isCorrect) {
                    addMethodInCache(methodInfo, m);
                    return m;
                }
            }
        }

        return null;
    }

    private Method searchMethodInCache(MethodInfo methodInfo) {
        return refCache.getMethod(methodInfo.toString());
    }

    private void addMethodInCache(MethodInfo methodInfo, Method method) {
        refCache.addMethod(methodInfo.toString(), method);
    }
}
