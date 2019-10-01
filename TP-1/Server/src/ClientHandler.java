import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.text.SimpleDateFormat;

public class ClientHandler {
	private Socket socket;
	private int clientNumber;
	private Path currentPath;
	
	public ClientHandler(Socket socket, int clientNumber, Path currentPath)
	{
		this.socket =socket;
		this.clientNumber =clientNumber;
		this.currentPath = currentPath;
		System.out.println("New connection with clients " + clientNumber + " at " + socket);
	}
	
	public void run()
	{
		try
		{
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			
			out.writeUTF("Hello from server - you are client " + clientNumber);
			
			while(readCommands());
		}
		catch (IOException e)
		{
			System.out.println("Error handling client " + clientNumber + "; " + e);
		}
		finally
		{
			try
			{
				socket.close();
			}
			catch (IOException e)
			{
				System.out.println("Couldn't close a socket, what's going on ?");
			}
			System.out.println("Connexion with client " + clientNumber + " closed");
		}
	}
	
	public boolean readCommands()
	{
		boolean res = true;	
		
		try
		{
			DataInputStream in = new DataInputStream(socket.getInputStream());		
			String cmd = in.readUTF();
			String[] cmdWord = cmd.split(" ", 2);
			
			System.out.println("[" + socket.getInetAddress() + ":" + socket.getLocalPort() + ":" + socket.getPort() + " - " + getTime() + "]" + cmd);
		
			
			if(cmdWord[0].equalsIgnoreCase("quit"))
			{
				/****** QUIT ******/
				res = false;
			}
			else if(cmdWord[0].equalsIgnoreCase("ls"))
			{
				/****** LS ******/
				String[] currentPathList = currentPath.toFile().list();
				for (int i=0; i<currentPathList.length; i++)
				{
					System.out.println(currentPathList[i]);
				}
			}
			else if(cmdWord[0].equalsIgnoreCase("cd"))
			{
				/****** CD ******/
				
				String newPath = currentPath.toString();
				if(cmdWord[1].equals(".."))
				{
					newPath = currentPath.getParent().toString();
				}
				else if(!cmdWord[1].equals("."))
				{
					newPath += cmdWord[1] + '\\';
				}
				System.out.println("going to : " + newPath);
				currentPath = Paths.get(newPath);
			}
			else if(cmdWord[0].equalsIgnoreCase("mkdir"))
			{
				mkdirCMD(cmdWord[1]);
			}
			else
			{
				System.out.println("Wrong command...");
			}
		}
		catch (IOException e)
		{
			System.out.println("Error handling client " + clientNumber + "; " + e);
		}
		return res;
	}
	
	private Boolean mkdirCMD(String name)
	{
		Path newFolderPath = Paths.get(currentPath.toString(), name);
		if(Files.notExists(newFolderPath))
		{
			try
			{
				Files.createDirectory(newFolderPath);
				return true;
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		{
			return false;
		}
	}
	
	private static String getTime()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd @ HH:mm:ss");
		return formatter.format(new Date());
	}
}
