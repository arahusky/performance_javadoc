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
import java.util.ArrayList;

/**
 *
 * @author Jakub Naplava
 */
public abstract class PerformanceWriter {

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
        PerformanceWriterImpl perfWriter = new PerformanceWriterImpl();
        output.appendOutput(perfWriter.returnTitleOutput());

        //list, that will contain all generators for the given merhod (doc) 
        ArrayList<MethodDoc> list = new ArrayList<>();

        for (String w : workloadNames) {
            MethodDoc[] docs = ClassParser.findMethods(w);
            for (MethodDoc md : docs) {
                list.add(md);
            }
        }

        if (list.isEmpty()) {
            //TODO throw some error or at least warning           
        } else if (list.size() == 1) {
            MethodDoc method = list.get(0);
            String uniqueWorkloadName = perfWriter.getUniqueInfo(method);
            PerformanceOutput perfOutp = new PerformanceOutputImpl(perfWriter.returnPerfoDiv(method, uniqueWorkloadName).toString());
            output.appendOutput(perfOutp);
        } else {
            //there will be a "select" to choose the workload you want
            //first we need to check that the descriptions are different -> also the workloads are different
            ArrayList<String> genNames = new ArrayList<>();
            
            for (MethodDoc m : list) {
                Generator gen = perfWriter.getGenerator(m.annotations());

                //if there is no annotation Generator, we have to tell it to the programmer and do not continue making performance for this method
                if (gen == null) {
                    //TODO some error
                } else {
                    String genName = gen.genName();
                    if (genNames.contains(genName)) {
                        //TODO there is already this name, it would be ambiguous, tell it to the programmer and end it
                    } else {
                        genNames.add(genName);
                    }
                }
            }
            
            ArrayList<HtmlTree> generatorDivs = new ArrayList<>();
            ArrayList<String> generatorDivsIDs = new ArrayList<>();
            for (MethodDoc m : list) {
                String uniqueWorkloadName = perfWriter.getUniqueInfo(m);
                generatorDivs.add(perfWriter.returnPerfoDiv(m, uniqueWorkloadName));
                generatorDivsIDs.add(uniqueWorkloadName);
            }

            HtmlTree tree = new HtmlTree(HtmlTag.DIV);
            //we need to give this div an unique id in order to be able to find it and replace it content
            //in this case the uniqueMethodID is generated from the package + methodName + its argument + just for sure (should never happen) there's also hashmap to remember, whether there was no such id before
            String uniqueMethodID = perfWriter.getUniqueInfo(doc);
            tree.addAttr(HtmlAttr.ID, uniqueMethodID);
            
            output.appendOutput(new PerformanceOutputImpl(returnSelect(genNames, generatorDivsIDs, uniqueMethodID).toString()));

            for (int i = 0; i<generatorDivs.size(); i++)
            {
                //possibly other 
                tree.addContent(htree);
            }
            PerformanceOutput perfOutp = new PerformanceOutputImpl(tree.toString());
            output.appendOutput(perfOutp);
            
            //output.appendOutput(new PerformanceOutputImpl(returnSelectJS(generatorDivs, generatorDivsIDs, uniqueMethodID).toString()));
        }
    }
    
    private static Content returnSelect(ArrayList<String> genNames, ArrayList<String> generatorDivsIDs, String methodID)
    {
        String uniqueSelectID = methodID + "workloadSelect";
        StringBuilder sb = new StringBuilder();
        
        sb.append("<select id=\"" + uniqueSelectID + "\">");        
        for (int i = 0; i<genNames.size(); i++)
        {
            sb.append("<option value =\"" + generatorDivsIDs.get(i) + "\">" + genNames.get(i) + "</option>");
        }        
        sb.append("</select>");       
        
        
        return new RawHtml(sb.toString());
    }    
    
    private static Content returnSelectJS( ArrayList<HtmlTree> generatorDivs, ArrayList<String> generatorDivsIDs, String methodID)
    {
        String selectName = methodID + "workloadSelect";
        StringBuilder sb = new StringBuilder("<script> $( document ).ready(function() { $(\"#" + selectName +"\").change(function() {");
        
        for (int i = 0; i<generatorDivs.size(); i++)
        {
            HtmlTree a = generatorDivs.get(i);
           sb.append("if ($(\"#" + selectName + "\").val() == \"" + generatorDivsIDs.get(i) + "\") { $(\"#" + methodID + "\").html(\"" + generatorDivs.get(i).toString().replaceAll("'", "\\\\'").replaceAll("\"", "\\\\\"").replaceAll("\n", "") + "\") };");
        }
        
        sb.append("}); }); </script>");
        return new RawHtml(sb.toString());
    }
}
