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

import cz.cuni.mff.d3s.tools.perfdoc.annotations.Workload;
import java.util.HashMap;

/**
 *
 * @author Jakub Naplava
 */
public class HashmapPerf<T, Integer> extends HashMap<T, T> {
    
    private static final long serialVersionUID = 1L;
    
    @Workload("example006.HashmapPerfGenerator#prepare")
    public boolean contains(int o) {
        return super.containsKey(o);
    }
}
