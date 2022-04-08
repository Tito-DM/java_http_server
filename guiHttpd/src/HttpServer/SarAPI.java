package HttpServer;

/**
 * Servicos e Aplicacoes em Redes
 * 2021/2022
 *
 * API Example - this class implements the javaRESTAPI interface.
 * It manages a list of groups, using Properties lists.
 * The user receives the current list, and may add or remove groups.
 * INCOMPLETE VERSION
 */

import HTTPFormat.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SarAPI extends JavaRESTAPI {
    private final String bdname = "grupos.txt";

    private groupDB db;

    /** Creates a new instance of rit2API */
    public SarAPI() {
        db = new groupDB(bdname);
    }

    /*
     * Functions that handle cookie related issues
     */

    // To be completed

    /** Select a subset of 'k' number of a set of numbers raging from 1 to 'max' */
    private int[] draw_numbers(int max, int k) {
        int[] vec = new int[k];
        int j;

        Random rnd = new Random(System.currentTimeMillis());
        for (int i = 0; i < k; i++) {
            do {
                vec[i] = rnd.nextInt(max) + 1;
                for (j = 0; j < i; j++) {
                    if (vec[j] == vec[i])
                        break;
                }
            } while ((i != 0) && (j < i));
        }
        return vec;
    }

    /** Selects the minimum number in the array */
    private int minimum(int[] vec, int max) {
        int min = max + 1, n = -1;
        for (int i = 0; i < vec.length; i++) {
            if (vec[i] < min) {
                n = i;
                min = vec[i];
            }
        }
        if (n == -1) {
            System.err.println("Internal error in API.minimum\n");
            return max + 1;
        }
        vec[n] = max + 1; // Mark position as used
        return min;
    }

    /** Prepares the SARsAPI web page that is sent as reply to the API call */
    private String make_Page(String ip, int port, String tipo, String grupo, int n, String n1, String na1, String n2,
            String na2, String n3, String na3, boolean count, String lastUpdate , String cooky) {
        // Draw "lucky" numbers
        int[] set1 = draw_numbers(50, 5);
        int[] set2 = draw_numbers(9, 2);
        StringBuilder bufLatAdded= new StringBuilder();

    

        // Prepare string html with web page
        String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\r\n<html>\r\n<head>\r\n";
        html += "<meta content=\"text/html; charset=ISO-8859-1\" http-equiv=\"content-type\">\r\n";
        html += "<title>SAR API.htm</title>\r\n</head>\r\n<body>\r\n";
        html += "<p align=\"center\"><img src=\"gifs/fctunl_big.gif\" border=\"0\" height=\"94\" width=\"600\"></p>\r\n";
        html += "<h1 align=\"center\">P&aacute;gina de teste</h1>\r\n";
        html += "<h1 align=\"center\"><font color=\"#800000\">SAR</font> <font color=\"#c0c0c0\">2021/2022</font></h1>\r\n";
        html += "<h3 align=\"center\">1&ordm; Trabalho de laborat&oacute;rio</h3>\r\n";
        html += "<p align=\"left\">Ligou a partir de <font color=\"#ff0000\">" + ip + "</font>:";
        html += "<font color=\"#ff0000\">" + port + "</font> num browser do tipo <font color=\"#ff0000\">" + tipo
                + "</font>.</p>\r\n";
        if (n >= 0) {
            html += "<p align=\"left\">Os elementos do grupo <font color=\"#0000ff\">"
                    + (grupo.length() > 0 ? (grupo) : "?") + "</font> j&aacute; actualizaram ";
            html += "<font color=\"#0000ff\">" + n + "</font> vezes o grupo no servidor.</p>\r\n";
        }
        if (n >= 0) {
            html += "<p align=\"left\">O &uacute;ltimo acesso ao servidor por este utilizador foi em: " +
                    " <font color=\"#0000ff\">" + lastUpdate + "</font>.</p>\r\n";
        }
        html += "<form method=\"post\" action=\"SarAPI\">\r\n<h3>\r\nDados do grupo</h3>";
        html += "<p>Grupo <input name=\"Grupo\" size=\"2\" type=\"text\"" +
                (grupo.length() > 0 ? " value=\"" + grupo + "\"" : "") + "></p>\r\n";
        html += "<p>N&uacute;mero <input name=\"Num1\" size=\"5\" type=\"text\"" +
                (n1.length() > 0 ? " value=" + n1 : "") +
                ">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Nome <input name=\"Nome1\" size=\"80\" type=\"text\"" +
                (na1.length() > 0 ? " value=" + na1 : "") +
                "></p>\r\n";
        html += "<p>N&uacute;mero <input name=\"Num2\" size=\"5\" type=\"text\"" +
                (n2.length() > 0 ? " value=" + n2 : "") +
                ">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Nome <input name=\"Nome2\" size=\"80\" type=\"text\"" +
                (na2.length() > 0 ? " value=" + na2 : "") +
                "></p>\r\n";
        html += "<p>N&uacute;mero <input name=\"Num3\" size=\"5\" type=\"text\"" +
                (n3.length() > 0 ? " value=" + n3 : "") +
                ">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Nome <input name=\"Nome3\" size=\"80\" type=\"text\"" +
                (na3.length() > 0 ? " value=" + na3 : "") +
                "></p>\r\n";
        html += "<p><input name=\"Contador\"" + (count ? " checked=\"checked\"" : "")
                + " value=\"ON\" type=\"checkbox\">Contador</p>\r\n";
        html += "<p><input value=\"Submeter\" name=\"BotaoSubmeter\" type=\"submit\">";
        html += "<input value=\"Apagar\" name=\"BotaoApagar\" type=\"submit\">";
        html += "<input value=\"Limpar\" type=\"reset\" value=\"Reset\" name=\"BotaoLimpar\">";
        html += "</p>\r\n</form>\r\n";
        html += "<h3>Grupos registados</h3>";
        html += db.table_group_html();
        html += "<h3>Um exemplo de cont&eacute;udo din&acirc;mico :-)</h3>";
        html += "<p align=\"left\">Se quiser deitar dinheiro fora, aqui v&atilde;o algumas sugest&otilde;es para ";
        html += "o pr&oacute;ximo <a href=\"https://www.jogossantacasa.pt/web/JogarEuromilhoes/?\">Euromilh&otilde;es</a>: ";
    
        html +=bufLatAdded.append("<h3> The Broswer Update group ").append(cooky).append(" ").append(db.get_group_info(cooky, "counter")).append(" Times <h3/>");
        
       
        for (int i = 0; i < 5; i++)
            html += (i == 0 ? "" : " ") + "<font color=\"#00ff00\">" + minimum(set1, 50) + "</font>";
        html += " + <font color=\"#800000\">" + minimum(set2, 9) + "</font> <font color=\"#800000\">" + minimum(set2, 9)
                + "</font></p>\r\n";
        html += "<p align=\"left\">&nbsp;</p>\r\n";
        html += "<p align=\"left\"><font face=\"Times New Roman\">&copy; 2021/2022</font></p>\r\n</body>\r\n</html>\r\n";

        return html; // HTML page code
    }

    /** Runs GET/HEAD method */
    @Override
    public boolean doGet(Socket s, Headers headers, Properties cookies, HTTPAnswer ans) {
        System.out.println("run API GET");

        String group = "", nam1 = "", n1 = "", nam2 = "", n2 = "", nam3 = "", n3 = "", lastUpdate = "";
      
        int cnt = -1;
        /**
         * This part must check if the browser is sending the sarCookie
         * If it is, it must deliver a web page with the last group introduced by the
         * user
         * Otherwise, the fields must be empty
         */
      

        System.out.println("Cookies ignored in API GET");

        // Don't forget to convert Names from API POST format to HTML format before
        // preparing the web page
        String aux = JavaRESTAPI.postString2htmlString(nam1);
        if (aux != null)
            nam1 = aux;
        aux = JavaRESTAPI.postString2htmlString(nam2);
        if (aux != null)
            nam2 = aux;
        aux = JavaRESTAPI.postString2htmlString(nam3);
        if (aux != null)
            nam3 = aux;
        aux = JavaRESTAPI.postString2htmlString(lastUpdate);
        if (aux != null)
            lastUpdate = aux;

        // Prepare html page
        String html = make_Page(s.getInetAddress().getHostAddress(), s.getPort(), headers.getHeaderValue("User-Agent"),
                group, cnt, n1, nam1, n2, nam2, n3, nam3, false, lastUpdate,headers.getHeaderValue("Cookie"));

        // Prepare answer
        ans.set_code(HTTPReplyCode.OK);
        ans.set_text_headers(html); // sets text headers.
        // Complete the code. Add the missing header fieds!
        System.out.println("Missing header fields in API GET");

        return true;
    }

    /** Runs POST method */
    @Override
    public boolean doPost(Socket s, Headers headers, Properties cookies, Properties fields, HTTPAnswer ans) {
        // Put POST implementation here
        System.out.println("run API POST");
        String group = fields.getProperty("Grupo", "");
        String nam1 = fields.getProperty("Nome1", "");
        String n1 = fields.getProperty("Num1", "");
        String nam2 = fields.getProperty("Nome2", "");
        String n2 = fields.getProperty("Num2", "");
        String nam3 = fields.getProperty("Nome3", "");
        String n3 = fields.getProperty("Num3", "");
        boolean SubmitButton = (fields.getProperty("BotaoSubmeter") != null);
        boolean DeleteButton = (fields.getProperty("BotaoApagar") != null);
        boolean chekBtn = (fields.getProperty("Contador") != null);
        String lastUpdate = "";

        System.err.println("Button: " + (SubmitButton ? "Submit" : "")
                + (DeleteButton ? "Delete" : "") + "\n");
        int cnt = -1;

        System.out.println("Command not implemented in API POST");
        // store the data in the db

        // ...

        // Don't forget to convert Names from API format to HTML format before preparing
        // the web page
        String aux = JavaRESTAPI.postString2htmlString(nam1);
        if (aux != null)
            nam1 = aux;
        aux = JavaRESTAPI.postString2htmlString(nam2);
        if (aux != null)
            nam2 = aux;
        aux = JavaRESTAPI.postString2htmlString(nam3);
        if (aux != null)
            nam3 = aux;
        aux = JavaRESTAPI.postString2htmlString(lastUpdate);
        if (aux != null)
            lastUpdate = aux;

        // check if delete btn was clicked
        if (DeleteButton){
            db.remove_group(group);
        }

      

        // save to db
        db.store_group(group,chekBtn , n1, nam1, n2, nam2, n3, nam3);
        db.save_group_db();

        // Prepare html page
        String html = make_Page(s.getInetAddress().getHostAddress(), s.getPort(), headers.getHeaderValue("User-Agent"),
                group, cnt, n1, nam1, n2, nam2, n3, nam3, (fields.getProperty("Contador") != null), lastUpdate, "");

        // Prepare answer
       // ans.set_code(HTTPReplyCode.OK);
        ans.set_text_headers(html);

        // prepare the cookies
        ans.set_header("Set-Cookie", group);
       
        

        return true;
    }

}
