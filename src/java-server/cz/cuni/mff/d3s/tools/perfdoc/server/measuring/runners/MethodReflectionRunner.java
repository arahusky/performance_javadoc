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
package cz.cuni.mff.d3s.tools.perfdoc.server.measuring.runners;

import cz.cuni.mff.d3s.tools.perfdoc.server.MethodInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.MethodReflectionInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkSetting;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.MeasuringUtils;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.MethodRunner;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.MeasurementStatistics;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.WorkloadImpl;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of BenchmarkRunner that uses reflection to measure method
 * execution time.
 *
 * @author Jakub Naplava
 */
public class MethodReflectionRunner extends MethodRunner {

    private static final Logger log = Logger.getLogger(MethodReflectionRunner.class.getName());
    
    //measured method
    private Method method;
    
    //indicator, whether first parameter of measured method is Blackhole
    private boolean hasFirstParamBlackhole;

    @Override
    public MeasurementStatistics measure(BenchmarkSetting setting) throws Throwable {
        int warmupTime = setting.getMeasurementQuality().getWarmupTime();
        int warmupMeasurements = setting.getMeasurementQuality().getNumberOfWarmupMeasurements();
        int measurementTime = setting.getMeasurementQuality().getMeasurementTime();
        int measurements = setting.getMeasurementQuality().getNumberOfMeasurements();

        //the 0-th item in parameters should be the WorkloadImpl instance
        Object workloadCandidate = setting.getGeneratorArguments().getValues()[0];

        if (!(workloadCandidate instanceof WorkloadImpl)) {
            return null;
        }

        WorkloadImpl workload = (WorkloadImpl) workloadCandidate;

        //measured method 
        MethodInfo methodInfo = setting.getMeasuredMethod();
        //generator
        MethodInfo generatorInfo = setting.getGenerator();

        //we expect to get more specific MethodReflectionInfo from BenchmarkSetting
        if (!(methodInfo instanceof MethodReflectionInfo) || !(generatorInfo instanceof MethodReflectionInfo)) {
            log.log(Level.SEVERE, "Method or Generator were not passed as instances of MethodReflectionInfo, thus no measurement can be performed.");
            return null;
        }

        this.method = ((MethodReflectionInfo) methodInfo).getMethod();
        Method generator = ((MethodReflectionInfo) generatorInfo).getMethod();
        Class<?> generatorClass = ((MethodReflectionInfo) generatorInfo).getContainingClass();

        hasFirstParamBlackhole = MeasuringUtils.hasMeasuredMethodBlackhole(method);
        
        MeasurementStatistics statistics = new MeasurementStatistics();

        String msg = "New measurement: method: '" + methodInfo.getMethodName()
                + "' generator: '" + generatorInfo.getMethodName()
                + "' arguments: " + setting.getGeneratorArguments().getValuesDBFormat(true);

        log.log(Level.CONFIG, msg);

        //warmup
        long warmupMeasurementsDone = 0;
        long warmupTimeSpent = 0;
        long warmupStartTime = System.currentTimeMillis() / 1000;

        while (true) {
            warmupTimeSpent = (System.currentTimeMillis() / 1000) - warmupStartTime;
            if ((warmupTimeSpent >= warmupTime) || (warmupMeasurementsDone >= warmupMeasurements)) {
                break;
            }

            workload.reset();
            //preparing new arguments and instance for new calls
            generator.invoke(generatorClass.newInstance(), setting.getGeneratorArguments().getValues());

            //Thread.yield();
            //arguments and instance on which the call will be performed should be now prepared in workload
            warmupMeasurementsDone += reflectionCallCycle(workload, null);
        }

        //measurement
        long measurementCyclesSpent = 0;
        long measurementTimeSpent = 0;
        long measurementStartTime = System.currentTimeMillis() / 1000;

        //suggesting JVM that it could run garbagge collector
        //System.gc();
        
        while (true) {
            measurementTimeSpent = (System.currentTimeMillis() / 1000) - measurementStartTime;
            if ((measurementTimeSpent >= measurementTime) || (measurementCyclesSpent >= measurements)) {
                break;
            }

            workload.reset();
            //preparing new arguments and instance for new calls
            generator.invoke(generatorClass.newInstance(), setting.getGeneratorArguments().getValues());
            //Thread.yield();

            //arguments and instance on which the call will be performed should be now prepared in workload
            measurementCyclesSpent += reflectionCallCycle(workload, statistics);
        }

        log.log(Level.FINE, "Code was succesfully measured.");
        return statistics;
    }

    /**
     * Performs one reflective measurement cycle on given method with instances
     * and arguments obtained from given workload.
     *
     * @param method method, on which the measurement will be reflectively
     * performed
     * @param workload workloadImpl containing prepared arguments and instances
     * @param statistics instance of Statistics, where the measured results will
     * be stored (in case of measuring cycle), if just warming up, then null
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * 
     * @return number of measurements done during this call
     */
    private int reflectionCallCycle(WorkloadImpl workload, MeasurementStatistics statistics) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        int measurementsCount = 0;
        
        //arguments and instance on which the call will be performed should be now prepared in workload
        for (Object[] objs : workload.getCalls()) {
            //second item are the arguments for the tested method
            Object[] args = (Object[]) objs[1];
            //first item is the instance (possibly null for non-static methods)
            Object objectOnWhichToInvoke = objs[0];
            
            //if the first parameter of the measured method is Blackhole, we need to push new instance of it to the beggining of arguments
            if (hasFirstParamBlackhole) {
                args = MeasuringUtils.pushBlackholeToBegin(args);
            }
            
            long before = System.nanoTime();
            method.invoke(objectOnWhichToInvoke, args);
            long after = System.nanoTime();

            //if not warmup (but measurement) cycle
            if (statistics != null) {
                statistics.addResult(after - before);
            }

            Method afterMeasurementMethod = workload.getAfterMeasurementMethod();
            if (afterMeasurementMethod != null) {
                Object instance = workload.getInstance();
                afterMeasurementMethod.invoke(instance, objs);
            }
            
            measurementsCount++;
        }

        Method afterBenchmarkMethod = workload.getAfterBenchmarkMethod();
        if (afterBenchmarkMethod != null) {
            Object instance = workload.getInstance();
            afterBenchmarkMethod.invoke(instance, (Object[]) null);
        }
        
        return measurementsCount;
    }
}
