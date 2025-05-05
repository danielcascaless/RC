package es.um.redes.nanoFiles.udp.message;

import es.um.redes.nanoFiles.util.FileInfo;
import java.util.Set;


/**
 * Clase que modela los mensajes del protocolo de comunicación entre pares para
 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
 * mensajes son intercambiados entre las clases DirectoryServer y
 * DirectoryConnector, y se codifican como texto en formato "campo:valor".
 * 
 */
public class DirMessage {
    public static final int PACKET_MAX_SIZE = 65507; // 65535 - 8 (UDP header) - 20 (IP header)

    private static final char DELIMITER = ':'; // Define el delimitador
    private static final char END_LINE = '\n'; // Define el carácter de fin de línea

    /**
     * Nombre del campo que define el tipo de mensaje (primera línea)
     */
    private static final String FIELDNAME_OPERATION = "operation";
    private static final String FIELDNAME_PROTOCOL_ID = "protocolid";
    private static final String FIELDNAME_USER = "user";
    private static final String FIELDNAME_MESSAGE = "message";
    private static final String FIELDNAME_STATUS = "status";

    /**
     * Tipo del mensaje, de entre los tipos definidos en DirMessageOps.
     */
    private String operation = DirMessageOps.OPERATION_INVALID;
    /**
     * Identificador de protocolo usado, para comprobar compatibilidad del directorio.
     */
    private String protocolId;
    private String user;
    private String message;
    private String status;
    private String fileName;
    private int serverPort;
    private String serverAddress;



    public DirMessage(String op) {
        this.operation = op;
    }

    public DirMessage(String op, String protocolId) {
        this.operation = op;
        this.protocolId = protocolId;
    }

    public DirMessage(String op, String user, String message) {
        this.operation = op;
        this.user = user;
        this.message = message;
    }

    public DirMessage(String op, String user, String message, String status) {
        this.operation = op;
        this.user = user;
        this.message = message;
        this.status = status;
    }
    
    public DirMessage(String op, int serverPort, FileInfo[] fileset) {
        this.operation = op;
        this.serverPort = serverPort;

        StringBuilder sb = new StringBuilder();
        for (FileInfo file : fileset) {
            sb.append(file.getFileName()).append(",");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1); // quitar última coma
        }
        this.message = sb.toString();
    }

    public String getOperation() {
        return operation;
    }

    public void setProtocolID(String protocolId) {
        if (!operation.equals(DirMessageOps.OPERATION_PING)) {
            throw new RuntimeException(
                    "DirMessage: setProtocolId called for message of unexpected type (" + operation + ")");
        }
        this.protocolId = protocolId;
    }

    public String getProtocolId() {
        return protocolId;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public int getServerPort() {
        return serverPort;
    }
    
    public String getServerAddress() {
        return serverAddress;
    }

    public DirMessage(String op, int serverPort, Set<FileInfo> fileset) {
        this.operation = op;
        this.serverPort = serverPort;

        StringBuilder sb = new StringBuilder();
        for (FileInfo file : fileset) {
            sb.append(file.getFileName()).append(",");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        this.message = sb.toString();
    }


    public static DirMessage fromString(String message) {
    	
        String[] lines = message.split(END_LINE + "");
        DirMessage m = null;

        for (String line : lines) {
            int idx = line.indexOf(DELIMITER); // Posición del delimitador
            String fieldName = line.substring(0, idx).toLowerCase(); // minúsculas
            String value = line.substring(idx + 1).trim();

            switch (fieldName) {
            case FIELDNAME_OPERATION: {
                assert (m == null);
                m = new DirMessage(value);
                break;
            }
            case FIELDNAME_PROTOCOL_ID: {
                assert (m != null);
                m.setProtocolID(value);
                break;
            }
            case FIELDNAME_USER: {
                assert (m != null);
                m.setUser(value);
                break;
            }
            case FIELDNAME_MESSAGE: {
                assert (m != null);
                m.setMessage(value);
                break;
            }
            case FIELDNAME_STATUS: {
                assert (m != null);
                m.setStatus(value);
                break;
            }
            case "serverport": {
                m.serverPort = Integer.parseInt(value);
                break;
            }

            default:
                System.err.println("PANIC: DirMessage.fromString - message with unknown field name " + fieldName);
                System.err.println("Message was:\n" + message);
                System.exit(-1);
            }
        }

        return m;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(FIELDNAME_OPERATION + DELIMITER + operation + END_LINE); // Construimos el campo

        if (protocolId != null) {
            sb.append(FIELDNAME_PROTOCOL_ID + DELIMITER + protocolId + END_LINE);
        }
        if (user != null) {
            sb.append(FIELDNAME_USER + DELIMITER + user + END_LINE);
        }
        if (message != null) {
            sb.append(FIELDNAME_MESSAGE + DELIMITER + message + END_LINE);
        }
        if (status != null) {
            sb.append(FIELDNAME_STATUS + DELIMITER + status + END_LINE);
        }
        if (serverPort != 0) {
            sb.append("serverPort").append(DELIMITER).append(serverPort).append(END_LINE);
        }


        sb.append(END_LINE); // Marcamos el final del mensaje
        return sb.toString();
    }
}