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
import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.Workload;
import java.util.Random;

/**
 * This class contains workload generators for MyArrayList.
 *
 * @author Jakub Naplava
 */
public class MyAListGenerator {

    @Generator(description = "Succesfull search in a collection", name = "Succesfull search")
    public void prepareDataGood(
            Workload workload,
            ServiceWorkload service,
            @ParamNum(description = "Collection size", min = 1, max = 1000000, step = 1) int collection_size) {

        //creating collection of requested size
        MyArrayList<Object> myList = new MyArrayList<>();
        for (int n = 0; n < collection_size; n++) {
            myList.add(n);
        }

        //we may use the same collection multiple times
        int times = service.getPriority() * 10;
        Random random = new Random();
        for (int i = 0; i < times; i++) {
            //every workload contains randomly chosen item of the collection
            workload.addCall(myList, new Object[]{random.nextInt(collection_size)});
        }
    }

    @Generator(description = "Unsuccesfull search in a collection", name = "Unsuccesfull search")
    public void prepareDataBad(
            Workload workload,
            ServiceWorkload service,
            @ParamNum(description = "Collection size", min = 1, max = 1000000, step = 1) int collection_size) {

        //creating collection of requested size
        MyArrayList<Object> myList = new MyArrayList<>();
        for (int n = 0; n < collection_size; n++) {
            myList.add(n);
        }

        //the argument of the method is the item not contained in the collection
        Object[] args = new Object[]{collection_size};

        //we may use the same collection multiple times
        int times = service.getPriority() * 10;
        for (int i = 0; i < times; i++) {
            workload.addCall(myList, args);
        }
    }

    @SuppressWarnings("unchecked")
    @Generator(description = "Sort of the collection.", name = "Collection sort")
    public void prepareDataSort(
            Workload workload,
            ServiceWorkload service,
            @ParamNum(description = "Collection size", min = 1, max = 100000, step = 100) int collection_size) {

        //we will prepare more collections so that the generator does not have to call us multiple times
        int times = service.getPriority() * 5;
        Random random = new Random();
        for (int i = 0; i < times; i++) {
            MyArrayListMoreOps myList = new MyArrayListMoreOps();
            for (int n = 0; n < collection_size; n++) {
                myList.add(random.nextInt());
            }
            //every call of sort method discards the collection for further calls
            workload.addCall(myList, new Object[0]);
        }
    }

    @SuppressWarnings("unchecked")
    @Generator(description = "Tests performance of the collection with user-defined sequence of operations upon the collection", name = "Operations mix")
    public void prepareDataMultiple(
            Workload workload,
            ServiceWorkload service,
            @ParamNum(description = "Initial collection size", min = 1, max = 100000, step = 1) int collection_size,
            @ParamNum(description = "Number of additions", min = 1, max = 1000, step = 1, axis = false) int additions,
            @ParamNum(description = "Number of removals", min = 1, max = 1000, step = 1, axis = false) int removals,
            @ParamNum(description = "Number of searches", min = 1, max = 1000, step = 1, axis = false) int searches,
            @ParamNum(description = "Number of iterations", min = 1, max = 500, step = 1) int iterations) {

        //we will prepare more collections so that the generator does not have to call us multiple times
        int times = service.getPriority() * 3;
        Object[] args = new Object[]{additions, removals, searches, iterations};
        Random random = new Random();
        for (int i = 0; i < times; i++) {
            MyArrayListMoreOps myList = new MyArrayListMoreOps();
            for (int n = 0; n < collection_size; n++) {
                myList.add(random.nextInt());
            }
            workload.addCall(myList, args);
        }
    }
}
