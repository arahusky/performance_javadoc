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

import com.sun.net.httpserver.HttpExchange;
import cz.cuni.mff.d3s.tools.perfdoc.server.HttpExchangeUtils;
import cz.cuni.mff.d3s.tools.perfdoc.server.MethodInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.cache.html.ResultCacheForWeb;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Site handler that shows all measured methods for the given class
 *
 * @author Jakub Naplava
 */
public class ClassSiteHandler extends AbstractSiteHandler {

    private static final Logger log = Logger.getLogger(ClassSiteHandler.class.getName());

    @Override
    public void handle(HttpExchange exchange, ResultCacheForWeb res) {
        log.log(Level.INFO, "Got new class-site request. Starting to handle it.");

        //the requested class (format: package.className)
        String className = exchange.getRequestURI().getQuery();

        if (res != null) {
            //the methods of the class that have been measured 
            Collection<MethodInfo> testedMethods = res.getDistinctClassMethods(className);

            addCode(returnHeading(className));
            String classOutput = formatMethods(testedMethods);
            addCode(classOutput);
            String output = getCode();

            HttpExchangeUtils.sentSuccesHeaderAndBodyAndClose(exchange, output.getBytes(), log);
        } else {
            //there is no database connection available
            //sending information about internal server error
            HttpExchangeUtils.sentErrorHeaderAndClose(exchange, "Database not available.", 500, log);
        }

        log.log(Level.INFO, "Data were succesfully sent to the user.");
    }

    private String returnHeading(String className) {
        StringBuilder sb = new StringBuilder();

        sb.append("<p><a href = \"http://localhost:8080/cache\"><-- Back to classes overview </a></p>");
        sb.append("<h1>Class <i>").append(className).append("</i></h1>");
        sb.append("<h2>Methods</h2>");

        return sb.toString();
    }

    private String formatMethods(Collection<MethodInfo> methods) {

        //unable to retrieve data from database
        if (methods == null) {
            return "<p>Sorry, but there was an error when trying to connect to database.</p>";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("<ul>");
        for (MethodInfo method : methods) {
            String methodInfo = formatMethod(method);
            String url = getQueryURL(method.toString());
            sb.append("<li><a href= \"method?" + url + "\">" + methodInfo + "</a></li>");
        }
        sb.append("</ul>");

        return sb.toString();
    }

    private String formatMethod(MethodInfo method) {
        if (method.getParams().isEmpty()) {
            return method.getMethodName() + "()";
        }
        String methodName = method.getMethodName();        
        
        StringBuilder parameterInfo = new StringBuilder();
        List<String> paramNames = method.getParams();
        for (int i = 0; i<paramNames.size() - 1; i++) {
            parameterInfo.append(paramNames.get(i)).append(",");
        }
        parameterInfo.append(paramNames.get(paramNames.size() - 1));
        return (methodName + "(" + parameterInfo + ")");
    }
}
