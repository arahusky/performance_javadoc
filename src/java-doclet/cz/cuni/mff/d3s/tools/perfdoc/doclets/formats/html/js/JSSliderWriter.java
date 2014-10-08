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

/**
 *
 * @author Jakub Naplava
 */
public class JSSliderWriter {

    /**
     * the temporary code is the code where all the code for slider is saved
     * when the signal, that the generator is in a good format comes, this code
     * is saved in the global JavascriptCodeBox
     */
    private static StringBuilder temporaryGeneratorCode;

    //Cached template codes (so that we do not have to read from a file repeatedly)
    private static String doubleSliderTemplate;    
    private static String singleSliderTemplate;
    
    /**
     * Starts a new temporary code
     */
    public static void startNewGeneratorCode() {
        temporaryGeneratorCode = new StringBuilder();
    }

    /**
     * Adds new slider into code (if axis is true, then the range slider will be
     * added, otherwise normal slider)
     */
    public static void addNewSlider(String uniqueSliderName, String uniqueTextboxName, double minValue, double maxValue, double step, boolean axis) {
        String script;
        if (axis) {
            if (doubleSliderTemplate == null) {
                doubleSliderTemplate = JavascriptLoader.getFileContent("doubleslider.js");
            }
            script = doubleSliderTemplate;
        } else {
            if (singleSliderTemplate == null) {
                singleSliderTemplate = JavascriptLoader.getFileContent("singleslider.js");
            } 
            script = singleSliderTemplate;
        }
        
        script = script.replace("$minValue", minValue + "");
        script = script.replace("$maxValue", maxValue + "");
        script = script.replace("$step", step + "");
        
        script = script.replaceAll("slider-range", uniqueSliderName);
        script = script.replaceAll("amount", uniqueTextboxName);

        temporaryGeneratorCode.append(script).append("\n");
    }

    /**
     * Adds the temporary code to the JSCode that will be added to the page
     */
    public static void endGeneratorCode() {
        JavascriptCodeBox.addLocalCode(temporaryGeneratorCode.toString());
    }

}
