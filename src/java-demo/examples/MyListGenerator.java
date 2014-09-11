/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package examples;

import cz.cuni.mff.d3s.tools.perfdoc.annotations.Generator;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.Workload;

/**
 *
 * @author arahusky
 */
public class MyListGenerator {

    @Generator(description = "Search in a collection", genName = "SomeGen")
    public void prepareData(
            Workload workload,
            ServiceWorkload service,
            @ParamNum(description = "Collection size", min = 0, max = 10000, step = 1) int collection_size) 
    {
        int times = service.getNumberResults();
        
        for (int i = 0; i<times; i++) {
            MyArrayList<Object> myList = new MyArrayList<Object>();
            
            for (int n = 0; n<collection_size; n++){
                myList.add(n);
            }
            
            workload.addCall(myList, new Object[] {collection_size + 1});
        }        
    }
}
