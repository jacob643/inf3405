import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
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
		
		while(readCommands());
		
		System.out.println("quit done !");
		
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
	
	private static boolean readCommands()
	{
		boolean res = true;
		String cmd = userInput.nextLine();		
		String[] cmdWord = cmd.split(" ");
		
		if(cmdWord[0].equals("quit"))
		{
			System.out.println("quitting...");
			res = false;
		}
		
		try
		{
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());	
			out.writeUTF(cmd);
		}
		catch (IOException e)
		{
			System.out.println("Error handling command " + cmd + "; " + e);
		}
		return res;
	}
}