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
package cz.cuni.mff.d3s.tools.perfdoc.doclets.formats.html;

/**
 * Class that contains all doclet specific arguments.
 *
 * To create new doclet specific argument, see ConfigurationImpl: int
 * optionLength(String option)
 *
 * @author Jakub Naplava
 */
public class DocletArguments {

    //location, where the server will be contacted
    private static String serverAddress = "http://localhost:4040";
    
    //classpath of all workloads (or connected classes)
    private static String[] workloadPath;

    public static void setArguments(String[][] options) {
        for (String[] s : options) {
            switch (s[0].toLowerCase()) {
                case "-serveraddress":                
                    serverAddress = s[1];
                    break;
                case "-workloadpath":
                    setWorkloadPath(s[1]);
                    break;
            }
        }
    }
    
    private static void setWorkloadPath(String workloadPath) {
        DocletArguments.workloadPath = workloadPath.split(";");
    }

    public static String getServerAddress() {
        return serverAddress;
    }

    public static String[] getWorkloadPath() {
        return workloadPath;
    }
}
