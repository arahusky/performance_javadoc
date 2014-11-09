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

package cz.cuni.mff.d3s.tools.perfdoc.server.measuring;

import cz.cuni.mff.d3s.tools.perfdoc.server.MethodInfo;
import java.util.Objects;

/**
 * 
 * @author Jakub Naplava
 */
public class BenchmarkSettingImpl implements BenchmarkSetting{

    private final MethodInfo testedMethod;
    private final MethodInfo workload;
    private final MethodArguments arguments;
    private final int priority;
    
    public BenchmarkSettingImpl(MethodInfo testedMethod, MethodInfo workload, MethodArguments arguments, int priority) {
        this.testedMethod = testedMethod;
        this.workload = workload;
        this.arguments = arguments;
        this.priority = priority;
    }
    
    public BenchmarkSettingImpl(MeasureRequest measureRequest, MethodArguments arguments) {
        this.testedMethod = measureRequest.getTestedMethod();
        this.workload = measureRequest.getWorkload();
        this.priority = measureRequest.getPriority();
        this.arguments = arguments;
    }
    
    
    @Override
    public MethodInfo getTestedMethod() {
        return testedMethod;
    }

    @Override
    public MethodInfo getWorkload() {
        return workload;
    }

    @Override
    public MethodArguments getWorkloadArguments() {
        return arguments;
    }

    @Override
    public int getPriority() {
        return priority;
    }    

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + Objects.hashCode(this.testedMethod);
        hash = 61 * hash + Objects.hashCode(this.workload);
        hash = 61 * hash + Objects.hashCode(this.arguments);
        hash = 61 * hash + this.priority;
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof BenchmarkSettingImpl)) {
            return false;
        }
        BenchmarkSettingImpl bs = (BenchmarkSettingImpl) o;
        
        return bs.arguments.equals(this.arguments) &&
                (bs.priority == this.priority) &&
                bs.testedMethod.equals(this.testedMethod) &&
                bs.workload.equals(this.workload);
    }
}
