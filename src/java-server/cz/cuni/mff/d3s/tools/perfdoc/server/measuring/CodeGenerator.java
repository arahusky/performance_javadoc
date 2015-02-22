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

import cz.cuni.mff.d3s.tools.perfdoc.server.MethodReflectionInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.exception.CompileException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * This class generates and compiles code based on templates. Code generation is
 * based on Apache Velocity.
 *
 * There are three basic templates, one for a call of generator (TGenerator),
 * one for a call of measured method (TMethod) and finally the one, that
 * performs measurement (TMeasurement)
 *
 * @author Jakub Naplava
 */
public class CodeGenerator {

    private static final Logger log = Logger.getLogger(CodeGenerator.class.getName());

    //directory, where templates are stored
    private static final String templateDir = "src/java-server/cz/cuni/mff/d3s/tools/perfdoc/server/measuring/resources/";

    //directory, where directories containing java source files, arised by transforming templates, are stored
    private static final String javaSourceDir = "src/java-server/cz/cuni/mff/d3s/tools/perfdoc/server/measuring/resources/";

    //directory, where directories, containing compiled source files are stored
    private static final String compiledClassDir = "src/java-server/cz/cuni/mff/d3s/tools/perfdoc/server/measuring/resources/";

    //root directory for workloads (Workload, WorkloadImpl, ServiceWorkload, ServiceWorkloadImpl)
    private static final String workloadsRootDir = "out/classes/java-server";

    //name of template containing measured method call
    private static final String templateMethodName = "TMethod";

    //name of template containing generator
    private static final String templateGeneratorName = "TGenerator";

    //name of template containg measurement code
    private static final String templateMeasurementName = "TMeasurement";

    private final BenchmarkSetting setting;

    static {
        //initiating Velocity engine
        try {
            Velocity.init();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Unable to start Velocity.", e);
        }
    }

    public CodeGenerator(BenchmarkSetting setting) {
        this.setting = setting;
    }

    /**
     * Generates source code and compiles it.
     *
     * @throws CompileException when any error occurred during
     */
    public void generate() throws CompileException, IOException {
        makeAndCompileGeneratorCode(setting);
        makeAndCompileMethodCode(setting);
        makeAndCompileMeasurementCode(setting);
    }

    /**
     * Processes transformation of template according to VelocityContext and
     * writes the result into new file.
     *
     * @param vc VelocityContext containing the replacements
     * @param templateName the name of template to be transformed
     * @param directoryName the directory, where the file will be saved
     */
    private void writeCode(VelocityContext vc, String templateName, String directoryName) throws IOException {

        //location, where the result (transformed template) will be stored
        String saveToLocation = javaSourceDir + directoryName + "/" + templateName + ".java";
        File saveToFile = new File(saveToLocation);
        try {
            saveToFile.createNewFile();
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Unable to create new file for java source", ex);
            throw ex;
        }

        //location of the template to be transformed
        String template = templateDir + templateName + ".vm";

        //try with-resources block handles closing (flushing)
        try (FileWriter fw = new FileWriter(new File(saveToLocation))) {
            Velocity.mergeTemplate(template, "UTF-8", vc, fw);
        } catch (IOException ex) {
            log.log(Level.SEVERE, "An error occurred while trying to save java source into a file " + saveToLocation, ex);
            throw ex;
        }
    }

    private void makeAndCompileMethodCode(BenchmarkSetting setting) throws CompileException, IOException {

        MethodReflectionInfo mrInfo = (MethodReflectionInfo) setting.getTestedMethod();
        Method testedMethod = mrInfo.getMethod();

        VelocityContext context = new VelocityContext();

        context.put("mFunction", testedMethod);
        context.put("mFunctionIsStatic", Modifier.isStatic(testedMethod.getModifiers()));
        context.put("mClass", mrInfo.getContainingClass().getName());

        writeCode(context, templateMethodName, "directory");

        String javaSourceName = javaSourceDir + "directory" + "/" + templateMethodName + ".java";
        String javaClassDirectory = compiledClassDir + "directory";

        List<String> classPaths = getCompilationClassPaths();
        classPaths.add(javaClassDirectory);

        Compiler.compile(javaSourceName, classPaths);
    }

    private void makeAndCompileGeneratorCode(BenchmarkSetting setting) throws CompileException, IOException {

        MethodReflectionInfo mrInfo = (MethodReflectionInfo) setting.getWorkload();
        Method testedMethod = mrInfo.getMethod();

        VelocityContext context = new VelocityContext();

        context.put("gFunction", testedMethod);
        context.put("gFunctionIsStatic", Modifier.isStatic(testedMethod.getModifiers()));
        context.put("gClass", mrInfo.getContainingClass().getName());

        //TODO if enum - need to prefix with full name + dot
        context.put("gArgument", setting.getWorkloadArguments().getValues());

        writeCode(context, templateGeneratorName, "directory");

        String javaSourceName = javaSourceDir + "directory" + "/" + templateGeneratorName + ".java";
        String javaClassDirectory = compiledClassDir + "directory";

        List<String> classPaths = getCompilationClassPaths();
        classPaths.add(javaClassDirectory);

        Compiler.compile(javaSourceName, classPaths);
    }

    private void makeAndCompileMeasurementCode(BenchmarkSetting setting) throws CompileException, IOException {

        MethodReflectionInfo mrInfo = (MethodReflectionInfo) setting.getTestedMethod();
        Method testedMethod = mrInfo.getMethod();

        VelocityContext context = new VelocityContext();

        //TODO
        context.put("propertyWarmupCount", 10);
        context.put("propertyCallsCount", 40);
        context.put("propertyPriority", 1);

        context.put("mClass", mrInfo.getContainingClass().getName());

        writeCode(context, templateMeasurementName, "directory");

        String javaClassDirectory = compiledClassDir + "directory";
        String javaSourceName = javaSourceDir + "directory" + "/" + templateMeasurementName + ".java";

        List<String> classPaths = getCompilationClassPaths();
        classPaths.add(javaClassDirectory);

        Compiler.compile(javaSourceName, classPaths);
    }

    /**
     * Returns class-paths containing all possible class-path dependencies for
     * compiling source codes.
     *
     * @return
     */
    private static List<String> getCompilationClassPaths() {
        List<String> classPaths = ClassParser.getClassPaths();
        classPaths.add(workloadsRootDir);

        return classPaths;
    }

    /**
     * Returns directory containing generated code.
     *
     * @return
     */
    public static String getDirectory() {
        return "";
    }
}
