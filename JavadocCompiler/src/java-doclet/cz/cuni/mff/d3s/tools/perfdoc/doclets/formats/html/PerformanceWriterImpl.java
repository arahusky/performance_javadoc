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
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.tools.doclets.formats.html.markup.HtmlAttr;
import com.sun.tools.doclets.formats.html.markup.HtmlTag;
import com.sun.tools.doclets.formats.html.markup.HtmlTree;
import com.sun.tools.doclets.formats.html.markup.RawHtml;
import com.sun.tools.doclets.internal.toolkit.Content;
import com.sun.tools.doclets.internal.toolkit.util.DocletConstants;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.AnnotationParser;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.AnnotationWorker;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.Generator;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum;
import cz.cuni.mff.d3s.tools.perfdoc.exceptions.GeneratorParamNumException;
import cz.cuni.mff.d3s.tools.perfdoc.exceptions.GeneratorParameterException;
import cz.cuni.mff.d3s.tools.perfdoc.exceptions.NoEnumValueException;
import cz.cuni.mff.d3s.tools.perfdoc.exceptions.NoWorkloadException;
import cz.cuni.mff.d3s.tools.perfdoc.exceptions.UnsupportedParameterException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jakub Naplava
 */
public class PerformanceWriterImpl {

    static ConfigurationImpl configuration = ConfigurationImpl.getInstance();

    /**
     * Method that prepares the title of Performance part
     *
     * @return Performance output containing performance title
     */
    public PerformanceOutput returnTitleOutput() {
        PerformanceOutput result = new PerformanceOutputImpl(DocletConstants.NL + "<dt>"
                + "<span class=\"strong\">" + "Performance:"
                + "</span>" + "</dt>" + "<dd>" + "</dd>");
        return result;
    }

    /**
     * Generates the code that represents one generator
     *
     * @param doc the MethodDoc that represents the generator
     * @param uniqueWorkloadName
     * @param hidden whether the particular div should be hidden or not
     * @return HtmlTree that represents the generator or null if some error
     * occurred
     */
    public HtmlTree returnPerfoDiv(MethodDoc doc, String uniqueWorkloadName, boolean hidden) {

        //the main HtmlTree, that will contain two subtrees (left and right), that represent the left part and right part of the performance look
        HtmlTree navList = new HtmlTree(HtmlTag.DIV);
        if (hidden) {
            navList.addAttr(HtmlAttr.CLASS, "wrapperHidden");
        } else {
            navList.addAttr(HtmlAttr.CLASS, "wrapper");
        }
        navList.addAttr(HtmlAttr.ID, uniqueWorkloadName);

        HtmlTree leftSide = new HtmlTree(HtmlTag.DIV);
        leftSide.addAttr(HtmlAttr.CLASS, "left");

        try {
            addFormPart(leftSide, doc, uniqueWorkloadName);
        } catch (GeneratorParameterException e) {
            String parameter = e.getMessage();
            configuration.root.printWarning("The parameter \"" + parameter + "\" in generator " + doc.qualifiedName() + " has no annotation and is not Workload or ServiceWorkload. Therefore no performance info will be generated.");
            return null;
        } catch (NoWorkloadException | NumberFormatException ex) {
            configuration.root.printWarning(ex.getMessage());
            return null;
        } catch (UnsupportedParameterException ex) {
            String parameter = ex.getMessage().split("-")[0];
            String type = ex.getMessage().split("-")[1];
            configuration.root.printWarning("The parameter \"" + parameter + "\" in generator " + doc.qualifiedName() + " is of unsupported type " + type + ". Therefore no performance info will be generated.");
            return null;
        } catch (GeneratorParamNumException ex) {
            String parameter = ex.getMessage();
            configuration.root.printWarning("The parameter \"" + parameter + "\" in generator " + doc.qualifiedName() + " has number type, but lacks any ParamNum acnnotation. Therefore no performance info will be generated.");
            return null;
        } catch (NoEnumValueException ex) {
            String parameter = ex.getMessage();
            configuration.root.printWarning("The enum parameter \"" + parameter + "\" in generator " + doc.qualifiedName() + " has got no possible value defined. Therefore no performance info will be generated.");
            return null;
        }

        HtmlTree rightSide = new HtmlTree(HtmlTag.DIV);
        rightSide.addAttr(HtmlAttr.CLASS, "right");
        rightSide.addContent("Here will be the image / table + checkbox to choose the values");

        navList.addContent(rightSide);
        navList.addContent(leftSide);

        return navList;
    }

    /**
     * Generates the performance code (workload description, sliders, ...,
     * submit button) for the particular workload
     *
     * @param content the content to which the code should be added
     * @param doc the MethodDoc that represents the workload
     * @param workloadName
     * @throws NoSuchFieldException when the workload does not contain any
     * generator annotation
     */
    private void addFormPart(Content content, MethodDoc doc, String workloadName) throws GeneratorParameterException, NoWorkloadException, UnsupportedParameterException, NumberFormatException, GeneratorParamNumException, NoEnumValueException {
        AnnotationDesc[] annotations = doc.annotations();

        //we get the generator annotation of the doc (it was already checked by classparser that it is not null)
        Generator gen = AnnotationWorker.getGenerator(annotations);

        //first part of the left tree is the generator description
        HtmlTree description = new HtmlTree(HtmlTag.P);
        description.addContent(gen.description());
        content.addContent(description);

        //then comes the string Configuration finally followed by all sliders/textboxes/...
        HtmlTree configurationTree = new HtmlTree(HtmlTag.P);
        //configuration.addStyle(HtmlStyle.strong); //TODO why not working?
        configurationTree.addContent("Configuration:");
        content.addContent(configurationTree);

        Parameter[] param = doc.parameters();

        if (param.length < 2) {
            throw new NoWorkloadException("Workload " + doc.qualifiedName() + " has not enough arguments (less then 2). Therefore no performance info will be generated.");
        }

        if (!param[0].type().qualifiedTypeName().equals("cz.cuni.mff.d3s.tools.perfdoc.workloads.Workload")) {
            throw new NoWorkloadException("Workload " + doc.qualifiedName() + " does not contain Workload variable as the first parameter. Therefore no performance info will be generated.");
        }

        if (!param[1].type().qualifiedTypeName().equals("cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload")) {
            throw new NoWorkloadException("Workload " + doc.qualifiedName() + " does not contain ServiceWorkload variable as the second parameter. Therefore no performance info will be generated.");
        }

        //the number of parameter
        int number = 0;

        //telling JSSlider to begin generating JS
        JSSliderWriter.startNewGeneratorCode();
        JSControlWriter.startNewControlButton(workloadName);

        for (int i = 2; i < param.length; i++) {
            addParameterPerfo(param[i], content, workloadName, number);
            number++;
        }

        //telling JSSlider, that there's no error in generator and he can add it to the JS code
        JSSliderWriter.endGeneratorCode();

        //telling controlWriter, that there's not error in the generator so that he can add the control code to his global code
        JSControlWriter.endCurrentButton();

        content.addContent(JSControlWriter.returnButton(workloadName));
    }

    /**
     * Method that gets the parameter and content, and to the content adds the
     * appropriate element allowing user to select the value
     *
     * @param p the parameter of generator, whose performance part we are
     * maintaining
     * @param content the content to that the performance will be added
     * @param doc the methodDoc representing the method to that this parameter
     * belongs
     * @number the number of parameter
     * @throws GeneratorArgumentException when the param has no annotation and
     * is also not a Workload or ServiceWorkload
     */
    private void addParameterPerfo(Parameter param, Content content, String workloadName, int number) throws GeneratorParameterException, UnsupportedParameterException, NumberFormatException, GeneratorParamNumException, NoEnumValueException {
        AnnotationDesc[] annotations = param.annotations();

        if (annotations.length == 0) {
            String type = param.typeName();
            if (!(type.equals("cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload") || type.equals("cz.cuni.mff.d3s.tools.perfdoc.workloads.Workload"))) {
                throw new GeneratorParameterException(param.name());
            }
        }

        String description = null;

        for (AnnotationDesc annot : annotations) {
            switch (annot.annotationType().toString()) {
                case "cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamDesc":
                    description = AnnotationParser.getAnnotationValueString(annot, "cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamDesc.description()");
                    break;
                case "cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum":
                    description = AnnotationParser.getAnnotationValueString(annot, "cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum.description()");
                    break;
                //we do not abandon user to have there another annotations 
            }
        }

        //according the type of the parameter we call appropriate method
        switch (param.typeName()) {
            case "int":
            case "float":
            case "double":
                addParameterNum(param, description, content, workloadName, number);
                break;
            case "String":
                addParameterString(description, content, workloadName, number);
                break;
            default:
                FieldDoc[] enumValues = ClassParser.findEnums(param.type().qualifiedTypeName());

                //if it is not enum, or the enum has no possible values
                if (enumValues == null) {
                    throw new UnsupportedParameterException(param.name() + "-" + param.typeName());
                } else if (enumValues.length == 0) {
                    throw new NoEnumValueException(param.name());
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
    private void addParameterNum(Parameter param, String description, Content content, String workloadName, int number) throws NumberFormatException, GeneratorParamNumException {
        AnnotationDesc[] annotations = param.annotations();

        ParamNum paramNum = AnnotationWorker.getParamNum(annotations);
        if (paramNum == null) {
            throw new GeneratorParamNumException(param.name());
        }

        double min = paramNum.min();
        double max = paramNum.max();
        double step = paramNum.step();
        boolean axis = paramNum.axis();

        String uniqueSliderName = workloadName + "_slider_" + number;
        String uniqueTextboxName = workloadName + "_sliderTextBox_" + number;
        JSSliderWriter.addNewSlider(uniqueSliderName, uniqueTextboxName, min, max, step, axis);

        content.addContent(new RawHtml("<p>" + "<label for=\"" + uniqueTextboxName + "\">" + description + ":   </label>"));
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
    private void addParameterEnum(FieldDoc[] enumValues, String description, Content content, String workloadName, int number) {
        String uniqueSelectName = workloadName + "_" + number;

        StringBuilder sb = new StringBuilder();
        sb.append("<p><label for=\"" + uniqueSelectName + "\">" + description + "</label>: <select id=\"" + uniqueSelectName + "\">");

        for (FieldDoc f : enumValues) {
            sb.append("<option>" + f.name() + "</option>");
        }

        sb.append("</select> </p>");
        content.addContent(new RawHtml(sb.toString()));

        //registering this select to the control
        JSControlWriter.addEnumControl(uniqueSelectName, description);
    }

    /**
     * Method to count the unique identifier (through all .html document), that
     * represents the concrete method
     *
     * @param doc the methodDoc of method, for which the unique ID will be
     * counted
     * @return the unique info (packageName_className_method_babreviatedParams)
     */
    public String getUniqueInfo(MethodDoc doc) {
        //TODO replace the dots in the package name
        String containingPackage = doc.containingPackage().name();
        String className = doc.containingClass().name();
        String methodName = doc.name();
        String abbrParams = getAbbrParams(doc);

        String fullMethodName = (containingPackage + "_" + className + "_" + methodName + "_" + abbrParams);
        String number = WorkloadBase.getNewWorkloadID(fullMethodName) + "";

        return (fullMethodName + "_" + number);
    }

    /**
     * Gets the abbreviated form of the parameters type of given method.
     *
     * @param doc
     * @return In the parameter declared order returns the begin letter of the
     * parameters types. For example for method foo(String, int, float) the
     * result would be "sif"
     */
    private String getAbbrParams(MethodDoc doc) {
        //the following method returns parameters in the declared order (otherwise, there would be no chance to have it unique)
        Parameter[] params = doc.parameters();
        String abbrParams = "";

        for (int i = 0; i < params.length; i++) {
            switch (params[i].typeName()) {
                case "int":
                    abbrParams += "i";
                    break;
                case "double":
                    abbrParams += "d";
                    break;
                case "float":
                    abbrParams += "f";
                    break;
                case "String":
                    abbrParams += "s";
                    break;
                default:
                    //enum situation
                    abbrParams += params[i].typeName().charAt(0);
                    break;
            }
        }

        return abbrParams;
    }

    /**
     * inner class, that adds the ending to every workload so that the workload
     * ID is unique
     */
    private static class WorkloadBase {

        private static HashMap<String, Integer> map = new HashMap<>();

        /**
         * Gets a workloadName and returns the appropriate ending, so that the
         * result workloadName is unique. The class has s static hashmap, that
         * contains all the workloadNames, that has already been asked for. If
         * the workload is not there yet, it adds it there (with number of use
         * set to 0) and returns 0, otherwise gets the number of usage, add one
         * to it and returns
         *
         * @param workload
         * @return
         */
        public static int getNewWorkloadID(String workload) {

            //the default value (when there's no such a key) is 0
            int value = 0;

            //if the key is contained, we get its value and add one to it 
            if (map.containsKey(workload)) {
                value = map.get(workload);
                value++;
            }

            map.put(workload, value);
            return value;
        }
    }
}
