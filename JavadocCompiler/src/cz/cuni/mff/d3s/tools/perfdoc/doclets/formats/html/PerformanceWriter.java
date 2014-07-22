/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.cuni.mff.d3s.tools.perfdoc.doclets.formats.html;

/**
 *
 * @author Jakub Naplava
 */
public abstract class PerformanceWriter {
    
    /**
     * Return the return tag output.
     *
     * @param returnTag the return tag to output.
     * @return the output of the return tag.
     */
    protected abstract PerformanceOutput returnTitleOutput();
    
    public static void genPerfOutput(HtmlDocletWriter writer, PerformanceOutput output, String[] workloadNames)
    {
        PerformanceWriterImpl perfWriter = new PerformanceWriterImpl(writer);
        output.appendOutput(perfWriter.returnTitleOutput());
        
        if (workloadNames.length == 1)
        {
            output.appendOutput(perfWriter.returnPerfoBody(workloadNames[0]));
        }
        else
        {
            
        }
    }
}
