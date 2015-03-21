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

import cz.cuni.mff.d3s.tools.perfdoc.annotations.AfterBenchmark;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.AfterMeasurement;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.Generator;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.Workload;

/**
 *
 * @author Jakub Naplava
 */
public class GeneratorMockup {

    @Generator(description = "Some desc", genName = "Some genName")
    public void generate(
            Workload workload,
            ServiceWorkload service,
            @ParamNum(description = "Some param", min = 1, max = 10000, step = 100) int someParam) {

        for (int i = 0; i < 100; i++) {
            workload.addCall(null, i);
        }

        workload.setHooks(this);
    }

    @AfterMeasurement
    public void afterMeasurementMethod(Object instance, Object[] args) {
        //doNothing
    }

    @AfterBenchmark
    public void afterBenchmarkMethod() {
        //doNothing
    }
}
