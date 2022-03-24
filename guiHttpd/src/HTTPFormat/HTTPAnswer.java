package HTTPFormat;

/**
 * Servicos e Applicacoes em Redes 2021/2022
 *
 * HTTPAnswer.java
 *
 * Class that stores all information about a HTTP reply INCOMPLETE VERSION
 *
 */
import HttpServer.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class HTTPAnswer {

    /**
     * Reply code information
     */
    public HTTPReplyCode code;
    /**
     * Reply headers data
     */
    public Properties headers; // Header fields
    public ArrayList<String> set_cookies; // List to sore Set cookie header fields
    /**
     * Reply contents They are stored either in a text buffer or in a file
     */
    public String text; // buffer with reply contents for dynamic API responses or server generated code
    public File file; // file used if text == null, for file responses
    Log log;
    String id_str; // Thread id - for logging purposes

    /**
     * Creates a new instance of HTTPAnswer
     */
    public HTTPAnswer(Log log, String id_str, String server_name) {
        this.code = new HTTPReplyCode(); // code constains an instance of the HTTPReplyCode Class thar contains HTTP
        // code values and an HTTP version field.
        this.id_str = id_str;
        this.log = log;
        headers = new Properties(); // propreties object to store HTTP headers in the format
        // (Key=(String)Header;Value=(String)HeaderValue)
        set_cookies = new ArrayList<String>(); // Array List of Strings to contain the Strings that make up the several
        // values of the Set_Cookie Header.
        text = null;
        file = null;
        /**
         * define Server header field name
         */
        headers.setProperty("Server", server_name);
    }

    void Log(boolean in_window, String s) {
        if (in_window) {
            log.Log(id_str + "  " + s);
        } else {
            System.out.print(id_str + "  " + s);
        }
    }

    /*
     * Method to set the HTTP reply code of the answer
     */
    public void set_code(int _code) {
        code.set_code(_code);
    }

    /*
     * Method to set the HTTP version of the answer
     */
    public void set_version(String v) {
        code.set_version(v);
    }

    /*
     * Method to set an HTTP header of the answer
     */
    public void set_header(String name, String value) {
        headers.setProperty(name, value);
    }

    /*
     * Method to add a cookie to the list of cookies to add in the Set_Cookie header
     */
    public void set_cookie(String setcookie_line) {
        set_cookies.add(setcookie_line);
    }

    /**
     * Sets the headers needed in a reply with a static file content and fill
     * the file property with the File object of the static file to send
     *
     * @param _f
     * @param mime_enc
     */
    public void set_file_headers(File _f, String mime_enc) {
        file = _f;
        // header lines not set in 'headers'!
        DateFormat httpformat = new SimpleDateFormat("EE, d MMM yyyy HH:mm:ss zz", Locale.UK);
        httpformat.setTimeZone(TimeZone.getTimeZone("GMT"));
        headers.setProperty("Date", httpformat.format(new Date()));

        /*set_version("1.1");*/
     
        set_Date();
         
        set_header("Server", "myServer");
        set_header("Last-Modified", httpformat.format(file.lastModified()));
        set_header("Content-Length", String.valueOf(file.length()));
        set_header("Content-Type", mime_enc); 
        // ...
       /* Log(true, "Header fields not defined in HTTPAnswer.set_file\n");*/
    }

    /**
     * Sets the headers needed in a reply with a locally generated HTML string
     * (_text obejct) and fill the text property with the String object
     * containing the HTML to send
     *
     * @param _text
     */
    public void set_text_headers(String _text) {
        text = _text;
        // header lines not set in 'headers'!
        // ... set_version("1.1");
       
        set_Date();
        set_header("Server", "myServer");
        set_header("Content-Length", String.valueOf(text.length()));
        set_header("Content-Type", "text/html");
        
        // ...
       /* Log(true, "Header fields not defined in HTTPAnswer.set_text\n");*/
    }

    /**
     * Returns the current value of the answer code
     *
     * @return
     */
    public int get_code() {
        return code.get_code();
    }

    /**
     * Returns a string with the first line of the answer
     *
     * @return
     */
    public String get_first_line() {
        return code.toString();
    }

    /**
     * Returns an iterator over all header names
     *
     * @return
     */
    public Iterator<Object> get_Iterator_parameter_names() {
        return headers.keySet().iterator();
    }

    /**
     * Returns the array list with all set_cookies
     *
     * @return
     */
    public ArrayList<String> get_set_cookies() {
        return set_cookies;
    }

    /**
     * Sets the "Date" header field with the local date in HTTP format
     */
    void set_Date() {
        DateFormat httpformat = new SimpleDateFormat("EE, d MMM yyyy HH:mm:ss zz", Locale.UK);
        httpformat.setTimeZone(TimeZone.getTimeZone("GMT"));
        headers.setProperty("Date", httpformat.format(new Date()));
    }

    /**
     * Prepares an HTTP answer with an error code
     *
     * @param _code
     * @param version
     */
    public void set_error(int _code, String version) {
        set_version(version);
        set_Date();
        code.set_code(_code);
        if (code.get_code_txt() == null) {
            code.set_code(HTTPReplyCode.BADREQ);
        }

        if (!version.equalsIgnoreCase("HTTP/1.0")) {
            headers.setProperty("Connection", "close");
        }
        // Prepares a web page with an error description
        String txt = "<HTML>\r\n";
        txt = txt + "<HEAD><TITLE>Error " + code.get_code() + " -- " + code.get_code_txt()
                + "</TITLE></HEAD>\r\n";
        txt = txt + "<H1> Error " + code.get_code() + " : " + code.get_code_txt() + " </H1>\r\n";
        txt = txt + "  by " + headers.getProperty("Server") + "\r\n";
        txt = txt + "</HTML>\r\n";

        // Set the header properties
        set_text_headers(txt);
    }

    /**
     * Sends the HTTP reply to the client using 'pout' text device
     *
     * @param pout
     * @param send_data indicates if data is present in the response or only
     * headers
     * @param echo
     * @throws java.io.IOException
     */
    public void send_Answer(PrintStream pout, boolean send_data, boolean echo) throws IOException {
       
        if (code.get_code_txt() == null) {
            code.set_code(HTTPReplyCode.BADREQ);
        }
        if (echo) {
            Log(true, "Answer: " + code.toString() + "\n");
        }
        pout.print(code.toString() + "\r\n");
        /**
         * Send all headers except set_cookie
         */

        Iterator entries = headers.entrySet().iterator();
        
       while (entries.hasNext()) {
        Map.Entry pair = (Map.Entry)entries.next();
         pout.print(pair.getKey() + ":" + pair.getValue() + "\r\n");  
    }
        // ...
        pout.print(code.toString() + "\r\n");
        /**
         * Send set_cookie header (built using the set_cookies arraylist)s
         */
        // ...
      
        pout.print("\r\n");

        if (send_data) {

            if (text != null) {
                pout.print(text);
            } else if (file != null) {
                FileInputStream fin = new FileInputStream(file);
                if (fin == null) {
                    Log(true, "Internal error sending answer data\n");
                    return;
                }

                byte[] data = new byte[fin.available()];
                fin.read(data); // Read the entire file to buffer 'data'
                // IMPORTANT - Please modify this code to send a file chunk-by-chunk
                // to avoid having CRASHES with BIG FILES
                Log(true, "HTTPAnswer may fail for large files - please modify it\n");
                pout.write(data);
                fin.close();
            } else if ((code.get_code() != HTTPReplyCode.NOTMODIFIED)
                    && (code.get_code() != HTTPReplyCode.TMPREDIRECT)) {
                Log(true, "Internal server error sending answer\n");
            }
        }
        pout.flush();
        if (echo) {
            Log(false, "\n");
        }
    }
}
