/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.cuni.mff.d3s.tools.perfdoc.doclets.formats.html;


/**
 * Interface, that represents performance output
 * @author Jakub Naplava
 */
public interface PerformanceOutput {
    /**
     * Set the output for the performance.
     * @param o an object representing the output.
     */
    public abstract void setOutput(Object o);

    /**
     * Append the given output to this output.
     * @param o a PerformanceOutput representing the output.
     */
    public abstract void appendOutput(PerformanceOutput o);
}

