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

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * @author arahusky
 */
public class ClassParser {

    public Class<?> clazz;
    
    public static void main(String[] args) {
        try {
            ClassParser cp  = new ClassParser("simplehtmldoclet.SimpleHTMLDoclet");
            System.out.println(cp.clazz.getName());
            Method[] methods = cp.clazz.getMethods();
            for (Method m : methods)
            {
                System.out.println(m.getName());
            }
            
            System.out.println("finding method");
            
            Method m = cp.findMethod("main", "j");
            
            System.out.println(m.getName());
        } catch (MalformedURLException ex) {
            System.out.println("malformed url");;
        } catch (ClassNotFoundException ex) {
            System.out.println("class not found");
        }
    }

    /**
     * Creates new ClassParser instance for the specified class, which is determined by a className
     * @param className
     * @throws MalformedURLException when there was a problem when parsing class location
     * @throws ClassNotFoundException when the class was not found
     */
    public ClassParser(String className) throws MalformedURLException, ClassNotFoundException {
        loadClass(className);
    }

    /**
     * Loads and saves the specified class
     * @param className
     * @throws ClassNotFoundException when tested method or generator method were not found
     * @throws MalformedURLException when files in which to search the files are in a bad format 
     */
    private void loadClass(String className) throws ClassNotFoundException, MalformedURLException {
        File file = new File("C:\\Users\\arahusky\\Google Drive\\Rocnikac\\SimpleHTMLDoclet\\build\\classes");

        //convert the file to URL format
        URL url = file.toURI().toURL();
        URL[] urls = new URL[]{url};

        //load this folder into Class loader
        ClassLoader cl = new URLClassLoader(urls);

        clazz = cl.loadClass(className);
    }

    /**
     * Finds specified method
     * @param methodName
     * @param abbrParams 
     * @return the Method instance if found, otherwise null
     * @throws ClassNotFoundException 
     */
    public Method findMethod(String methodName, String abbrParams) throws ClassNotFoundException {

        Method[] methods = clazz.getMethods();

        for (Method m : methods) {
            if (m.getName().equals(methodName) && (m.getParameterCount() == abbrParams.length())) {
                Parameter[] parameters = m.getParameters();

                boolean isCorrect = true;

                for (int i = 0; i < parameters.length; i++) {
                    //TODO repair!!!
//                    if (parameters[i].getType().getTypeName().charAt(0) != abbrParams.charAt(i)) {
//                        System.out.println("---------");
//                        System.out.println(parameters[i].getType().getTypeName());
//                        isCorrect = false;
//                        break;
//                    }
                }

                if (isCorrect) {
                    return m;
                }
            }
        }

        return null;
    }
}
