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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
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
     * @param additions Number of additions
     * @param removals Number of removals
     * @param searches Number of searches
     * @param iterations Number of iterations
     * @return Hash generated against DCE. The goal of returning integer is to
     * disallow optimizer to optimize our code as a code, which does nothing
     * (Dead code elimination). Assuming that adding up integers is relatively
     * cheap operation (compared to other operations used in method), we sum up
     * hash values of objects stored in list and the computed value return.
     */
    @SuppressWarnings("unchecked")
    @Workload("example001.MyAListGenerator#prepareDataMultiple")
    public int doMultiple(int additions, int removals, int searches, int iterations) {

        int countedHash = 0;

        for (int i = 0; i < additions; i++) {
            this.add(i);
        }

        for (int i = 0; i < searches; i++) {
            this.contains(i);
        }

        for (int i = 0; i < iterations; i++) {
            Iterator it = iterator();
            Object o;
            while (it.hasNext()) {
                o = it.next();
                countedHash += System.identityHashCode(o);
            }
        }

        for (int i = 0; i < removals; i++) {
            this.remove(i);
        }

        return countedHash;
    }
}
