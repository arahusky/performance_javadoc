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

package cz.cuni.mff.d3s.tools.perfdoc.workloads;

import cz.cuni.mff.d3s.tools.perfdoc.annotations.AfterBenchmark;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.AfterMeasurement;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jakub Naplava
 */
public class WorkloadImpl implements Workload{

    private final List<Object[]> list = new ArrayList<>();
    
    private Object instance;
    
    private Method afterMeasurementMethod;
    
    private Method afterBenchmarkMethod;
    
    
    @Override
    public void addCall(Object obj, Object... args) {
        list.add(new Object[] {obj, args});
    }
    
    public Object[] getCall()
    {
        if (list.isEmpty())
            return null;
        
        return list.remove(list.size() - 1);
    }    
    
    public List<Object[]> getCalls() {
        return list;
    }

    @Override
    public void setHooks(Object obj) {
        this.instance = obj;
        
        Class<?> objClass = obj.getClass();
        
        Method[] methods = objClass.getMethods();
        
        for (Method method : methods) {
            Annotation annotation = method.getAnnotation(AfterMeasurement.class);            
            if (annotation != null) {
                afterMeasurementMethod = method;
            }
            
            annotation = method.getAnnotation(AfterBenchmark.class);
            if (annotation != null) {
                afterBenchmarkMethod = method;
            }
        }
    }

    public Object getInstance() {
        return instance;
    }

    public Method getAfterMeasurementMethod() {
        return afterMeasurementMethod;
    }

    public Method getAfterBenchmarkMethod() {
        return afterBenchmarkMethod;
    }    
    
    /**
     * Empties workload.
     */
    public void reset() {
        this.list.clear();
        this.afterBenchmarkMethod = null;
        this.afterMeasurementMethod = null;
        this.instance = null;
    }
}
