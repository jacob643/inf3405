import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.*;
import java.util.Scanner;

public class Client {
	
	private static String ip = "";
	private static int port = 0;
	private static Scanner userInput = new Scanner(System.in);
	private static Socket socket;
	
	public static void main(String[] args) throws Exception
	{
		while(!requestValidAdress());//call init until we have valid entries
		
		socket = new Socket(ip, port);
		
		System.out.format("The server is running on %s:%d%n",  ip, port);
		
		DataInputStream in = new DataInputStream(socket.getInputStream());
		
		String helloMessageFromServer = in.readUTF();
		System.out.println(helloMessageFromServer);
		
		while(sendCommands());
		
		socket.close();
		System.out.println("Session closed");
	}
	
	private static void showRules()
	{
		System.out.println("Which server do you want to communicate with ?");
		System.out.println("enter ip and port between 5000 and 5050");
		System.out.println("ex. 192.168.1.101:5000");
	}
	
	private static Boolean validateFormat(String firstInput, String[] input)
	{
		////////////////////////////////////////////////////////////
		// check for valid format
		////////////////////////////////////////////////////////////
		
		if(input.length != 5)
		{
			System.out.println(firstInput + " does not have the good format--> X.X.X.X:X");
			return false;
		}
		return true;
	}
	
	private static Boolean validateIpAddress(String[] input)
	{
		////////////////////////////////////////////////////////////
		// check for valid ip address
		////////////////////////////////////////////////////////////

		for(int i = 0; i < 4; i++)
		{
			int num;
			try 
			{
				num = Integer.parseInt(input[i]);
			}
			catch(Exception e)
			{
				System.out.println(input[i] + " must be a valid number.");
				return false;
			}
			
			if(num < 0 || num > 255)
			{
				System.out.println("the number: " + input[i] + 
						" must be between 0 and 255");
				return false;
			}
		}

		ip = 	input[0] + '.' +
				input[1] + '.' +
				input[2] + '.' +
				input[3];
		return true;

	}
	
	private static Boolean validatePort(String[] input)
	{
		////////////////////////////////////////////////////////////
		// check for valid port
		////////////////////////////////////////////////////////////
		
		try 
		{
			port = Integer.parseInt(input[4]);
		}
		catch(Exception e)
		{
			System.out.println(input[4] + " must be a valid number.");
			return false;
		}

		if(port < 5000 || port > 5050)
		{
			System.out.println("port: " + port + " must be between 5000 and 5050");
			return false;
		}
		
		return true;
	}
	
	private static Boolean requestValidAdress()
	{
		showRules();
		String firstInput = userInput.nextLine();
		String[] input = firstInput.split("[:\\.]", 5 + 1);
		
		if( !validateFormat(firstInput, input) ||
			!validateIpAddress(input) ||
			!validatePort(input))
		{
			return false;
		}
		
		////////////////////////////////////////////////////////////
		// all verifications pass
		////////////////////////////////////////////////////////////
		return true;
	}
	
	private static void ReceiveDownloadAnswer(String name) throws IOException
	{
		System.out.println("in download cmd");

		DataInputStream in = new DataInputStream(socket.getInputStream());
		String home = System.getProperty("user.home");
		Path filePath = Paths.get(home + "\\Desktop\\" + name);
		OutputStream out = null;

		int i = 0;
		boolean work = false;
		do
		{
			Path newFilePath = filePath;
			if (i != 0)
			{
				newFilePath = Paths.get(filePath.toString() + Integer.toString(i));
			}
			System.out.println(newFilePath.toString());			
			try
			{
				out = Files.newOutputStream(newFilePath, StandardOpenOption.CREATE_NEW);
				work = true;
			}
			catch(Exception e) {i++;}
			
		} while (!work);
		
		System.out.println("in and out created");
		/*int readSize = 0;
		int toReadSize = Size > 1000 ? 1000 : Size;
		do
		{
			byte[] chunk = new byte[1000];
			in.read(chunk, 0, toReadSize);
			System.out.println("read done");
			out.write(chunk, readSize, toReadSize);
			System.out.println("write done in : " + filePath.toString());
			readSize += toReadSize;
		} while(Size < readSize);*/
		int length = in.readInt();
		
		byte[] chunk = new byte[length];
		in.read(chunk, 0, length);
		System.out.println("read done");
		
		out.write(chunk);
		System.out.println("write done");
	}
	
	private static boolean sendCommands()
	{
		boolean exit = false;
		String cmd = userInput.nextLine();
		String[] cmdWord = cmd.split(" ");
		System.out.println("juste apres anvoyer le split");
		
		if(cmdWord[0].equals("exit"))
		{
			System.out.println("quitting...");
			exit = true;
		}
		System.out.println("apres verif pour exit");
		System.out.println("voici la cmd: " + cmd);
		System.out.println("voici cmd[1]: " + cmdWord[0]);

		try
		{
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());	
			System.out.println("juste avant anvoyer la cmd au serv");
			out.writeUTF(cmd);
			System.out.println("juste apres anvoyer la cmd au serv");
			if(cmdWord[0].equals("download"))
			{
				System.out.println("juste avant receivedownload answer");
				ReceiveDownloadAnswer(cmdWord[1]);
			}
			else
			{
				while(ReceiveAnwser());
			}
		}
		catch (IOException e)
		{
			System.out.println("Error handling command " + cmd + "; " + e);
		}
		return !exit;
	}
	
	private static boolean ReceiveAnwser()
	{
		boolean res = true;
		try
		{
			DataInputStream in = new DataInputStream(socket.getInputStream());			
			String AnwserFromServer = in.readUTF();
			
			if(AnwserFromServer.equals("end"))
			{
				res = false;
			}
			else 
			{
				System.out.println(AnwserFromServer);

			}
		}
		catch (IOException e)
		{
			System.out.println("Error handling reception " + "; " + e);
		}
		return res;
	}

}