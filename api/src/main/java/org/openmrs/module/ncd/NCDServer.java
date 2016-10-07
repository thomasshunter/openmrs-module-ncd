package org.openmrs.module.ncd;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.module.ncd.events.IStopEventListener;
import org.openmrs.module.ncd.events.PropertyEventHandler;
import org.openmrs.module.ncd.events.StopEvent;
import org.openmrs.module.ncd.utilities.NCDUtilities;

public class NCDServer implements PropertyEventHandler, Runnable {
	private static Log logger = LogFactory.getLog(NCDServer.class);
	
	final private static int EXIT_WAIT_TIME = 250;
    final public static int STOP_WAIT_TIME = 30000;   // wait no more than 30 seconds to shut down	
	final private static int CONN_WAIT_TIME = 30000;
	
	private static int port = 7093;	
	
	private boolean shouldShutdown = false;
	private int currentMessagesInProcessing = 0;	
	private static ArrayList<IStopEventListener> stopListeners = new ArrayList<IStopEventListener>();
	private ArrayList<NCDConnection> connections = new ArrayList<NCDConnection>();
	private ServerSocket serverSocket = null;
	
	public NCDServer() {
		logger.debug("Construct new NCDServer instance.");
		// Initialize member port and listen for changes to the global property
        NCDUtilities.addPropertyEventHandler("ncd.listenerPort", this);        
	}		
	
	public static void addStopEventListener(IStopEventListener stopEventListener)
    {
        stopListeners.add(stopEventListener);
    }
    
    public static void removeStopEventListener(IStopEventListener stopEventListener)
    {
        stopListeners.remove(stopEventListener);
    }
    
    public synchronized void removeConnection(NCDConnection conn) {
    	connections.remove(conn);
    }
    
    private synchronized void fireStopEvent()
    {
        StopEvent stopEvent = new StopEvent(this);        
        for (IStopEventListener listener : stopListeners)
        {
            listener.handleStopEvent(stopEvent);
        }
    }
    
    public void incrementMessagesInProcessing() {
    	currentMessagesInProcessing++;
    }
    
    public void decrementMessageInProcessing() {
    	currentMessagesInProcessing--;
    }
	
	/**
     * Starts the HL7 Server.  If one is already started, it will be stopped and
     * a new one will be started.
     *
     */
    public void startServer()
    {
        stopServer(STOP_WAIT_TIME);        
        
        logger.debug("Starting NCD on port " + port + "...");
        
        synchronized(connections) {
        	shouldShutdown = false;
        }
        currentMessagesInProcessing = 0;
        
        Thread newThread = new Thread(this);
        newThread.start();
    }
    
    public void run() {
		try {
			serverSocket = new ServerSocket();
			serverSocket.setReuseAddress(true);
			serverSocket.bind(new InetSocketAddress(port));
            serverSocket.setSoTimeout(CONN_WAIT_TIME);
			logger.info("NCD Server started...");			
			while (!shouldShutdown) {
				try {
					logger.debug("Waiting for connection...");
					Socket socket = serverSocket.accept();
					logger.debug("Connection established with " + 
							socket.getInetAddress().getHostAddress() + " on port " + 
							socket.getPort() + ".");
					NCDConnection newConnection = new NCDConnection(this, socket);					
					connections.add(newConnection);
					logger.debug("There are now " + connections.size() + " connections active.");
					Thread newThread = new Thread(newConnection);
					newThread.start();					
				} catch (InterruptedIOException ie) {
					// ignore - just timed out waiting for connection
				} catch (Exception e) {
					// log the connection and try to accept a connection again.
					logger.error("Exception while waiting for a connection: ", e);
				}
			}
			logger.debug("closing server socket.");
			try {
				serverSocket.close();
				serverSocket = null;
			} catch (IOException e) {
			}
		} catch (Exception e) {
			logger.error("Exception setting up connection mechanism: ", e);
		}
		
	} 
    
    /**
     * Attempts to gracefully stop the server by allowing existing requests
     * to finish processing.
     * 
     * @param maxWaitTime - maximum time, in milliseconds, to wait for requests to finish
     */
    public void stopServer(int maxWaitTime)
    {
        logger.info("Stopping server...");

		boolean canExit = false;
		int waitCount = 0;	

		synchronized (connections) // we need some object to synchronize on
		{
			shouldShutdown = true;
		}

		// wait for current requests to be processed
		while (!canExit) {
			synchronized (connections) // we need some object to synchronize on
			{
				canExit = (currentMessagesInProcessing < 1);
			}

			if (!canExit && (waitCount < (maxWaitTime / EXIT_WAIT_TIME))) {
				try {
					Thread.sleep(EXIT_WAIT_TIME);
				} catch (InterruptedException e) {
					// just check again
				}

				++waitCount;
			} else {
				canExit = true;
			}
			
			for(NCDConnection conn : connections) {
				conn.endConnection();				
			}	
        }								
		logger.info("Server stopped.");
		fireStopEvent();
		connections.clear();
    }        
	
	 /** Called by OpenMRS when the value of the property registered in the
     * constructor (the server port) is changed.
     */
    public void propertyRegistered(GlobalProperty arg0) {
        
        try {
            int temp = Integer.parseInt(arg0.getPropertyValue());
            port = temp;
        }
        catch (Throwable t) {
            logger.error("invalid value for global property \"" + arg0.getProperty() + "\", expected a decimal integer. Using " + port + " instead.", t);
        }
    }
	
	/** Called by OpenMRS when the value of the property registered in the
     * constructor (the server port) is changed.
     */
    public void propertyChanged(GlobalProperty arg0) {
        
        int temp = port;
        propertyRegistered(arg0);
        
        if (temp != port) {

            try {

                // We can only change the listening port by stopping and starting
                // the server.
                // NOTE: startServer first stops the existing server.
                startServer();
            }
            catch (Throwable t) {

                logger.error("error restarting service to change ports: " + t.getMessage(), t);
            }
        }
    }
}
