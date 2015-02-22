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
 *
 * @author Jakub Naplava
 */
public class GralTestGenerator {

    @Generator(description = "Generator to test the time of creating and showing of the scatter plot in the JFrame.", genName = "Test generator")
    public void prepareDataPlotScatter(
            Workload workload,
            ServiceWorkload service,
            @ParamNum(description = "Number of points", min = 1, max = 10000, step = 1) int number_points,
            @ParamDesc("Frame size (resolution)") Size size) {

        int times = service.getNumberCalls();
        JFrame frame = new JFrame();
        prepareFrame(frame, size);
        for (int i = 0; i < times; i++) {
            workload.addCall(new GralTest(), new Object[]{frame, number_points});
        }
    }
    
    @Generator(description = "Generator to test the time of creating and showing of the raster plot in the JFrame.", genName = "Test generator")
    public void prepareDataPlotRaster(
            Workload workload,
            ServiceWorkload service,
            @ParamNum(description = "Size", min = 1, max = 300, step = 1) int number_points,
            @ParamNum(description = "Zoom", min = 0.1, max = 5, step = 0.2) double zoom,
            @ParamDesc("Frame size (resolution)") Size size) {

        int times = service.getNumberCalls();
        JFrame frame = new JFrame();
        prepareFrame(frame, size);
        for (int i = 0; i < times; i++) {
            workload.addCall(new GralTest(), new Object[]{frame, number_points, zoom});
        }
    }
    
    
    private void prepareFrame(JFrame frame, Size size) {
        switch (size) {
            case QVGA:
                frame.setSize(320, 240);
                break;
            case VGA:
                frame.setSize(640, 480);
                break;
            case SVGA:
                frame.setSize(800, 600);
                break;
            case XGA:
                frame.setSize(1024, 768);
                break;
            case HD720:
                frame.setSize(1280, 720);
                break;
            case HD1080:
                frame.setSize(1920, 1080);
                break;
        }
    }
}
