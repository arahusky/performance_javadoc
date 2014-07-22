/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.cuni.mff.d3s.tools.perfdoc.doclets.formats.html;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author Jakub Naplava
 */
public class JSWriter {
    //static variable to indicate, whether the file is already opened
    static boolean isOpened;
    private PrintWriter writer;
    public JSWriter(String filename) throws IOException {
        
        if (!isOpened) {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
            startFile();
        } else {
            //open the PrintWriter in append-mode in case, that the file was not opened by out code yet
            writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
        }

        isOpened = true;
    }
    
    private void startFile() throws IOException
    {
        writer.println("<script>");
    }
    
    private void endFile() throws IOException
    {
        writer.println("<script>");
        writer.close();
    }
    
    protected void write(String input) throws IOException
    {
        writer.write(input);
    }
}

