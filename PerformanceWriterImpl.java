/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.cuni.tools.doclets.formats.html;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import cz.cuni.annotations.AnnotationParser;
import cz.cuni.annotations.Generator;
import cz.cuni.tools.doclets.formats.html.markup.HtmlAttr;
import cz.cuni.tools.doclets.formats.html.markup.HtmlStyle;
import cz.cuni.tools.doclets.formats.html.markup.HtmlTag;
import cz.cuni.tools.doclets.formats.html.markup.HtmlTree;
import cz.cuni.tools.doclets.formats.html.markup.RawHtml;
import cz.cuni.tools.doclets.internal.toolkit.Content;
import cz.cuni.tools.doclets.internal.toolkit.util.DocletConstants;
import java.io.IOException;
import java.lang.annotation.Annotation;

/**
 *
 * @author arahusky
 */
public class PerformanceWriterImpl {
    
    //writer for getting configuration information
    private final HtmlDocletWriter htmlWriter;
    
    //writer for writing the (JQuery) sliders
    private JSSliderWriter sliderWriter;
    
    //writer for writing (JQuery) control check (e.g. whether there's just one axis attribute)
    private JSWriter controlWriter;

    public PerformanceWriterImpl(HtmlDocletWriter htmlWriter) {
        this.htmlWriter = htmlWriter;
        
        try
        {
            sliderWriter = new JSSliderWriter("slider.js");
            controlWriter = new JSWriter("control.js");
        }
        catch (IOException e)
        {
            //TODO throw some exception
        }
    }
    
    /**
     * Method that prepares the title of Performance part
     * @return Performance output containing performance title
     */
     public PerformanceOutput returnTitleOutput() {
        PerformanceOutput result = new PerformanceOutputImpl(DocletConstants.NL + "<dt>" +
            "<span class=\"strong\">" + htmlWriter.configuration.getText("doclet.Performance_Title") +
            "</span>" + "</dt>" + "<dd>" + "</dd>");
        return result;
    }
     
     /**
      * Method to generate the performance body from one workload
      * @param workload the workload in format packageName.className#method
      * @return new PerformanceOutput that represents the body of generated performance code
      */
     public PerformanceOutput returnPerfoBody(String workload)
     {
         PerformanceOutput res = new PerformanceOutputImpl("");
         
         MethodDoc[] d = ClassParser.findMethods(workload);
         
         
         /*for (MethodDoc doc : d)
         {
             res.appendOutput(new PerformanceOutputImpl(doc.name()));
         }*/
         
                 
         //return res;
         
         return returnOnePerfoDiv(d[0]);
     }
     
     public PerformanceOutput returnPerfoBody(String[] workload)
     {
         return null;
     }     
     
     private PerformanceOutput returnOnePerfoDiv(MethodDoc doc)
     {
         //every div must have unique id - name of the method + number of parameters
         String id  = doc.name() + doc.parameters().length;
         
         //the main HtmlTree, that will contain two subtrees (left and right side), that represent the left part and right part of the performance look
         HtmlTree navList = new HtmlTree(HtmlTag.DIV);      
         navList.addAttr(HtmlAttr.CLASS, "wrapper");
         navList.addAttr(HtmlAttr.ID, id);
         
         HtmlTree leftSide = new HtmlTree(HtmlTag.DIV);
         leftSide.addAttr(HtmlAttr.CLASS, "left");
         addFormPart(leftSide, doc);
         
         HtmlTree rightSide = new HtmlTree(HtmlTag.DIV);
         rightSide.addAttr(HtmlAttr.CLASS, "right");
         rightSide.addContent("Here will be the image / table + checkbox to choose the values");
         
         navList.addContent(leftSide);
         navList.addContent(rightSide);
         
          PerformanceOutput p = new PerformanceOutputImpl(navList.toString());
          return p;
     }     
     
     private void addFormPart(Content content, MethodDoc doc)
     {
         AnnotationDesc[] annotations = doc.annotations();
                  
         Generator gen = getGenerator(annotations);
        
         //first part of the left tree is the generator description
         HtmlTree description = new HtmlTree(HtmlTag.P);
         description.addContent(gen.description());
         content.addContent(description);
         
         //then comes the string Configuration finally followed by all sliders/textboxes/...
         HtmlTree configuration = new HtmlTree(HtmlTag.P);
         configuration.addStyle(HtmlStyle.strong);
         configuration.addContent("Configuration:");
         content.addContent(configuration);
         
         Parameter[] param = doc.parameters();
         for (Parameter p : param)
         {
             addParameterPerfo(p, content);
         }
         
         //TODo generate submit button (needs to check, send data, start receiving and possibly also block itself)
     }
     
     /**
      * 
      * @param annotations The array of annotations that belong to one method
      * @return the first Generator annotation from the annotations
      */
     private Generator getGenerator(AnnotationDesc[] annotations) {
        for (AnnotationDesc annot : annotations) {
            if ("cz.cuni.annotations.Generator".equals(annot.annotationType().toString())) {
                final String description = AnnotationParser.getAnnotationValueString(annot, "cz.cuni.annotations.Generator.description()");
                final String genName = AnnotationParser.getAnnotationValueString(annot, "cz.cuni.annotations.Generator.genName()");

                return new Generator() {

                    @Override
                    public String description() {
                        return description;
                    }

                    @Override
                    public String genName() {
                        return genName;
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return Generator.class;
                    }
                };

            }
        }
         
         return null;
     }
     
     /**
      * Method that gets the parameter and content, and to the content adds the appropriate element allowing user to select the value
      * @param p the parameter of generator, whose performance part we are maintaining
      * @param content the content to that the performance will be added
      */
     private void addParameterPerfo(Parameter param, Content content)
     {
         AnnotationDesc[] annotations = param.annotations();
         
         if (annotations.length == 0)
         {
             //TODO must be Workload, or ServiceWorkload or some error
         }
         
         String description = null;
         
         for (AnnotationDesc annot : annotations)
         {
             switch(annot.annotationType().toString())
             {
                 case "cz.cuni.annotations.ParamDesc":
                     description = AnnotationParser.getAnnotationValueString(annot, "cz.cuni.annotations.ParamDesc.description()");
                     break;
                 case "cz.cuni.annotations.ParamNum":
                     description = AnnotationParser.getAnnotationValueString(annot, "cz.cuni.annotations.ParamNum.description()");
                     break;
                 default: System.out.println(annot.annotationType().toString());
             }
         }
         
         //according the type of the parameter we call appropriate method
         switch(param.typeName())
         {
             case "int":
             case "float":
             case "double":
                 addParameterNum(param, description, content);
                 break;
             case "String":
                 addParameterString(param, description, content);
                 break;
             case "enum":
                 addParameterEnum(param, description, content);
                 break;
             default:
                 //TODO some error
                 break;
         }         
     }
     
     
     private void addParameterNum(Parameter param, String description, Content content)
     {         
         AnnotationDesc[] annotations = param.annotations();
         double min = 0;
         double max = 0;
         double step = 0;
         boolean axis = true;
         
         //TODO check whether such annotation exists
         
         //TODO if axis, if not axis
         
         for (AnnotationDesc annot : annotations) {
             if (annot.annotationType().toString().equals("cz.cuni.annotations.ParamNum")) {
                 try {
                     min = AnnotationParser.getAnnotationValueDouble(annot, "cz.cuni.annotations.ParamNum.min()");
                     max = AnnotationParser.getAnnotationValueDouble(annot, "cz.cuni.annotations.ParamNum.max()");
                     
                     step = AnnotationParser.getAnnotationValueDouble(annot, "cz.cuni.annotations.ParamNum.step()");
                     //if there is no step specified, we will use default value, which is 1
                     step = (step == Double.MIN_VALUE)? 1 :step;
                     
                     axis = AnnotationParser.getAnnotationValueBoolean(annot, "cz.cuni.annotations.ParamNum.axis()");
                 } catch (NumberFormatException e) {
                     System.out.println(e);
                     //TODO
                 }
                 
                
                 break;
             }
         }         
      
        String uniqueSliderName = "";
        String uniqueTextboxName = "";
        
        //TODO why does not function without this and no throws IOException
        try
        {
        sliderWriter.addNewSlider(uniqueSliderName, uniqueTextboxName, min, max, step, axis);
        }
        catch (IOException e)
        {
            System.out.println("something");
        }
       
        
        content.addContent(new RawHtml("<p>" + "<label for=\"" + uniqueTextboxName + "\">" + description + ":   </label>"));
        content.addContent(new RawHtml("<input type=\"text\" id=\"" + uniqueTextboxName  + "\" style=\"border:0; color:#f6931f; font-weight:bold;\"> </p>"));
        content.addContent(new RawHtml("<div id=\"" + uniqueSliderName + "\" style=\"margin:10\"></div>")); 
     }
     
     private void addParameterString(Parameter param, String description, Content content)
     {
         String uniqueID = param.name();
         String input = "<p><label for=\"textValue5\">" + description + "</label>: <input type=\"text\" id=\"" + uniqueID + "\"> </p>";
         content.addContent(new RawHtml(input));
     }
     
     private void addParameterEnum(Parameter param, String description, Content content)
     {
         //TODO
     }
}
