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
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.MeasurementQuality;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.MethodArguments;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.MethodArgumentsImpl;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.MeasurementStatistics;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.Statistics;

/**
 * Provider of simple Benchmark-mockups.
 *
 * @author Jakub Naplava
 */
public class BenchmarkMockups {

    public static final MethodInfo measuredMethod1 = new MethodInfo("package1#class1#method1#@param1#0");
    public static final MethodInfo measuredMethod2 = new MethodInfo("package1#class1#method2#@param1@param2#0");
    public static final MethodInfo measuredMethod3 = new MethodInfo("package2#class1#method1##0");
    public static final MethodInfo measuredMethod4 = new MethodInfo("package2#class1#method1#@param1#0");

    public static final MethodInfo generator1 = new MethodInfo("package3#class0#method0#@w1@sw1@param1#0");
    public static final MethodInfo generator2 = new MethodInfo("package3#class0#method1#@w1@sw1@param1@param2#0");
    public static final MethodInfo generator3 = new MethodInfo("package4#class1#method1#@w1@sw1@param1#0");
    public static final MethodInfo generator4 = new MethodInfo("package4#class1#method2#@w1@sw1@param2#0");

    public static final Object[] generatorArguments1Raw = new Object[]{"w1", "sw1", "arg1"};
    public static final Object[] generatorArguments2Raw = new Object[]{"w1", "sw1", 1, 2.0};
    public static final Object[] generatorArguments3Raw = new Object[]{"w1", "sw1", 2D};
    public static final Object[] generatorArguments4Raw = new Object[]{"w1", "sw1", "auto"};

    //w1, sw1 represents Workload, ServiceWorkload instance
    public static final MethodArguments generatorArguments1 = new MethodArgumentsImpl(generatorArguments1Raw);
    public static final MethodArguments generatorArguments2 = new MethodArgumentsImpl(generatorArguments2Raw);
    public static final MethodArguments generatorArguments3 = new MethodArgumentsImpl(generatorArguments3Raw);
    public static final MethodArguments generatorArguments4 = new MethodArgumentsImpl(generatorArguments4Raw);

    public static final MeasurementQuality measurementQuality1 = new MeasurementQuality(1, 1, 1,1,1,4);
    public static final MeasurementQuality measurementQuality2 = new MeasurementQuality(2,1,1,2,2,6);
    public static final MeasurementQuality measurementQuality3 = new MeasurementQuality(3,2,1,2,2,8);
    public static final MeasurementQuality measurementQuality4 = new MeasurementQuality(4,2,2,2,3,10);

    public static final BenchmarkSettingImpl benSet1 = new BenchmarkSettingImpl(measuredMethod1, generator1, generatorArguments1, measurementQuality1);
    public static final BenchmarkSettingImpl benSet2 = new BenchmarkSettingImpl(measuredMethod2, generator2, generatorArguments2, measurementQuality2);
    public static final BenchmarkSettingImpl benSet3 = new BenchmarkSettingImpl(measuredMethod3, generator3, generatorArguments3, measurementQuality3);
    public static final BenchmarkSettingImpl benSet4 = new BenchmarkSettingImpl(measuredMethod4, generator4, generatorArguments4, measurementQuality4);

    public static final Statistics statistics1 = new MeasurementStatistics("{1}");
    public static final Statistics statistics2 = new MeasurementStatistics("{10,12,10,8,55,1}");
    public static final Statistics statistics3 = new MeasurementStatistics("{1,2,3,4,5,6,7,8,9,10}");
    public static final Statistics statistics4 = new MeasurementStatistics("{0,10,12,13,10,15}");

    public static final BenchmarkResult benResult1 = new BenchmarkResultImpl(statistics1, benSet1);
    public static final BenchmarkResult benResult2 = new BenchmarkResultImpl(statistics2, benSet2);
    public static final BenchmarkResult benResult3 = new BenchmarkResultImpl(statistics3, benSet3);
    public static final BenchmarkResult benResult4 = new BenchmarkResultImpl(statistics4, benSet4);
}
