package HttpServer;

/**
 * Servicos e aplicacoes em Rede 
 * 2021/2022
 *
 * httpThread.java
 *
 * Class that handles client's requests.
 * It must handle HTTP GET, HEAD and POST client requests
 * INCOMPLETE VERSION
 *
 */

import HTTPFormat.*;
import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class httpThread extends Thread {
    
    HttpServer ServerGui;
    ServerSocket ss;
    Socket client;
    DateFormat httpformat;
    
    /** Creates a new instance of httpThread */
    public httpThread (HttpServer ServerGui, ServerSocket ss, Socket client) {
        this.ServerGui= ServerGui;
        this.ss= ss;
        this.client = client;
        httpformat= new SimpleDateFormat ("EE, d MMM yyyy HH:mm:ss zz", Locale.UK);
        httpformat.setTimeZone (TimeZone.getTimeZone ("GMT"));
        setPriority ( NORM_PRIORITY - 1 );
    }
    
    /** The type for unguessable files */
    String guessMime (String fn) {
        String lcname = fn.toLowerCase ();
        int extenStartsAt = lcname.lastIndexOf ('.');
        if (extenStartsAt<0) {
            if (fn.equalsIgnoreCase ("makefile"))
                return "text/plain";
            return "unknown/unknown";
        }
        String exten = lcname.substring (extenStartsAt);
        // System.out.println("Ext: "+exten);
        if (exten.equalsIgnoreCase (".htm"))
            return "text/html";
        else if (exten.equalsIgnoreCase (".html"))
            return "text/html";
        else if (exten.equalsIgnoreCase (".gif"))
            return "image/gif";
        else if (exten.equalsIgnoreCase (".jpg"))
            return "image/jpeg";
        else
            return "application/octet-stream";
    }
    

    public void Log (boolean in_window, String s) {
        if (in_window)
            ServerGui.Log ("" + client.getInetAddress ().getHostAddress () + ";"
                    + client.getPort () + "  " + s);
        else
            System.out.print ("" + client.getInetAddress ().getHostAddress () +
                    ";" + client.getPort () + "  " + s);
    }
    
        /** Loads a class code into the VM */
    private JavaRESTAPI start_API (String name) {
        name= name.substring (name.lastIndexOf (java.io.File.separatorChar)+1, name.length ());
        JavaRESTAPI api = null;
        try {
            Class apiClass= Class.forName ("HttpServer."+name);
            Object apiObject= apiClass.newInstance ();
            api= (JavaRESTAPI)apiObject;
        } catch (ClassNotFoundException e) {
            System.err.println ("API class not found:"+e);
        } catch (InstantiationException e) {
            System.err.println ("API class instantiation:"+e);
        } catch (IllegalAccessException e) {
            System.err.println ("API class access:"+e);
        }
        return api;
    }

    @Override
    public void run( ) {

        HTTPAnswer ans= null;   // HTTP answer object
        HTTPRequest receivedhttp = null; //HTTP request object
        PrintStream pout= null;

        try {
            InputStream in = client.getInputStream( );
            BufferedReader bin = new BufferedReader(
                    new InputStreamReader(in, "8859_1" ));
            OutputStream out = client.getOutputStream( );
            pout = new PrintStream(out, false, "8859_1");
            
            //create an object to store the http request
            receivedhttp= new HTTPRequest (ServerGui, client.getInetAddress ().getHostAddress () + ":"
                            + client.getPort (), ss.getLocalPort ());  
            boolean ok= receivedhttp.parse_Request (bin, true); //reads the input http request if everything was read ok it returnstrue

            // Prepares an answer object
            ans= new HTTPAnswer(ServerGui,
                client.getInetAddress ().getHostAddress () + ":" + client.getPort (),
                HttpServer.server_name+" - "+InetAddress.getLocalHost().getHostName ()+"-"+ServerGui.server.getLocalPort ());

            //API URL received 
            if (receivedhttp.url_txt.toLowerCase().endsWith ("api")) 
            {
                while ( receivedhttp.url_txt.startsWith ("/") )
                  receivedhttp.url_txt = receivedhttp.url_txt.substring (1);  ///remove "/" from url_txt
                JavaRESTAPI api= start_API (receivedhttp.url_txt);
                  if (api != null) {
                      try {
                          Log(true, "run JavaAPI\n");
                          api.doGet (client, receivedhttp.headers,receivedhttp.get_cookies(), ans);
                      } catch (Exception e) {
                          ans.set_error (HTTPReplyCode.BADREQ, receivedhttp.version);
                      }
                  } else
                      ans.set_error (HTTPReplyCode.NOTFOUND, receivedhttp.version);
            }else {
                // Get file with contents
                String filename= ServerGui.getRaizHtml() + receivedhttp.url_txt + (receivedhttp.url_txt.equals("/")?"index.htm":"");
                System.out.println("Filename= "+filename);
                File f= new File(filename);

                if (f.exists() && f.isFile()) {
                    // Define reply contents
                    ans.set_code(HTTPReplyCode.OK);
                    ans.set_version(receivedhttp.version);
                    ans.set_file_headers(new File(filename), guessMime(filename));
                    // NOTICE that only the first line of the reply is sent!
                    // No additional headers are defined!
                } else {
                    System.out.println( "File not found" );
                    ans.set_error(HTTPReplyCode.NOTFOUND, receivedhttp.version);
                    // NOTICE that some code is missing in HTTPAnswer!
                }
            }
            // Send reply
            ans.send_Answer(pout, true, true);

            in.close();
            pout.close();
            out.close();
        } catch ( IOException e ) {
            if (ServerGui.active())
                System.out.println( "I/O error " + e );
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                // Ignore
                System.out.println("Error closing client"+e);
            }
            ServerGui.thread_ended();
            Log(true, "Closed TCP connection\n");
        }
    }

}
