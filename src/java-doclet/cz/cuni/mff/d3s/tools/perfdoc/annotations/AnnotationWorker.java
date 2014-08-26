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
import java.lang.annotation.Annotation;

/**
 *
 * @author arahusky
 */
public class AnnotationWorker {

    /**
     * Returns the first ParamNum annotation from given annotations
     * @param annotations The array of annotations that belong to one method
     * @return the first ParamNum annotation from the annotations
     */
    public static ParamNum getParamNum(AnnotationDesc[] annotations) {
        for (AnnotationDesc annot : annotations) {
            if ("cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum".equals(annot.annotationType().toString())) {
                final String description = AnnotationParser.getAnnotationValueString(annot, "cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum.description()");
                final double min = AnnotationParser.getAnnotationValueDouble(annot, "cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum.min()");
                final double max = AnnotationParser.getAnnotationValueDouble(annot, "cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum.max()");
                
                double stepPom = AnnotationParser.getAnnotationValueDouble(annot, "cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum.step()");
                //if there is no step specified, we will use default value, which is 1
                final double step = (stepPom == Double.MIN_VALUE) ? 1 : stepPom;
                
                final boolean axis = AnnotationParser.getAnnotationValueBoolean(annot, "cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum.axis()");
                                
                return new ParamNum() {

                    @Override
                    public String description() {
                        return description;
                    }

                    @Override
                    public double min() {
                        return min;
                    }

                    @Override
                    public double max() {
                        return max;
                    }

                    @Override
                    public double step() {
                        return step;
                    }

                    @Override
                    public boolean axis() {
                        return axis;
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return ParamNum.class;
                    }
                };
            }
        }
        
        return null;
    }

    /**
     * Returns the first Generator anotation from given annotations
     *
     * @param annotations The array of annotations that belong to one method
     * @return the first Generator annotation from the annotations
     */
    public static Generator getGenerator(AnnotationDesc[] annotations) {
        for (AnnotationDesc annot : annotations) {
            if ("cz.cuni.mff.d3s.tools.perfdoc.annotations.Generator".equals(annot.annotationType().toString())) {
                final String description = AnnotationParser.getAnnotationValueString(annot, "cz.cuni.mff.d3s.tools.perfdoc.annotations.Generator.description()");
                final String genName = AnnotationParser.getAnnotationValueString(annot, "cz.cuni.mff.d3s.tools.perfdoc.annotations.Generator.genName()");

                return new Generator() {

                    @Override
                    public String description() {
                        return description;
                    }

                    @Override
                    public String genName() {
                        return genName;
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return Generator.class;
                    }
                };

            }
        }

        return null;
    }
}
