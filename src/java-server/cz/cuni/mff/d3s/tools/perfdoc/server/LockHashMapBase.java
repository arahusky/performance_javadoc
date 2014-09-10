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

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of LockBase interface, that uses HashMap to store Locks under
 * given hashes
 *
 * @author Jakub Naplava
 */
public class LockHashMapBase implements LockBase {

    private final HashMap<String, Lock> lockBase = new HashMap<>();

    private static final Logger log = Logger.getLogger(LockHashMapBase.class.getName());

    private final Object privateLock = new Object();

    /**
     * {@inheritDoc} *
     */
    @Override
    public void waitUntilFree(String hash) {
        Lock o;
        synchronized (privateLock) {
            o = lockBase.get(hash);

            //if there is no object assigned to the hash, the new Lock will be created
            if (o == null) {
                o = new ReentrantLock();
                o.lock();
                lockBase.put(hash, o);
                log.log(Level.CONFIG, "The lock for \"{0}\" was created", hash);
                return;
            }
        }

        //if there is already object we must wait until it's free
        o.lock();
        log.log(Level.CONFIG, "The lock for \"{0}\" was locked", hash);
    }

    /**
     * {@inheritDoc} *
     */
    @Override
    public void freeLock(String hash) {
        //object already exists and I hold the lock
        //get the object from hashmap, free it and call notify() on it
        Lock o = lockBase.get(hash);
        o.unlock();
        log.log(Level.CONFIG, "The lock for \"{0}\" was unlocked", hash);
    }
}
