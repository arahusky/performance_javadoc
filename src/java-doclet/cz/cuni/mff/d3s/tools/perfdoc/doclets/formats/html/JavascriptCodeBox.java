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
 * The class, where all the javascript code generated by performance tool is
 * saved. It has two parts, the global code, which represents the functions that
 * are being used by other functions and the local code, which represents
 * functions, that are created dynamically (e.g. sliders, button handlers)
 *
 * @author Jakub Naplava
 */
public class JavascriptCodeBox {

    private static StringBuilder globalCode = new StringBuilder("<script>");

    private static StringBuilder localCode = new StringBuilder("<script>");

    /**
     * indicates, whether the code was already printed out on the page
     */
    public static boolean isUsedOnThePage = false;
    
    /**
     * Adds the code to the local code
     * @param code The code that will be added to the current local code
     */
    public static void addLocalCode(String code) {
        localCode.append(code);
    }

    /**
     * Adds global code to the content
     * @param content The content to which to code will be added
     */
    public static void addGlobalCodeToContentAndEmpty(cz.cuni.mff.d3s.tools.perfdoc.doclets.internal.toolkit.Content content) {
        //need to add all the global functions 
        addGlobalCode();

        globalCode.append("</script>");
        content.addContent(new cz.cuni.mff.d3s.tools.perfdoc.doclets.formats.html.markup.RawHtml(globalCode.toString()));
        globalCode = new StringBuilder("<script>");
    }

    /**
     * adds all the needed functions to the global code 
     */
    private static void addGlobalCode() {
        globalCode.append(JSAjaxHandler.returnCallServerFunction("http://localhost:8080/measure"));
        globalCode.append(JSAjaxHandler.returnErrorFunction());

        globalCode.append(JSControlWriter.returnIsDivisibleFunction());
        globalCode.append(JSControlWriter.returnIsIntervalFunction());
        
        globalCode.append(" var globalIdentifier = Math.random().toString(36).substring(7);");
    }

    /**
     * Adds the currently saved local code to the content
     * @param content The content to which to code will be added
     */
    public static void addLocalCodeToContentAndEmpty(cz.cuni.mff.d3s.tools.perfdoc.doclets.internal.toolkit.Content content) {
        localCode.append("</script>");
        content.addContent(new cz.cuni.mff.d3s.tools.perfdoc.doclets.formats.html.markup.RawHtml(localCode.toString()));
        localCode = new StringBuilder("<script>");
    }

    /**
     * Determines, whether the local code (=code) is empty
     * @return true, if is empty
     */
    public static boolean isEmpty() {
        return (localCode.toString().equals("<script>"));
    }
}
