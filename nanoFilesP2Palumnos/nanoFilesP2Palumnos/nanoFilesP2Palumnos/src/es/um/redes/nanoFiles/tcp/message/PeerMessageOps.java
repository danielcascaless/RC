package es.um.redes.nanoFiles.tcp.message;

import java.util.Map;
import java.util.TreeMap;

public class PeerMessageOps {

    public static final byte OPCODE_INVALID_CODE = 0;

    /*
     * TODO: (Boletín MensajesBinarios) Añadir aquí todas las constantes que definen
     * los diferentes tipos de mensajes del protocolo de comunicación con un par
     * servidor de ficheros (valores posibles del campo "operation").
     */
    // Basic operations (client requests)
    public static final byte OP_GET_CHUNK = 0x01;           // Request a file chunk
    public static final byte OP_FILE_HASH = 0x02;           // Request file hash verification
    public static final byte OP_STOP_TRANSFER = 0x03;       // Stop ongoing transfer
    public static final byte OP_UPLOAD_FILE = 0x04;         // Initiate file upload
    public static final byte OP_FILENAME_TO_SAVE = 0x05;    // Specify filename for saving
    
    // Server responses
    public static final byte OP_FILE_CHUNK = 0x11;          // File chunk data
    public static final byte OP_FILE_NOT_FOUND = 0x12;       // Requested file not found
    public static final byte OP_TRANSFER_ACCEPTED = 0x13;    // Transfer request accepted
    public static final byte OP_CHUNK_OUT_OF_RANGE = 0x14;   // Invalid chunk range
    public static final byte OP_FILE_EXISTS = 0x15;          // File already exists
    public static final byte OP_TRANSFER_COMPLETE = 0x16;    // Transfer completed

    /*
     * TODO: (Boletín MensajesBinarios) Definir constantes con nuevos opcodes de
     * mensajes definidos anteriormente, añadirlos al array "valid_opcodes" y añadir
     * su representación textual a "valid_operations_str" EN EL MISMO ORDEN.
     */
    private static final Byte[] _valid_opcodes = {
        OPCODE_INVALID_CODE,
        OP_GET_CHUNK,
        OP_FILE_HASH,
        OP_STOP_TRANSFER,
        OP_UPLOAD_FILE,
        OP_FILENAME_TO_SAVE,
        OP_FILE_CHUNK,
        OP_FILE_NOT_FOUND,
        OP_TRANSFER_ACCEPTED,
        OP_CHUNK_OUT_OF_RANGE,
        OP_FILE_EXISTS,
        OP_TRANSFER_COMPLETE
    };
    
    private static final String[] _valid_operations_str = {
        "INVALID_OPCODE",
        "GET_CHUNK",
        "FILE_HASH",
        "STOP_TRANSFER",
        "UPLOAD_FILE",
        "FILENAME_TO_SAVE",
        "FILE_CHUNK",
        "FILE_NOT_FOUND",
        "TRANSFER_ACCEPTED",
        "CHUNK_OUT_OF_RANGE",
        "FILE_EXISTS",
        "TRANSFER_COMPLETE"
    };

    private static Map<String, Byte> _operation_to_opcode;
    private static Map<Byte, String> _opcode_to_operation;

    static {
        _operation_to_opcode = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        _opcode_to_operation = new TreeMap<>();
        for (int i = 0; i < _valid_operations_str.length; ++i) {
            _operation_to_opcode.put(_valid_operations_str[i], _valid_opcodes[i]);
            _opcode_to_operation.put(_valid_opcodes[i], _valid_operations_str[i]);
        }
    }

    /**
     * Transforma una cadena en el opcode correspondiente
     */
    protected static byte operationToOpcode(String opStr) {
        return _operation_to_opcode.getOrDefault(opStr, OPCODE_INVALID_CODE);
    }

    /**
     * Transforma un opcode en la cadena correspondiente
     */
    public static String opcodeToOperation(byte opcode) {
        String operation = _opcode_to_operation.get(opcode);
        return operation != null ? operation : "UNKNOWN_OPCODE";
    }
    
    /**
     * Verifica si un opcode es válido
     */
    public static boolean isValidOpcode(byte opcode) {
        return _opcode_to_operation.containsKey(opcode);
    }
}