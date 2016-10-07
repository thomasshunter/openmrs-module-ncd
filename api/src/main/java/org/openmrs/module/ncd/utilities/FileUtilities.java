/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Class providing useful utilities in the way of file interaction.
 */
public class FileUtilities
{
   private static final int BLOCK_SIZE = 1024;
   
   private FileUtilities()
   {
   }
   
   public static String readFileFromClasspathToString( Class<?> requestingClass, String fileName ) throws IOException
   {
      InputStream stream = requestingClass.getClassLoader().getResourceAsStream( fileName );
      
      if( stream == null )
      {
         throw new IOException( "Resource not found: " + fileName );
      }
      
      try
      {
         return readContentToString( new InputStreamReader( stream ) );
      }
      finally
      {
         stream.close();
      }
   }
   
   /**
    * Reads the content from a Reader to a single String.
    * Note: This method automatically closes the specified Reader.
    * @param r The reader whose content will be read.
    * @return The String content behind the specified Reader.
    * @throws IOException
    */
   public static String readContentToString( Reader r ) throws IOException
   {
      try
      {
         StringBuffer sb = new StringBuffer( );
         char[] b = new char[ BLOCK_SIZE ];
         int n;
         
         // Read one block at a time
         while( ( n = r.read( b ) ) > 0 )
         {
            sb.append( b, 0, n );
         }
         
         return sb.toString( );
      }
      finally
      {
         r.close();
      }
   }
}
