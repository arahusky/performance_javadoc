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
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.MethodRunner;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.statistics.Statistics;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.WorkloadImpl;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jakub Naplava
 */
public class MethodReflectionRunner extends MethodRunner {

    private static final Logger log = Logger.getLogger(MethodReflectionRunner.class.getName());

    @Override
    public Statistics measure(BenchmarkSetting setting) {
        //the 0-th item in parameters should be the WorkloadImpl instance
        Object workloadCandidate = setting.getWorkloadArguments().getValues()[0];

        if (!(workloadCandidate instanceof WorkloadImpl)) {
            return null;
        }
        
        WorkloadImpl workload = (WorkloadImpl) workloadCandidate;
        
        //tested method
        MethodInfo methodInfo = setting.getTestedMethod();
        //generator
        MethodInfo generatorInfo = setting.getWorkload();
        
        //we expect to get more specific MethodReflectionInfo from BenchmarkSetting
        if (!(methodInfo instanceof MethodReflectionInfo) || !(generatorInfo instanceof MethodReflectionInfo)) {
            //TODO throw some exception
            return null;
        }
        
        Method method = ((MethodReflectionInfo) methodInfo).getMethod();
        Method generator = ((MethodReflectionInfo) generatorInfo).getMethod();
        Class<?> generatorClass = ((MethodReflectionInfo) generatorInfo).getContainingClass();

        Statistics statistics = new Statistics();

        String msg = "Starting to measure..." + ", tested Method:{0}" + methodInfo.getMethodName()
                + "generator:{0}" + generatorInfo.getMethodName()
                + "class Generator:{0}" + generatorInfo.getQualifiedClassName();

        log.log(Level.CONFIG, msg);
        try {
            //Generator prepares us the arguments for the invidual calls of tested method.
            generator.invoke(generatorClass.newInstance(), setting.getWorkloadArguments().getValues());

            Object[] objs;

            /*The arguments for tested method should be prepared in WorkloadImpl.
            We get one by one and measure the measured method with them.*/
            while ((objs = workload.getCall()) != null) {
                //second item are the arguments for the tested method
                Object[] args = (Object[]) objs[1];
                Object objectOnWhichToInvoke = objs[0];

                long before = System.nanoTime();
                method.invoke(objectOnWhichToInvoke, args);
                long after = System.nanoTime();

                statistics.addResult(after - before);
            }
        } catch (IllegalAccessException ex) {
            log.log(Level.SEVERE, "MethodReflectionRunner: An IllegalAccessException occured", ex);
            return null;
        } catch (IllegalArgumentException ex) {
            log.log(Level.SEVERE, "MethodReflectionRunner: Some bad arguments were passed to tested/generator method", ex);
            return null;
        } catch (InstantiationException ex) {
            log.log(Level.SEVERE, "MethodReflectionRunner: Could not istantiate generator class", ex);
            return null;
        } catch (InvocationTargetException ex) {
            log.log(Level.SEVERE, "MethodReflectionRunner: An InvocationTargetException occured when trying to invoke generator/tested method", ex);
            return null;
        }

        return statistics;
    }
}
