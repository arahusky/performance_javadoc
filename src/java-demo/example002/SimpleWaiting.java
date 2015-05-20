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
package example002;

import cz.cuni.mff.d3s.tools.perfdoc.annotations.Workload;
import java.util.Random;

/**
 * Simple class to demonstrate the functionality of measurer and generators. It
 * shows the possibility of re-using one generator multiple times.
 *
 * @author Jakub Naplava
 */
public class SimpleWaiting {

    /**
     * Waits (first)*10 ms
     *
     * @param first
     * @throws java.lang.InterruptedException
     */
    @Workload("example002.Generators#prepareData1")
    public static void simpleWait(int first) throws InterruptedException {

        for (int i = 0; i < first; i++) {
            Thread.sleep(10);
        }
    }

    /**
     * Waits a random piece of time, that increases with param length
     *
     * @param first
     * @throws java.lang.InterruptedException
     */
    @Workload("example002.Generators#prepareData1")
    public void randomWait(int first) throws InterruptedException {
        for (int i = 0; i < first; i++) {
            Random r = new Random();
            Thread.sleep(r.nextInt(10));
        }
    }
}
