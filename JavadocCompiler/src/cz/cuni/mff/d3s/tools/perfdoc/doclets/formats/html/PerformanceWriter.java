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
import cz.cuni.mff.d3s.tools.perfdoc.exceptions.GeneratorParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jakub Naplava
 */
public abstract class PerformanceWriter {

    static ConfigurationImpl configuration = ConfigurationImpl.getInstance();

    /**
     * Return the return tag output.
     *
     * @return the output of the return tag.
     */
    protected abstract PerformanceOutput returnTitleOutput();

    /**
     * Generates performance measurement for the given method
     *
     * @param doc measured method
     * @param output the PerformanceOutput to which the measurement code will be
     * added
     * @param workloadNames the workloadNames for the given method
     */
    public static void genPerfOutput(MethodDoc doc, PerformanceOutput output, String[] workloadNames) {

        //list, that will contain all generators for the given method (doc) 
        ArrayList<MethodDoc> list = new ArrayList<>();
        for (String w : workloadNames) {
            MethodDoc[] docs = ClassParser.findMethods(w);
            for (MethodDoc md : docs) {
                list.add(md);
            }
        }

        if (list.isEmpty()) {
            configuration.root.printWarning("There were found no generators for method: " + doc.qualifiedName() + ". Therefore, no performance info for this method will be added.");
        } else if (list.size() == 1) {
            addPerfoInfoOneDiv(list.get(0), output);
        } else {
            try {
                addPerfoInfoMoreDivs(doc, list, output);
            } catch (GeneratorParameterException ex) {
                configuration.root.printWarning(ex.getMessage());
            }
        }
    }

    /**
     * Adds performance info with one workload
     *
     * @param method the measured method
     * @param output the PerformanceOutput to insert the content
     */
    private static void addPerfoInfoOneDiv(MethodDoc method, PerformanceOutput output) {
        PerformanceWriterImpl perfWriter = new PerformanceWriterImpl();

        String uniqueWorkloadName = perfWriter.getUniqueInfo(method);
        HtmlTree tree = perfWriter.returnPerfoDiv(method, uniqueWorkloadName, false);

        if (tree != null) {
            //if the tree was succesfully built, we can add the performance title and then the tree
            output.appendOutput(perfWriter.returnTitleOutput());
            output.appendOutput(new PerformanceOutputImpl(tree.toString()));
        }
    }

    /**
     * Add the performance info with multiple wokloads
     *
     * @param doc the measured method
     * @param list the List of all workloads
     * @param output the PerformanceOutput to insert the content
     * @throws GeneratorParameterException when there's some invalid content in generator
     */
    private static void addPerfoInfoMoreDivs(MethodDoc doc, List<MethodDoc> list, PerformanceOutput output) throws GeneratorParameterException {

        if (list.isEmpty()) {
            configuration.root.printWarning(" There were found no generators for method: " + doc.qualifiedName() + ". Therefore, no performance info for this method will be added.");
            return;
        } else if (list.size() == 1) {
            addPerfoInfoOneDiv(list.get(0), output);
            return;
        }

        PerformanceWriterImpl perfWriter = new PerformanceWriterImpl();

        //first we need to check that the descriptions are different -> also the workloads are different
        ArrayList<String> genNames = new ArrayList<>();

        for (MethodDoc m : list) {
            Generator gen = perfWriter.getGenerator(m.annotations());

            String genName = gen.genName();
            if (genNames.contains(genName)) {
                //if for the measured method already exists generator, that has the genName 
                throw new GeneratorParameterException("The method: " + doc.qualifiedName() + " has two or more workloads that have the same genName. Therefore, no performance info for this method will be added.");
            } else {
                genNames.add(genName);
            }
        }

        //arraylist, that contains the HtmlTrees of all workloads for the measured method
        ArrayList<HtmlTree> generatorDivs = new ArrayList<>();

        //arraylist, that contains the unique IDs of Divs, which are stored in generatorDivs
        ArrayList<String> generatorDivsIDs = new ArrayList<>();

        //filling in these two arraylist by going through the workloads and generating their's code
        for (int i = 0; i < list.size(); i++) {
            //the i-th generator
            MethodDoc m = list.get(i);
            String uniqueWorkloadName = perfWriter.getUniqueInfo(m);

            if (i == 0) {
                //if it is the first workload, we do not want it to be hidden
                HtmlTree t = perfWriter.returnPerfoDiv(m, uniqueWorkloadName, false);

                //if there was an error, we call ourselves, but without the first (= bad) method
                if (t == null) {
                    addPerfoInfoMoreDivs(doc, list.subList(1, list.size()), output);
                    return;
                } else {
                    generatorDivs.add(t);
                }
            } else {
                HtmlTree t = perfWriter.returnPerfoDiv(m, uniqueWorkloadName, true);

                //if there was an error, we call ourselves, but without the bad method (i-th), which we have to first locate
                if (t == null) {
                    List<MethodDoc> firstPart = list.subList(0, i);

                    //if the item is not the last one, we need to merge the first part (all before this) with second part (all after this)
                    if (i != list.size() - 1) {
                        List<MethodDoc> secondPart = list.subList(i + 1, list.size());
                        firstPart.addAll(secondPart);
                    }
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
        //we need to give this div an unique id in order to be able to find it and replace it content
        //in this case the uniqueMethodID is generated from the package + methodName + its argument + just for sure (should never happen) there's also hashmap to remember, whether there was no such id before
        String uniqueMethodID = perfWriter.getUniqueInfo(doc);
        tree.addAttr(HtmlAttr.ID, uniqueMethodID);

        //second we add the select content
        output.appendOutput(new PerformanceOutputImpl(returnSelect(genNames, generatorDivsIDs, uniqueMethodID).toString()));

        //then we add all the workloads (divs)
        for (int i = 0; i < generatorDivs.size(); i++) {
            tree.addContent(generatorDivs.get(i));
        }
        PerformanceOutput perfOutp = new PerformanceOutputImpl(tree.toString());
        output.appendOutput(perfOutp);

        //and finally we add javascript code to maintain the select changes
        output.appendOutput(new PerformanceOutputImpl(returnSelectJS(generatorDivsIDs, uniqueMethodID).toString()));

    }

    /**
     * Prepares select html tag with some options, that are given
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
        //TODO possibly add to some JSSelectWriter
        String selectName = methodID + "workloadSelect";

        //we need a variable to store the last selected div in order to be able to hide it, when some new option is selected
        String lastSelectedVariableName = selectName + "Last";

        StringBuilder sb = new StringBuilder("<script> ");
        sb.append("var " + lastSelectedVariableName + " = \"" + generatorDivsIDs.get(0) + "\";");
        sb.append("$(function() {");
        sb.append("$(\"#" + selectName + "\").change(function() {");
        sb.append("var now = $(\"#" + selectName + "\").val();");
        sb.append("$(\"#\" + now).show();");
        sb.append("$(\"#\" + " + lastSelectedVariableName + ").hide();");
        sb.append(lastSelectedVariableName + " = now;");

        sb.append("}); }); </script>");
        return new RawHtml(sb.toString());
    }

}
