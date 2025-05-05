package es.um.redes.nanoFiles.udp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.LinkedList;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;


/**
 * Cliente con métodos de consulta y actualización específicos del directorio
 */

/*PABLO 1º guardado*/

public class DirectoryConnector {
	/**
	 * Puerto en el que atienden los servidores de directorio
	 */
	private static final int DIRECTORY_PORT = 6868;
	/**
	 * Tiempo máximo en milisegundos que se esperará a recibir una respuesta por el
	 * socket antes de que se deba lanzar una excepción SocketTimeoutException para
	 * recuperar el control
	 */
	private static final int TIMEOUT = 1000;
	/**
	 * Número de intentos máximos para obtener del directorio una respuesta a una
	 * solicitud enviada. Cada vez que expira el timeout sin recibir respuesta se
	 * cuenta como un intento.
	 */
	private static final int MAX_NUMBER_OF_ATTEMPTS = 5;
	/**
	 * Socket UDP usado para la comunicación con el directorio
	 */
	private DatagramSocket socket;
	/**
	 * Dirección de socket del directorio (IP:puertoUDP)
	 */
	private InetSocketAddress directoryAddress;
	/**
	 * Nombre/IP del host donde se ejecuta el directorio
	 */
	private String directoryHostname;

	public DirectoryConnector(String hostname) throws IOException {
		// Guardamos el string con el nombre/IP del host
		directoryHostname = hostname;
	
		/*
		 * TODO: (Boletín SocketsUDP) Convertir el string 'hostname' a InetAddress y
		 * guardar la dirección de socket (address:DIRECTORY_PORT) del directorio en el
		 * atributo directoryAddress, para poder enviar datagramas a dicho destino.
		 */
		
		InetAddress serverIp = InetAddress.getByName(hostname);
		this.directoryAddress = new InetSocketAddress(serverIp, DIRECTORY_PORT);
		
		/*
		 * TODO: (Boletín SocketsUDP) Crea el socket UDP en cualquier puerto para enviar
		 * datagramas al directorio
		 */

		this.socket = new DatagramSocket();
	}

	/**
	 * Método para enviar y recibir datagramas al/del directorio
	 * 
	 * @param requestData los datos a enviar al directorio (mensaje de solicitud)
	 * @return los datos recibidos del directorio (mensaje de respuesta)
	 */
	private byte[] sendAndReceiveDatagrams(byte[] requestData) {
		byte responseData[] = new byte[DirMessage.PACKET_MAX_SIZE];
		byte response[] = null;
		if (directoryAddress == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP server destination address is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"directoryAddress\"");
			System.exit(-1);

		}
		if (socket == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP socket is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"socket\"");
			System.exit(-1);
		}
		/*
		 * TODO: (Boletín SocketsUDP) Enviar datos en un datagrama al directorio y
		 * recibir una respuesta. El array devuelto debe contener únicamente los datos
		 * recibidos, *NO* el búfer de recepción al completo.
		 */
		
		DatagramPacket packetToServer = new DatagramPacket(requestData, requestData.length, this.directoryAddress);
		
		
		/*
		 * TODO: (Boletín SocketsUDP) Una vez el envío y recepción asumiendo un canal
		 * confiable (sin pérdidas) esté terminado y probado, debe implementarse un
		 * mecanismo de retransmisión usando temporizador, en caso de que no se reciba
		 * respuesta en el plazo de TIMEOUT. En caso de salte el timeout, se debe volver
		 * a enviar el datagrama y tratar de recibir respuestas, reintentando como
		 * máximo en MAX_NUMBER_OF_ATTEMPTS ocasiones.
		 */
		
		DatagramPacket packetFromServer = new DatagramPacket(responseData, responseData.length);
		int attempts = 0;
		boolean received = false;
		while (attempts < MAX_NUMBER_OF_ATTEMPTS && received == false) {
			try {
				socket.send(packetToServer);
				// Establecemos un temporizador de 1 segundo para evitar que receive se
				// bloquee indefinidamente (en caso de que el servidor no responda)
				socket.setSoTimeout(TIMEOUT);
				// Tratamos de recibir la respuesta
				socket.receive(packetFromServer);
				received = true;
			} catch (IOException e) {
				attempts++;
				if (attempts == MAX_NUMBER_OF_ATTEMPTS) {
					System.err.println("Error al recibir la respuesta del servidor.");
					e.printStackTrace();

				}
			}
		}
		
		
		String stringFromServer = new String(responseData, 0, packetFromServer.getLength());
		response = stringFromServer.getBytes();
		
		/*
		 * TODO: (Boletín SocketsUDP) Las excepciones que puedan lanzarse al
		 * leer/escribir en el socket deben ser capturadas y tratadas en este método. Si
		 * se produce una excepción de entrada/salida (error del que no es posible
		 * recuperarse), se debe informar y terminar el programa.
		 */
		/*
		 * NOTA: Las excepciones deben tratarse de la más concreta a la más genérica.
		 * SocketTimeoutException es más concreta que IOException.
		 */



		if (response != null && response.length == responseData.length) {
			System.err.println("Your response is as large as the datagram reception buffer!!\n"
					+ "You must extract from the buffer only the bytes that belong to the datagram!");
		}
		return response;
	}

	/**
	 * Método para probar la comunicación con el directorio mediante el envío y
	 * recepción de mensajes sin formatear ("en crudo")
	 * 
	 * @return verdadero si se ha enviado un datagrama y recibido una respuesta
	 */
	public boolean testSendAndReceive() {
		/*
		 * TODO: (Boletín SocketsUDP) Probar el correcto funcionamiento de
		 * sendAndReceiveDatagrams. Se debe enviar un datagrama con la cadena "ping" y
		 * comprobar que la respuesta recibida empieza por "pingok". En tal caso,
		 * devuelve verdadero, falso si la respuesta no contiene los datos esperados.
		 */
		boolean success = false;
		
		byte[] response = sendAndReceiveDatagrams("login".getBytes());
		byte[] responseRequired = "loginok".getBytes();
		if (Arrays.equals(response, responseRequired)) {
			success = true;
		}


		return success;
	}

	public String getDirectoryHostname() {
		return directoryHostname;
	}


	/**
	 * Método para "hacer ping" al directorio, comprobar que está operativo y que
	 * usa un protocolo compatible. Este método no usa mensajes bien formados.
	 * 
	 * @return Verdadero si
	 */
	public boolean pingDirectoryRaw() {
		boolean success = false;
		/*
		 * TODO: (Boletín EstructuraNanoFiles) Basándose en el código de
		 * "testSendAndReceive", contactar con el directorio, enviándole nuestro
		 * PROTOCOL_ID (ver clase NanoFiles). Se deben usar mensajes "en crudo" (sin un
		 * formato bien definido) para la comunicación.
		 * 
		 * PASOS: 1.Crear el mensaje a enviar (String "ping&protocolId"). 2.Crear un
		 * datagrama con los bytes en que se codifica la cadena : 4.Enviar datagrama y
		 * recibir una respuesta (sendAndReceiveDatagrams). : 5. Comprobar si la cadena
		 * recibida en el datagrama de respuesta es "welcome", imprimir si éxito o
		 * fracaso. 6.Devolver éxito/fracaso de la operación.
		 */
        String protocolId = NanoFiles.PROTOCOL_ID;
        String message = "ping&" + protocolId;
        byte[] response = sendAndReceiveDatagrams(message.getBytes());
        if (response != null && new String(response).equals("welcome")) {
            System.out.println("Ping successful: Directory is compatible.");
            success = true;
        } else {
            System.err.println("Ping failed: Directory response was not 'welcome'.");
        }


		return success;
	}

	/**
	 * Método para "hacer ping" al directorio, comprobar que está operativo y que es
	 * compatible.
	 * 
	 * @return Verdadero si el directorio está operativo y es compatible
	 */
	public boolean pingDirectory() {
		boolean success = false;
		/*
		 * TODO: (Boletín MensajesASCII) Hacer ping al directorio 1.Crear el mensaje a
		 * enviar (objeto DirMessage) con atributos adecuados (operation, etc.) NOTA:
		 * Usar como operaciones las constantes definidas en la clase DirMessageOps :
		 * 2.Convertir el objeto DirMessage a enviar a un string (método toString)
		 * 3.Crear un datagrama con los bytes en que se codifica la cadena : 4.Enviar
		 * datagrama y recibir una respuesta (sendAndReceiveDatagrams). : 5.Convertir
		 * respuesta recibida en un objeto DirMessage (método DirMessage.fromString)
		 * 6.Extraer datos del objeto DirMessage y procesarlos 7.Devolver éxito/fracaso
		 * de la operación
		 */
        DirMessage pingMessage = new DirMessage(DirMessageOps.OPERATION_PING, NanoFiles.PROTOCOL_ID);
        String messageToSend = pingMessage.toString();
        byte[] response = sendAndReceiveDatagrams(messageToSend.getBytes());
        DirMessage responseMessage = DirMessage.fromString(new String(response));
        if (responseMessage != null && responseMessage.getOperation().equals(DirMessageOps.OPERATION_PING_OK)) {
            System.out.println("Ping successful: Directory is compatible.");
            success = true;
        } 
		else {
            System.err.println("Ping failed: Directory response was not 'pingok'.");
        }


		return success;
	}

	/**
	 * Método para dar de alta como servidor de ficheros en el puerto indicado y
	 * publicar los ficheros que este peer servidor está sirviendo.
	 * 
	 * @param serverPort El puerto TCP en el que este peer sirve ficheros a otros
	 * @param files      La lista de ficheros que este peer está sirviendo.
	 * @return Verdadero si el directorio tiene registrado a este peer como servidor
	 *         y acepta la lista de ficheros, falso en caso contrario.
	 */
	public boolean registerFileServer(int serverPort, FileInfo[] files) {

	    // Crear mensaje DirMessage (asegúrate de tener este constructor en DirMessage)
	    DirMessage registerMessage = new DirMessage(DirMessageOps.OPERATION_REGISTER, serverPort, files);

	    // Enviar mensaje y esperar respuesta
	    byte[] registerResponse = sendAndReceiveDatagrams(registerMessage.toString().getBytes());
	    if (registerResponse == null) {
	        System.out.println("No response from directory server");
	        return false;
	    }

	    // Parsear respuesta
	    DirMessage registerAck = DirMessage.fromString(new String(registerResponse));
	    if (!registerAck.getOperation().equals(DirMessageOps.OPERATION_REGISTER_OK)) {
	        System.out.println("Bad response: " + registerAck.getOperation());
	        return false;
	    }

	    System.out.println("File server registered successfully.");
	    return true;
	}

	/**
	 * Método para obtener la lista de ficheros que los peers servidores han
	 * publicado al directorio. Para cada fichero se debe obtener un objeto FileInfo
	 * con nombre, tamaño y hash. Opcionalmente, puede incluirse para cada fichero,
	 * su lista de peers servidores que lo están compartiendo.
	 * 
	 * @return Los ficheros publicados al directorio, o null si el directorio no
	 *         pudo satisfacer nuestra solicitud
	 */
	public FileInfo[] getFileList() {
	    FileInfo[] filelist = new FileInfo[0];
	    
	    LinkedList<FileInfo> files = new LinkedList<>();
	    DirMessage messageToServer = new DirMessage(DirMessageOps.OPERATION_GET_FILELIST);
	    byte[] dataToServer = messageToServer.toString().getBytes();
	    byte[] dataFromServer = sendAndReceiveDatagrams(dataToServer);
	    String messageFromServer = new String(dataFromServer, 0, dataFromServer.length);
	    DirMessage responseFromServer = DirMessage.fromString(messageFromServer);

	    while (responseFromServer.getOperation().equals(DirMessageOps.OPERATION_FILEINFO)) {
	    	files.add(FileInfo.fromString(responseFromServer.getMessage()));
	    	
	        dataFromServer = sendAndReceiveDatagrams(dataToServer);
	        messageFromServer = new String(dataFromServer, 0, dataFromServer.length);
	        responseFromServer = DirMessage.fromString(messageFromServer);
	    }

	    if (responseFromServer.getOperation().equals(DirMessageOps.OPERATION_GET_FILELIST_OK)) {
	        filelist = new FileInfo[files.size()];
	        files.toArray(filelist);
	    }

	    return filelist;
	}


	/**
	 * Método para obtener la lista de servidores que tienen un fichero cuyo nombre
	 * contenga la subcadena dada.
	 * 
	 * @filenameSubstring Subcadena del nombre del fichero a buscar
	 * 
	 * @return La lista de direcciones de los servidores que han publicado al
	 *         directorio el fichero indicado. Si no hay ningún servidor, devuelve
	 *         una lista vacía.
	 */
	public InetSocketAddress[] getServersSharingThisFile(String filenameSubstring) {
		// TODO: Ver TODOs en pingDirectory y seguir esquema similar
		LinkedList<InetSocketAddress> servers = new LinkedList<>();
		DirMessage messageToServer = new DirMessage(DirMessageOps.OPERATION_GET_SERVERS, filenameSubstring);
		byte[] dataToServer = messageToServer.toString().getBytes();
		byte[] dataFromServer = sendAndReceiveDatagrams(dataToServer);
		String messageFromServer = new String(dataFromServer, 0, dataFromServer.length);
		DirMessage responseFromServer = DirMessage.fromString(messageFromServer);

		while (responseFromServer.getOperation().equals(DirMessageOps.OPERATION_SERVERINFO)) {
			servers.add(InetSocketAddress.createUnresolved(responseFromServer.getServerAddress(), responseFromServer.getServerPort()));
			dataFromServer = sendAndReceiveDatagrams(dataToServer);
			messageFromServer = new String(dataFromServer, 0, dataFromServer.length);
			responseFromServer = DirMessage.fromString(messageFromServer);
		}

		if (responseFromServer.getOperation().equals(DirMessageOps.OPERATION_GET_SERVERS_OK)) {
			InetSocketAddress[] serversList = new InetSocketAddress[servers.size()];
			servers.toArray(serversList);
			return serversList;
		}

		return new InetSocketAddress[0];
	}

	/**
	 * Método para darse de baja como servidor de ficheros.
	 * 
	 * @return Verdadero si el directorio tiene registrado a este peer como servidor
	 *         y ha dado de baja sus ficheros.
	 */
	public boolean unregisterFileServer() {
		boolean success = false;
		DirMessage messageToServer = new DirMessage(DirMessageOps.OPERATION_UNREGISTER);
		String messageToSend = messageToServer.toString();
		byte[] response = sendAndReceiveDatagrams(messageToSend.getBytes());
		DirMessage responseMessage = DirMessage.fromString(new String(response));

		if (responseMessage != null && responseMessage.getOperation().equals(DirMessageOps.OPERATION_UNREGISTER_OK)) {
			System.out.println("File server unregistered successfully.");
			success = true;
		} else {
			System.err.println("File server unregistration failed.");
		}

		return success;
	}

}
