package HttpServer;

/**
 * Servicos e Aplicacoes em Redes
 *
 * javaRESTAPI.java
 *
 * Abstract class that defines the interface that must be provided by an API class.
 * It also provides some auxiliary functions that handle char conversion
 * INCOMPLETE VERSION
 *
 * @author  Luis Bernardo
 */

import HTTPFormat.*;
import java.net.Socket;
import java.util.Properties;

public abstract class JavaRESTAPI {

    
    /** Converts POST string into Java string (ISO-8859-1) (removes formating codes) */
    public static String postString2string(String in_s) {
        if (in_s == null)
            return null;
        StringBuilder out_s= new StringBuilder();
        int i= 0;
        while (i<in_s.length()) {
            switch (in_s.charAt (i)) {
                case '%':   try {   // "%dd" - character code in hexadecimal
                                i++;
                                byte[] n= new byte[1];
                                n[0]= (byte)Integer.parseInt(in_s.substring(i, i+2), 16);
                                if (n[0] == -96)    // Patch for MSIE
                                    out_s.append (' ');
                                else
                                    out_s.append (new String(n, "ISO-8859-1"));
                                i++;    // Jumps first char
                            }
                            catch (Exception e) {
                                System.err.println("Error parging POST string: "+e);
                                return null;
                            }
                            break;
                case '+':   out_s.append(' ');
                            break;
                default:    out_s.append(in_s.charAt (i));
            }
            i++;
        }
        //System.out.println("CGI2STR: '"+in_s+"' > '"+out_s+"'");
        return out_s.toString ();
    }
    
    /** Converts java string (ISO-8859-1) to HTML format */
    public static String String2htmlString(String in_s) {
        if (in_s == null)
            return null;
        StringBuilder out_s= new StringBuilder();
        for (int i= 0; i<in_s.length(); i++) {
            switch (in_s.charAt (i)) {
                case ' ': out_s.append("&nbsp;"); break;

                default: out_s.append(in_s.charAt (i));
            }
        }
        //System.out.println("STR2HTML: '"+in_s+"' > '"+out_s+"'");
        return out_s.toString ();        
    }

    /** Convert JAVA string (ISO-8859-1) to HTML format */
    public static String postString2htmlString(String in_s) {
        return String2htmlString(postString2string(in_s));
    }
    
    /** Private method returns page with "Not Implemented" */
    private void not_implemented(HTTPAnswer reply, String method) {
        // Define html error page
        String txt= "<HTML>\n";
        txt=txt+"<HEAD><TITLE>Error - " + method + " not implemented\n</TITLE></HEAD>\n";
        txt= txt+ "<H1> Error - " + method + " not implemented </H1>\n";
        txt= txt+ "  by JavaAPI\n";
        txt= txt + "</HTML>\n";
        // Prepare reply code and header fields
        reply.set_text_headers(txt);
        reply.set_code(HTTPReplyCode.NOTIMPLEMENTED);
        reply.set_version("HTTP/1.1");
    }
            
    /** Runs GET method */
    public boolean doGet(Socket s, Headers head, Properties cookies, HTTPAnswer reply) { 
        // By default returns - "not supported"
        //return a 200 code
        reply.set_code(HTTPReplyCode.OK);
        not_implemented(reply, "GET");
        return true;
    }
            
    /** Runs POST method */
    public boolean doPost(Socket s, Headers head, Properties cookies, Properties fields, HTTPAnswer reply) { 
        // By default returns - "not supported"
        System.out.println("default POST");
        not_implemented(reply, "POST");
        return true;
    }
        
}
