/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.cuni.mff.d3s.tools.perfdoc.doclets.formats.html;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.tools.doclets.formats.html.markup.HtmlAttr;
import com.sun.tools.doclets.formats.html.markup.HtmlTag;
import com.sun.tools.doclets.formats.html.markup.HtmlTree;
import com.sun.tools.doclets.formats.html.markup.RawHtml;
import com.sun.tools.doclets.internal.toolkit.Content;
import com.sun.tools.doclets.internal.toolkit.util.DocletConstants;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.AnnotationParser;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.Generator;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;

/**
 *
 * @author Jakub Naplava
 */
public class PerformanceWriterImpl {

    //writer for getting configuration information
    private final HtmlDocletWriter htmlWriter;

    //writer for writing the (JQuery) sliders
    private JSSliderWriter sliderWriter;

    //writer for writing (JQuery) control check (e.g. whether there's just one axis attribute)
    private JSWriter controlWriter;

    public PerformanceWriterImpl(HtmlDocletWriter htmlWriter) {
        this.htmlWriter = htmlWriter;

        try {
            sliderWriter = new JSSliderWriter("slider.js");
            controlWriter = new JSWriter("control.js");
        } catch (IOException e) {
            //TODO throw some exception
        }
    }

    /**
     * Method that prepares the title of Performance part
     *
     * @return Performance output containing performance title
     */
    public PerformanceOutput returnTitleOutput() {
        PerformanceOutput result = new PerformanceOutputImpl(DocletConstants.NL + "<dt>"
                + "<span class=\"strong\">" + "Performace:"
                + "</span>" + "</dt>" + "<dd>" + "</dd>");
        return result;
    }

    /**
     * Method to generate the performance body from one workload
     *
     * @param workload the workload in format packageName.className#method
     * @return new PerformanceOutput that represents the body of generated
     * performance code
     */
    public PerformanceOutput returnPerfoBody(String workload) {
        PerformanceOutput res = new PerformanceOutputImpl("");

        MethodDoc[] d = ClassParser.findMethods(workload);

        /*for (MethodDoc doc : d)
         {
         res.appendOutput(new PerformanceOutputImpl(doc.name()));
         }*/
         //return res;
        return returnOnePerfoDiv(d[0]);
    }

    public PerformanceOutput returnPerfoBody(String[] workload) {
        return null;
    }

    //TODO would be better to return HTML tree with div
    private PerformanceOutput returnOnePerfoDiv(MethodDoc doc) {
        //every div must have unique id
        String uniqueWorkloadName = getUniqueInfo(doc);

        //the main HtmlTree, that will contain two subtrees (left and right side), that represent the left part and right part of the performance look
        HtmlTree navList = new HtmlTree(HtmlTag.DIV);
        navList.addAttr(HtmlAttr.CLASS, "wrapper");
        navList.addAttr(HtmlAttr.ID, uniqueWorkloadName);

        HtmlTree leftSide = new HtmlTree(HtmlTag.DIV);
        leftSide.addAttr(HtmlAttr.CLASS, "left");
        addFormPart(leftSide, doc, uniqueWorkloadName);

        HtmlTree rightSide = new HtmlTree(HtmlTag.DIV);
        rightSide.addAttr(HtmlAttr.CLASS, "right");
        rightSide.addContent("Here will be the image / table + checkbox to choose the values");

        navList.addContent(leftSide);
        navList.addContent(rightSide);

        PerformanceOutput p = new PerformanceOutputImpl(navList.toString());
        return p;
    }

    private void addFormPart(Content content, MethodDoc doc, String workloadName) {
        AnnotationDesc[] annotations = doc.annotations();

        Generator gen = getGenerator(annotations);

        //first part of the left tree is the generator description
        HtmlTree description = new HtmlTree(HtmlTag.P);
        description.addContent(gen.description());
        content.addContent(description);

        //then comes the string Configuration finally followed by all sliders/textboxes/...
        HtmlTree configuration = new HtmlTree(HtmlTag.P);
        //configuration.addStyle(HtmlStyle.strong); //TODO why not working?
        configuration.addContent("Configuration:");
        content.addContent(configuration);

        Parameter[] param = doc.parameters();

        //the number of parameter
        int number = 0;
        for (Parameter p : param) {
            addParameterPerfo(p, content, workloadName, number);
            number++;
        }

        //TODo generate submit button (needs to check, send data, start receiving and possibly also block itself)
    }

    /**
     *
     * @param annotations The array of annotations that belong to one method
     * @return the first Generator annotation from the annotations
     */
    private Generator getGenerator(AnnotationDesc[] annotations) {
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
     */
    private void addParameterPerfo(Parameter param, Content content, String workloadName, int number) {
        AnnotationDesc[] annotations = param.annotations();

        if (annotations.length == 0) {
            //TODO must be Workload, or ServiceWorkload or some error
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
                default:
                    System.out.println(annot.annotationType().toString());
                    //TODO asi by nemela byt pritomna - error
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
                addParameterString(param, description, content, workloadName, number);
                break;
            case "enum":
                addParameterEnum(param, description, content, workloadName, number);
                break;
            default:
                //TODO some error
                break;
        }
    }

    private void addParameterNum(Parameter param, String description, Content content, String workloadName, int number) {
        AnnotationDesc[] annotations = param.annotations();
        double min = 0;
        double max = 0;
        double step = 0;
        boolean axis = true;

         //TODO check whether such annotation exists
         //TODO if axis, if not axis - range slider, classic slider
         
        for (AnnotationDesc annot : annotations) {
            if (annot.annotationType().toString().equals("cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum")) {
                try {
                    min = AnnotationParser.getAnnotationValueDouble(annot, "cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum.min()");
                    max = AnnotationParser.getAnnotationValueDouble(annot, "cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum.max()");

                    step = AnnotationParser.getAnnotationValueDouble(annot, "cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum.step()");
                    //if there is no step specified, we will use default value, which is 1
                    step = (step == Double.MIN_VALUE) ? 1 : step;

                    axis = AnnotationParser.getAnnotationValueBoolean(annot, "cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum.axis()");
                } catch (NumberFormatException e) {
                    System.out.println(e);
                    //TODO
                }

                break;
            }
        }

        String uniqueSliderName = workloadName + "-slider" + number;
        String uniqueTextboxName = workloadName + "-sliderTextBox" + number;
        sliderWriter.addNewSlider(uniqueSliderName, uniqueTextboxName, min, max, step, axis, content);

        content.addContent(new RawHtml("<p>" + "<label for=\"" + uniqueTextboxName + "\">" + description + ":   </label>"));
        content.addContent(new RawHtml("<input type=\"text\" id=\"" + uniqueTextboxName + "\" style=\"border:0; color:#f6931f; font-weight:bold;\"> </p>"));
        content.addContent(new RawHtml("<div id=\"" + uniqueSliderName + "\" style=\"margin:10\"></div>"));
    }

    private void addParameterString(Parameter param, String description, Content content, String workloadName, int number) {
        String uniqueTextboxName = workloadName + "-" + number;
        String input = "<p><label for=\"" + uniqueTextboxName + "\">" + description + "</label>: <input type=\"text\" id=\"" + uniqueTextboxName + "\"> </p>";
        content.addContent(new RawHtml(input));
    }

    private void addParameterEnum(Parameter param, String description, Content content, String workloadName, int number) {
        //TODO
    }

    /**
     * Method to count the unique identifier (through all .html document), that
     * represents the concrete method
     *
     * @param doc the methodDoc of method, for which the unique ID will be
     * counted
     * @return the unique info (packageName-method-abbreviatedParams)
     */
    private String getUniqueInfo(MethodDoc doc) {
        String containingPackage = doc.containingPackage().name();
        String methodName = doc.name();
        String abbrParams = getAbbrParams(doc);

        String fullMethodName = (containingPackage + "-" + methodName + "-" + abbrParams);
        String number = WorkloadBase.getNewWorkloadID(fullMethodName) + "";

        return fullMethodName + "-" + number;
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
                default: //TODO check enum (params[i].type() instanceof Enum<?>);
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

        private static HashMap<String, Integer> map = new HashMap<String, Integer>();

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

