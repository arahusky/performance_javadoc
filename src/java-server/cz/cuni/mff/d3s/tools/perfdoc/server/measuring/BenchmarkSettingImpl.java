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
 * Basic implementation of BenchmarkSetting interface.
 *
 * @author Jakub Naplava
 */
public class BenchmarkSettingImpl implements BenchmarkSetting {

    private final MethodInfo measuredMethod;
    private final MethodInfo generator;
    private final MethodArguments arguments;
    private final MeasurementQuality measurementQuality;

    public BenchmarkSettingImpl(MethodInfo measuredMethod, MethodInfo generator, MethodArguments arguments, MeasurementQuality measurementQuality) {
        this.measuredMethod = measuredMethod;
        this.generator = generator;
        this.arguments = arguments;
        this.measurementQuality = measurementQuality;
    }

    public BenchmarkSettingImpl(MeasureRequest measureRequest, MethodArguments arguments) {
        this.measuredMethod = measureRequest.getMeasuredMethod();
        this.generator = measureRequest.getGenerator();
        this.measurementQuality = measureRequest.getMeasurementQuality();
        this.arguments = arguments;
    }

    @Override
    public MethodInfo getMeasuredMethod() {
        return measuredMethod;
    }

    @Override
    public MethodInfo getGenerator() {
        return generator;
    }

    @Override
    public MethodArguments getGeneratorArguments() {
        return arguments;
    }

    @Override
    public MeasurementQuality getMeasurementQuality() {
        return measurementQuality;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.measuredMethod);
        hash = 89 * hash + Objects.hashCode(this.generator);
        hash = 89 * hash + Objects.hashCode(this.arguments);
        hash = 89 * hash + Objects.hashCode(this.measurementQuality);
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

        return bs.arguments.equals(this.arguments)
                && bs.measurementQuality.equals(this.measurementQuality)
                && bs.measuredMethod.equals(this.measuredMethod)
                && bs.generator.equals(this.generator);
    }
}
