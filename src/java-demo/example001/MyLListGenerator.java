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

package example001;

import cz.cuni.mff.d3s.tools.perfdoc.annotations.Generator;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamDesc;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.Workload;
import java.util.Random;

/**
 *
 * @author Jakub Naplava
 */
public class MyLListGenerator {
    
     @Generator(description = "Succesfull search in a collection", genName = "Succesfull search")
    public void prepareDataGood(
            Workload workload,
            ServiceWorkload service,
            @ParamNum(description = "Collection size", min = 1, max = 10000, step = 1) int collection_size) {

        MyLinkedList<Object> myList = new MyLinkedList<>();
        for (int n = 0; n < collection_size; n++) {
            myList.add(n);
        }

        int times = service.getPriority() * 10;
        Random random = new Random();
        for (int i = 0; i < times; i++) {
            workload.addCall(myList, new Object[]{random.nextInt(collection_size)});
        }
    }

    @Generator(description = "Unsuccesfull search in a collection", genName = "Unsuccesfull search")
    public void prepareDataBad(
            Workload workload,
            ServiceWorkload service,
            @ParamDesc("asd") String s,
            @ParamNum(description = "Collection size", min = 1, max = 10000, step = 1) int collection_size) {

        MyLinkedList<Object> myList = new MyLinkedList<>();
        for (int n = 0; n < collection_size; n++) {
            myList.add(n);
        }

        Object[] args = new Object[]{collection_size};
        int times = service.getPriority() * 10;
        for (int i = 0; i < times; i++) {
            workload.addCall(myList, args);
        }
    }
}
