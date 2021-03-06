import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
	
	private static void ReceiveDownloadAnswer(String filename) throws IOException
	{
		DataInputStream in = new DataInputStream(socket.getInputStream());
		String home = System.getProperty("user.home");
		String filePath = home + "\\Desktop\\";
		OutputStream out = null;
		
		String msgFromClient = in.readUTF();
		if (msgFromClient.equals("download error"))
		{
			System.out.println("download error : Wrong argument");
			return;
		}

		int i = 0;
		boolean work = false;
		Path newFilePath = Paths.get(filePath + filename);
		do
		{
			if (i != 0)
			{
				newFilePath = Paths.get(filePath + "(copy " + Integer.toString(i) + ")" + filename);
			}
			try
			{
				out = Files.newOutputStream(newFilePath, StandardOpenOption.CREATE_NEW);
				work = true;
			}
			catch(Exception e) {i++;}
			
		} while (!work);
		
		long lengthToRead = in.readLong();
		long initial = lengthToRead;
		byte[] chunk = new byte[1000];
		int length;
		long progress = 0;
		
		System.out.println("downloading " + lengthToRead + " byte!");
		
		while(lengthToRead > 0)
		{
			length = in.read(chunk);
			out.write(chunk, 0, length);
			lengthToRead -= length;
			long newProgress = 10 - (lengthToRead*10)/initial;
			if (newProgress != progress) System.out.println(newProgress + "0%");
			progress = newProgress;
		}
		System.out.println("download done in " + newFilePath);
		out.close();
		
	}
	
	
	private static void SendUpload(DataOutputStream out, String filename) throws IOException
	{		
		String home = System.getProperty("user.home");
		Path filePath = Paths.get(home + "\\Desktop\\" + filename);

		File file = new File(filePath.toString());

		if (!file.exists() || !file.isFile()) 
		{
			System.out.println("upload error : Wrong argument");
			out.writeUTF("upload error");
			return;
		}
		else
		{
			out.writeUTF("no error");
		}
		
		InputStream in = Files.newInputStream(filePath, StandardOpenOption.READ);

		long LengthToWrite = file.length();
		
		System.out.println("uploading " + LengthToWrite + " bytes!");
		out.writeLong(LengthToWrite);
		byte[] chunk = new byte[1000];
		long initial = LengthToWrite;
		long progress = 0;
		

		while(LengthToWrite > 0)
		{
			int length = in.read(chunk);
			out.write(chunk, 0 , length);
			LengthToWrite -= length;
			long newProgress = 10 - (LengthToWrite*10)/initial;
			if (newProgress != progress) System.out.println(newProgress + "0%");
			progress = newProgress;
		}
		System.out.println("upload done !");
		in.close();
	}
	
	private static boolean sendCommands()
	{
		boolean exit = false;
		String cmd = userInput.nextLine();
		String[] cmdWord = cmd.split(" ", 2);
		
		if(cmdWord[0].equals("exit"))
		{
			System.out.println("quitting...");
			exit = true;
		}

		try
		{
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());	
			out.writeUTF(cmd);
			if(cmdWord[0].equals("download"))
			{
				ReceiveDownloadAnswer(cmdWord[1]);
			}
			else if(cmdWord[0].equals("upload"))
			{
				SendUpload(out, cmdWord[1]);
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