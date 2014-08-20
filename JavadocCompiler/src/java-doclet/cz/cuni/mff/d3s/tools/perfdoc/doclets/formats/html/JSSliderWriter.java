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
        String script = "";
        if (axis) {
            script = " $( \"#slider-range\" ).slider({ range: true, min:" + minValue + ", max: " + maxValue + ", step:" + step
                    + ",slide: function( event, ui ) { if (ui.values[1] - ui.values[0] ==0) "
                    + " { $( \"#amount\" ).val( ui.values[ 0 ]); } else { $( \"#amount\" ).val( ui.values[ 0 ] + \" to \" + ui.values[ 1 ] ); };	} });"
                    + "\n $( \"#amount\" ).val( $( \"#slider-range\" ).slider( \"values\", 0 ) + \" to \" + $( \"#slider-range\" ).slider( \"values\", 1 ) );  \n";
        } else {
            script = " $( \"#slider-range\" ).slider({ min:" + minValue + ", max:" + maxValue + ", step:" + step + ", slide: function( event, ui ) {"
                    + "$( \"#amount\" ).val( ui.value ); } }); \n $( \"#amount\" ).val( $( \"#slider-range\" ).slider( \"value\" ) );  \n";
        }

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
