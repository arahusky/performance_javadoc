/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.cuni.mff.d3s.tools.perfdoc.annotations;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;

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
        return false;
    }
}