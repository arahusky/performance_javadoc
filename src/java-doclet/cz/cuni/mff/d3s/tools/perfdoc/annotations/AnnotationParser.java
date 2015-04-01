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

package cz.cuni.mff.d3s.tools.perfdoc.annotations;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import java.lang.annotation.Annotation;

/**
 * Class, that should make it easier to work with AnnotationDesc
 *
 * @author Jakub Naplava
 */
public class AnnotationParser {

    /**
     * Method to return the String annotation value, that belongs to some key
     *
     * @param annotation
     * @param key
     * @return the value, that belongs to the key or null, if no such exists
     */
    public static String getAnnotationValueString(AnnotationDesc annotation, String key) {
        ElementValuePair[] evp = annotation.elementValues();

        for (ElementValuePair e : evp) {
            if (e.element().toString().equals(key)) {
                return e.value().toString().substring(1, e.value().toString().length() - 1);
            }
        }

        return null;
    }

    /**
     * Method to return the Double annotation value, that belongs to some key
     *
     * @param annotation
     * @param key
     * @return the value, that belongs to the key or null, if no such exists
     */
    public static double getAnnotationValueDouble(AnnotationDesc annotation, String key) throws NumberFormatException {
        ElementValuePair[] evp = annotation.elementValues();

        for (ElementValuePair e : evp) {
            if (e.element().toString().equals(key)) {
                return Double.parseDouble(e.value().toString());
            }
        }

        //to indicate, that nothing was found
        return Double.MIN_VALUE;
    }

    /**
     * Method to return the boolean annotation value, that belongs to some key
     *
     * @param annotation
     * @param key
     * @return the value, that belongs to the key or null, if no such exists
     */
    public static boolean getAnnotationValueBoolean(AnnotationDesc annotation, String key) throws NumberFormatException {
        ElementValuePair[] evp = annotation.elementValues();

        for (ElementValuePair e : evp) {
            if (e.element().toString().equals(key)) {
                return Boolean.parseBoolean(e.value().toString());
            }
        }

        //default value of axis is true
        return true;
    }
    
    public static ParamNum getParamNum(Annotation[] annotations) {
        for (Annotation a : annotations) {
            if (a.annotationType().getName().equals(ParamNum.class.getName())) {
                return (ParamNum) a;
            }
        }
        
        return null;
    }
}