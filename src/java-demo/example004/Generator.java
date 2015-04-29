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

package example004;

import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.Workload;
import java.util.Random;

/**
 *
 * @author Jakub Naplava
 */
public class Generator {
 
    @cz.cuni.mff.d3s.tools.perfdoc.annotations.Generator(description =  "Adds up two numbers", name = "Add up")
    public void prepareDataAdd(
            Workload workload,
            ServiceWorkload service,
            @ParamNum(description = "Non-sense", min = 1, max = 100000, step = 1) int non_sense            
    ) {
        int times = service.getPriority() * 3;
        Random random = new Random();
        for (int i = 0; i < times; i++) {
            int x = random.nextInt();
            int y = random.nextInt();
            workload.addCall(null, new Object[] {x,y});
        }   
    }
}
