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

import cz.cuni.mff.d3s.tools.perfdoc.server.HttpMeasureServer;
import cz.cuni.mff.d3s.tools.perfdoc.server.MethodInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.ReflectionCache;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.ReflectionConcurrentMapCache;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a .class file and provides simple methods to work with
 * it.
 *
 * This class also maintains loading class into JVM (if is not present).
 *
 * @author Jakub Naplava
 */
public class ClassParser {

    private static final Logger log = Logger.getLogger(ClassParser.class.getName());

    //class path, where the classes, which are needed for correct run, are located
    private static final List<String> classPaths;

    static {
        classPaths = prepareClassPaths();
    }

    //cache where the already loaded measuredMethods and generators are stored
    private ReflectionCache refCache;

    //loaded class
    private Class<?> clazz;

    //classloader that is used to load all measuredMethods and generators
    static ClassLoader cl;

    /**
     * Creates new ClassParser instance for the specified class, which is
     * determined by a className.
     *
     * @param className
     * @throws ClassNotFoundException when measuredMethod or generator method
     * were not found
     * @throws MalformedURLException when files in which to search the files are
     * in a bad format
     */
    public ClassParser(String className) throws MalformedURLException, ClassNotFoundException, IOException {
        loadClass(className);
    }

    /**
     * Loads and saves the specified class.
     *
     * @param className
     * @throws ClassNotFoundException when measuredMethod or generator method
     * were not found
     * @throws MalformedURLException when files in which to search the files are
     * in a bad format
     */
    private void loadClass(String className) throws ClassNotFoundException, MalformedURLException, IOException {
        try {
            refCache = ReflectionConcurrentMapCache.getInstance();

            if (cl == null) {
                URL[] urls = findClassPaths();
                                
                cl = new URLClassLoader(urls);
                                
                clazz = cl.loadClass(className);
                refCache.addClass(className, clazz);
                log.log(Level.CONFIG, "ClassName, clazz); {0} was found and saved.", className);
                return;
            }

            if ((clazz = refCache.getClass(className)) == null) {
                clazz = cl.loadClass(className);
                refCache.addClass(className, clazz);
                log.log(Level.CONFIG, "ClassName, clazz); {0} was found and saved.", className);
            }
        } catch (ClassNotFoundException e) {
            System.out.println(e);
            log.log(Level.SEVERE, "Class was not found", e);
            throw e;
        } catch (IOException ei) {
            System.out.println(ei);
            log.log(Level.SEVERE, "Unable to read from file containing class directories, or its bad format", ei);
            throw ei;
        }
    }

    /**
     * Returns array containing URL of all class paths.
     */
    private URL[] findClassPaths() throws IOException {
        List<URL> urls = new ArrayList<>();

        for (String path : prepareClassPaths()) {
            File file = new File(path);
            URL url = file.toURI().toURL();
            urls.add(url);
        }

        return urls.toArray(new URL[urls.size()]);
    }

    /**
     * Gets class paths of all methods, that are used by measuredMethod or
     * generator.
     *
     * Specifically reads all lines from Class_classPath.txt, where the paths
     * are stored.
     */
    private static List<String> prepareClassPaths() {

        String fileContainingClassPaths = "Class_classPath.txt";
        String configurationDirectory = HttpMeasureServer.getConfigurationDirectory();
        
        List<String> classPaths = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(configurationDirectory + fileContainingClassPaths))) {
            String line;
            while ((line = reader.readLine()) != null) {
                classPaths.add(line);
            }
        } catch (FileNotFoundException ex) {
            log.log(Level.SEVERE, "File containing class paths was not found.", ex);
        } catch (IOException e) {
            log.log(Level.SEVERE, "There was a problem while working with file containing class paths.", e);
        }

        if (classPaths.isEmpty()) {
            log.log(Level.WARNING, "File containing class paths is empty, the server will probably have troubles finding the right generators.");
        }

        return classPaths;
    }

    /**
     * Returns the class path, where the classes, which are needed for correct
     * run, are located.
     */
    public static List<String> getClassPaths() {
        //we do not want to share our private list, but only its copy
        List<String> newList = new ArrayList<>(classPaths.size());
        for (String str : classPaths) {
            newList.add(str);
        }
        
        return newList;
    }

    /**
     * Finds specified method determined by MethodInfo instance.
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
            if (m.getName().equals(methodName) && (m.getParameterTypes().length == params.size())) {
                Class<?>[] parameters = m.getParameterTypes();

                boolean isCorrect = true;

                for (int i = 0; i < parameters.length; i++) {
                    if (!parameters[i].getCanonicalName().equals(params.get(i))) {
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

    /**
     * Return class loaded by this ClassParser.
     */
    public Class<?> getLoadedClass() {
        return clazz;
    }

    private Method searchMethodInCache(MethodInfo methodInfo) {
        return refCache.getMethod(methodInfo.toString());
    }

    private void addMethodInCache(MethodInfo methodInfo, Method method) {
        refCache.addMethod(methodInfo.toString(), method);
    }
}
