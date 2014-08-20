package simplehtmldoclet;

import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamDesc;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.ParamNum;
import cz.cuni.mff.d3s.tools.perfdoc.annotations.Generator;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 *
 * @author arahusky
 */

public class HTMLGenerator {
    
    public HTMLGenerator(Method m)
    {
        this.m = m;
    }
    
    /**
     * Generator
     */
    private Method m;
    
    private int id = 0;
    
    
    public void generate(String name)
    {
        
            
    }
    
    private void GenerateRest(BufferedWriter out) throws IOException
    {
      
    }
    
    private void WriteInScriptSliderDouble(ParamNum param, BufferedWriter out, BufferedWriter jsout) throws IOException
    {
       
    }
    
    private void WriteInScriptString(ParamDesc param, BufferedWriter out, BufferedWriter jsout) throws IOException
    {
        out.write("<p>" + "<label for=\"textValue" + id + "\">" + param.description() + ":   </label>");
        out.write("<input type=\"text\" id=\"textValue" + (id++)  + "\"> </p>");
    }
    
    private void WriteInScriptEnum(ParamEnum param, BufferedWriter out, BufferedWriter jsout) throws IOException
    {
        out.write("<p>" + "<label for=\"textValue" + id + "\">" + param.description() + ":   </label>");
        
    }
    
    private void PrepareHeader(BufferedWriter out) throws IOException
    {
        out.write("<html>");
        out.write("<head>"
                + "<meta charset=\"utf-8\">\n" + "<title>jQuery UI Slider - Range slider</title>\n" +
                "  <link rel=\"stylesheet\" href=\"http://code.jquery.com/ui/1.10.4/themes/smoothness/jquery-ui.css\">\n" +
                "  <script src=\"http://code.jquery.com/jquery-1.10.2.js\"></script>\n" +
                "  <script src=\"http://code.jquery.com/ui/1.10.4/jquery-ui.js\"></script>\n" +
                "  <link rel=\"stylesheet\" href=\"http:/resources/demos/style.css\">\n" 
                + "</head>");
        out.write("<body>");
        out.write("<script src=\"jsscript.js\"></script>");
    }
    
    private void PrepareEnd(BufferedWriter out) throws IOException
    {
        out.write("</body>");
        out.write("</html>");
    }
    
    private BufferedWriter PrepareJS() throws IOException
    {        
        return new BufferedWriter(new FileWriter("jsscript.js"));
    }
    
    
}
