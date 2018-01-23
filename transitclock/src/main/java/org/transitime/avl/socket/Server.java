package org.transitime.avl.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Test program. The Server listens for data on the specified port of a socket.
 * 
 * @author Michael
 *
 */
public class Server {
	public static void main(String args[]) {
		int portNumber = 4444; //Integer.parseInt(args[0]);

		System.out.println("Server running. Listening to port " + portNumber);
		try {
			// Setup the socket
			ServerSocket serverSocket = new ServerSocket(portNumber);
			Socket clientSocket = serverSocket.accept();
			BufferedReader in =
					new BufferedReader(new InputStreamReader(
							clientSocket.getInputStream()));
			
			while (true) {
				String inputLine = in.readLine();
				System.out.println("inputLine=" + inputLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
