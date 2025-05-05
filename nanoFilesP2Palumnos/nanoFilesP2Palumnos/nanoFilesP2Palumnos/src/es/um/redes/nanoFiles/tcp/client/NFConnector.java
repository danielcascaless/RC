package es.um.redes.nanoFiles.tcp.client;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import es.um.redes.nanoFiles.util.FileInfo;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor
public class NFConnector {
	
	private Socket socket; // Socket para la conexión TCP con el servidor
	private InetSocketAddress serverAddr; // Dirección del servidor
	private DataInputStream dis; // Stream para recibir datos del servidor
	private DataOutputStream dos; // Stream para enviar datos al servidor
	
	public NFConnector(InetSocketAddress fserverAddr) throws UnknownHostException, IOException {
		this.serverAddr = fserverAddr;
		/*
		 * TODO: (Boletín SocketsTCP) Se crea el socket a partir de la dirección del
		 * servidor (IP, puerto). La creación exitosa del socket significa que la
		 * conexión TCP ha sido establecida.
		 */
		this.socket = new Socket(serverAddr.getAddress(), serverAddr.getPort()); // Establece la conexión TCP
		/*
		 * TODO: (Boletín SocketsTCP) Se crean los DataInputStream/DataOutputStream a
		 * partir de los streams de entrada/salida del socket creado. Se usarán para
		 * enviar (dos) y recibir (dis) datos del servidor.
		 */
		this.dis = new DataInputStream(socket.getInputStream()); // Inicializa el stream de entrada
		this.dos = new DataOutputStream(socket.getOutputStream()); // Inicializa el stream de salida
	}

	public void test() {

		final int TEST_VALUE = 42; // Valor que se enviará al servidor
		/*
		 * TODO: (Boletín SocketsTCP) Enviar entero cualquiera a través del socket y
		 * después recibir otro entero, comprobando que se trata del mismo valor.
		 */
		try {
			
			dos.writeInt(TEST_VALUE); // Envía el valor al servidor
			dos.flush(); // Asegura que los datos se envíen inmediatamente
			int receivedValue = dis.readInt(); // Recibe el valor desde el servidor
			// Verifica si el valor recibido es igual al enviado
			if (TEST_VALUE == receivedValue) {
				System.out.println("Test exitoso con " + serverAddr + ". Valor: " + receivedValue);
			} else {
				System.err.println("Test fallido: valor enviado (" + TEST_VALUE + "), valor recibido (" + receivedValue + ")");
			}
		} catch (IOException e) {
			System.err.println("Error durante el test: " + e.getMessage()); // Manejo de errores
		}
	}

    public void close() throws IOException {
        if (dis != null) dis.close();
        if (dos != null) dos.close();
        if (socket != null) socket.close();
    }
	
	public InetSocketAddress getServerAddr() {
		return serverAddr; // Devuelve la dirección del servidor
	}
	
	public boolean uploadFile(FileInfo fileInfo) throws IOException {
	    // Conecta al servidor y envía OP_UPLOAD con fileInfo
	    return true;  // Devuelve true si la subida fue exitosa
	}
	
	public boolean downloadChunk(String fileHash, String localFileName) throws IOException {
	    // Envía OP_GET_CHUNK con fileHash al servidor
	    // Recibe datos y guarda el trozo en localFileName
	    return true;  // Devuelve true si el chunk se descargó bien
	}

}