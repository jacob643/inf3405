import java.io.DataInputStream;
import java.net.Socket;

public class Client {
	private static Socket socket;
	
	public static void main(String[] args) throws Exception
	{
		String serverAddress = "127.0.0.1";
		int port = 5000;
		
		socket = new Socket(serverAddress, port);
		
		System.out.format("The server is running on %s:%d%n",  serverAddress, port);
		
		DataInputStream in = new DataInputStream(socket.getInputStream());
		
		String helloMessageFromServer = in.readUTF();
		System.out.println(helloMessageFromServer);
		
		socket.close();
	}
}