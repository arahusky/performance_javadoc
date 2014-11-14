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

import com.sun.javadoc.MethodDoc;
import com.sun.tools.doclets.formats.html.markup.HtmlAttr;
import com.sun.tools.doclets.formats.html.markup.HtmlTag;
import com.sun.tools.doclets.formats.html.markup.HtmlTree;
import com.sun.tools.doclets.formats.html.markup.RawHtml;
import com.sun.tools.doclets.internal.toolkit.Content;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.Generator;
import cz.cuni.mff.d3s.tools.perfdoc.doclets.formats.html.js.JSAjaxHandler;
import cz.cuni.mff.d3s.tools.perfdoc.doclets.formats.html.js.JavascriptCodeBox;
import cz.cuni.mff.d3s.tools.perfdoc.exceptions.GeneratorParameterException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Jakub Naplava
 */
public abstract class PerformanceWriter {

    static ConfigurationImpl configuration = ConfigurationImpl.getInstance();

    static PerformanceBodyWriter perfWriter = new PerformanceBodyWriter();

    /**
     * Method that prepares the title of Performance part
     *
     * @return Performance output containing performance title
     */
    protected abstract PerformanceOutput returnTitleOutput();

    /**
     * Generates performance measurement code for the given method
     *
     * @param doc measured method
     * @param output the PerformanceOutput to which the measurement code will be
     * added
     * @param workloadNames the workloadNames for the given method
     */
    public static void genPerfOutput(MethodDoc doc, PerformanceOutput output, String[] workloadNames) throws ClassNotFoundException, MalformedURLException {

        //passing full method name to the ajax handler
        JSAjaxHandler.setTestedMethod(perfWriter.getUniqueFullInfo(doc));

        //list, that will contain all workloads for the given method (doc) 
        List<Method> workloads = new ArrayList<>();
        for (String w : workloadNames) {
            //for given workloadName may exist 0..n of generators
            Method[] docs = ClassParser.findMethods(w);
            if (docs != null) {
                workloads.addAll(Arrays.asList(docs));
            }
        }

        if (workloads.isEmpty()) {
            configuration.root.printWarning("There were found no generators for method: " + doc.qualifiedName() + ". Therefore, no performance info for this method will be added.");
        } else if (workloads.size() == 1) {
            addPerfoInfoOneDiv(workloads.get(0), output);
        } else {
            try {
                addPerfoInfoMoreDivs(doc, workloads, output);
            } catch (GeneratorParameterException ex) {
                configuration.root.printWarning(ex.getMessage());
            }
        }
    }

    /**
     * Adds performance info with one workload
     *
     * @param method the measured method
     * @param output the PerformanceOutput to which to insert the content
     */
    private static void addPerfoInfoOneDiv(Method method, PerformanceOutput output) {

        String workloadFullName = perfWriter.getUniqueFullInfoReflection(method);
        String uniqueWorkloadName = perfWriter.getUniqueInfo(workloadFullName);
        HtmlTree tree = perfWriter.returnPerfoDiv(method, uniqueWorkloadName, workloadFullName, false);

        if (tree != null) {
            //if the tree was succesfully built, we can add the performance title and then the tree
            output.appendOutput(perfWriter.returnTitleOutput());
            output.appendOutput(new PerformanceOutputImpl(tree.toString()));
        }
    }

    /**
     * Add the performance info with multiple workloads
     *
     * @param doc the measured method
     * @param list the List of all workloads
     * @param output the PerformanceOutput to which to insert the generated
     * content
     * @throws GeneratorParameterException when there's some invalid content in
     * generator
     */
    private static void addPerfoInfoMoreDivs(MethodDoc doc, List<Method> list, PerformanceOutput output) throws GeneratorParameterException {

        if (list.isEmpty()) {
            configuration.root.printWarning(" There were found no generators for method: " + doc.qualifiedName() + ". Therefore, no performance info for this method will be added.");
            return;
        } else if (list.size() == 1) {
            addPerfoInfoOneDiv(list.get(0), output);
            return;
        }

        //first we need to check that the descriptions are different -> also the workloads are different
        ArrayList<String> genNames = new ArrayList<>();

        for (Method m : list) {
            Generator gen = m.getAnnotation(cz.cuni.mff.d3s.tools.perfdoc.annotations.Generator.class);
            String genName = gen.genName();
            
            if (genNames.contains(genName)) {
                //if for the measured method already exists generator, that has the genName 
                throw new GeneratorParameterException("The method: " + doc.qualifiedName() + " has two or more workloads that have the same genName. Therefore, no performance info for this method will be added.");
            } else {
                genNames.add(genName);
            }
        }

        //list, that contains the HtmlTrees of all workloads for the measured method
        ArrayList<HtmlTree> generatorDivs = new ArrayList<>();

        //list, that contains the unique IDs of Divs, which are stored in generatorDivs
        ArrayList<String> generatorDivsIDs = new ArrayList<>();

        //filling in these two arraylist by going through the workloads and generating their's code
        for (int i = 0; i < list.size(); i++) {
            //the i-th generator
            Method m = list.get(i);

            String workloadFullName = perfWriter.getUniqueFullInfoReflection(m);
            String uniqueWorkloadName = perfWriter.getUniqueInfo(workloadFullName);

            if (i == 0) {
                //if it is the first workload, we do not want it to be hidden
                HtmlTree t = perfWriter.returnPerfoDiv(m, uniqueWorkloadName, workloadFullName, false);

                if (t == null) {
                    //if there was an error, we call ourselves, but without the first (= bad) method
                    addPerfoInfoMoreDivs(doc, list.subList(1, list.size()), output);
                    //and exit the method
                    return;
                } else {
                    generatorDivs.add(t);
                }
            } else {
                HtmlTree t = perfWriter.returnPerfoDiv(m, uniqueWorkloadName, workloadFullName, true);

                //if there was an error, we call ourselves, but without the bad method (i-th), which we have to first locate and exclude
                if (t == null) {
                    List<Method> firstPart = list.subList(0, i);

                    //if the item is not the last one, we need to merge the first part (all before this) with second part (all after this)
                    if (i != list.size() - 1) {
                        List<Method> secondPart = list.subList(i + 1, list.size());
                        firstPart.addAll(secondPart);
                    }
                    //generating performance information without the bad method
                    addPerfoInfoMoreDivs(doc, firstPart, output);
                    return;
                } else {
                    generatorDivs.add(t);
                }
            }
            generatorDivsIDs.add(uniqueWorkloadName);
        }

        //now everything is prepared (checked) and we start adding the content
        //first the performance title
        output.appendOutput(perfWriter.returnTitleOutput());

        //then the tree (div), that will contain all content
        HtmlTree tree = new HtmlTree(HtmlTag.DIV);
        //we need to give this div an unique id in order to be able to find it and replace its content
        //in this case the uniqueMethodID is generated from the package + methodName + its argument + just for sure (should never happen) there's also hashmap to remember, whether there was no such id before
        String uniqueMethodID = perfWriter.getUniqueInfo(perfWriter.getUniqueFullInfo(doc));
        tree.addAttr(HtmlAttr.ID, uniqueMethodID);

        //second we add the select content
        HtmlTree selectButton = new HtmlTree(HtmlTag.UL);
        HtmlTree contentLI = new HtmlTree(HtmlTag.LI);
        contentLI.addContent("Workload: ");
        contentLI.addContent(returnSelect(genNames, generatorDivsIDs, uniqueMethodID));
        selectButton.addContent(contentLI);
        output.appendOutput(new PerformanceOutputImpl(selectButton.toString()));

        for (HtmlTree generatorDiv : generatorDivs) {
            tree.addContent(generatorDiv);
        }
        PerformanceOutput perfOutp = new PerformanceOutputImpl(tree.toString());
        output.appendOutput(perfOutp);

        //and finally we add javascript code to maintain the select changes
        JavascriptCodeBox.addLocalCode(returnSelectJS(generatorDivsIDs, uniqueMethodID).toString());

    }

    /**
     * Prepares select html tag (the generator choose) with some given options
     *
     * @param genNames the generator names, that are shown as the possible
     * option values of the select
     * @param generatorDivsIDs the IDs of the generators, that are saved as the
     * option attribute values
     * @param methodID the methodID from which the unique ID of the select is
     * made (adding suffix "workloadSelect")
     * @return the content that contains the generated HTML code of the select
     */
    private static Content returnSelect(ArrayList<String> genNames, ArrayList<String> generatorDivsIDs, String methodID) {
        String uniqueSelectID = methodID + "workloadSelect";
        StringBuilder sb = new StringBuilder();

        sb.append("<select id=\"" + uniqueSelectID + "\">");
        for (int i = 0; i < genNames.size(); i++) {
            sb.append("<option value =\"" + generatorDivsIDs.get(i) + "\">" + genNames.get(i) + "</option>");
        }
        sb.append("</select>");

        return new RawHtml(sb.toString());
    }

    /**
     * Prepares the javascript code to control the actions of the particular
     * select.
     *
     * @param generatorDivsIDs the divsIDs that should be shown, when the
     * particular option in the select is selected
     * @param methodID the unique identifier of the method, from which the
     * unique ID of select and also the unique variable name used for storing
     * last selected value is dedicated
     * @return the content that contains generated JS code
     */
    private static Content returnSelectJS(ArrayList<String> generatorDivsIDs, String methodID) {
        String selectName = methodID + "workloadSelect";

        //we need a variable to store the last selected div in order to be able to hide it, when some new option is selected
        String lastSelectedVariableName = selectName + "Last";

        StringBuilder sb = new StringBuilder();
        sb.append("var " + lastSelectedVariableName + " = \"" + generatorDivsIDs.get(0) + "\";");
        sb.append("$(\"#" + selectName + "\").change(function() {");
        sb.append("var now = $(\"#" + selectName + "\").val();");
        sb.append("$(\"#\" + now).show();");
        sb.append("$(\"#\" + " + lastSelectedVariableName + ").hide();");
        sb.append(lastSelectedVariableName + " = now;");

        sb.append("});");
        return new RawHtml(sb.toString());
    }
}
