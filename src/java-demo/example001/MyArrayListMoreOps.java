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

import cz.cuni.mff.d3s.tools.perfdoc.annotations.Workload;
import cz.cuni.mff.d3s.tools.perfdoc.blackhole.Blackhole;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * This class contains additional methods one finds useful when inspecting
 * performance of ArrayList\<T\>.
 *
 * @author Jakub Naplava
 */
@SuppressWarnings("rawtypes")
public class MyArrayListMoreOps extends ArrayList {

    private static final long serialVersionUID = 1L;

    /**
     * Sorts the collection.
     */
    @Workload("example001.MyAListGenerator#prepareDataSort")
    @SuppressWarnings("unchecked")
    public void sort() {
        Collections.sort(this);
    }

    /**
     * Performs operations upon the collection
     *
     * The benchmarking harness supports two ways to deal with dead code
     * elimination.
     *
     * The first one is explicit using of Blackhole class, which must be defined
     * as the first parameter of the measured method and is prepared by
     * benchmarking harness. This Blackhole can be then used to consume values
     * (from operations having no side effect). This way is showed by this
     * method.
     *
     * The second way is to change return type to, for example, int. Assuming
     * that adding up integers is relatively cheap operation (compared to other
     * operations used in method), we sum up hash values of objects returned
     * from methods having no side effect, and then return counted value.
     *
     * @param bh Blackhole to consume not-returnable values
     * @param additions Number of additions
     * @param removals Number of removals
     * @param searches Number of searches
     * @param iterations Number of iterations
     */
    @SuppressWarnings("unchecked")
    @Workload("example001.MyAListGenerator#prepareDataMultiple")
    public void doMultiple(Blackhole bh, int additions, int removals, int searches, int iterations) {

        for (int i = 0; i < additions; i++) {
            this.add(i);
        }

        for (int i = 0; i < searches; i++) {
            bh.consume(this.contains(i));
        }

        for (int i = 0; i < iterations; i++) {
            Iterator it = iterator();
            while (it.hasNext()) {
                bh.consume(it.next());
            }
        }

        for (int i = 0; i < removals; i++) {
            this.remove(i);
        }
    }
}
