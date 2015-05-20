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

import javax.swing.JFrame;

/**
 * Resolution enum.
 * 
 * @author Jakub Naplava
 */
public enum Size {

    QVGA(320,240),
    VGA(640,480),
    SVGA(800,600),
    XGA(1024,768),
    HD720(1280,720),
    HD1080(1920,1080)
    ;
        
    private final int width;
    private final int height;
    
    Size(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    public void setSize(JFrame frame) {
        frame.setSize(width, height);
    }    
}
