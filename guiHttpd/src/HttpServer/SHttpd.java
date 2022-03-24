/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package HttpServer;

import static HttpServer.HttpServer.server_name;
import java.net.ServerSocket;
/**
 *   /** HTTP server thread 
 * @author pedroamaral */
 

class SHttpd extends Thread {
        HttpServer root;
        ServerSocket ss;
        volatile boolean active;
        
        SHttpd ( HttpServer root, ServerSocket ss ) {
            this.root= root;
            this.ss= ss;
        }
        
        public void wake_up () {
            this.interrupt ();
        }
        
        public void stop_thread () {
            active= false;
            this.interrupt ();
        }
        
        @Override
        public void run () {
            System.out.println (
                    "\n******************** "+HttpServer.server_name+" started ********************\n");
            active= true;
            while ( active ) {
                try {
                    httpThread conn = new httpThread ( root, ss, ss.accept () );
                    conn.start ( );
                    root.thread_started ();
                } catch (java.io.IOException e) {
                    root.Log ("IO exception: "+ e + "\n");
                    active= false;
                }
            }
        }
} // end of class SHttpd
