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

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import com.sun.tools.javadoc.Main;
import java.util.ArrayList;

/**
 * Class that maintains searching of generators (including loading .java file
 * (using doclet API))
 *
 * @author Jakub Naplava
 */
public class ClassParser {

    public static String[][] arguments;

    /**
     *
     * @param workloadName the workloadName in format package.className#method
     * @return all methods, that are in the specified package with the specified
     * method name and contain generator annotation
     */
    public static MethodDoc[] findMethods(String workloadName) {
     
        String[] field = workloadName.split("#");

        //class part is containing dots instead of slashes (we need to replace it) and add .java to the end
        String className = field[0].replaceAll("\\.", "/") + ".java";
        String methodName = field[1];

        //setting the Analyzer methodName to the correct one
        Analyzer.methodName = methodName;

        String[] sourcePath = returnSourcePath();
                
        if (sourcePath == null) {
            Main.execute("", Analyzer.class.getName(), new String[]{className});
        } else {
            //debug
            System.out.println("searching for class:" + sourcePath[1] + "/" + className);
            
            //todo fixme
            String[] args = new String[]{sourcePath[0], sourcePath[1], sourcePath[1] + "/" + className};
            
            Main.execute("", Analyzer.class.getName(), args);
        }

        return Analyzer.methods;
    }    

    /**
     * Method that finds all the possible value of the given (enum) class
     *
     * @param className1 the name of the .java file (without .java)
     * @return the FieldDoc[] containing all enum values of given enum or null
     * if the given name is not an enum
     */
    public static FieldDoc[] findEnums(String className1) {
        String className = className1.replaceAll("\\.", "/") + ".java";

        String[] sourcePath = returnSourcePath();

        if (sourcePath == null) {
            Main.execute("", EnumAnalyzer.class.getName(), new String[]{className});
        } else {
            //debug
            System.out.println("searching for class:" + sourcePath[1] + "/" + className);
            
            //TODO fixme
            Main.execute("", EnumAnalyzer.class.getName(), new String[]{sourcePath[1] + "/" + className});
        }

        Main.execute("", EnumAnalyzer.class.getName(), new String[]{className});
        System.out.println("succesfull");
        return EnumAnalyzer.enumValues;
    }
    
    private static String[] returnSourcePath() {

        for (int i = 0; i < arguments.length; i++) {
            String[] arr = arguments[i];
            if (arr[0].equals("-sourcepath")) {
                return new String[]{"-sourcepath", arr[1]};
            }
        }

        return null;
    }

    /**
     * doclet class, that gets the RootDoc from the javadoc and searches for the
     * right methods in the specified class
     */
    public static class Analyzer extends Doclet {

        //should be set before the javadoc run
        public static String methodName;

        public static MethodDoc[] methods;

        /**
         * The method in right format for javadoc (to behave as a doclet)
         *
         * @param root
         * @return
         */
        public static boolean start(RootDoc root) {

            //should never happen
            if (root.classes().length != 1) {
                return false;
            }

            ArrayList<MethodDoc> list = new ArrayList<>();

            if (root.classes().length == 0) {
                return false;
            }

            ClassDoc classDoc = root.classes()[0];
 
            for (MethodDoc methodDoc : classDoc.methods()) {
                if (methodDoc.name().equals(methodName) && checkAnnotation(methodDoc)) {
                    list.add(methodDoc);
                }
            }

            methods = list.toArray(new MethodDoc[list.size()]);
            return true;
        }

        /**
         * Checks, whether the specified method has just just one
         * Generator-annotation
         *
         * @param doc the MethodDoc of the investigated method
         * @return true, if the specified method has one Generator-annotation
         */
        private static boolean checkAnnotation(MethodDoc doc) {
            AnnotationDesc[] annotations = doc.annotations();

            int numberOfGenerators = 0;

            for (AnnotationDesc annot : annotations) {
                if ("cz.cuni.mff.d3s.tools.perfdoc.annotations.Generator".equals(annot.annotationType().toString())) {
                    numberOfGenerators++;
                }
            }

            return (numberOfGenerators == 1);
        }
    }

    /**
     * doclet class, that gets the RootDoc from the javadoc and searches for the
     * enum values
     */
    public static class EnumAnalyzer extends Doclet {

        public static FieldDoc[] enumValues = null;

        public static boolean start(RootDoc root) {
            //debug
            System.out.println("I started");
            ClassDoc[] classes = root.classes();

            if (classes.length == 0) {
                //debug
                System.out.println("ended first");
                return false;
            }

            ClassDoc cd = classes[0];

            if (cd.isEnum()) {
                enumValues = cd.enumConstants();
            }

            //debug
            System.out.println("still here");
            return true;
        }

        /**
         * needs to be added because otherwise it is working in pre-5.0
         * compatibility mode which makes some methods behave unexpected, mostly
         * because enum was added in Java 5.0
         */
        public static LanguageVersion languageVersion() {
            return LanguageVersion.JAVA_1_5;
        }
    }
}
