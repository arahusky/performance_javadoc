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
package cz.cuni.mff.d3s.tools.perfdoc.annotations.workers;

import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamDesc;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 *
 * @author Jakub Naplava
 */
public class AnnotationUtils {

    /**
     * Returns description of generator parameters (except for first two of
     * them, which parameterless Workloads).
     *
     * @param method The method, which parameters will be obtained
     * @return the String[] where i-th item contains the description obtained
     * from i-th method parameter (description comes from ParamDesc or ParamNum
     * annotation)
     */
    public static String[] geParameterDescriptions(Method method) {

        Annotation[][] annotations = method.getParameterAnnotations();
        String[] result = new String[annotations.length - 2];

        //first two parameters are Workload and ServiceWorkload
        for (int i = 2; i < annotations.length; i++) {;
            Annotation[] annot = annotations[i];

            for (Annotation a : annot) {
                if ("cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum".equals(a.annotationType().getName())) {
                    result[i - 2] = ((ParamNum) a).description();
                } else if ("cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamDesc".equals(a.annotationType().getName())) {
                    result[i - 2] = ((ParamDesc) a).value();
                }
            }
        }
        return result;
    }
}
