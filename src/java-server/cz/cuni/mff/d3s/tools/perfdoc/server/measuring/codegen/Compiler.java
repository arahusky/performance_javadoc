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
package cz.cuni.mff.d3s.tools.perfdoc.server.measuring.codegen;

import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.exception.CompileException;
import java.io.File;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Class, that provides simple API for compiling java source code with
 * additional class paths dependencies.
 *
 * Inspired by SPL Tool (http://sourceforge.net/p/spl-tools/code/ci/master/tree/src/java/cz/cuni/mff/spl)
 * 
 * @author Jakub Naplava
 */
public class Compiler {

    private static final Logger log = Logger.getLogger(Compiler.class.getName());

    /**
     * Obtains java compiler for compiling source code.
     *
     * @return System java compiler.
     * @throws CompileException, if system java compiler is not present,
     * probably not running on JDK but JRE.
     */
    private static JavaCompiler getCompiler() throws CompileException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new CompileException("Could not obtain system java compiler, probably running on JRE not JDK.");
        } else {
            return compiler;
        }
    }

    /**
     * Prepares options for compiling. Namely converts class paths to use-able
     * data structure.
     *
     * @return
     */
    private static Iterable<String> getOptions(Iterable<String> classPaths) {
        StringBuilder classPathBuilder = new StringBuilder();
        for (String classPath : classPaths) {
            classPathBuilder.append(File.pathSeparator + classPath);
        }

        List<String> options = new ArrayList<>();
        options.addAll(Arrays.asList("-cp", classPathBuilder.toString()));
        return options;
    }

    /**
     * Compiles java source located on source path with given class paths
     * dependencies.
     *
     * @param sourcePath
     * @param classPaths
     * @throws CompileException
     */
    public static void compile(String sourcePath, Iterable<String> classPaths) throws CompileException {

        JavaCompiler compiler = getCompiler();

        Locale defaultLocale = null;
        Charset defaultCharset = null;

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, defaultLocale, defaultCharset);
        Iterable<String> options = getOptions(classPaths);
        
        /*TODO check nejak moc tam toho je
        for (String s : options) {
            System.out.println(s);
        }*/
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(sourcePath));
        Writer defaultToStdErr = null;
        Iterable<String> noAnnotationClasses = null;

        JavaCompiler.CompilationTask task = compiler.getTask(defaultToStdErr, fileManager,
                diagnostics, options, noAnnotationClasses, compilationUnits);

        if (!task.call()) {
            for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
                log.log(Level.SEVERE, "Diagnostics:{0}", d.toString());
            }

            throw new CompileException("Failed to compile requested source code.");
        }
    }
}
