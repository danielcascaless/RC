package es.um.redes.nanoFiles.udp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Arrays;


import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFDirectoryServer {
	/**
	 * Número de puerto UDP en el que escucha el directorio
	 */
	public static final int DIRECTORY_PORT = 6868;

	/**
	 * Socket de comunicación UDP con el cliente UDP (DirectoryConnector)
	 */
	private DatagramSocket socket = null;
	/*
	 TODO: Añadir aquí como atributos las estructuras de datos que sean necesarias
	 * para mantener en el directorio cualquier información necesaria para la
	 * funcionalidad del sistema nanoFilesP2P: ficheros publicados, servidores
	 * registrados, etc.  DONE
	 */
	 
	 /**
     * Estructura de datos para mantener los ficheros publicados
     * Clave: Nombre del fichero
     * Valor: Información del fichero (FileInfo)
     */
    private HashMap<String, FileInfo> files;

    /**
     * Estructura de datos para mantener los servidores registrados
     * Clave: Dirección del servidor
     * Valor: Lista de ficheros que el servidor está compartiendo
     */
    private HashMap<InetSocketAddress, LinkedList<String>> servers;
    



	/**
	 * Probabilidad de descartar un mensaje recibido en el directorio (para simular
	 * enlace no confiable y testear el código de retransmisión)
	 */
	private double messageDiscardProbability;

	public NFDirectoryServer(double corruptionProbability) throws SocketException {
		/*
		 * Guardar la probabilidad de pérdida de datagramas (simular enlace no
		 * confiable)
		 */
		messageDiscardProbability = corruptionProbability;
		/*
		 * TODO: (Boletín SocketsUDP) Inicializar el atributo socket: Crear un socket
		 * UDP ligado al puerto especificado por el argumento directoryPort en la
		 * máquina local, DONE
		 */
		/*
		 * TODO: (Boletín SocketsUDP) Inicializar atributos que mantienen el estado del
		 * servidor de directorio: ficheros, etc.) DONE
		 */
          // Inicializar el atributo socket: Crear un socket UDP ligado al puerto especificado por el argumento directoryPort en la máquina local
        socket = new DatagramSocket(DIRECTORY_PORT);
    
        // Inicializar atributos que mantienen el estado del servidor de directorio: ficheros, etc.
        // Por ejemplo, podríamos usar un HashMap para almacenar los servidores y los archivos que comparten
        files = new HashMap<>();
        servers = new HashMap<>();


		if (NanoFiles.testModeUDP) {
			if (socket == null) {
				System.err.println("[testMode] NFDirectoryServer: code not yet fully functional.\n"
						+ "Check that all TODOs in its constructor and 'run' methods have been correctly addressed!");
				System.exit(-1);
			}
		}
	}

	public DatagramPacket receiveDatagram() throws IOException {
		DatagramPacket datagramReceivedFromClient = null;
		boolean datagramReceived = false;
		while (!datagramReceived) {
			/*
			 * TODO: (Boletín SocketsUDP) Crear un búfer para recibir datagramas y un
			 * datagrama asociado al búfer (datagramReceivedFromClient) DONE
			 */
			byte[] buffer = new byte[1024];
			datagramReceivedFromClient = new DatagramPacket(buffer, buffer.length);

			/*
			 * TODO: (Boletín SocketsUDP) Recibimos a través del socket un datagrama DONE
			 */
			socket.receive(datagramReceivedFromClient);

				// Vemos si el mensaje debe ser ignorado (simulación de un canal no confiable)
				double rand = Math.random();
				if (rand < messageDiscardProbability) {
					System.err.println(
							"Directory ignored datagram from " + datagramReceivedFromClient.getSocketAddress());
				} else {
					datagramReceived = true;
					System.out
							.println("Directory received datagram from " + datagramReceivedFromClient.getSocketAddress()
									+ " of size " + datagramReceivedFromClient.getLength() + " bytes.");
				}
			}

		
		return datagramReceivedFromClient;
	}

	public void runTest() throws IOException {

		System.out.println("[testMode] Directory starting...");

		System.out.println("[testMode] Attempting to receive 'ping' message...");
		DatagramPacket rcvDatagram = receiveDatagram();
		sendResponseTestMode(rcvDatagram);

		System.out.println("[testMode] Attempting to receive 'ping&PROTOCOL_ID' message...");
		rcvDatagram = receiveDatagram();
		sendResponseTestMode(rcvDatagram);
	}

	private void sendResponseTestMode(DatagramPacket pkt) throws IOException {
		/*
		 * TODO: (Boletín SocketsUDP) Construir un String partir de los datos recibidos
		 * en el datagrama pkt. A continuación, imprimir por pantalla dicha cadena a
		 * modo de depuración. DONE
		 */
		String messageFromClient = new String(pkt.getData(), 0, pkt.getLength());
		System.out.println("Data received: " + messageFromClient);

		/*
		 * TODO: (Boletín SocketsUDP) Después, usar la cadena para comprobar que su
		 * valor es "ping"; en ese caso, enviar como respuesta un datagrama con la
		 * cadena "pingok". Si el mensaje recibido no es "ping", se informa del error y
		 * se envía "invalid" como respuesta. DONE
		 */
		String responseMessage;
		if (messageFromClient.equals("ping")) {
			responseMessage = "pingok";
		} else if (messageFromClient.startsWith("ping&")) {
			String protocolId = messageFromClient.substring(5);
			if (protocolId.equals(NanoFiles.PROTOCOL_ID)) {
				responseMessage = "welcome";
			} else {
				responseMessage = "denied";
			}
		} else {
			responseMessage = "invalid";
		}

		/*
		 * TODO: (Boletín Estructura-NanoFiles) Ampliar el código para que, en el caso
		 * de que la cadena recibida no sea exactamente "ping", comprobar si comienza
		 * por "ping&" (es del tipo "ping&PROTOCOL_ID", donde PROTOCOL_ID será el
		 * identificador del protocolo diseñado por el grupo de prácticas (ver
		 * NanoFiles.PROTOCOL_ID). Se debe extraer el "protocol_id" de la cadena
		 * recibida y comprobar que su valor coincide con el de NanoFiles.PROTOCOL_ID,
		 * en cuyo caso se responderá con "welcome" (en otro caso, "denied"). DONE MAS MENOS
		 */
		
		byte[] responseData = responseMessage.getBytes();
		DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, pkt.getSocketAddress());
		socket.send(responsePacket);
	}

	public void run() throws IOException {

		System.out.println("Directory starting...");

		while (true) { // Bucle principal del servidor de directorio
			DatagramPacket rcvDatagram = receiveDatagram();

			sendResponse(rcvDatagram);

		}
	}

	private void sendResponse(DatagramPacket pkt) throws IOException {
	    String messageFromClient = new String(pkt.getData(), 0, pkt.getLength());
	    System.out.println("Data received: " + messageFromClient);

	    DirMessage dirMessage = DirMessage.fromString(messageFromClient);
	    String operation = dirMessage.getOperation();
	    DirMessage msgToSend = new DirMessage(DirMessageOps.OPERATION_INVALID);

	    switch (operation) {
	        case DirMessageOps.OPERATION_PING:
	            if (dirMessage.getProtocolId().equals(NanoFiles.PROTOCOL_ID)) {
	                msgToSend = new DirMessage(DirMessageOps.OPERATION_PING_OK);
	            } else {
	                msgToSend = new DirMessage(DirMessageOps.OPERATION_INVALID);
	            }
	            break;

	        case DirMessageOps.OPERATION_REGISTER:
	        	String[] fileNames = dirMessage.getMessage().split(",");
	        	LinkedList<String> fileList = new LinkedList<>(Arrays.asList(fileNames));

	            // Obtener la dirección IP del datagrama y el puerto del mensaje
	        	InetSocketAddress serverAddress = new InetSocketAddress(pkt.getAddress(), dirMessage.getServerPort());

	            // Guardar en el mapa de servidores
	            servers.put(serverAddress, fileList);
	            
	            // Guardar cada archivo en el mapa de archivos
	            for (String fileName : fileList) {
	                files.put(fileName, new FileInfo(fileName, serverAddress));
	            }

	            msgToSend = new DirMessage(DirMessageOps.OPERATION_REGISTER_OK);
	            break;


	        case DirMessageOps.OPERATION_GET_FILELIST:
	            StringBuilder sb = new StringBuilder();
	            for (String fileName : files.keySet()) {
	                sb.append(fileName).append(",");
	            }
	            if (sb.length() > 0) sb.setLength(sb.length() - 1);
	          
	            msgToSend = new DirMessage(DirMessageOps.OPERATION_GET_FILELIST_OK, null, sb.toString());
	            break;

	        default:
	            System.err.println("Unexpected message operation: \"" + operation + "\"");
	            msgToSend = new DirMessage(DirMessageOps.OPERATION_INVALID);
	    }

	    String responseMessage = msgToSend.toString();
	    byte[] responseBytes = responseMessage.getBytes();
	    DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, pkt.getSocketAddress());
	    socket.send(responsePacket);
	}

}