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

import com.sun.tools.doclets.formats.html.markup.HtmlAttr;
import com.sun.tools.doclets.formats.html.markup.HtmlTag;
import com.sun.tools.doclets.formats.html.markup.HtmlTree;
import com.sun.tools.doclets.formats.html.markup.RawHtml;
import com.sun.tools.doclets.internal.toolkit.Content;
import com.sun.tools.doclets.internal.toolkit.util.DocletConstants;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.AnnotationParser;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.Generator;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamDesc;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum;
import cz.cuni.mff.d3s.tools.perfdoc.doclets.formats.html.js.JSControlWriter;
import cz.cuni.mff.d3s.tools.perfdoc.doclets.formats.html.js.JSSliderWriter;
import cz.cuni.mff.d3s.tools.perfdoc.exceptions.GeneratorParamNumException;
import cz.cuni.mff.d3s.tools.perfdoc.exceptions.GeneratorParameterException;
import cz.cuni.mff.d3s.tools.perfdoc.exceptions.NoEnumValueException;
import cz.cuni.mff.d3s.tools.perfdoc.exceptions.NoGeneratorAnnotation;
import cz.cuni.mff.d3s.tools.perfdoc.exceptions.NoWorkloadException;
import cz.cuni.mff.d3s.tools.perfdoc.exceptions.UnsupportedParameterException;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 *
 * @author Jakub Naplava
 */
public class PerformanceBodyWriter {

    static ConfigurationImpl configuration = ConfigurationImpl.getInstance();

    /**
     * {@inheritDoc}
     */
    public PerformanceOutput returnTitleOutput() {
        PerformanceOutput result = new PerformanceOutputImpl(DocletConstants.NL + "<dt>"
                + "<span class=\"strong\">" + "Performance:"
                + "</span>" + "</dt>" + "<dd>" + "</dd>");
        return result;
    }

    /**
     * Generates the code that represents one generator.
     *
     * @param doc the MethodDoc that represents the generator
     * @param uniqueGeneratorName
     * @param hidden whether the particular div should be hidden or not
     * @return HtmlTree that represents the generator or null if some error
     * occurred
     */
    public HtmlTree returnPerfoDiv(Method doc, String uniqueGeneratorName, String generatorFullName, boolean hidden) {

        //the main HtmlTree, that will contain two subtrees (left and right), that represent the left part and right part of the performance look
        HtmlTree navList = new HtmlTree(HtmlTag.DIV);
        if (hidden) {
            navList.addAttr(HtmlAttr.CLASS, "wrapperHidden");
        } else {
            navList.addAttr(HtmlAttr.CLASS, "wrapper");
        }
        navList.addAttr(HtmlAttr.ID, uniqueGeneratorName);

        HtmlTree leftSide = new HtmlTree(HtmlTag.DIV);
        leftSide.addAttr(HtmlAttr.CLASS, "left");

        try {
            addFormPart(leftSide, doc, uniqueGeneratorName, generatorFullName);
        } catch (GeneratorParameterException e) {
            String parameter = e.getMessage();
            configuration.root.printWarning("The parameter '" + parameter + "' in generator '" + doc.getName() + "' has no annotation and is not Workload or ServiceWorkload. Therefore no performance info will be generated.");
            return null;
        } catch (NoWorkloadException | NumberFormatException ex) {
            configuration.root.printWarning(ex.getMessage());
            return null;
        } catch (UnsupportedParameterException ex) {
            String parameterTypeName = ex.getMessage();
            configuration.root.printWarning("There is some parameter in generator '" + doc.getName() + "' of unsupported type '" + parameterTypeName +  "'. Therefore no performance info will be generated.");
            return null;
        } catch (GeneratorParamNumException ex) {
            configuration.root.printWarning("Some of the generator '" + doc.getName() + "' parameters is of number type, but lacks any ParamNum acnnotation. Therefore no performance info will be generated.");
            return null;
        } catch (NoEnumValueException ex) {
            configuration.root.printWarning("Some enum parameter of generator '" + doc.getName() + "' has got no possible value defined. Therefore no performance info will be generated.");
            return null;
        } catch (IOException ex) {
            configuration.root.printWarning(ex.getMessage());
        } catch (ClassNotFoundException ex) {
            configuration.root.printWarning("Workload/Enum class could not be found. . Therefore no performance info will be generated.");
            return null;
        } catch (NoGeneratorAnnotation ex) {
            configuration.root.printWarning("Found method: '" + doc.getName() + " that has a name referenced from Workload annotation, but having no Generator annotation, thus is not considered to be generator.");
            return null;
        }

        HtmlTree rightSide = new HtmlTree(HtmlTag.DIV);
        rightSide.addAttr(HtmlAttr.CLASS, "right");

        //div, where the graph will be shown
        HtmlTree graph = new HtmlTree(HtmlTag.DIV);
        graph.addAttr(HtmlAttr.CLASS, "graph");
        graph.addContent("Place, where the measured results will be shown.");
        rightSide.addContent(graph); 
        
        //div, where the table with results will be shown
        HtmlTree table = new HtmlTree(HtmlTag.DIV);
        table.addAttr(HtmlAttr.CLASS, "table hidden");
        table.addContent("Table containing measured results.");
        rightSide.addContent(table); 
        
        //div, where radios to choose output are
        HtmlTree outputFormat = new HtmlTree(HtmlTag.DIV);
        outputFormat.addAttr(HtmlAttr.CLASS, "radio");
        outputFormat.addContent(new RawHtml("<div></div>"));
        
        rightSide.addContent(outputFormat);
        
        navList.addContent(rightSide);
        navList.addContent(leftSide);

        return navList;
    }

    /**
     * Generates the performance code (generator description, sliders, ...,
     * submit button) for the particular generator.
     *
     * @param content the content to which the code should be added
     * @param doc the MethodDoc that represents the workload
     * @param generatorName
     * @throws NoSuchFieldException when the workload does not contain any
     * generator annotation
     */
    private void addFormPart(Content content, Method doc, String generatorName, String generatorFullName) throws GeneratorParameterException, NoWorkloadException, UnsupportedParameterException, NumberFormatException, GeneratorParamNumException, NoEnumValueException, IOException, ClassNotFoundException, NoGeneratorAnnotation {
        //getting generator annotation of the doc (it was already checked by classparser that it is not null)
        Generator gen = doc.getAnnotation(Generator.class);
        
        //if method does not have annotation Generator
        if (gen == null) {
            throw new NoGeneratorAnnotation();
        }
        
        //first part of the left tree is the generator description
        HtmlTree description = new HtmlTree(HtmlTag.P);
        
        description.addContent(gen.description());
        content.addContent(description);

        //then comes the string Configuration finally followed by all sliders/textboxes/...
        HtmlTree configurationTree = new HtmlTree(HtmlTag.P);

        configurationTree.addContent(new RawHtml("<b>Configuration:</b>"));
        content.addContent(configurationTree);

        Class<?>[] paramTypes = doc.getParameterTypes();
        int numberOfParameters = paramTypes.length;      

        if (numberOfParameters < 2) {
            throw new NoWorkloadException("Workload " + doc.getName() + " has not enough arguments (less then 2). Therefore no performance info will be generated.");
        }

        if (!paramTypes[0].getName().equals("cz.cuni.mff.d3s.tools.perfdoc.workloads.Workload")) {
            throw new NoWorkloadException("Workload " + doc.getName() + " does not contain Workload variable as the first parameter. Therefore no performance info will be generated.");
        }

        if (!paramTypes[1].getName().equals("cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload")) {
            throw new NoWorkloadException("Workload " + doc.getName() + " does not contain ServiceWorkload variable as the second parameter. Therefore no performance info will be generated.");
        }

        //the number of parameter
        int number = 0;

        //telling JSSlider to begin generating JS
        JSSliderWriter.startNewGeneratorCode();
        JSControlWriter.startNewControlButton(generatorName, generatorFullName);

        for (int i = 2; i < numberOfParameters; i++) {
            addParameterPerfo(doc, i, content, generatorName, number);
            number++;
        }

        //telling JSSlider, that there's no error in generator and he can add it to the JS code
        JSSliderWriter.endGeneratorCode();

        //telling controlWriter, that there's not error in the generator so that he can add the control code to his global code
        JSControlWriter.endCurrentButton();

        //adding submit button action to the code
        content.addContent(JSControlWriter.returnButton(generatorName));
    }

    /**
     * Method that gets the parameter and content, and to the content adds the
     * appropriate element allowing user to select the value
     *
     * @param method method, which parameter we want to add
     * @param numberOfParam the position of parameter in method parameters
     * @param content the content to that the performance will be added
     * @param doc the methodDoc representing the method to that this parameter
     * belongs
     * @number the number of parameter
     * @throws GeneratorArgumentException when the param has no annotation and
     * is also not a Workload or ServiceWorkload
     */
    private void addParameterPerfo(Method method, int numberOfParam, Content content, String workloadName, int number) throws GeneratorParameterException, UnsupportedParameterException, NumberFormatException, GeneratorParamNumException, NoEnumValueException, IOException, ClassNotFoundException {
        Annotation[] annotations = method.getParameterAnnotations()[numberOfParam];
        String parameterTypeName = method.getParameterTypes()[numberOfParam].getName();
        
        if (annotations.length == 0) {            
            if (!(parameterTypeName.equals(ServiceWorkload.class.getName()) || parameterTypeName.equals("cz.cuni.mff.d3s.tools.perfdoc.workloads.Workload"))) {
                throw new GeneratorParameterException();
            }
        }

        String description = null;
        
        for (Annotation annot : annotations) {
            switch (annot.annotationType().getName()) {
                case "cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamDesc":
                    ParamDesc pd = (ParamDesc) annot;
                    description = pd.value();
                    break;
                case "cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum":
                    ParamNum pn = (ParamNum) annot;
                    description = pn.description();
                    break;
                //we do not forbid user to have there another annotations 
            }
        }

        //according the type of the parameter we call appropriate method
        switch (parameterTypeName) {
            case "int":
            case "float":
            case "double":
                addParameterNum(annotations, description, content, workloadName, number);
                break;
            case "java.lang.String":
                addParameterString(description, content, workloadName, number);
                break;
            default:
                Object[] enumValues = ClassParser.findEnums(parameterTypeName);

                //if it is not enum, or the enum has no possible values
                if (enumValues == null) {
                    throw new UnsupportedParameterException(parameterTypeName);
                } else if (enumValues.length == 0) {
                    throw new NoEnumValueException();
                } else {
                    addParameterEnum(enumValues, description, content, workloadName, number);
                }

                break;
        }
    }

    /**
     * Adds the slider to the content
     *
     * @param param the Parameter
     * @param description the parameter description
     * @param content the Content to which the slider will be added
     * @param workloadName the workloadName from which (with number) the unique
     * ID will be counted
     * @param number the number of parameter in the generator
     * @throws NumberFormatException
     * @throws GeneratorParamNumException if there is no ParamNum annotation
     * associated with this parameter
     */
    private void addParameterNum(Annotation[] annotations, String description, Content content, String workloadName, int number) throws NumberFormatException, GeneratorParamNumException, IOException {
       
        ParamNum paramNum = AnnotationParser.getParamNum(annotations);
        if (paramNum == null) {
            throw new GeneratorParamNumException();
        }

        double min = paramNum.min();
        double max = paramNum.max();
        double step = paramNum.step();
        boolean axis = paramNum.axis();

        String uniqueSliderName = workloadName + "_slider_" + number;
        String uniqueTextboxName = workloadName + "_sliderTextBox_" + number;
        JSSliderWriter.addNewSlider(uniqueSliderName, uniqueTextboxName, min, max, step, axis);

        String labelName = uniqueTextboxName + "Label";
        content.addContent(new RawHtml("<p>" + "<label for=\"" + uniqueTextboxName + "\" id = \"" + labelName + "\">" + description + "      </label>"));
        content.addContent(new RawHtml("<input type=\"text\" id=\"" + uniqueTextboxName + "\" style=\"border:0; color:#f6931f; font-weight:bold;\"> </p>"));
        content.addContent(new RawHtml("<div id=\"" + uniqueSliderName + "\" style=\"margin:10\"></div>"));

        //registering this slider to the control
        if (axis) {
            JSControlWriter.addDoubleSliderControl(uniqueTextboxName, uniqueSliderName, description);
        } else {
            JSControlWriter.addSliderControl(uniqueTextboxName, uniqueSliderName, description);
        }
    }

    /**
     * Adds the select textbox to the content
     *
     * @param description description of the select tag
     * @param content the Content to which the select tag should be added
     * @param workloadName the workloadName from which (with number) the unique
     * ID will be counted
     * @param number the number of parameter in the generator
     */
    private void addParameterString(String description, Content content, String workloadName, int number) {
        String uniqueTextboxName = workloadName + "_" + number;
        String input = "<p><label for=\"" + uniqueTextboxName + "\">" + description + "</label>: <input type=\"text\" id=\"" + uniqueTextboxName + "\"> </p>";
        content.addContent(new RawHtml(input));

        //registering this textbox to the control
        JSControlWriter.addStringControl(uniqueTextboxName, description);
    }

    /**
     * Adds the select html select tag with given enumValues and id to the
     * content
     *
     * @param enumValues the options of the select tag
     * @param description description of the select tag
     * @param content the Content to which the select tag should be added
     * @param workloadName the workloadName from which (with number) the unique
     * ID will be counted
     * @param number the number of parameter in the generator
     */
    private void addParameterEnum(Object[] enumValues, String description, Content content, String workloadName, int number) {
        String uniqueSelectName = workloadName + "_" + number;

        StringBuilder sb = new StringBuilder();
        sb.append("<p><label for=\"" + uniqueSelectName + "\">" + description + "</label>: <select id=\"" + uniqueSelectName + "\">");

        for (Object f : enumValues) {
            sb.append("<option>" + f + "</option>");
        }

        sb.append("</select> </p>");
        content.addContent(new RawHtml(sb.toString()));

        //registering this select to the control
        JSControlWriter.addEnumControl(uniqueSelectName, description);
    }
}
