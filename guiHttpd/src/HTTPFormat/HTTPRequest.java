package HTTPFormat;

/**
 * Servicos e Aplicacoes em Redes
 * 2021/2022
 *
 * HTTPQuery.java
 *
 * Class that stores all information about a HTTP request
 * Incomplete Version
 *
 */

import HttpServer.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Properties;
import java.util.StringTokenizer;


public class HTTPRequest {
    public String method;   // stores the HTTP Method of the request
    public String url_txt;  // stores the url of the request
    public String version;  // stores the HTTP version of the request
    public Headers headers; // stores the HTTP headers of the request
    public Properties cookies; //stores cookies received in the Cookie Headers
    private final Log log;  // log object
    public String text;     //store possible contents in an HTTP request (for example POST contents)
    private final String id_str;    // id_string for logging purposes
    public int local_port;  // local HTTP server port
    
    
    /** 
     * Creates a new instance of HTTPQuery
     * @param log   log object
     * @param id    log id
     * @param local_port local HTTP server port
     */
    public HTTPRequest (Log log, String id, int local_port) {
        // initializes everything to null
        this.headers= new Headers (log);
        this.log= log;
        this.id_str= id;
        this.local_port= local_port;
        this.url_txt= null;
        this.method= null;
        this.version= null;
        this.text= null;
        this.cookies = new Properties();
    }
    
    /** 
     * Creates a clone of an instance of HTTPQuery
     * @param q     existing query to be cloned
     * @param id    new log id 
     */
    public HTTPRequest (HTTPRequest q, String id) {
        // initializes everything to null
        headers= new Headers (q.log);
        this.log= q.log;
        this.id_str= id;
        this.local_port= q.local_port;
    }
        
    /**
     * Logs a message to the screen prepending the log id
     * @param in_window write in window or only in the command line
     * @param s message to be written
     */
    public void Log (boolean in_window, String s) {
        if (in_window)
            log.Log (id_str+ "  " + s );
        else
            System.out.print (id_str + "  " + s);
    }

    /**
     * Get a header property value
     * @param hdrName   header name
     * @return          header value
     */
    public String getHeaderValue(String hdrName) {
        return headers.getHeaderValue(hdrName);
    }
    
    /**
     * Set a header property value
     * @param hdrName   header name
     * @param hdrVal    header value
     */
    public void setHeader(String hdrName, String hdrVal) {
        headers.setHeader(hdrName, hdrVal);
    }

    
    /** Returns the Cookie Properties object */
    public Properties get_cookies () {
        return this.cookies;
    }
    
    
    /**
     * Remove a header property name
     * @param hdrName   header name
     * @return true if successful
     */
    public boolean removeHeader(String hdrName) {
        return headers.removeHeader(hdrName);
    }
    
    /** Parses a new HTTP query from an input steam
     * @param bin   input stream
     * @param echo  if true, echoes the received message to the screen
     * @return HTTPReplyCode.OK when successful, or HTTPReplyCode.BADREQ in case of error
     * @throws java.io.IOException 
     */
    public boolean parse_Request (BufferedReader bin, boolean echo) throws IOException {
        // Get first line
        String request = bin.readLine( );  	// Reads the first line
        if (request == null) {
            if (echo) Log (true, "Invalid request Connection closed\n");
            return false;
        }
        Log( true, "Request: " + request + "\n");
        StringTokenizer st= new StringTokenizer(request);
        if (st.countTokens() != 3) return false;  // Invalid request
        method= st.nextToken();    // USES HTTP syntax
        url_txt= st.nextToken();    // for requesting files
        version= st.nextToken();
        // It does not read the other header fields! 
        // read the remaining headers inside parse_headers method of headers object
       
        headers.parse_Headers(bin, echo);
        
        // check if the Content-Length size is different than zero. If true read the body of the request (that can contain POST data)
        int clength= 0;
        try {
            String len= headers.getHeaderValue("Content-Length");
            if (len != null)
                clength= Integer.parseInt (len);
            else if (!bin.ready ())
                clength= 0;
        } catch (NumberFormatException e) {
            if (echo) Log (true, "Bad request\n");
            return false;
        }
        if (clength>0) {
            // Length is not 0 - read data to string
            String str= new String ();
            char [] cbuf= new char [clength];

            int n, cnt= 0;
            while ((cnt<clength) && ((n= bin.read (cbuf)) > 0)) {
                str= str + new String (cbuf);
                cnt += n;
            }
            if (cnt != clength) {
                Log (false, "Read request with "+cnt+" data bytes and Content-Length = "+clength+" bytes\n");
                return false;
            }
            text= str;
            if (echo)
                Log (true, "Contents('"+text+"')\n");
        }

        return true;
    }    
}