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

import cz.cuni.mff.d3s.tools.perfdoc.server.HttpMeasureServer;
import cz.cuni.mff.d3s.tools.perfdoc.server.MethodInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.MethodReflectionInfo;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.BenchmarkSetting;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.ClassParser;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.MeasurementQuality;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.MeasuringUtils;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.MethodArguments;
import cz.cuni.mff.d3s.tools.perfdoc.server.measuring.exception.CompileException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import org.apache.commons.lang.StringEscapeUtils;
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

    /**
     * The root of all directories mentioned below is the root of the packages
     * (e.g. jar folder or out/classes), which can be obtained using
     * HttpMeasureServer.getApplicationRootDir().
     */
    
    //directory, where templates are stored
    private static final String templateDir = "/cz/cuni/mff/d3s/tools/perfdoc/server/measuring/resources/";

    //directory, where directories containing java source files, arised by transforming templates, are stored
    private static String javaDestinationDir = "./";

    //directory, where directories, containing compiled source files are stored
    private static String compiledClassDestinationDir = "./";

    //name of template containing measured method call
    private static final String templateMethodName = "TMethod";

    //name of template containing generator
    private static final String templateGeneratorName = "TGenerator";

    //name of template containg measurement code
    private static final String templateMeasurementName = "TMeasurement";

    private final BenchmarkSetting setting;

    //name of directory, where the (for current BenchmarkSetting) generated code will be placed
    private final String directoryName;

    public CodeGenerator(BenchmarkSetting setting) {
        this.setting = setting;
        this.directoryName = getDirectoryName();
    }

    public static void setGeneratedCodeDirectory(String directory) {
        javaDestinationDir = directory;
        compiledClassDestinationDir = directory;
    }

    /**
     * Generates source code and compiles it.
     *
     * @throws CompileException when any error occurred during
     * @throws java.io.IOException
     */
    public void generate() throws CompileException, IOException {

        createOrReplaceDirectory();

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
    private void writeCode(VelocityContext vc, String templateName) throws IOException {

        //location, where the result (transformed template) will be stored
        String saveToLocation = javaDestinationDir + directoryName + "/" + templateName + ".java";
        File saveToFile = new File(saveToLocation);
        try {
            saveToFile.createNewFile();
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Unable to create new file for java source", ex);
            throw ex;
        }

        //template to be transformed
        InputStream template = getClass().getResourceAsStream(templateDir + templateName + ".vm");
        InputStreamReader input = new InputStreamReader(template);
        
        //try with-resources block handles closing (flushing)
        try (FileWriter fw = new FileWriter(new File(saveToLocation))) {
            Velocity.evaluate(vc, fw, "", input);
        } catch (IOException ex) {
            log.log(Level.SEVERE, "An error occurred while trying to save java source into a file " + saveToLocation, ex);
            throw ex;
        }
    }

    private void makeAndCompileMethodCode(BenchmarkSetting setting) throws CompileException, IOException {

        MethodReflectionInfo mrInfo = (MethodReflectionInfo) setting.getMeasuredMethod();
        Method measuredMethod = mrInfo.getMethod();

        VelocityContext context = new VelocityContext();

        context.put("mFunction", measuredMethod);
        context.put("mFunctionIsStatic", Modifier.isStatic(measuredMethod.getModifiers()));
        context.put("mClass", mrInfo.getContainingClass().getName());
        context.put("mFunctionIsNotVoid", !(measuredMethod.getReturnType().equals(Void.TYPE)));

        boolean hasMeasuredMethodFirstParamBlackhole = MeasuringUtils.hasMeasuredMethodBlackhole(measuredMethod);
        context.put("mFunctionHasBlackhole", hasMeasuredMethodFirstParamBlackhole);

        Class<?>[] measureParams = null;
        //if the measured method has first parameter Blackhole, we must remove this parameter from parameters that generator prepares, because it is the parameter that is prepared by harness
        if (hasMeasuredMethodFirstParamBlackhole) {
            Class<?>[] realParams = measuredMethod.getParameterTypes();
            measureParams = Arrays.copyOfRange(measuredMethod.getParameterTypes(), 1, realParams.length);
        } else {
            measureParams = measuredMethod.getParameterTypes();
        }
        context.put("mFunctionMeasureParams", measureParams);

        writeCode(context, templateMethodName);

        String javaSourceName = javaDestinationDir + directoryName + "/" + templateMethodName + ".java";
        String javaClassDirectory = compiledClassDestinationDir + directoryName;

        List<String> classPaths = getCompilationClassPaths();
        classPaths.add(javaClassDirectory);

        Compiler.compile(javaSourceName, classPaths);
    }

    private void makeAndCompileGeneratorCode(BenchmarkSetting setting) throws CompileException, IOException {

        MethodReflectionInfo mrInfo = (MethodReflectionInfo) setting.getGenerator();
        Method generator = mrInfo.getMethod();

        VelocityContext context = new VelocityContext();

        context.put("gFunction", generator);
        context.put("gFunctionIsStatic", Modifier.isStatic(generator.getModifiers()));
        context.put("gClass", mrInfo.getContainingClass().getName());

        context.put("gParameterType", generator.getParameterTypes());

        context.put("gArgument", setting.getGeneratorArguments().getValues());

        writeCode(context, templateGeneratorName);

        String javaSourceName = javaDestinationDir + directoryName + "/" + templateGeneratorName + ".java";
        String javaClassDirectory = compiledClassDestinationDir + directoryName;

        List<String> classPaths = getCompilationClassPaths();
        classPaths.add(javaClassDirectory);

        Compiler.compile(javaSourceName, classPaths);
    }

    private void makeAndCompileMeasurementCode(BenchmarkSetting setting) throws CompileException, IOException {

        MethodReflectionInfo mrInfo = (MethodReflectionInfo) setting.getMeasuredMethod();
        Method testedMethod = mrInfo.getMethod();
        MeasurementQuality measurementQuality = setting.getMeasurementQuality();

        VelocityContext context = new VelocityContext();

        context.put("priority", measurementQuality.getPriority());
        context.put("warmupTime", measurementQuality.getWarmupTime());
        context.put("warmupCycles", measurementQuality.getNumberOfWarmupMeasurements());
        context.put("measurementCycles", measurementQuality.getNumberOfMeasurements());
        context.put("measurementTime", measurementQuality.getMeasurementTime());

        String pathToMainDir = System.getProperty("user.dir");
        String pathToDir = pathToMainDir + File.separator
                + getDirectory().replaceAll("/", Matcher.quoteReplacement(File.separator)) + File.separator;
        context.put("directoryWhereToSaveResults", StringEscapeUtils.escapeJava(pathToDir));

        context.put("mClass", mrInfo.getContainingClass().getName());
        context.put("mFunctionIsStatic", Modifier.isStatic(testedMethod.getModifiers()));

        writeCode(context, templateMeasurementName);

        String javaClassDirectory = compiledClassDestinationDir + directoryName;
        String javaSourceName = javaDestinationDir + directoryName + "/" + templateMeasurementName + ".java";

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
        classPaths.add(HttpMeasureServer.getApplicationRootDir());

        return classPaths;
    }

    /**
     * Returns name of the directory, where the current generated code will be
     * placed.
     *
     * @return
     */
    private String getDirectoryName() {
        /*Every measurement is uniquely defined by the measured method, generator 
         and the arguments for the generator.*/
        MethodInfo method = setting.getMeasuredMethod();
        MethodInfo generator = setting.getGenerator();
        MethodArguments generatorArgs = setting.getGeneratorArguments();

        return ("measurement_method" + method.hashCode() + "generator" + generator.hashCode() + "args" + generatorArgs.hashCode());
    }

    /**
     * Creates new directory, where the generated files will be placed
     */
    private void createOrReplaceDirectory() {
        File file = new File(javaDestinationDir + directoryName);

        if (!file.exists()) {
            file.mkdir();
        } else {
            //TODO anyone other is performing measurement, we should wait (Monitor)
        }
    }

    /**
     * Returns path to the folder (relatively to the main folder) containing
     * generated code.
     */
    public String getDirectory() {
        return compiledClassDestinationDir + directoryName;
    }

    /**
     * Deletes all content, that was created for CodeGeneration.
     *
     * Namely the folder, where all generated content is placed.
     */
    public void deleteGeneratedContent() {
        File dir = new File(getDirectory());

        String[] entries = dir.list();
        for (String s : entries) {
            File currentFile = new File(dir.getPath(), s);
            currentFile.delete();
        }

        dir.delete();
    }
}
