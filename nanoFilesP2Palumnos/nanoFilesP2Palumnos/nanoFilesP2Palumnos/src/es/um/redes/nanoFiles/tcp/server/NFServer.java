package es.um.redes.nanoFiles.tcp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;

import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileDatabase;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFServer implements Runnable {

	public static final int PORT = 10000;

	private ServerSocket serverSocket = null;
	
	private static FileDatabase fileDb;

	public NFServer(FileDatabase fileDb) throws IOException {
		/*
		 * TODO: (Boletín SocketsTCP) Crear una direción de socket a partir del puerto
		 * especificado (PORT)
		 */
		this.serverSocket = new ServerSocket(PORT);
		
		NFServer.fileDb = fileDb;
		/*
		 * TODO: (Boletín SocketsTCP) Crear un socket servidor y ligarlo a la dirección
		 * de socket anterior
		 */
		if (!serverSocket.isBound()) {
			throw new IOException("El socket no pudo ser ligado al puerto especificado.");
		}
	}

	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación.
	 * 
	 */
	public void test() {
		if (serverSocket == null || !serverSocket.isBound()) {
			System.err.println(
					"[fileServerTestMode] Failed to run file server, server socket is null or not bound to any port");
			return;
		} else {
			System.out
					.println("[fileServerTestMode] NFServer running on " + serverSocket.getLocalSocketAddress() + ".");
		}

		while (true) {
			/*
			 * TODO: (Boletín SocketsTCP) Usar el socket servidor para esperar conexiones de
			 * otros peers que soliciten descargar ficheros.
			 */
			try {
				Socket clientSocket = serverSocket.accept(); // Esperar conexiones de clientes
				System.out.println("Conexión aceptada desde: " + clientSocket.getRemoteSocketAddress());
				serveFilesToClient(clientSocket); // Delegar la comunicación al método serveFilesToClient
			} catch (IOException e) {
				System.err.println("Error al aceptar la conexión: " + e.getMessage());
				break;
			}
		}
	}

	/**
	 * Método que ejecuta el hilo principal del servidor en segundo plano, esperando
	 * conexiones de clientes.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		/*
		 * TODO: (Boletín SocketsTCP) Usar el socket servidor para esperar conexiones de
		 * otros peers que soliciten descargar ficheros DONE
		 */
		while (true) {
			try {
				Socket clientSocket = serverSocket.accept(); // Esperar conexiones de clientes
				System.out.println("Conexión aceptada desde: " + clientSocket.getRemoteSocketAddress());

				/*
				 * TODO: (Boletín TCPConcurrente) Crear un hilo nuevo de la clase
				 * NFServerThread, que llevará a cabo la comunicación con el cliente que se
				 * acaba de conectar, mientras este hilo vuelve a quedar a la escucha de
				 * conexiones de nuevos clientes (para soportar múltiples clientes). Si este
				 * hilo es el que se encarga de atender al cliente conectado, no podremos tener
				 * más de un cliente conectado a este servidor.
				 */
				Thread clientThread = new Thread(() -> serveFilesToClient(clientSocket));
				clientThread.start(); // Iniciar el hilo
			} catch (IOException e) {
				System.err.println("Error al aceptar la conexión: " + e.getMessage());
			}
		}
	}

	/*
	 * TODO: (Boletín SocketsTCP) Añadir métodos a esta clase para: 1) Arrancar el
	 * servidor en un hilo nuevo que se ejecutará en segundo plano 2) Detener el
	 * servidor (stopserver) 3) Obtener el puerto de escucha del servidor etc.
	 */
	public void startServer() {
		Thread serverThread = new Thread(this);
		serverThread.start();
		System.out.println("Servidor iniciado en segundo plano.");
	}

	public void stopServer() {
		try {
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
				System.out.println("Servidor detenido.");
			}
		} catch (IOException e) {
			System.err.println("Error al detener el servidor: " + e.getMessage());
		}
	}

	public int getPort() {
		return serverSocket != null ? serverSocket.getLocalPort() : -1;
	}

	/**
	 * Método de clase que implementa el extremo del servidor del protocolo de
	 * transferencia de ficheros entre pares.
	 * 
	 * @param socket El socket para la comunicación con un cliente que desea
	 *               descargar ficheros.
	 */
	public static void serveFilesToClient(Socket socket) {
	    try (DataInputStream dis = new DataInputStream(socket.getInputStream());
	         DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

	        while (!socket.isClosed()) {
	            PeerMessage message = PeerMessage.readMessageFromInputStream(dis);
	            byte opcode = message.getOpcode();

	            switch (opcode) {
	            
	                case PeerMessageOps.OP_GET_CHUNK:
	                    // 1. Buscar archivo en la base de datos local usando el hash
	                	FileInfo fileInfo = fileDb.lookupHashSubstring(message.getFileHash());
	                    if (fileInfo == null) {
	                        // Enviar mensaje de error si el archivo no existe
	                        PeerMessage notFoundMsg = new PeerMessage(PeerMessageOps.OP_FILE_NOT_FOUND);
	                        notFoundMsg.writeMessageToOutputStream(dos);
	                        break;
	                    }

	                    // 2. Leer el chunk solicitado del archivo
	                    byte[] chunkData = readFileChunk(fileInfo.getFilePath(), message.getOffset(), message.getSize());
	                    
	                    // 3. Enviar el chunk al cliente
	                    PeerMessage chunkMsg = new PeerMessage(
	                        PeerMessageOps.OP_FILE_CHUNK,
	                        message.getOffset(),
	                        chunkData.length,
	                        chunkData
	                    );
	                    chunkMsg.writeMessageToOutputStream(dos);
	                    break;

	                case PeerMessageOps.OP_FILE_HASH:
	                    
	                	boolean fileExists = fileDb.lookupHashSubstring(message.getFileHash()) != null;
	                    PeerMessage hashResponse = new PeerMessage(
	                        fileExists ? PeerMessageOps.OP_FILE_EXISTS : PeerMessageOps.OP_FILE_NOT_FOUND
	                    );
	                    hashResponse.writeMessageToOutputStream(dos);
	                    break;

	                default:
	                    System.err.println("Opcode desconocido: " + opcode);
	                    // Responder con error de protocolo
	                    PeerMessage errorMsg = new PeerMessage(PeerMessageOps.OPCODE_INVALID_CODE);
	                    errorMsg.writeMessageToOutputStream(dos);
	            }
	        }
	    } catch (IOException e) {
	        System.err.println("Error en comunicación con cliente " + socket.getRemoteSocketAddress() + ": " + e.getMessage());
	    }
	}

	// Método auxiliar para leer chunks de archivo
	private static byte[] readFileChunk(String filePath, long offset, int size) throws IOException {
	    try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
	        file.seek(offset);
	        byte[] chunk = new byte[size];
	        file.readFully(chunk);
	        return chunk;
	    }
	}
	
	public void start() throws IOException {
	    System.out.println("Server listening on port " + serverSocket.getLocalPort());
	    while (true) {
	        Socket clientSocket = serverSocket.accept();
	        NFServerThread clientHandler = new NFServerThread(clientSocket);
	        new Thread(clientHandler).start();
	    }
	}
	
	public void shutdown() {
	    try {
	        serverSocket.close();
	        System.out.println("Server shut down.");
	    } catch (IOException e) {
	        System.err.println("Error shutting down server: " + e.getMessage());
	    }
	}

		/*
		 * TODO: (Boletín SocketsTCP) Para servir un fichero, hay que localizarlo a
		 * partir de su hash (o subcadena) en nuestra base de datos de ficheros
		 * compartidos. Los ficheros compartidos se pueden obtener con
		 * NanoFiles.db.getFiles(). Los métodos lookupHashSubstring y
		 * lookupFilenameSubstring de la clase FileInfo son útiles para buscar ficheros
		 * coincidentes con una subcadena dada del hash o del nombre del fichero. El
		 * método lookupFilePath() de FileDatabase devuelve la ruta al fichero a partir
		 * de su hash completo.
		 */
		// Implementación pendiente de la lógica de búsqueda y envío de ficheros
	}





