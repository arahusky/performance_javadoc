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
package cz.cuni.mff.d3s.tools.perfdoc.doclets.formats.html;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that maintains searching of generators (including loading .java file)
 * (using doclet API)
 *
 * @author Jakub Naplava
 */
public class ClassParser {

    private static ClassLoader workloadClassLoader;

    /**
     *
     * @param workloadName the workloadName in format package.className#method
     * @return all methods, that are in the specified package with the specified
     * method name and contain generator annotation
     */
    public static Method[] findMethods(String workloadName) throws ClassNotFoundException, MalformedURLException {
        if (workloadClassLoader == null) {
            initializeWorkloadClassLoader();
        }

        String[] field = workloadName.split("#");

        String className = field[0];
        String methodName = field[1];

        Class<?> cls = workloadClassLoader.loadClass(className);
        Method[] methods = cls.getMethods();
        List<Method> retMethods = new ArrayList<>();

        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                retMethods.add(m);
            }
        }

        return retMethods.toArray(new Method[retMethods.size()]);
    }

    /**
     * Method that finds all the possible value of the given (enum) class
     *
     * @param className the name of the class
     * @return the FieldDoc[] containing all enum values of given enum or null
     * if the given name is not an enum
     */
    public static Object[] findEnums(String className) throws MalformedURLException, ClassNotFoundException {
        if (workloadClassLoader == null) {
            initializeWorkloadClassLoader();
        }

        //load the Address class in 'c:\\other_classes\\'
        Class<?> cls = workloadClassLoader.loadClass(className);

        return cls.getEnumConstants();
    }

    private static void initializeWorkloadClassLoader() throws MalformedURLException {
        List<URL> list = new ArrayList<>();
        for (String wPath : DocletArguments.getWorkloadPath()) {
            File file = new File(wPath);

            //convert the file to URL format
            list.add(file.toURI().toURL());
        }
        
        URL[] urls = list.toArray(new URL[list.size()]);

        //load this folder into Class loader
        workloadClassLoader = new URLClassLoader(urls, ClassParser.class.getClassLoader());
    }
}
