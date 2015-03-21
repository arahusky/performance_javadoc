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
package cz.cuni.mff.d3s.tools.perfdoc.server.workloads;

import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkloadImpl;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.WorkloadImpl;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Jakub Naplava
 */
public class WorkloadsTest {

    @Test
    public void testBasicWorkloadImpl() throws NoSuchMethodException {
        WorkloadImpl workloadImpl = new WorkloadImpl();

        GeneratorMockup generatorMockup = new GeneratorMockup();
        generatorMockup.generate(workloadImpl, new ServiceWorkloadImpl(), 500);

        Method afterBenchmarkMethod = workloadImpl.getAfterBenchmarkMethod();
        Method afterMeasurementMethod = workloadImpl.getAfterMeasurementMethod();

        Method afterBenchmarkMethodRight = generatorMockup.getClass().getMethod("afterBenchmarkMethod");
        Method afterMeasurementMethodRight = generatorMockup.getClass().getMethod("afterMeasurementMethod", Object.class, Object[].class);

        Assert.assertEquals(afterBenchmarkMethodRight, afterBenchmarkMethod);
        Assert.assertEquals(afterMeasurementMethodRight, afterMeasurementMethod);

        List<Object[]> calls = workloadImpl.getCalls();

        for (int i = 0; i < 100; i++) {
            Assert.assertNull(calls.get(i)[0]);
            
            int num = (int) ((Object[]) calls.get(i)[1])[0];
            
            Assert.assertEquals(i, num);
        }
    }
}
