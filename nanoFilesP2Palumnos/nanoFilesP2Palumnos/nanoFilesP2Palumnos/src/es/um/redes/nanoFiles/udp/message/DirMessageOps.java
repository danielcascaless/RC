package es.um.redes.nanoFiles.udp.message;

public class DirMessageOps {

    /*
     * TODO: (Boletín MensajesASCII) Añadir aquí todas las constantes que definen
     * los diferentes tipos de mensajes del protocolo de comunicación con el
     * directorio (valores posibles del campo "operation").
     */
    public static final String OPERATION_INVALID = "invalid_operation";
    public static final String OPERATION_PING = "ping";
    public static final String OPERATION_PING_OK = "ping_ok";
    public static final String OPERATION_LOGIN = "login";
    public static final String OPERATION_LOGOUT = "logout";
    public static final String OPERATION_SEND_MESSAGE = "send_message";
    public static final String OPERATION_RECEIVE_MESSAGE = "receive_message";
    public static final String OPERATION_LIST_USERS = "list_users";
    public static final String OPERATION_USER_STATUS = "user_status";
    public static final String OPERATION_ERROR = "error";
    public static final String OPERATION_REGISTER = "register";
    public static final String OPERATION_REGISTER_OK = "register_ok";
    public static final String OPERATION_GET_FILELIST = "get_filelist";
    public static final String OPERATION_GET_FILELIST_OK = "get_filelist_ok";
    public static final String OPERATION_GET_SERVERS = "get_servers";
    public static final String OPERATION_GET_SERVERS_OK = "get_servers_ok   ";
    public static final String OPERATION_UNREGISTER = "unregister";
    public static final String OPERATION_UNREGISTER_OK = "unregister_ok";
    public static final String OPERATION_FILEINFO = "fileinfo";
    public static final String OPERATION_SERVERINFO = "serverinfo";


}
