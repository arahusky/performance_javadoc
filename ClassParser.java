/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.cuni.tools.doclets.formats.html;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import com.sun.tools.javadoc.Main;
import java.util.ArrayList;



/**
 * Class that maintains searching of generators (including loading .java file (using doclet API))
 * @author arahusky
 */
public class ClassParser {
    /**
     * 
     * @param workloadName the workloadName in format package.className#method
     * @return 
     */
    public static MethodDoc[] findMethods(String workloadName) {
        
       String[] field = workloadName.split("#");
       
       //class part is containing dots instead of slashes (we need to replace it) and add .java to the end
       String className = field[0].replaceAll("\\.", "/") + ".java";
       String methodName = field[1];
        
       //setting the Analyzer methodName to the correct one
      Analyzer.methodName = methodName;
       
       Main.execute("", Analyzer.class.getName(), new String[] { className });
       
       
       return Analyzer.methods;
    }
    
    /**
     * doclet class, that gets the RootDoc from the javadoc and searches for the right methods in the specified class
     */
     public static class Analyzer extends Doclet {
        
        //should be set before the javadoc run
        public static String methodName;
        
        public static MethodDoc[] methods;

        /**
         * The method in right format for javadoc (to behave as a doclet)
         */
        public static boolean start(RootDoc root) {
                        
            //should never happen
            if (root.classes().length != 1)
                return false;
            
            ArrayList<MethodDoc> list = new ArrayList<>();
            
            ClassDoc classDoc = root.classes()[0];
            
            for (MethodDoc methodDoc : classDoc.methods()) {
                if (methodDoc.name().equals(methodName) && checkAnnotation(methodDoc))
                {                    
                        list.add(methodDoc);
                }
            }
            
            methods = list.toArray(new MethodDoc[list.size()]);
            return true;
        }
        
        /**
         * Checks, whether the specified method has just just one Generator-annotation
         * @param doc the MethodDoc of the investigated method
         * @return true, if the specified method has one Generator-annotation
         */
        private static boolean checkAnnotation(MethodDoc doc)
        {
            AnnotationDesc[] annotations = doc.annotations();
            
            int numberOfGenerators = 0;
            
            for (AnnotationDesc annot : annotations)
            {
                if ("cz.cuni.annotations.Generator".equals(annot.annotationType().toString()))
                {
                    numberOfGenerators++;
                }
            }
            
            return (numberOfGenerators == 1);
        }
    }
}
