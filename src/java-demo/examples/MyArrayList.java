/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package examples;

import cz.cuni.mff.d3s.tools.perfdoc.annotations.Workload;
import java.util.ArrayList;

/**
 *
 * @author arahusky
 */
public class MyArrayList<T> extends ArrayList<T> {

    private static final long serialVersionUID = 1L;
    
    @Workload("examples.MyListGenerator#prepareData")
    @Override
    public boolean contains(Object o){
        return indexOf(o) >= 0;
    }
    
}
