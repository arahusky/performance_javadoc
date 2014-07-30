/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package simplehtmldoclet;

import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamDesc;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.Generator;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.Workload;
import simplehtmldoclet.enums.SomeTestingEnum;
import simplehtmldoclet.enums.next.SomeOtherEnum;

/**
 *
 * @author arahusky
 */
public class TestedClass {
    
    @Generator(description = "Tests perfomance of a collection with user defined number of operations.", genName = "Mix of operations")
      public void prepareData(
              Workload workload,
              ServiceWorkload sw,
              @ParamNum(description = "Collection size", min = 10, max = 5000) int collection_size
              )
      {
      }
      
    @Generator(description = "Tests perfomance of a collection with user defined number of operations.", genName = "Mix of operations")
      public void prepareData2(
              Workload workload,
              ServiceWorkload sw,
              @ParamNum(description = "Collection size", min = 10, max = 5000) int collection_size,
              @ParamNum(description = "Number of additions", min = 0, max = 100) int additions,
              @ParamNum(description = "Number of removals", min = 0, max = 100, axis = false) int removals,
              @ParamNum(description = "Number of searches", min = 0, max = 100, step = 2) int searches,
              @ParamNum(description = "Just some percentage", min = 0, max = 100, step = 0.01) float percentage,
              @ParamDesc(description = "Some String") String str,
              @ParamDesc(description = "Some enum") SomeTestingEnum someTest,
              @ParamDesc(description = "Second enum") SomeOtherEnum blah
              )
      {
      }
      
      @Generator(description = "Some another generator", genName = "Some another")   
      public void prepareData1
      (
              Workload workload,
              ServiceWorkload sw,
        @ParamNum(description = "Number of removalss", min = 0, max = 100) int removalss,
              @ParamNum(description = "Number of searchess", min = 0, max = 100, step = 1.2) int searchess,
              @ParamNum(description = "Just some percentagse", min = 0, max = 100, step = 0.01) float percentages
             // @ParamDesc(description = "Some Strings") String strs  
              )
      {
          
      }
      
        @Generator(description = "Some another generator1", genName = "Some another1")   
      public void prepareData1
      (
              Workload workload,
              ServiceWorkload sw,
        @ParamNum(description = "Number of removalss", min = 0, max = 100) int removalss,
              @ParamNum(description = "Number of searchess", min = 0, max = 100, step = 1.2) int searchess,
              @ParamDesc(description = "Some String") String str,
              @ParamDesc(description = "some") SomeEnum someEnum
              )
      {
          
      }
}
