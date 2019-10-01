import java.util.Scanner;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Server {
	
	private static String ip = "";
	private static int port = 0;
	private static Scanner userInput = new Scanner(System.in);
	private static Path currentPath = Paths.get("C:/");
	private static ServerSocket Listener;
	
	public static void main(String[] args) throws Exception
	{

		System.out.println(currentPath.toString());
		
		
		while(!requestValidAdress());//call init until we have valid entries
		
		
		
		int clientNumber = 0;
		
		Listener = new ServerSocket();
		Listener.setReuseAddress(true);
		
		Listener.bind(new InetSocketAddress(ip, port));		
		
		System.out.println(getTime() + " address: " + ip + ":" + port);
			
		try
		{
			while (true)
			{
				new ClientHandler(Listener.accept(), clientNumber++, currentPath).run();
			}
		}
		finally
		{
			Listener.close();
		}
		
	}
	
	private static String getTime()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd @ HH:mm:ss");
		return formatter.format(new Date());
	}
	
	private static void showRules()
	{
		System.out.println("What is your address ?");
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
	
}



