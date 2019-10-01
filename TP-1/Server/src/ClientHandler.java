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
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			DataInputStream in = new DataInputStream(socket.getInputStream());		
			String cmd = in.readUTF();
			String[] cmdWord = cmd.split(" ", 2);
			
			System.out.println("[" + socket.getInetAddress() + ":" + socket.getLocalPort() + ":" + socket.getPort() + " - " + getTime() + "]" + cmd);
		
			
			if(cmdWord[0].equalsIgnoreCase("quit"))
			{
				
				/****** QUIT ******/
				
				out.writeUTF("quit : done !");
				out.writeUTF("end");
				res = false;
			}
			else if(cmdWord[0].equalsIgnoreCase("ls"))
			{
				/****** LS ******/
				String[] currentPathList = currentPath.toFile().list();
				if(currentPathList == null)
				{
					System.out.println("ls : this folder is empty");
					out.writeUTF("ls : this folder is empty");
					out.writeUTF("end");
				}
				else
				{
					out.writeUTF("ls done : " + currentPath.toFile().getName().toString() + " contains :");
					System.out.println("ls : " + currentPath.toFile().getName().toString() + " contains :");
					
					for (int i=0; i<currentPathList.length; i++)
					{
						System.out.println(currentPathList[i]);
						out.writeUTF(currentPathList[i]);
					}
					out.writeUTF("end");
				}
			}
			else if(cmdWord[0].equalsIgnoreCase("cd"))
			{
				/****** CD ******/
				
				String newPath = currentPath.toString();

				if(cmdWord.length !=1)
				{
					boolean newPathExist = false;
					
					if(cmdWord[1].equals(".."))
					{
						newPath = currentPath.getParent().toString();
						newPathExist = true;
					}
					else if(cmdWord[1].equals("."))
					{
						newPath = Paths.get("C:/").toString();
						newPathExist = true;
					}
					else
					{
						String followingPath = cmdWord[1];
						for(int i=2; i<cmdWord.length;i++)
						{
							followingPath = followingPath + " " + cmdWord[i];
						}
						
						String[] currentPathList = currentPath.toFile().list();
						for (int i=0; i<currentPathList.length; i++)
						{
							if(currentPathList[i].equals(followingPath))
							{
								newPathExist = true;
							}
						}
						
						newPath = newPath + "\\" + followingPath;
					}
					
					System.out.println("cd : going to : " + newPath);
					
					if(newPathExist)
					{
						currentPath = Paths.get(newPath);
						
						System.out.println("cd : done !");	
						out.writeUTF("cd done : Your are in " + currentPath.toFile().getName().toString());
						out.writeUTF("end");
					}
					else
					{
						System.out.println("cd error : The path " + newPath + " doesn't exist !");
						out.writeUTF("cd error : The path " + newPath + " doesn't exist !");
						out.writeUTF("end");
					}
				}
				else
				{
					System.out.println("cd : No arguments");
					out.writeUTF("cd : No arguments");
					out.writeUTF("end");
				}
			}
			else if(cmdWord[0].equalsIgnoreCase("mkdir"))
			{
				mkdirCMD(cmdWord[1]);
			}
			else
			{
				System.out.println("Wrong command...");
				out.writeUTF("Wrong command...");
				out.writeUTF("end");
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
