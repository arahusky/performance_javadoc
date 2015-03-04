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
package cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.sitehandlers;

import cz.cuni.mff.d3s.tools.perfdoc.server.MethodInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Jakub Naplava
 */
public class SiteHandlingUtils {

    /**
     * Encodes method (in database format), so that it can be easily passes as
     * an URL query Format: className&methodName&param1&param2&...&paramN
     *
     * @param method
     * @return
     */
    public static String getQueryURL(String method) {
        String result = method.replaceAll("#@", "&").replaceAll("#", "&").replaceAll("@", "&");

        return result;
    }

    /**
     * Inverse method to getQueryURL, decodes given query to MethodInfo instance
     *
     * @param query
     * @return as described; else if there is anything wrong null
     */
    public static MethodInfo getMethodFromQuery(String query) {
        String[] chunks = query.split("&");

        if (chunks.length < 2) {
            return null;
        }
        String className = chunks[0];
        String methodName = chunks[1];

        ArrayList<String> params = new ArrayList<>();

        for (int i = 2; i < chunks.length; i++) {
            params.add(chunks[i]);
        }

        return new MethodInfo(className, methodName, params);
    }
    
    /**
     * Chains the parameters in List.
     * @param parameters
     * @return chained parameters in format: param1,param2,...,paramN
     */
    public static String chainParameters(List<String> parameters) {
        StringBuilder sb = new StringBuilder();

        if (parameters == null || parameters.isEmpty()) {
            return "";
        }
        
        for (int i = 0; i < parameters.size() - 1; i++) {
            sb.append(parameters.get(i) + ",");
        }

        sb.append(parameters.get(parameters.size() - 1));
        return sb.toString();
    }
}
