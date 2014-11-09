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
          
      @Generator(description = "First waiting generator", genName = "First wait")   
      public void prepareData1
      (
              Workload workload,
              ServiceWorkload sw,
              @ParamNum(description = "Waiting time (*3ms)", min = 0, max = 100) int wait,
              @ParamNum(description = "Waiting time (*4ms)", min = 0, max = 100, step = 2) int wait2,
              @ParamDesc("Multiple waiting time by") SomeEnum en
              )
      {
          int times = sw.getNumberCalls();          
          int multiply = 1;
          
          switch (en) {
              case TWO:
                  multiply = 2;
                  break;
              case THREE:
                  multiply = 3;
          }
          
          SimpleWaiting simple = new SimpleWaiting();
          Object[] args = new Object[] {wait * multiply, wait2*2 * multiply};
          for (int i = 0; i<times; i++) {          
                workload.addCall(simple, args);
          }          
      }
      
       @Generator(description = "Second waiting generator", genName = "Second wait")   
      public void prepareData1
      (
              Workload workload,
              ServiceWorkload sw,
              @ParamNum(description = "Waiting time (*2ms)", min = 0, max = 100) int wait,
              @ParamDesc("Multiple waiting time by") SomeOtherEnum en
              )
      {
          int times = sw.getNumberCalls();          
          int multiply = 1;
          
          switch (en) {
              case TWO:
                  multiply = 2;
                  break;
              case FOUR:
                  multiply = 4;
          }
          
          SimpleWaiting simple = new SimpleWaiting();
          Object[] args = new Object[] {wait * multiply, 1};
          for (int i = 0; i<times; i++) {          
                workload.addCall(simple, args);
          }           
      }

}

