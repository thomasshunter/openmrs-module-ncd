/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.nlp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author jlbrown
 *
 */
public class DummyNlpAnalyzer implements INlpAnalyzer
{
   
   public boolean analyze(String condition, String obxChunk)
   {
       BufferedWriter writer = null;
       try
       {
           writer = new BufferedWriter(new FileWriter("rex_test_values.txt", true));
           writer.append(condition);
           writer.append("-->");
           writer.append(obxChunk);
           writer.append("\r\n");
           writer.flush();
       }
       catch (IOException e)
       {
           if (writer != null)
           {
               try
               {
                   writer.close();
               }
               catch (IOException ex)
               {
                   // do nothing
               }
           }
               
       }
       //TODO this is a stub for the class that Jeff F. is working on.
       return false;
   }
}
