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
package cz.cuni.mff.d3s.tools.perfdoc.doclets.formats.html.js;

import cz.cuni.mff.d3s.tools.perfdoc.doclets.internal.toolkit.Configuration;
import cz.cuni.mff.d3s.tools.perfdoc.doclets.internal.toolkit.util.DirectoryManager;
import cz.cuni.mff.d3s.tools.perfdoc.doclets.internal.toolkit.util.Util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

/**
 * Class to provide access to stored javascript files (in resource folder)
 *
 * @author Jakub Naplava
 */
public class JavascriptLoader {

    //the folder, where all javascript files are located
    private static final String jsFolderLocation = Util.RESOURCESDIR + "/js/";

    /**
     * Returns the content of the specified javascript file
     *
     * @param jsFileName the name of the requested javascript file
     * @return the content of requested file. If no file is found, or any error
     * occurs while reading it, then null is returned.
     * @throws java.io.IOException when the requested file was not found, or
     * some problem occurred during the reading
     */
    public static String getFileContent(String jsFileName) throws IOException {
        StringBuilder sb = new StringBuilder();

        //using getResourceAsStream called on Configuration ensures the correct folder location
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Configuration.class.getResourceAsStream(
                jsFolderLocation + DirectoryManager.URL_FILE_SEPARATOR + jsFileName)))) {
            int letter;
            while ((letter = reader.read()) != -1) {
                sb.append((char) letter);
            }
            return sb.toString();
        } catch (IOException | NullPointerException e) {
            throw new IOException("File" + jsFolderLocation + DirectoryManager.URL_FILE_SEPARATOR + jsFileName + "was not found.");
        }
    }
}
