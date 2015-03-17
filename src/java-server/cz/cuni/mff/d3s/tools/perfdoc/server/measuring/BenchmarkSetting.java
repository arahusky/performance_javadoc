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

/**
 * Binds together all the information describing single point. That is measured
 * method, generator, parameters (for generator) and the measurement quality.
 *
 * @author Jakub Naplava
 */
public interface BenchmarkSetting {

    MethodInfo getMeasuredMethod();

    MethodInfo getGenerator();

    MethodArguments getGeneratorArguments();

    MeasurementQuality getMeasurementQuality();
}
