/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.cuni.mff.d3s.tools.perfdoc.doclets.formats.html;

import com.sun.tools.doclets.formats.html.markup.RawHtml;
import com.sun.tools.doclets.internal.toolkit.Content;
import java.io.IOException;

/**
 *
 * @author Jakub Naplava
 */
public class JSSliderWriter extends JSWriter{

    public JSSliderWriter(String filename) throws IOException {
        super(filename);
    }
    
    public void addNewSlider(String uniqueSliderName, String uniqueTextboxName, double minValue, double maxValue, double step, boolean axis, Content content)
    {
        String script = "<script> $(function() { $( \"#slider-range\" ).slider({ range: true, min:" + minValue + ", max: " + maxValue + ", step:" + step
                + ",slide: function( event, ui ) { if (ui.values[1] - ui.values[0] ==0) "
                + " { $( \"#amount\" ).val( ui.values[ 0 ]); } else { $( \"#amount\" ).val( ui.values[ 0 ] + \" - \" + ui.values[ 1 ] ); };	} });"
                + "$( \"#amount\" ).val( $( \"#slider-range\" ).slider( \"values\", 0 ) + \" - \" + $( \"#slider-range\" ).slider( \"values\", 1 ) ); }); </script>";

        script = script.replaceAll("slider-range", uniqueSliderName);
        script = script.replaceAll("amount", uniqueTextboxName);

        
        content.addContent(new RawHtml(script));
    }
    
}

