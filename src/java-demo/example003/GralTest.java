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

import cz.cuni.mff.d3s.tools.perfdoc.annotations.Workload;
import de.erichseifert.gral.examples.rasterplot.SimpleRasterPlot;
import de.erichseifert.gral.examples.xyplot.ScatterPlot;
import javax.swing.JFrame;

/**
 * This class contains method to measure the execution time of creating and
 * plotting of two simple plots provided by Gral library.
 *
 * @author Jakub Naplava
 */
public class GralTest {

    /**
     * Creates new ScatterPlot with given amount of points and shows it in the
     * given frame.
     *
     * @param frame JFrame, where the ScatterPlot will be displayed
     * @param howManyPoint how many points will be in the graph
     */
    @Workload("example003.GralTestGenerator#prepareDataPlotScatter")
    public static void plotScatterPlot(JFrame frame, int howManyPoint) {
        ScatterPlot splot = new ScatterPlot(howManyPoint);
        frame.getContentPane().add(splot);
    }

    /**
     * Creates new RasterPlot with given size and zoom and shows it in the given
     * frame
     *
     * @param frame JFrame, where the ScatterPlot will be displayed
     * @param size
     * @param zoom
     */
    @Workload("example003.GralTestGenerator#prepareDataPlotRaster")
    public static void plotSimpleRasterPlot(JFrame frame, int size, double zoom) {
        SimpleRasterPlot raster = new SimpleRasterPlot(size, zoom);
        frame.getContentPane().add(raster);
    }
}
