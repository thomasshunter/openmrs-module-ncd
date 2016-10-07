/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.DateFormat;
import java.util.Date;

import org.openmrs.module.ncd.utilities.HL7Utilities;

import ca.uhn.hl7v2.protocol.Transportable;
import ca.uhn.hl7v2.protocol.impl.ClientSocketStreamSource;
import ca.uhn.hl7v2.protocol.impl.MLLPTransport;
import ca.uhn.hl7v2.protocol.impl.TransportableImpl;

/**
 * @author jlbrown
 *
 */
public class CDPClient
{
    private SocketAddress cdpAddress = null;
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT_NUM = 7093;
    private BufferedReader fileReader = null; 
    
    public static void main(String argv[]) throws Exception
    {
        if( argv.length == 0 )
        {
            System.out.print("Usage: CDPClient filename [host [port]]\n");
            System.out.print("   filename - a file of HL7 messages\n");
            System.out.print("   host - host name of the server to which the HL7 messages in the file will be sent." +
            		"The default if none is specified is localhost.\n");
            System.out.print("   port - the TCP port on which the server is listening." +
                    "The default if none is specified is 7093.\n");
        }
        else
        {
            String filename = argv[0];
            String host = DEFAULT_HOST;
            int port = DEFAULT_PORT_NUM;
            
            if( argv.length > 1 )
            {
                host = argv[1];                
            }
            
            if( argv.length > 2 )
            {
                port = Integer.parseInt(argv[2]);
            }
            
            CDPClient cdpClient = new CDPClient(host, port);
            cdpClient.sendHL7(filename);
        }
    }
    
    public CDPClient(String host, int port) throws Exception
    {        
        try
        {
            cdpAddress = new InetSocketAddress(host, port);
        }
        catch (Exception e)
        {
            System.out.println("Could not open socket.");
            throw e;
        }
    }
    
    public void sendHL7(String filename) throws Exception
    {        
        MLLPTransport transport = null;
        fileReader = new BufferedReader(new FileReader(filename));
        int msgCount = 1;
        try
        {
            ClientSocketStreamSource source = new ClientSocketStreamSource(cdpAddress);
            transport = new MLLPTransport(source);

            transport.connect();
            
            source.getSocket().setSoTimeout(60000);
            // read the message from the file
            String msg = HL7Utilities.readMessageFromFile(fileReader, true);
            while (msg != null)
            {
                // send the message
                transport.send(new TransportableImpl(msg));                
                System.out.println("Wrote message #" + msgCount + "\n");                
                
                // read the ack message
                Transportable response = transport.receive();
                System.out.println("Reponse to message #" + msgCount + ": " + response.getMessage() + "\n");
                
                // read the next message from the file
                msgCount++;
                msg = HL7Utilities.readMessageFromFile(fileReader, false);
            }            
        }
        catch (Exception e)
        {
            System.out.println("Error processing msg# " + msgCount +": " + e.getMessage() + "\n\n");
        }
        finally
        {
            if (transport != null)
            {
                transport.disconnect();
            }
            fileReader.close();
            Date now = new Date();
            DateFormat dateFormatter = DateFormat.getTimeInstance();
            System.out.println("Done sending HL7 messages at " + dateFormatter.format(now));
        }
    }
}
