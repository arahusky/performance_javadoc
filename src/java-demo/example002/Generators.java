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

package example002;

import cz.cuni.mff.d3s.tools.perfdoc.annotations.Generator;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamDesc;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.Workload;
import example002.enums.SomeOtherEnum;

/**
 *
 * @author Jakub Naplava
 */
public class Generators {
          
      @Generator(description = "First waiting generator", name = "First wait")   
      public void prepareData1
      (
              Workload workload,
              ServiceWorkload sw,
              @ParamNum(description = "Waiting time (*2ms)", min = 1, max = 100, step = 2) int wait,
              @ParamDesc("Multiple waiting time by") SomeEnum en
              )
      {
          int times = 1;          
          int multiply = en.getNumberOfMillisToWait();
          
          SimpleWaiting simple = new SimpleWaiting();
          Object[] args = new Object[] {wait * multiply};
          for (int i = 0; i<times; i++) {          
                workload.addCall(simple, args);
          }          
      }
      
       @Generator(description = "Second waiting generator", name = "Second wait")   
      public void prepareData1
      (
              Workload workload,
              ServiceWorkload sw,
              @ParamNum(description = "Waiting time (*3ms)", min = 1, max = 100) int wait,
              @ParamDesc("Multiple waiting time by") SomeOtherEnum en
              )
      {
          int times = 1;          
          int multiply = en.getNumberOfMillisToWait();
          
          SimpleWaiting simple = new SimpleWaiting();
          Object[] args = new Object[] {wait * multiply};
          for (int i = 0; i<times; i++) {          
                workload.addCall(simple, args);
          }           
      }

}

