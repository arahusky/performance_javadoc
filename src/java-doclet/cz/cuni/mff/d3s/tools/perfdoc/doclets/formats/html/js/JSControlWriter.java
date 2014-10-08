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
package cz.cuni.mff.d3s.tools.perfdoc.doclets.formats.html.js;

import com.sun.tools.doclets.formats.html.markup.RawHtml;
import com.sun.tools.doclets.internal.toolkit.Content;
import java.util.ArrayList;

/**
 * Class that provides method for generating the code to generate the action
 * after the submit button is pressed. It involves parameter check and if
 * everything's good than call of appropriate Ajax function
 *
 * @author Jakub Naplava
 */
public class JSControlWriter {

    private static ArrayList<InfoItem> list;
    private static String buttonName;
    
    private static String workloadName;
    private static String fullGeneratorName;

    public static void startNewControlButton(String workloadName, String fullGeneratorName) {
        JSControlWriter.workloadName = workloadName;
        JSControlWriter.fullGeneratorName = fullGeneratorName;
        
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
        JavascriptCodeBox.addLocalCode(generateControl());
    }

    /**
     * Private method to create the action for one button click of one workload
     *
     * @return the Content containing the control code
     */
    private static String generateControl() {
        //function, that will check all the parameters
        Content checkParamsContent = returnCheckParameters();
        StringBuilder sb = new StringBuilder();
        sb.append("$(\"#" + buttonName + "\").click(function() {");

        //we call the function, that checks the parameters
        sb.append("var paramResult = " + returnCheckParameterFunctionName() + "(); ");

        //if there are no errors
        sb.append("if ( paramResult.error == \"\") {");
        
        JSAjaxHandler.generator = fullGeneratorName;
        
        //adding url adress, where the table formed results will be shown
        String divName = JSAjaxHandler.returnDivName();
        String adress = JavascriptCodeBox.serverAdress + "/cache/detailed?";
        sb.append("var stringData = (paramResult.values + \"\").replace(/ /g, '_');");
        sb.append("var method1 = parseToUrl('" + JSAjaxHandler.testedMethod + "');");
        sb.append("var method2 = parseToUrl('" + JSAjaxHandler.generator + "');");
        sb.append("var adress = '" + adress 
                + "' + method1 + 'separator=' +"
                + "method2 + 'separator=' + stringData;");
        sb.append("$(\"#" + divName + " .right .graph\").html('All parameteres checked succesfully. Sending request to the server. <br /> Please wait...');");
        sb.append("$(\"#" + divName + " .right .url\").html(\"<a target=\\\"_blank\\\" href=\" + adress + \"> Results in a table.</a> \");");
                
        sb.append(JSAjaxHandler.returnSuccessButtonHandleFunction());
        sb.append("}");

        //otherwise if there were any errors, we just print them (alert)
        sb.append("else { alert( \"Cannot send your request: \" + paramResult.error); }");

        sb.append("}); \n");

        //and finally adding the checking function to the code
        sb.append(checkParamsContent);
        return sb.toString();
    }

    /**
     * The function that generates the code to check the parameters
     *
     * @return the generated code
     */
    private static Content returnCheckParameters() {
        String checkParametersFunctionName = returnCheckParameterFunctionName();
        StringBuilder sb = new StringBuilder();
        sb.append("function " + checkParametersFunctionName + "() {");

        //some variables that will be very likely used during the parameter check (needs to be predefined, so that we can later use just the variable without var
        sb.append("var error = \"\"; ");
        sb.append("var value;");
        sb.append("var values = [];");
        sb.append("var rangeValue;");
        sb.append("var rangeValueName = \"\";");
        sb.append("var min = 0;");
        sb.append("var max = 0;");
        sb.append("var step = 0;");
        sb.append("var intervals = 0;");
        sb.append("var pom;");

        for (int i = 0; i<list.size(); i++) {
            InfoItem it = list.get(i);
            switch (it.type) {
                case slider:
                    sb.append(addSliderControl(it, true));
                    break;
                case doubleSlider:
                    sb.append(addDoubleSliderControl(it, i));
                    break;
                case select:
                    sb.append(addEnumControl(it));
                    break;
                case textbox:
                    sb.append(addStringControl(it));
                    break;
            } 
            sb.append(" values.push(value);");
        }

        //we add the control of number of range sliders (must be precisely one)
        sb.append(returnNumberIntervalsCheck());
        sb.append("var paramResult = { ");
        sb.append("error : error, ");
        sb.append("values : values, ");
        sb.append("rangeValue : rangeValue,");
        sb.append("rangeValueName : rangeValueName ");
        sb.append("}; ");
        sb.append("return paramResult;");
        sb.append("} \n");
        return new RawHtml(sb.toString());
    }

    /**
     * Adds the control of the single (= not range) slider
     *
     * @param it the InfoItem that describes the slider
     * @param saveValues whether to save slider values or not
     * @return the control code
     */
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

    /**
     * Adds the control of the range slider
     *
     * @param it the InfoItem that describes the slider
     * @param number the number of parameter in generator
     * @return
     */
    private static Content addDoubleSliderControl(InfoItem it, int number) {
        StringBuilder sb = new StringBuilder();

        //storing all informations from slider
        sb.append(returnCodeValue(it.textbox));
        sb.append(returnCodeMinValueSlider(it.slider));
        sb.append(returnCodeMaxValueSlider(it.slider));
        sb.append(returnCodeStepValueSlider(it.slider));

        //checking whether text is interval or not
        sb.append(returnIntervalCheck(it, number));

        return new RawHtml(sb.toString());
    }

    private static Content addEnumControl(InfoItem it) {
        //there is just no limitation for enums given
        
        //therefore the code that saves the value of the select tag into the variable "value"
        return new RawHtml(returnCodeValue(it.id));
    }

    private static Content addStringControl(InfoItem it) {
        //there is just no limitation for strings given
        
        //therefore the code that saves the value of the textbox into the variable "value"
        return new RawHtml(returnCodeValue(it.id));
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

    private static String returnIntervalCheck(InfoItem it, int number) {
        String labelName = it.textbox + "Label"; 
        return ("pom = isInterval(value, min, max, step); if (pom == \"true\") { intervals++; rangeValue = " + number + "; rangeValueName = $(\"#"+ labelName + "\").text();}"
                + "else if (pom == \"false\") {" + addSliderControl(it, false) + "}");
    }

    private static String returnNumberIntervalsCheck() {
        return ("if (intervals != 1) { error +=\"\\n The number of axis attributes (range sliders) must be 1 (now is \" + intervals + \").\";}");
    }

    /**
     * returns the function, that checks, whether the specified variable
     * represents correct interval *
     */
    public static String returnIsIntervalFunction() {
        return JavascriptLoader.getFileContent("isinterval.js");
    }

    /**
     * returns the function that checks, whether the modulus of two numbers is 0
     * (this function should handle the problems with floating point
     * representation of numbers by translating them to integers and rounding
     */
    public static String returnIsDivisibleFunction() {
        return JavascriptLoader.getFileContent("isdivisible.js");
    }   
    
     /**
     * Class that describes the parameter (slider, range slider, textbox or
     * select)
     */
    private static class InfoItem {

        String id;
        String description;
        ItemType type;

        //if it is select or textbox use this constructor
        public InfoItem(String id, String description, ItemType type) {
            this.id = id;
            this.description = description;
            this.type = type;
        }

        String slider;
        String textbox;

        //if it is any kind of slider use this constructor
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
