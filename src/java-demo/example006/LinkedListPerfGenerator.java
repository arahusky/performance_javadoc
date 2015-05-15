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

package example006;

import cz.cuni.mff.d3s.tools.perfdoc.annotations.Generator;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.Workload;
import java.util.Random;

/**
 *
 * @author Jakub Naplava
 */
public class LinkedListPerfGenerator {
    
    @Generator(description = "asd", name = "as")
    public void prepare(Workload workload, ServiceWorkload service,
            @ParamNum(description = "Collection size", min = 1, max = 5000000, step = 1) int collection_size
            ) {
        LinkedListPerf<Object> myList = new LinkedListPerf<>();
        for (int n = 0; n < collection_size; n++) {
            myList.add(n);
        }
        
        //we may use the collection multiple times
        int times = service.getPriority() * 100;
        Random random = new Random();
        for (int i = 0; i < times; i++) {
            //every workload contains randomly chosen item of the collection
            workload.addCall(myList, new Object[]{random.nextInt(collection_size)});
        }
    }
}
