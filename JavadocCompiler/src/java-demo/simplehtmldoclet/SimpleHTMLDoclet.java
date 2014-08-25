/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package simplehtmldoclet;

import cz.cuni.mff.d3s.tools.perfdoc.annotations.*;
import java.text.DecimalFormat;
import java.util.Random;

/**
 *
 * @author arahusky
 */
public class SimpleHTMLDoclet {

    /**
     * @param args the command line arguments
     */
    @Workloads({ @Workload("simplehtmldoclet.TestedClass#prepareData1"),  @Workload("simplehtmldoclet.TestedClass#prepareData2")})
    public static void main(String[] args) throws InterruptedException { 
        System.out.println("Going to wait for: " + args.length);
        Random r = new Random();
        for (int i = 0; i<args.length; i++) {
        Thread.sleep(r.nextInt(150));
        }        
    }
    
    /**
     * 
     * @param param Some parameter
     * @return always true
     */
    @Workload("simplehtmldoclet.TestedClass#prepareData1")
    public int[] ListClone(int[] param) throws InterruptedException
    {
        return null;     
    }
    
    /**
     * 
     * @param param Param
     * @param param2 Param2 is nothing
     * @return always null
     */
    //@Workload("some value")
    @Workload(value = "simplehtmldoclet.TestedClass#prepareData2")
    public String foo1(String param, int param2) throws InterruptedException
    {
        for (int i = 0; i<param.length(); i++) {    
            Random r =new Random();
        Thread.sleep(r.nextInt(30));
        }
        return null;
    }
    
}
