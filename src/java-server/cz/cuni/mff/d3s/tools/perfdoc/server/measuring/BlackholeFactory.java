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

import cz.cuni.mff.d3s.tools.perfdoc.blackhole.Blackhole;

/**
 * This class is the provider of Blackhole class.
 *
 * The main concern with Blackhole is that there should be as little of its
 * instance as possible (to make the minimal system perturbation), therefore we
 * decided to provide just one instance of it with singleton patter (Bill Pugh
 * Singleton Implementation).
 *
 * @author Jakub Naplava
 */
public class BlackholeFactory {

    private BlackholeFactory() {
    }

    /**
     * This class enables lazy loading.
     *
     * When the BlackholeFactory class is loaded, SingletonHelper class is not
     * yet loaded into memory and only if someone calls getInstance,
     * SingletonHelper gets loaded and creates new instance of Blackhole.
     */
    private static class SingletonHelper {

        private static final Blackhole INSTANCE = new Blackhole();
    }

    public static Blackhole getInstance() {
        return SingletonHelper.INSTANCE;
    }
}
