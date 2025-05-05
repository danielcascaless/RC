package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.StructureViolationException;


public class PeerMessage {

    private byte opcode;

    /*
     * TODO: (Boletín MensajesBinarios) Añadir atributos u otros constructores
     * específicos para crear mensajes con otros campos, según sea necesario
     * 
     */
    private byte[] fileHash;  // Hash del archivo (20 bytes para SHA-1)
    private String fileName;  // Nombre del archivo
    private long offset;      // Desplazamiento en el archivo
    private int size;         // Tamaño del chunk o nombre del archivo
    private byte[] chunkData; // Datos del fragmento

    public PeerMessage() {
        opcode = PeerMessageOps.OPCODE_INVALID_CODE;
    }

    public PeerMessage(byte op) {
        opcode = op;
    }
    
    // Constructor para mensajes con hash de archivo
    public PeerMessage(byte op, String fileHash) {
        this(op);
        this.fileHash = hexStringToBytes(fileHash);
    }
    
    // Constructor para mensajes con nombre de archivo
    public PeerMessage(byte op, String fileName, int size) {
        this(op);
        this.fileName = fileName;
        this.size = size;
    }
    
    // Constructor para mensajes con datos de chunk
    public PeerMessage(byte op, long offset, int size, byte[] chunkData) {
        this(op);
        this.offset = offset;
        this.size = size;
        this.chunkData = chunkData;
    }
    
    // Constructor para solicitudes de chunk
    public PeerMessage(byte op, long offset, int size) {
        this(op);
        this.offset = offset;
        this.size = size;
    }

    /*
     * TODO: (Boletín MensajesBinarios) Crear métodos getter y setter para obtener
     * los valores de los atributos de un mensaje. Se aconseja incluir código que
     * compruebe que no se modifica/obtiene el valor de un campo (atributo) que no
     * esté definido para el tipo de mensaje dado por "operation".
     */
    public byte getOpcode() {
        return opcode;
    }
    
    public void setOpcode(byte opcode) {
        this.opcode = opcode;
    }
    
    public String getFileHash() {
        if (opcode != PeerMessageOps.OP_GET_CHUNK && 
            opcode != PeerMessageOps.OP_FILE_HASH) {
            throw new StructureViolationException("FileHash not available for this message type");
        }
        return bytesToHex(fileHash);
    }
    
    public void setFileHash(String fileHash) {
        if (opcode != PeerMessageOps.OP_GET_CHUNK && 
            opcode != PeerMessageOps.OP_FILE_HASH) {
            throw new StructureViolationException("Cannot set FileHash for this message type");
        }
        this.fileHash = hexStringToBytes(fileHash);
    }
    
    public String getFileName() {
        if (opcode != PeerMessageOps.OP_FILENAME_TO_SAVE) {
            throw new StructureViolationException("FileName not available for this message type");
        }
        return fileName;
    }
    
    public void setFileName(String fileName) {
        if (opcode != PeerMessageOps.OP_FILENAME_TO_SAVE) {
            throw new StructureViolationException("Cannot set FileName for this message type");
        }
        this.fileName = fileName;
        this.size = fileName.getBytes().length;
    }
    
    public long getOffset() {
        if (opcode != PeerMessageOps.OP_GET_CHUNK && 
            opcode != PeerMessageOps.OP_FILE_CHUNK) {
            throw new StructureViolationException("Offset not available for this message type");
        }
        return offset;
    }
    
    public void setOffset(long offset) {
        if (opcode != PeerMessageOps.OP_GET_CHUNK && 
            opcode != PeerMessageOps.OP_FILE_CHUNK) {
            throw new StructureViolationException("Cannot set Offset for this message type");
        }
        this.offset = offset;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public byte[] getChunkData() {
        if (opcode != PeerMessageOps.OP_FILE_CHUNK) {
            throw new StructureViolationException("ChunkData not available for this message type");
        }
        return chunkData;
    }
    
    public void setChunkData(byte[] chunkData) {
        if (opcode != PeerMessageOps.OP_FILE_CHUNK) {
            throw new StructureViolationException("Cannot set ChunkData for this message type");
        }
        this.chunkData = chunkData;
        this.size = chunkData.length;
    }
    
    // Métodos de utilidad para conversión hex<->bytes
    private static byte[] hexStringToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String bytesToHex(byte[] bytes) {
        final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars).toLowerCase();
    }

    /**
     * Método de clase para parsear los campos de un mensaje y construir el objeto
     * DirMessage que contiene los datos del mensaje recibido
     * 
     * @param data El array de bytes recibido
     * @return Un objeto de esta clase cuyos atributos contienen los datos del
     *         mensaje recibido.
     * @throws IOException
     */
    public static PeerMessage readMessageFromInputStream(DataInputStream dis) throws IOException {
        /*
         * TODO: (Boletín MensajesBinarios) En función del tipo de mensaje, leer del
         * socket a través del "dis" el resto de campos para ir extrayendo con los
         * valores y establecer los atributos del un objeto DirMessage que contendrá
         * toda la información del mensaje, y que será devuelto como resultado. NOTA:
         * Usar dis.readFully para leer un array de bytes, dis.readInt para leer un
         * entero, etc.
         */
        PeerMessage message = new PeerMessage();
        byte opcode = dis.readByte();
        message.setOpcode(opcode);
        
        switch (opcode) {
            case PeerMessageOps.OP_GET_CHUNK:
                message.fileHash = dis.readNBytes(20); // SHA-1 son 20 bytes
                message.offset = dis.readLong();
                message.size = dis.readInt();
                break;
                
            case PeerMessageOps.OP_FILE_CHUNK:
                message.offset = dis.readLong();
                message.size = dis.readInt();
                message.chunkData = new byte[message.size];
                dis.readFully(message.chunkData);
                break;
                
            case PeerMessageOps.OP_FILENAME_TO_SAVE:
                message.size = dis.readInt();
                byte[] nameBytes = new byte[message.size];
                dis.readFully(nameBytes);
                message.fileName = new String(nameBytes);
                break;
                
            case PeerMessageOps.OP_FILE_HASH:
                message.fileHash = dis.readNBytes(20); // SHA-1 son 20 bytes
                break;
                
            case PeerMessageOps.OP_FILE_NOT_FOUND:
                // No hay campos adicionales
                break;
                
            default:
                System.err.println("Unknown message opcode: " + opcode);
                throw new StructureViolationException("Unknown message opcode");
        }
        return message;
    }

    public void writeMessageToOutputStream(DataOutputStream dos) throws IOException {
        /*
         * TODO (Boletín MensajesBinarios): Escribir los bytes en los que se codifica el
         * mensaje en el socket a través del "dos", teniendo en cuenta opcode del
         * mensaje del que se trata y los campos relevantes en cada caso. NOTA: Usar
         * dos.write para leer un array de bytes, dos.writeInt para escribir un entero,
         * etc.
         */
        dos.writeByte(opcode);
        switch (opcode) {
            case PeerMessageOps.OP_GET_CHUNK:
                dos.write(fileHash);
                dos.writeLong(offset);
                dos.writeInt(size);
                break;
                
            case PeerMessageOps.OP_FILE_CHUNK:
                dos.writeLong(offset);
                dos.writeInt(size);
                dos.write(chunkData);
                break;
                
            case PeerMessageOps.OP_FILENAME_TO_SAVE:
                dos.writeInt(size);
                dos.write(fileName.getBytes());
                break;
                
            case PeerMessageOps.OP_FILE_HASH:
                dos.write(fileHash);
                break;
                
            case PeerMessageOps.OP_FILE_NOT_FOUND:
                // No hay campos adicionales
                break;
                
            default:
                System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode);
                throw new StructureViolationException("Unknown message opcode");
        }
    }
}