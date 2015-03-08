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
import cz.cuni.mff.d3s.tools.perfdoc.annotations.Workloads;
import java.util.LinkedList;

/**
 *
 * @author Jakub Naplava
 */
public class MyLinkedList<T> extends LinkedList<T> {
    
    private static final long serialVersionUID = 1L;
    
    @Workloads({
        @Workload("example001.MyLListGenerator#prepareDataBad"),
        @Workload("example001.MyLListGenerator#prepareDataGood")})
    @Override
    public boolean contains(Object o) {
        return super.contains(o);
    }
}
