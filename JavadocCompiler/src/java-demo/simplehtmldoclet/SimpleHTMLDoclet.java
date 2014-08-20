/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package simplehtmldoclet;

import cz.cuni.mff.d3s.tools.perfdoc.annotations.*;

/**
 *
 * @author arahusky
 */
public class SimpleHTMLDoclet {

    /**
     * @param args the command line arguments
     */
    @Workloads({ @Workload("simplehtmldoclet.TestedClass#prepareData1"),  @Workload("simplehtmldoclet.TestedClass#prepareData2")})
    public static void main(String[] args) {
        
    }
    
    /**
     * 
     * @param param Some parameter
     * @return always true
     */
    @Workload("simplehtmldoclet.TestedClass#prepareData1")
    public boolean foo(String param)
    {
        return true;    
    }
    
    /**
     * 
     * @param param Param
     * @param param2 Param2 is nothing
     * @return always null
     */
    //@Workload("some value")
    @Workload(value = "simplehtmldoclet.TestedClass#prepareData2")
    public String foo1(String param, int param2)
    {
        return null;
    }
    
}
