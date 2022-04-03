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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.crypto.Cipher;

import java.text.SimpleDateFormat;

public class httpThread extends Thread {

    HttpServer ServerGui;
    ServerSocket ss;
    Socket client;
    DateFormat httpformat;

    /** Creates a new instance of httpThread */
    public httpThread(HttpServer ServerGui, ServerSocket ss, Socket client) {
        this.ServerGui = ServerGui;
        this.ss = ss;
        this.client = client;
        httpformat = new SimpleDateFormat("EE, d MMM yyyy HH:mm:ss zz", Locale.UK);
        httpformat.setTimeZone(TimeZone.getTimeZone("GMT"));
        setPriority(NORM_PRIORITY - 1);
    }

    /** The type for unguessable files */
    String guessMime(String fn) {
        String lcname = fn.toLowerCase();
        int extenStartsAt = lcname.lastIndexOf('.');
        if (extenStartsAt < 0) {
            if (fn.equalsIgnoreCase("makefile"))
                return "text/plain";
            return "unknown/unknown";
        }
        String exten = lcname.substring(extenStartsAt);
        // System.out.println("Ext: "+exten);
        if (exten.equalsIgnoreCase(".htm"))
            return "text/html";
        else if (exten.equalsIgnoreCase(".html"))
            return "text/html";
        else if (exten.equalsIgnoreCase(".gif"))
            return "image/gif";
        else if (exten.equalsIgnoreCase(".jpg"))
            return "image/jpeg";
        else
            return "application/octet-stream";
    }

    public void Log(boolean in_window, String s) {
        if (in_window)
            ServerGui.Log("" + client.getInetAddress().getHostAddress() + ";"
                    + client.getPort() + "  " + s);
        else
            System.out.print("" + client.getInetAddress().getHostAddress() +
                    ";" + client.getPort() + "  " + s);
    }

    /** Loads a class code into the VM */
    private JavaRESTAPI start_API(String name) {
        name = name.substring(name.lastIndexOf(java.io.File.separatorChar) + 1, name.length());
        JavaRESTAPI api = null;
        try {
            Class apiClass = Class.forName("HttpServer." + name);
            Object apiObject = apiClass.newInstance();
            api = (JavaRESTAPI) apiObject;
        } catch (ClassNotFoundException e) {
            System.err.println("API class not found:" + e);
        } catch (InstantiationException e) {
            System.err.println("API class instantiation:" + e);
        } catch (IllegalAccessException e) {
            System.err.println("API class access:" + e);
        }
        return api;
    }

    @Override
    public void run() {

        HTTPAnswer ans = null; // HTTP answer object
        HTTPRequest receivedhttp = null; // HTTP request object
        PrintStream pout = null;

        try {
            InputStream in = client.getInputStream();
            BufferedReader bin = new BufferedReader(
                    new InputStreamReader(in, "8859_1"));
            OutputStream out = client.getOutputStream();
            pout = new PrintStream(out, false, "8859_1");
            do {
                try {

                    // create an object to store the http request
                    receivedhttp = new HTTPRequest(ServerGui, client.getInetAddress().getHostAddress() + ":"
                            + client.getPort(), ss.getLocalPort());
                    boolean ok = receivedhttp.parse_Request(bin, true); // reads the input http request if everything
                                                                        // was
                                                                        // read
                                                                        // ok it returnstrue

                    // check

                    // Prepares an answer object
                    ans = new HTTPAnswer(ServerGui,
                            client.getInetAddress().getHostAddress() + ":" + client.getPort(),
                            HttpServer.server_name + " - " + InetAddress.getLocalHost().getHostName() + "-"
                                    + ServerGui.server.getLocalPort());
                    String[] host = receivedhttp.headers.getHeaderValue("Host").split(":",
                            receivedhttp.headers.getHeaderValue("Host").length());
                  
                    if ( Integer.parseInt(host[1])  != ServerGui.getPortHTTPS()) {

                        ans.set_code(HTTPReplyCode.TMPREDIRECT);
                        ans.set_header("Location", "https://" + host[0] + ":" + ServerGui.getPortHTTPS());

                        ans.send_Answer(pout, false, false);
                    }
                    // set the timeout
                    if (receivedhttp.headers.getHeaderValue("Connection").equals("keep-alive")
                            && receivedhttp.version.equals("HTTP/1.1") && ServerGui.getKeepAlive() != 0) {
                        client.setSoTimeout(ServerGui.getKeepAlive());
                    }
                    // API URL received
                    if (receivedhttp.url_txt.toLowerCase().endsWith("api")) {
                        while (receivedhttp.url_txt.startsWith("/"))
                            receivedhttp.url_txt = receivedhttp.url_txt.substring(1); /// remove "/" from url_txt
                        JavaRESTAPI api = start_API(receivedhttp.url_txt);
                        // create a property to host the fields from form
                        Properties fields = new Properties();

                        if (receivedhttp.text != null) {
                            // parse the string comming from the form
                            String[] sentense = receivedhttp.text.split("&", receivedhttp.text.length());

                            for (String str : sentense) {
                                String[] new_str = str.split("=", str.length());
                                fields.put(new_str[0], new_str[1]);
                            }
                        }

                        if (api != null) {
                            try {

                                Log(true, "run JavaAPI\n");
                                // check if we received a get
                                if (receivedhttp.method.equals("GET")) {
                                    api.doGet(client, receivedhttp.headers, receivedhttp.get_cookies(), ans);
                                }
                                // check if we received a post
                                if (receivedhttp.method.equals("POST")) {
                                    api.doPost(client, receivedhttp.headers, receivedhttp.get_cookies(), fields, ans);
                                }

                            } catch (Exception e) {
                                ans.set_error(HTTPReplyCode.BADREQ, receivedhttp.version);
                            }
                        } else
                            ans.set_error(HTTPReplyCode.NOTFOUND, receivedhttp.version);
                    } else {
                        // Get file with contents
                        String filename = ServerGui.getRaizHtml() + receivedhttp.url_txt
                                + (receivedhttp.url_txt.equals("/") ? "index.htm" : "");
                        System.out.println("Filename= " + filename);
                        File f = new File(filename);

                        if (f.exists() && f.isFile()) {
                            // Define reply contents
                            ans.set_code(HTTPReplyCode.OK);
                            ans.set_version(receivedhttp.version);
                            ans.set_file_headers(new File(filename), guessMime(filename));
                            // NOTICE that only the first line of the reply is sent!
                            // No additional headers are defined!
                            // cheack if keep alive is 0
                            if (receivedhttp.headers.getHeaderValue("Connection").equals("keep-alive")
                                    && receivedhttp.version.equals("HTTP/1.1") && ServerGui.getKeepAlive() == 0) {
                                // close connection
                                ans.set_header("Connection", "close");
                            }
                        } else {
                            System.out.println("File not found");
                            ans.set_error(HTTPReplyCode.NOTFOUND, receivedhttp.version);
                            // NOTICE that some code is missing in HTTPAnswer!
                        }
                    }
                    // Send reply

                    // parse.request

                    // check if modify
                    String las_modify_since = receivedhttp.headers.getHeaderValue("If-Modified-Since");
                    String last_modity = ans.headers.getProperty("Last-Modified");

                    DateFormat httpformat = new SimpleDateFormat("EE, d MMM yyyy HH:mm:ss zz", Locale.UK);
                    httpformat.setTimeZone(TimeZone.getTimeZone("GMT"));
                    if (las_modify_since != null) {
                        try {
                            Date las_modify_since_date = httpformat.parse(las_modify_since);
                            Date last_modity_date = httpformat.parse(last_modity);

                            if (last_modity_date.after(las_modify_since_date)) {
                                ans.set_code(HTTPReplyCode.OK);
                                ans.send_Answer(pout, true, true);
                            } else {
                                ans.set_code(HTTPReplyCode.NOTMODIFIED);
                                ans.send_Answer(pout, false, true);
                            }

                        } catch (ParseException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else {
                        ans.set_code(HTTPReplyCode.OK);
                        ans.send_Answer(pout, true, true);
                    }
                } catch (IOException e) {
                    break;
                }
            } while (ans.headers.getProperty("Connection").equals("keep-alive")
                    && receivedhttp.version.equals("HTTP/1.1"));

            in.close();
            pout.close();
            out.close();
        } catch (IOException e) {
            if (ServerGui.active())
                System.out.println("I/O error " + e);
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                // Ignore
                System.out.println("Error closing client" + e);
            }
            ServerGui.thread_ended();
            Log(true, "Closed TCP connection\n");
        }
    }

}
