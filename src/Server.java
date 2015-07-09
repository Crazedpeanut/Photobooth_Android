import java.io.BufferedInputStream;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{
	public static void main(String[] args)
	{
		final String ipAddress = "0.0.0.0";
		final int port = 9999;
		
		boolean keepRunning = true;

		ServerSocket server = null;

		try
		{
			server = new ServerSocket(port);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return;
		}
		
		System.out.println("Starting Server");

		while(keepRunning)
		{
			Socket socket;
			try 
			{
				socket = server.accept();
				retrieveData(socket);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		
		System.out.println("Closing Server");	
		
		try
		{
			server.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void retrieveData(Socket socket)
	{
		BufferedInputStream bufferedInputStream;
		ObjectInputStream objectInputStream;
		FilesSerializable filesSerializable = new FilesSerializable();

		System.out.println("New Client Connection: " + socket.getInetAddress().toString() + ":" + socket.getPort());
		
		try
		{
			bufferedInputStream = new BufferedInputStream(socket.getInputStream());
			objectInputStream = new ObjectInputStream(bufferedInputStream);

			filesSerializable = (FilesSerializable)objectInputStream.readObject();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}


		for(String fileName : filesSerializable.fileNames)
		{
			System.out.println(fileName);
		}


	}
}