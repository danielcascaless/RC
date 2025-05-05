package es.um.redes.nanoFiles.util;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

/**
 * Database de archivos compartidos por este peer
 */
public class FileDatabase {
    private final Map<String, FileInfo> filesByHash;  // Mapa hash -> FileInfo
    private final Map<String, FileInfo> filesByName;  // Mapa nombre -> FileInfo

    public FileDatabase(String sharedFolder) {
        File theDir = new File(sharedFolder);
        if (!theDir.exists()) {
            theDir.mkdirs();
        }
        this.filesByHash = FileInfo.loadFileMapFromFolder(theDir);
        this.filesByName = new HashMap<>();
        
        // Indexar también por nombre
        for (FileInfo file : filesByHash.values()) {
            filesByName.put(file.fileName, file);
        }

        if (filesByHash.isEmpty()) {
            System.err.println("*ADVERTENCIA: No hay archivos en " + sharedFolder);
        }
    }

    /**
     * Obtiene todos los archivos como array
     */
    public FileInfo[] getFiles() {
        return filesByHash.values().toArray(new FileInfo[0]);
    }

    /**
     * Busca archivo por hash completo
     */
    public String lookupFilePath(String fileHash) {
        FileInfo f = filesByHash.get(fileHash);
        return (f != null) ? f.filePath : null;
    }

    /**
     * Busca archivo por subcadena del hash (nuevo)
     */
    public FileInfo lookupHashSubstring(String hashSubstring) {
        return filesByHash.values().stream()
            .filter(file -> file.fileHash.contains(hashSubstring))
            .findFirst()
            .orElse(null);
    }

    /**
     * Busca archivo por subcadena del nombre (nuevo)
     */
    public FileInfo lookupFilenameSubstring(String nameSubstring) {
        return filesByName.values().stream()
            .filter(file -> file.fileName.contains(nameSubstring))
            .findFirst()
            .orElse(null);
    }
    
    public FileInfo lookupFileInfo(String fileHash) {
        // Validación básica del hash
        if (fileHash == null || fileHash.length() != 40) {
            throw new IllegalArgumentException("Formato de hash inválido");
        }
        
        return filesByHash.get(fileHash.toLowerCase()); 
    }

    /**
     * Añade un archivo manualmente (útil para testing)
     */
    public synchronized void addFile(FileInfo file) {
        filesByHash.put(file.fileHash, file);
        filesByName.put(file.fileName, file);
    }
}