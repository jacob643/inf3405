import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.*;
import java.util.Date;
import java.text.SimpleDateFormat;

public class ClientHandler extends Thread {
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
				System.out.println("Couldn't close the socket");
			}
			System.out.println("Connexion with client " + clientNumber + " closed");
		}
		
		
	}
	
	public boolean readCommands() throws IOException
	{
		boolean res = true;	
		

		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		DataInputStream in = new DataInputStream(socket.getInputStream());		
		String cmd = in.readUTF();
		String[] cmdWord = cmd.split(" ", 2);
		
		System.out.println("[" + socket.getInetAddress() + ":" + socket.getLocalPort() + ":" + socket.getPort() + " - " + getTime() + "] > " + cmd);
	
		
		if(cmdWord[0].equalsIgnoreCase("exit"))
		{
			out.writeUTF("quit : done !");
			out.writeUTF("end");
			res = false;
		}
		else if(cmdWord[0].equalsIgnoreCase("ls"))
		{
			lsCMD(out);
		}
		else if(cmdWord[0].equalsIgnoreCase("cd"))
		{
			cdCMD(out, cmdWord);
		}
		else if(cmdWord[0].equalsIgnoreCase("mkdir"))
		{
			mkdirCMD(out, cmdWord[1]);
		}
		else if(cmdWord[0].equalsIgnoreCase("download"))
		{
			downloadCMD(out, cmdWord[1]);
		}
		else if(cmdWord[0].equalsIgnoreCase("upload"))
		{
			uploadCMD(out, cmdWord[1]);
		}
		else
		{
			out.writeUTF("Wrong command...");
			out.writeUTF("end");
		}

		return res;
	}
	
	private void uploadCMD(DataOutputStream outStream, String name) throws IOException
	{
		DataInputStream in = new DataInputStream(socket.getInputStream());
		OutputStream out = null;
		
		String msgFromClient = in.readUTF();
		if (msgFromClient.equals("upload error"))
		{
			return;
		}

		int i = 0;
		boolean work = false;
		Path newFilePath = Paths.get(currentPath + "//" + name);
		do
		{
			if (i != 0)
			{
				newFilePath = Paths.get(currentPath + "//" + "(copy " + Integer.toString(i) + ")" + name);
			}
			try
			{
				out = Files.newOutputStream(newFilePath, StandardOpenOption.CREATE_NEW);
				work = true;
			}
			catch(Exception e) {i++;}
			
		} while (!work);
		
		long lengthToRead = in.readLong();
		byte[] chunk = new byte[1000];
		int length;
		
		while(lengthToRead > 0)
		{	
			length = in.read(chunk);
			out.write(chunk, 0, length);
			lengthToRead -= length;
		}
		out.close();
	}

	private void downloadCMD(DataOutputStream out, String name) throws IOException
	{
		Path filePath = Paths.get(currentPath.toString() + "\\" + name);

		File file = new File(filePath.toString());
		if (!file.exists() || !file.isFile()) 
		{
			out.writeUTF("download error");
			return;
		}
		else
		{
			out.writeUTF("no error");
		}

		InputStream in = Files.newInputStream(filePath, StandardOpenOption.READ);
		
		long LengthToWrite = file.length();
				
		out.writeLong(LengthToWrite);
		byte[] chunk = new byte[1000];

		while(LengthToWrite > 0)
		{
			int length = in.read(chunk);
			out.write(chunk, 0 , length);
			LengthToWrite -= length;
		}
		in.close();
	}
	
	private void mkdirCMD(DataOutputStream out, String name)
	{
		Path newFolderPath = Paths.get(currentPath.toString(), name);
		if(Files.notExists(newFolderPath))
		{
			try
			{
				Files.createDirectory(newFolderPath);
				
				out.writeUTF("The folder " + name + " have been created in " + currentPath.toString());
				out.writeUTF("end");
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		else
		{
			
			try
			{			
				out.writeUTF("cd error : The directory " + name + " already exist !");
				out.writeUTF("end");
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	private void lsCMD(DataOutputStream out) 
	{
		try
		{
			String[] currentPathList = currentPath.toFile().list();
			if(currentPathList == null)
			{
				out.writeUTF("ls : this folder is empty");
				out.writeUTF("end");
				return;
			}
			
			for (int i=0; i<currentPathList.length; i++)
			{
				String test = currentPathList[i];
				String[] currentPathListDivided = test.split("\\.");
				if(currentPathListDivided.length != 1)
				{
					out.writeUTF("[FILE] " + currentPathList[i]);
				}
				else
				{
					out.writeUTF("[FOLDER] " + currentPathList[i]);
				}
				
			}
			out.writeUTF("end");
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void cdCMD(DataOutputStream out, String[] cmdWord)
	{
		String newPath = currentPath.toString();

		try
		{
			
			//testing for no arguments
			if(cmdWord.length ==1)
			{
				out.writeUTF("cd : No arguments");
				out.writeUTF("end");
			}
			
			
			boolean newPathExist = false;
			
			if(cmdWord[1].equals("."))
			{
				out.writeUTF("end");
				return;//we don't want to be in the folder C:\.\.\. because it's the same as C:
			}
			
			// check for parent folder
			if(cmdWord[1].equals(".."))
			{
				newPath = currentPath.getParent().toString();
				newPathExist = true;
			}
			
			// check for root folder
			if(cmdWord[1].equals("..."))
			{
				newPath = "C:/";
				newPathExist = true;
			}
			
			// check for folders in the current path
			if(!newPathExist)
			{
				String followingPath = cmdWord[1];
				
				String[] currentPathList = currentPath.toFile().list();
				for (int i=0; i<currentPathList.length && !newPathExist; i++)
				{
					newPathExist = currentPathList[i].equals(followingPath);
				}
				newPath = newPath + "\\" + followingPath;
			}

			// if we haven't found a solution by now, the path doesn't exist
			if(!newPathExist)
			{
				out.writeUTF("cd error : The path " + newPath + " doesn't exist !");
				out.writeUTF("end");
				return;
			}
			
			// that path is now the current path
			currentPath = Paths.get(newPath);
			out.writeUTF("You are now in the folder : " + currentPath.toString());
			out.writeUTF("end");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static String getTime()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd @ HH:mm:ss");
		return formatter.format(new Date());
	}
}
