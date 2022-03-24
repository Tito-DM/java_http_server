package HTTPFormat;

/**
 * Servicos e Aplicacoes em Redes 
 * 2021/2022
 *
 * HTTPReplyCode.java
 *
 * Class that manages the information about the first line of a HTTP reply: the
 *   reply code, reply code_text and version
 *
 */

public class HTTPReplyCode {

    public static final int NOTDEFINED= -1;
    public static final int OK= 200;
    public static final int NOTMODIFIED= 304;
    public static final int TMPREDIRECT= 307;
    public static final int BADREQ= 400;
    public static final int UNAUTHORIZED= 401;
    public static final int NOTFOUND= 404;
    public static final int PROXYAUTHENTIC=407;
    public static final int NOTIMPLEMENTED= 501;

    
    private int code;
    private String code_txt;
    private String version;
    
    
    /** Default constructor */
    public HTTPReplyCode() {
        code= NOTDEFINED;
        code_txt= null;
        version= "HTTP/1.1";
    }
    
    public HTTPReplyCode(int code, String version) {
        this.code= code;
        this.code_txt= HTTPReplyCode.code_text(code);
        this.version= version;
    }
    
    public HTTPReplyCode(HTTPReplyCode _code) {
        code= _code.code;
        code_txt= (_code.code_txt != null) ? _code.code_txt : null;
        version= (_code.version != null) ? _code.version : null;
    }

    public int get_code() { return code; }
    public String get_code_txt() { return code_txt; }
    public String get_version() { return version; }

    public void set_code(int code) { 
        this.code= code; 
        this.code_txt= code_text(code); 
    }
    /** Overwrites default code text
     * @param code_txt */
    public void set_code_txt(String code_txt) { 
        this.code_txt= code_txt; 
    }
    public void set_version(String version) {
        this.version= version;
    }

    public boolean isError() {
        return (code >= 400);
    }

    public boolean isUndef () {
        return code == -1;
    }

    @Override
    public String toString() {
        return version+" "+code+" "+code_txt;
    }
    
    /** Auxiliary function that returns the default code text */
    public static String code_text (int code) {
        switch (code) {
            case OK:
                return "OK";
            case NOTMODIFIED:
                return "Not Modified";
            case TMPREDIRECT:
                return "Temporary Redirect";
            case BADREQ:
                return "Bad Request";
            case NOTFOUND:
                return "File Not Found";
            case UNAUTHORIZED:
                return "Unauthorized";
            case NOTIMPLEMENTED:
                return "Not Implemented";
            case PROXYAUTHENTIC:
                return "Proxy Authentication Required";
            default:
                return null;
        }
    }
    

}
