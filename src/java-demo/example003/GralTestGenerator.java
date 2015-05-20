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
package example003;

import cz.cuni.mff.d3s.tools.perfdoc.annotations.Generator;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamDesc;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.ServiceWorkload;
import cz.cuni.mff.d3s.tools.perfdoc.workloads.Workload;
import javax.swing.JFrame;

/**
 * This class contains workload generators for GralTest.
 *
 * @author Jakub Naplava
 */
public class GralTestGenerator {

    @Generator(description = "Generator to test the time of creating and showing of the scatter plot in the JFrame.", name = "Test generator")
    public void prepareDataPlotScatter(
            Workload workload,
            ServiceWorkload service,
            @ParamNum(description = "Number of points", min = 1, max = 10000, step = 1) int number_points,
            @ParamDesc("Frame size (resolution)") Size size) {

        int times = service.getPriority() * 3;
        JFrame frame = new JFrame();
        size.setSize(frame);
        for (int i = 0; i < times; i++) {
            workload.addCall(new GralTest(), new Object[]{frame, number_points});
        }
    }

    @Generator(description = "Generator to test the time of creating and showing of the raster plot in the JFrame.", name = "Test generator")
    public void prepareDataPlotRaster(
            Workload workload,
            ServiceWorkload service,
            @ParamNum(description = "Size", min = 1, max = 300, step = 1) int number_points,
            @ParamNum(description = "Zoom", min = 0.1, max = 5, step = 0.2) double zoom,
            @ParamDesc("Frame size (resolution)") Size size) {

        JFrame frame = new JFrame();
        size.setSize(frame);
        workload.addCall(new GralTest(), new Object[]{frame, number_points, zoom});
    }
}
