package HTTPFormat;

/**
 * Redes Integradas de Telecomunicações II
 * MIEEC 2020/2021
 *
 * Headers.java
 *
 * Auxiliary class to handle indexed list of HTTP headers with repetitions
 *
 */

import HttpServer.Log;
import java.util.*;
import java.io.*;

/**
 *
 * @author pfa@fct.unl.pt
 */
public class Headers {

    public Properties headers;                            // Single value list
    private final Log log;                              // Log object

    /**
     * Creates an empty list of headers
     * @param log log object
     */
    public Headers(Log log) {
        headers = new Properties();
        this.log = log;
    }
    

    /**
     * Clears the contents of the list
     */
    public void clear() {
        headers.clear();
    }
    
    /**
     * Store a header value; it stores more than one value per property
     * @param hdrName   header name
     * @param hdrVal    header value
     */
    public void setHeader(String hdrName, String hdrVal) {
        String storedhdrVal= headers.getProperty(hdrName);
        if (storedhdrVal != null) {
          return;
        } else {
            headers.setProperty(hdrName, hdrVal);
        }       
    }

    /**
     * Returns the value of a property (returns the last one)
     * @param hdrName   header name
     * @return  the last header value
     */
    public String getHeaderValue(String hdrName) {
        return headers.getProperty(hdrName);
    }


    /**
     * Removes all the values of a header
     * @param hdrName   header name
     * @return true if a header was removed, false otherwise
     */
    public boolean removeHeader(String hdrName) {
        if (headers.containsKey(hdrName)) {
            headers.remove(hdrName);
            return true;
        } else
            return false;
    }
    
    /**
     * Returns an enumeration of all header names
     * @return an enumeration object
     */
    public Enumeration getAllHeaderNames() {
        return headers.keys();
    }
    
    /**
     * Parses an input stream for a sequence of headers, until an empty line is reached
     * @param in    input stream
     * @param echo  if true, echoes the headers received on screen
     * @return  true if headers were read successfully 
     * @throws IOException when a socket error occurs
     */
    public boolean parse_Headers(BufferedReader in, boolean echo) throws IOException {
        // Get other header parameters
        String req;
        while (((req = in.readLine()) != null) && (req.length() != 0)) {
            int ix;
            if (echo) log.Log("hdr(" + req + ")\n");
            if ((ix = req.indexOf(':')) != -1) {
                String hdrName = req.substring(0, ix);
                String hdrVal = req.substring(ix + 1).trim();
                setHeader(hdrName, hdrVal);         
            } else {
                if (echo) log.Log("Invalid header\n");
                return false;
            }
        }
        return true;
    }

    /**
     * write the headers list to an output stream
     * @param pout  output stream
     * @param echo  if true, echoes the headers sent on screen
     * @throws IOException when a socket error occurs
     */
    public void write_Headers(PrintStream pout, boolean echo) throws IOException {
        for (String hdrName : headers.stringPropertyNames()) {
                // Single value parameter
                String val = headers.getProperty(hdrName);
                pout.print(hdrName + ": " + val + "\r\n");
                if (echo) 
                    log.Log(hdrName + ": " + val + "\n");
        }
    }
    
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
