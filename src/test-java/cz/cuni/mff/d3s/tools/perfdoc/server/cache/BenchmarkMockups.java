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

package cz.cuni.mff.d3s.tools.perfdoc.server.cache;

import cz.cuni.mff.d3s.tools.perfdoc.server.MethodInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkResult;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkResultImpl;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkSettingImpl;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.MethodArguments;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.MethodArgumentsImpl;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.Statistics;

/**
 *  Provider of simple Benchmark-mockups. 
 * 
 * @author Jakub Naplava
 */
public class BenchmarkMockups {
    public static final MethodInfo method1 = new MethodInfo("package1#class1#method1#@param1#0");
    public static final MethodInfo method2 = new MethodInfo("package1#class1#method2#@param1@param2#0");
    public static final MethodInfo method3 = new MethodInfo("package2#class1#method1##0");
    public static final MethodInfo method4 = new MethodInfo("package2#class1#method1#@param1#0");

    public static final MethodInfo workload1 = new MethodInfo("package3#class0#method0#@w1@sw1@param1#0");
    public static final MethodInfo workload2 = new MethodInfo("package3#class0#method1#@w1@sw1@param1@param2#0");
    public static final MethodInfo workload3 = new MethodInfo("package4#class1#method1#@w1@sw1@param1#0");
    public static final MethodInfo workload4 = new MethodInfo("package4#class1#method2#@w1@sw1@param2#0");

    public static final Object[] methodArguments1Raw = new Object[] {"w1", "sw1", "arg1"};
    public static final Object[] methodArguments2Raw = new Object[] {"w1", "sw1", 1, 2.0};
    public static final Object[] methodArguments3Raw = new Object[] {"w1", "sw1", 2D};
    public static final Object[] methodArguments4Raw = new Object[] {"w1", "sw1", "auto"};
    
    //w1, sw1 represents Workload, ServiceWorkload instance
    public static final MethodArguments methodArguments1 = new MethodArgumentsImpl(methodArguments1Raw);
    public static final MethodArguments methodArguments2 = new MethodArgumentsImpl(methodArguments2Raw);
    public static final MethodArguments methodArguments3 = new MethodArgumentsImpl(methodArguments3Raw);
    public static final MethodArguments methodArguments4 = new MethodArgumentsImpl(methodArguments4Raw);

    public static final BenchmarkSettingImpl benSet1 = new BenchmarkSettingImpl(method1, workload1, methodArguments1, 1);
    public static final BenchmarkSettingImpl benSet2 = new BenchmarkSettingImpl(method2, workload2, methodArguments2, 2);
    public static final BenchmarkSettingImpl benSet3 = new BenchmarkSettingImpl(method3, workload3, methodArguments3, 3);
    public static final BenchmarkSettingImpl benSet4 = new BenchmarkSettingImpl(method4, workload4, methodArguments4, 4);
    
    public static final Statistics statistics1 = new Statistics("{1}"); 
    public static final Statistics statistics2 = new Statistics("{10,12,10,8,55,1}"); 
    public static final Statistics statistics3 = new Statistics("{1,2,3,4,5,6,7,8,9,10}"); 
    public static final Statistics statistics4 = new Statistics("{0,10,12,13,10,15}"); 
    
    public static final BenchmarkResult benResult1 = new BenchmarkResultImpl(statistics1, benSet1);
    public static final BenchmarkResult benResult2 = new BenchmarkResultImpl(statistics2, benSet2);
    public static final BenchmarkResult benResult3 = new BenchmarkResultImpl(statistics3, benSet3);
    public static final BenchmarkResult benResult4 = new BenchmarkResultImpl(statistics4, benSet4);
}
