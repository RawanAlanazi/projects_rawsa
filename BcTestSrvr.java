/* BcTestSrvr.java: Backchannel Test Server
 * Copyright (C) 2009 Stephen W. Thompson
 *
 * This file is part of EventWeb.
 *
 *   EventWeb is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   EventWeb is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with EventWeb.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Revision History:
 *     Ver       Date  Change
 *   0.1.0   03/12/08  Initial version
 *   0.2.0   03/20/08  Remove EvwUtil dependency
 *   0.2.1   03/28/08  Improve exception handling
 *   0.3.0   03/29/08  Split off SimpleBcTestSrvr from BcTestSrvr
 *   0.3.1   04/02/08  Use Logger V1.4.0
 *   0.3.2   06/20/08  Add I/O tracing and a simple help facility
 *   0.3.3   09/27/08  Include port in Accepting connections message
 *   0.4.0   10/06/08  Serve HTML page in addition to providing service
 *   0.4.1   01/26/09  EvwSrvcInfo merged into SrvcInfo
 *   0.4.2   02/19/09  Change default service path from /Test to /EvwTest
 *   0.4.3   12/11/09  Use SrvcInfo V1.4.0
 *   0.4.4   12/22/09  Use HttpSrvc V1.2.0
 *   0.4.5   11/11/13  Exit after displaying help
 *   0.4.6   04/29/15  Use EvwConfigUtil
 *   0.4.7   06/10/15  Use generic types
 *   0.4.8   09/30/15  Pass parameters as TvList instead of string
 *
 * Description:
 *   An EventWeb server for verifying Backchannel operation
 */

package eventweb;


// Import required packages
import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Iterator;

import eventweb.io.Logger;
import eventweb.net.MimeType;
import eventweb.net.conn.ByteFileHttpSrvc;
import eventweb.net.conn.SimpleBcTestSrvc;
import eventweb.net.srvr.SrvcInfo;
import eventweb.util.EvwConfigUtil;
import eventweb.util.TvList;




/**
 * EventWeb Backchannel Test Server.
 * <p>
 * An EventWeb server for verifying Backchannel operation.
 * </p>
 * <p>
 * In addition to it's test function, this server was created as
 * a somewhat simplified example of EventWeb coding.
 * A lot of items that would normally be in separate
 * classes are all bundled together. Big and ugly, but hopefully
 * easier to follow if you want to see how the server works.
 * </p>
 * @author Stephen W. Thompson
 * @version 0.4.8 &nbsp; 09/30/15
 */
public class BcTestSrvr
       extends Thread
{
  /** Class version */
  public static final String VERSION = new String("0.4.8");
  /** Class dependencies */
  public static final String[] DEP_LIST = {
      "eventweb.io.Logger",
      "eventweb.net.MimeType",
      "eventweb.net.conn.ByteFileHttpSrvc",
      "eventweb.net.conn.SimpleBcTestSrvc",
      "eventweb.net.srvr.SrvcInfo",
      "eventweb.util.EvwConfigUtil",
      "eventweb.util.TvList" };
  /** Program description */
  public static final String PROG_DESC = "Backchannel Test Server";

  /** Default service port string */
  private static final String DEF_SRVC_PORT_STR = "80";
  /** Default service path */
  private static final String DEF_SRVC_PATH = "/EvwTest";
  /** Default page path for web browser page requests */
  private static final String DEF_PAGE_PATH = "/";
  /** Default path for html files */
  private static final String DEF_FILE_PATH = ".";
  /** Client connection timeout */
  private static final int LSTNR_TIMEOUT = 1800000;  // 30 minutes
  /** Print timestamp flag */
  private static final boolean PRT_TIMESTAMP = true;

  /** Service port as string */
  private static String srvcPortStr = DEF_SRVC_PORT_STR;
  /** Service path */
  private static String srvcPath = DEF_SRVC_PATH;
  /** Path for html files */
  private static String pagePath = DEF_PAGE_PATH;
  /** File path */
  private static String filePath = DEF_FILE_PATH;
  /** Service port as int */
  private static int srvcPort = 0;
  /** The logger */
  private static Logger log;
  /** Web page server object */
  private static ByteFileHttpSrvc pageSrvc;
  /** The test service object */
  private static SimpleBcTestSrvc testSrvc;
  /** Socket the server listens on */
  private static ServerSocket srvrSocket = null;
  /** Flag used when closing down the service */
  private static boolean closePort = false;
  /** I/O tracing flag */
  private static boolean tracingIo = false;
  /** Debugging flag */
  private static boolean debugging = false;



  //  ********  Public Class Methods  ********

  /**
   * Main entry point for the server.
   * @param args Command line arguments
   */
  public static void main(String[] args)
  {
    BcTestSrvr srvr;
    String logFileName = "";
    File logFile = null;
    PrintWriter outWriter = new PrintWriter(System.out, true);
    PrintWriter logDest = null;
    TvList clParams;
    String[] pair;
    Iterator<String[]> pitr;
    String badParm = null;
    boolean helpReq = false;

    // Process command line parameters
    clParams = EvwConfigUtil.parseCmdLine(args);
    pitr = clParams.iterator();
    while (pitr.hasNext())
    {
      pair = pitr.next();
      if (pair[0].startsWith("deb"))
        debugging = true;
      else if (pair[0].startsWith("nodeb"))
        debugging = false;
      else if (   pair[0].startsWith("tra")
               || pair[0].startsWith("iotra") )
        tracingIo = true;
      else if (   pair[0].startsWith("notra") 
               || pair[0].startsWith("noiotra") )
        tracingIo = false;
      else if (pair[0].startsWith("log"))
        logFileName = pair[1];
      else if (   pair[0].startsWith("port")
               || pair[0].startsWith("srvcpo") )
        srvcPortStr = pair[1];
      else if (pair[0].startsWith("srvcpa") )
        srvcPath = pair[1];
      else if (pair[0].startsWith("pagepa") )
        pagePath = pair[1];
      else if (pair[0].startsWith("filepa") )
        filePath = pair[1];
      else if (   pair[0].startsWith("h")
               || pair[0].startsWith("?") )
        helpReq = true;
      else
        badParm = pair[0];
    }

    // Set up logger
    if (logFileName.length() > 1)
    {
      // A log file was specified as a command line parameter,
      // send log output to the file
      logFile = new File(logFileName);
      try
      {
        if (!logFile.exists()) logFile.createNewFile();
        logDest = new PrintWriter(new FileWriter(logFile), true);
      }
      catch (IOException ex)
      {
        System.err.println(ex.toString());
        logDest = outWriter;
      }
    }
    else
    {
      // No log file has been specified,
      // send log output to the console
      logDest = outWriter;
    }
    // Get a logger and configure it
    log = Logger.getLogger(logDest,
                           outWriter,
                           new PrintWriter(System.err, true));
    log.setDebugging(debugging);
    log.setTracingIo(tracingIo);

    // Print welcome banner
    log.printConln();
    log.printConln(PROG_DESC + " V" + VERSION + " Starting.", PRT_TIMESTAMP);

    if (badParm != null)
    {
      log.println("Invalid command line parameter: " + badParm);
      helpReq = true;
    }
    if (helpReq)
    {
      // Write help information to the console
      log.printConln("Command line parameters:");
      log.printConln("  -port <port_number> = Set service port          (default "
                   + DEF_SRVC_PORT_STR + ")");
      log.printConln("  -srvcpath <service_path> = Set service path     (default "
                   + DEF_SRVC_PATH + ")");
      log.printConln("  -pagepath <page_path> = Set page request path   (default "
                   + DEF_PAGE_PATH + ")");
      log.printConln("  -filepath <file_path> = Set html file root path (default "
                   + DEF_FILE_PATH + ")");
      log.printConln("  -log <log_file_name> = Specify log file         (default "
                   + "console)");
      log.printConln("  -iotrace = Turn on I/O tracing                  (default "
                   + "false)");
      log.printConln("  -debug = Turn on debugging                      (default "
                   + "false)");
      log.printConln("  -help = List command line parameters");
      System.exit(0);
    }

    // Parse service port
    try { srvcPort = Integer.parseInt(srvcPortStr); }
    catch (NumberFormatException ex) { srvcPort = 0; }

    if (tracingIo || debugging)
    {
      // Write runtime configuration info to the log
      log.println("Runtime configuration:");
      log.println("  SrvcPort = " + srvcPort);
      log.println("  SrvcPath = " + srvcPath);
      log.println("  PagePath = " + pagePath);
      log.println("  FilePath = " + filePath);
      if (logFileName.length() > 1)
        log.println("  Log = " + logFileName);
      else
        log.println("  Log = System.out");
      log.println("  TracingIo = " + tracingIo);
      log.println("  Debugging = " + debugging);
      log.println("  Working directory = " + new File(".").getAbsolutePath());
    }

    // Validate runtime configuration
    if (srvcPort < 1)
    {
      log.printErrln("ERROR: Invalid SrvcPort: " + srvcPortStr);
      log.printConln(PROG_DESC + " Exiting");
      System.exit(500);
    }

    // Create service instances
    pageSrvc = new ByteFileHttpSrvc();
    pageSrvc.setDebugging(debugging);
    pageSrvc.init(null, new TvList("", filePath));
    testSrvc = new SimpleBcTestSrvc();
    testSrvc.setDebugging(debugging);

    // Create a socket to listen on
    try
    {
      srvrSocket = new ServerSocket(srvcPort);
      srvrSocket.setSoTimeout(LSTNR_TIMEOUT);
    }
    catch (BindException ex)
    {
      // This occurs when the port is already in use
      log.printErrln("ERROR: Server Socket port " + srvcPort + ": "
                   + ex.getMessage() );    
      log.printConln(PROG_DESC + " Exiting", PRT_TIMESTAMP);
      System.exit(500);
    }
    catch (IOException ex)
    {
      log.printErrln("ERROR: Server Socket failure:" + Logger.PNL + ex );
      log.printConln(PROG_DESC + " Exiting");
      System.exit(500);
    }

    // Start the server
    srvr = new BcTestSrvr();
    srvr.start();

    // This section allows this server to be stoped by the service,
    // without affecting how the service runs in a full scale server.
    synchronized(testSrvc)
    {
      try { testSrvc.wait(); }
      catch (InterruptedException ex) { /* do nothing */ }
    }
    log.printConln("Server closing per client request", PRT_TIMESTAMP);
    try
    {
      closePort = true;
      srvrSocket.close();
    }
    catch (IOException ex)
    {
      log.printErrln("IOException closing ServerSocket");
    }
  } //  main



  //  ********  Constructors  ********

  /**
   * Create a new server instance.
   */
  public BcTestSrvr()
  {
    super();
  } //  BcTestSrvr constructor



  //  ********  Public Instance Methods  ********

  /**
   * Run the server.
   */
  public void run()
  {
    InputStream in;
    OutputStream out;
    BufferedReader fromClient;
    PrintWriter toClient;
    SrvcInfo reqInfo;
    String cName;

    // Log server ready message
    log.printConln("HTML page service is at " + srvcPort + ":" + pagePath,
                   PRT_TIMESTAMP);
    log.printConln("Backchannel test service is at "
                 + srvcPort + ":" + srvcPath,
                   PRT_TIMESTAMP);
    log.println(Logger.SEPARATOR_LINE);
    if (debugging) log.println();

    // Spawn threads to handle connections
    try
    {
      while (!closePort)
      {
        try
        {
          // Listen for a client connection
          Socket clientSoc = srvrSocket.accept();
          if (debugging || tracingIo)
            log.printConln("New client: "
                         + clientSoc.getInetAddress().getHostName()
                         + ":" + clientSoc.getPort(),
                         PRT_TIMESTAMP );

          try
          {
            // Get reader & writer to talk to the client
            in = clientSoc.getInputStream();
            out = clientSoc.getOutputStream();
            cName = clientSoc.getInetAddress().getHostName()
                  + ":" + clientSoc.getPort();
            fromClient = log.getLoggingReader(
                new InputStreamReader(in), cName + "Fc");
            toClient = log.getLoggingWriter(
                new OutputStreamWriter(out), cName + "Fc");
            
            // Get first line of client request and parse it
            reqInfo = new SrvcInfo(fromClient, toClient);
            reqInfo.addFirstLine(fromClient.readLine());
            if (reqInfo.reqUrl == null)
              log.println(clientSoc.getInetAddress().getHostName()
                  + ":" + clientSoc.getPort()
                  + " FC connect.  ---n/a---", true );
            else
              log.println(clientSoc.getInetAddress().getHostName()
                  + ":" + clientSoc.getPort()
                  + " FC connect.  "
                  + reqInfo.reqUrl.toString(), true );
            // Make sure the first line was a valid HTTP request
            if (reqInfo.errorCode != 100)
            {
              if (debugging)
                log.println("Invalid request. Error " + reqInfo.errorCode
                          + ". Closing client socket.");
              toClient.close();
              fromClient.close();
              continue;
            }
            if (   reqInfo.reqUrl.getPath().startsWith(srvcPath)
                || reqInfo.reqUrl.getPath().startsWith(pagePath) )
            {
              // Get the rest of the client request and parse it
              addRqstHdrs(reqInfo, fromClient);
              
              // Perform the service
              serve(reqInfo, out);
            }
            else
            {
              log.println("Invalid URL path: " + reqInfo.reqUrl.getPath());
              reqInfo.errorCode = 404;
              sendRespHdr(reqInfo, toClient);
            }
            // Close connections
            toClient.close();
            fromClient.close();
            if (debugging)
              log.println(clientSoc.getInetAddress().getHostName()
                        + ":" + clientSoc.getPort()
                        + " FC connection closed by server");
          }
          catch (IOException ex)
          {
            log.println("Client FC connection Failed:" + Logger.PNL
                      + "  " + ex.toString());
          }
        }
        catch (SocketTimeoutException ex)
        {
          // Not a serious problem, log a message and go back to listening
          log.println("ServerSocket Exception: " + ex.getMessage(),
                      PRT_TIMESTAMP);
        }
        catch (InterruptedIOException ex)
        {
          // This should never happen, but if it does it's probably not
          // serious, so we log a message and go back to listening
          log.println("ServerSocket InterruptedIOException:" + Logger.PNL
                    + "  " + ex);
        }
      }
      // The server is exiting, close the server socket
      srvrSocket.close();
    }
    // (srvrSocket) IOException caught here stops the server
    catch (IOException ex)
    {
      if (!closePort)
        log.printErrln("srvrSocket IOException:"
                     + Logger.PNL + "  " + ex);
    }

    // Log server terminating message
    log.printConln(PROG_DESC + " Exiting", PRT_TIMESTAMP);
    System.exit(0);
  } //  run



  //  ********  Private Instance Methods  ********

  /**
   * Get the rest of the client request.
   * <br>
   * Reads the rest of the client request, parses it,
   * and adds the information to the request info object.
   * @param sRqst The service request information object
   * @param in Reader for the client request input stream
   */
  private void addRqstHdrs(SrvcInfo sRqst, BufferedReader in)
  {
    String line;
    int eCode = 0;
    int leCode;

    for (;;)
    {
      try
      {
        line = in.readLine();
      }
      catch (IOException ex)
      {
        sRqst.errorCode = 400;
        log.println("WARNING: Bad HTTP Request");
        return;
      }
      // A blank line terminates the HTTP request header
      if ((line == null) || (line.length() == 0)) break;
      leCode = sRqst.addHdrLine(line);
      // Save the most severe error code we get
      if (leCode > eCode) eCode = leCode;
    } // for
    sRqst.errorCode = eCode;
    return;
  } //  addRqstHdrs



  /**
   * Provide the service.
   * @param sRqst The service request information object
   * @param outWrtr PrintWriter for sending character data to the client.
   * @param outStrm OutputStream for sending binary data to the client.
   */
  private void serve(SrvcInfo sRqst,
//                     PrintWriter outWrtr,
                     OutputStream outStrm )
  {
    
    try
    {
      if (sRqst.errorCode >= 300)
        throw(new IOException("Invalid HTTP request: "
                            + sRqst.reqUrl.getPath() ));
      if (debugging)
        log.println("Serving " + sRqst.reqUrl.toString(), true);
      
      if (sRqst.reqUrl.getPath().startsWith(srvcPath))
      {
        // Prepare the service for the client request
        // This call fills sRqst with service specific info
        // for the HTTP response header
        if (testSrvc.prepareSrvc(sRqst) >= 300)
          throw(new IOException("Invalid HTTP request: "
                              + sRqst.reqUrl.getPath() ));

        // Send HTTP response header to client
        sendRespHdr(sRqst, sRqst.fcToClient);

        if (!sRqst.reqMethod.equals("HEAD"))
        {
          // Perform the actual service
          // (Splitting the service invocation into prepareSrvc and
          //  performSrvc allows the service to be invoked by either
          //  a stand-alone server, or by a servlet.)
          if (testSrvc.performSrvc(sRqst, outStrm) >= 300)
            log.println("Service delivery failure");
        }
      }
      else
      {
        if (pageSrvc.prepareSrvc(sRqst) >= 300)
          throw(new IOException("Invalid HTTP request: "
                              + sRqst.reqUrl.getPath() ));

        // Send HTTP response header to client
        sendRespHdr(sRqst, sRqst.fcToClient);

        if (!sRqst.reqMethod.equals("HEAD"))
        {
          if (pageSrvc.performSrvc(sRqst, outStrm) >= 300)
            log.println("Service delivery failure");
        }        
      }
    }
    catch (IOException ex)
    {
      // Handle errors
      log.println("WARNING: " + ex.getMessage());
      sendRespHdr(sRqst, sRqst.fcToClient);
      sRqst.fcToClient.close();
    }

    if (debugging || (sRqst.errorCode != 200))
      log.println("BcTestSrvr.serve exiting. Status = "
                  + sRqst.errorCode, true );
  } //  serve



  /**
   * Send HTTP response header to the client
   * @param sRqst The service request information object
   * @param outWrtr PrintWriter for sending character data to the client
   */
  private void sendRespHdr(SrvcInfo sRqst, PrintWriter outWrtr)
  {
    String[] respHdr;

    // Send HTTP response header to client
    if (sRqst.errorCode < 400)
    {
      // Send initial HTTP status line
      if (sRqst.errorCode == 302)
        outWrtr.println("HTTP/1.0 " + sRqst.errorCode + " FOUND");
      else
        outWrtr.println("HTTP/1.0 " + sRqst.errorCode + " OK");
      // Send response header lines
      outWrtr.println("Server: EventWeb Server");
      outWrtr.println("Content-Type: "
                    + MimeType.mimeTypeString(sRqst.respMediaId));
      if (sRqst.rsrcLen != 0L)
        outWrtr.println("Content-Length: " + String.valueOf(sRqst.rsrcLen));
      while (!sRqst.custRespHdrs.isEmpty())
      {
        respHdr = sRqst.custRespHdrs.removeFirst();
        outWrtr.println(respHdr[0] + ": " + respHdr[1]);
      }
    }
    else
    {
      // Send HTTP status line indicating the error
      switch (sRqst.errorCode)
      {
        case 404:
          outWrtr.println("HTTP/1.0 " + sRqst.errorCode + " NOT FOUND");
          break;
        case 406:
          outWrtr.println("HTTP/1.0 " + sRqst.errorCode + " NOT ACCEPTABLE");
          break;
        case 500:
          outWrtr.println("HTTP/1.0 " + sRqst.errorCode + " SERVER ERROR");
          break;
        default:
          outWrtr.println("HTTP/1.0 " + sRqst.errorCode + " ERROR");
          break;
      }
    }
    // Blank line ends the HTTP header.
    outWrtr.println();
    outWrtr.flush();
  } //  sendRespHdr



 } //  class BcTestSrvr




// End of BcTestSrvr.java
