/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.cuni.tools.doclets.formats.html;

import cz.cuni.tools.doclets.internal.toolkit.taglets.TagletOutput;

/**
 *
 * @author arahusky
 */
public class PerformanceOutputImpl implements PerformanceOutput{
    private StringBuffer output;

    public PerformanceOutputImpl(String o) {
        setOutput(o);
    }
    
    /**
     * {@inheritDoc}
     */
    public void setOutput (Object o) {
        output = new StringBuffer(o == null ? "" : (String) o);
    }
    
    /**
     * {@inheritDoc}
     */
    public void appendOutput(PerformanceOutput o) {
        output.append(o.toString());
    }
    
    public String toString() {
        return output.toString();
    }

    /**
     * Check whether the performance output is empty.
     */
    public boolean isEmpty() {
        return (toString().trim().isEmpty());
    }
}
