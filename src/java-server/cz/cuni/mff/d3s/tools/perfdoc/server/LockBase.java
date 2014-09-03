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
package cz.cuni.mff.d3s.tools.perfdoc.server;

/**
 * Data structure, that holds the base of given hashes and for every of them
 * holds a lock, which is either locked or free
 *
 * @author Jakub Naplava
 */
public interface LockBase {

    /**
     * Waits until the lock that belongs to given hash is free and than locks it
     * for self
     *
     * @param hash
     */
    void waitUntilFree(String hash);

    /**
     * Releases lock for given hash
     *
     * @param hash
     */
    void freeLock(String hash);
}
