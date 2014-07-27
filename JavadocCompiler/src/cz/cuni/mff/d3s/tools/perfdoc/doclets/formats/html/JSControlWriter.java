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

import com.sun.tools.doclets.formats.html.markup.RawHtml;
import com.sun.tools.doclets.internal.toolkit.Content;
import java.util.ArrayList;

/**
 *
 * @author Jakub Naplava
 */
public class JSControlWriter {

    private static StringBuilder jsGlobalCode = new StringBuilder("<script> $(function() {");

    static {
        //importing functions that can be used by every control method
        jsGlobalCode.append(returnIsIntervalFunction() + "\n");
        jsGlobalCode.append(returnIsDivisibleFunction()+ "\n");
    }

    private static ArrayList<InfoItem> list;
    private static String buttonName;

    public static void startNewControlButton(String workloadName) {
        buttonName = returnButtonName(workloadName);
        list = new ArrayList<>();
    }

    private static String returnButtonName(String workloadName) {
        return workloadName + "controlButton";
    }

    private static String returnCheckParameterFunctionName() {
        return "checkParams" + buttonName;
    }

    public static Content returnButton(String workloadName) {
        String content = "<p><button id = \"" + returnButtonName(workloadName) + "\">Submit</button></p>";
        return new RawHtml(content);
    }

    public static void addSliderControl(String textboxName, String sliderName, String description) {
        list.add(new InfoItem(textboxName, sliderName, description, ItemType.slider));
    }

    public static void addDoubleSliderControl(String textboxName, String sliderName, String description) {
        list.add(new InfoItem(textboxName, sliderName, description, ItemType.doubleSlider));
    }

    public static void addStringControl(String textboxName, String description) {
        list.add(new InfoItem(textboxName, description, ItemType.textbox));
    }

    public static void addEnumControl(String selectName, String description) {
        list.add(new InfoItem(selectName, description, ItemType.select));
    }

    public static void endCurrentButton() {
        jsGlobalCode.append(generateControl());
    }

    private static Content generateControl() {

        Content checkParamsContent = returnCheckParameters();
        StringBuilder sb = new StringBuilder();
        sb.append("$(\"#" + buttonName + "\").click(function() {");

        sb.append("var error = " + returnCheckParameterFunctionName() + "(); ");
        sb.append("if ( error == \"\")");

        //TODO call the Ajax function
        sb.append("{ alert(\"All parameteres checked succesfully\"); } ");
        sb.append("else { alert( \"Cannot send your request: \" + error); }");

        sb.append("}); \n");
        sb.append(checkParamsContent);
        return new RawHtml(sb.toString());
    }

    private static Content returnCheckParameters() {
        String checkParametersFunctionName = returnCheckParameterFunctionName();
        StringBuilder sb = new StringBuilder();
        sb.append("function " + checkParametersFunctionName + "() {");
        //some variables that will be very likely used during the parameter check
        sb.append("var error = \"\"; ");
        sb.append("var value;");
        sb.append("var min = 0;");
        sb.append("var max = 0;");
        sb.append("var step = 0;");
        sb.append("var intervals = 0;");
        sb.append("var pom;");

        for (InfoItem it : list) {
            switch (it.type) {
                case slider:
                    sb.append(addSliderControl(it, true));
                    break;
                case doubleSlider:
                    sb.append(addDoubleSliderControl(it));
                    break;
                case select:
                    sb.append(addStringControl(it));
                    break;
                case textbox:
                    sb.append(addEnumControl(it));
                    break;
            }
        }
        sb.append(returnNumberIntervalsCheck());
        sb.append("return error;");
        sb.append("} \n");
        return new RawHtml(sb.toString());
    }

    public static void addToContentAndEmpty(cz.cuni.mff.d3s.tools.perfdoc.doclets.internal.toolkit.Content content) {
        jsGlobalCode.append("});</script>");
        content.addContent(new cz.cuni.mff.d3s.tools.perfdoc.doclets.formats.html.markup.RawHtml(jsGlobalCode.toString()));
        jsGlobalCode = new StringBuilder();
    }

    private static Content addSliderControl(InfoItem it, boolean saveValues) {
        StringBuilder sb = new StringBuilder();

        if (saveValues) {
            //storing all informations from slider
            sb.append(returnCodeValue(it.textbox));
            sb.append(returnCodeMinValueSlider(it.slider));
            sb.append(returnCodeMaxValueSlider(it.slider));
            sb.append(returnCodeStepValueSlider(it.slider));
        }

        //checking, whether the value is number
        sb.append(returnNaNCheck(it.description, saveValues));

        //if it is a number: checking, whether the value is at least as big as min and not bigger then max
        sb.append("else {" + returnRangeCheck(it.description));

        //and also whether it could be reached by adding the step to minimal value
        sb.append(returnStepCheck(it.description));
        sb.append("}; ");

        return new RawHtml(sb.toString());
    }

    private static Content addDoubleSliderControl(InfoItem it) {
        StringBuilder sb = new StringBuilder();

        //storing all informations from slider
        sb.append(returnCodeValue(it.textbox));
        sb.append(returnCodeMinValueSlider(it.slider));
        sb.append(returnCodeMaxValueSlider(it.slider));
        sb.append(returnCodeStepValueSlider(it.slider));

        //checking whether text is interval or not
        sb.append(returnIntervalCheck(it));

        return new RawHtml(sb.toString());
    }

    private static Content addEnumControl(InfoItem it) {
        //there is just no limitation for enums given
        return new RawHtml("");
    }

    private static Content addStringControl(InfoItem it) {
        //there is just no limitation for strings given
        return new RawHtml("");
    }

    /**
     * Returns the value of the slider (its textbox)
     */
    private static String returnCodeValue(String valueLocation) {
        return ("value = $(\"#" + valueLocation + "\").val();");
    }

    /**
     * Returns the minimal value of the slider
     */
    private static String returnCodeMinValueSlider(String sliderLocation) {
        return ("min = $(\"#" + sliderLocation + "\").slider(\"option\",\"min\");");
    }

    /**
     * Returns the maximal value of the slider
     */
    private static String returnCodeMaxValueSlider(String sliderLocation) {
        return ("max = $(\"#" + sliderLocation + "\").slider(\"option\",\"max\");");
    }

    /**
     * Returns the step of the slider
     */
    private static String returnCodeStepValueSlider(String sliderLocation) {
        return ("step = $(\"#" + sliderLocation + "\").slider(\"option\",\"step\");");
    }

    /**
     * Checks whether the value is a number
     */
    private static String returnNaNCheck(String description, boolean concreteError) {
        if (concreteError) {
            return ("if (isNaN(value)) { error +=\"\\n The value in " + description + " is not a number.\";}");
        } else {
            return ("if (isNaN(value)) { error +=\"\\n The value in " + description + " is in a bad format.\";}");
        }
    }

    /**
     * Checks whether the value is bigger or equal to the minimal value and not
     * bigger than maximum
     */
    private static String returnRangeCheck(String description) {

        return ("if ((min > value) || (max < value)) { error +=\"\\n The number in " + description + " is not in given range (\" + min + \" to \" + max + \").\";}");
    }

    /**
     * Checks whether value could be reached by adding step to minimal value.
     */
    private static String returnStepCheck(String description) {
        return ("if (!isDivisible(value - min, step)) { error +=\"\\n The number in " + description + " could not be reached by the step of \" + step + \".\";}");
    }

    private static String returnIntervalCheck(InfoItem it) {
        return ("pom = isInterval(value, min, max, step); if (pom == \"true\") { intervals++; }"
                + "else if (pom == \"false\") {" + addSliderControl(it, false) + "}");
    }

    private static String returnNumberIntervalsCheck() {
        return ("if (intervals != 1) { error +=\"\\n The number of axis attributes (range sliders) must be 1 (now is \" + intervals + \").\";}");
    }

    /**
     * returns the function, that checks, whether the specified variable
     * represents correct interval *
     */
    private static String returnIsIntervalFunction() {
        return "function isInterval(str, min, max, step) {"
                + "if (str.indexOf(\"to\") == -1) return \"false\"; "
                + "var array = str.split(\"to\");"
                + "if (array.length != 2) return \"false\"; "
                + "if (isNaN(array[0].trim()) || isNaN(array[1].trim())) return \"false\";"
                + "if ((array[0].trim() < min) || (array[1].trim() > max) || (array[0].trim() > array[1].trim())) return \"false\";"
                + "if (! (isDivisible(array[0].trim() - min, step) && isDivisible(array[1].trim() - min, step))) return \"false\";"
                + "if (array[0].trim() == array[1].trim()) return \"single\";"
                + "return \"true\"; }";
    }

    /**
     * TODO problem with representation
     *
     * @return
     */
    private static String returnIsDivisibleFunction() {
        return "function isDivisible(u, d) {"
                + " u = u * Math.pow(10, u.toString().replace(/^\\d+\\./, '').length);"
                + " d = d * Math.pow(10, d.toString().replace(/^\\d+\\./, '').length);"
                + " return (u % d) === 0; }";
    }

    private static class InfoItem {

        String id;
        String description;
        ItemType type;

        public InfoItem(String id, String description, ItemType type) {
            this.id = id;
            this.description = description;
            this.type = type;
        }

        String slider;
        String textbox;

        public InfoItem(String textbox, String slider, String description, ItemType type) {
            this.textbox = textbox;
            this.slider = slider;
            this.description = description;
            this.type = type;
        }
    }

    private enum ItemType {

        slider, doubleSlider, select, textbox
    }
}
