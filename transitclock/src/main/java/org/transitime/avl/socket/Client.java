package org.transitime.avl.socket;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Test program. Client writes data to the server using a socket.
 * 
 * @author Michael
 *
 */
public class Client {
	public static void main(String args[]) {
		int portNumber = 4444; //Integer.parseInt(args[0]);
		String hostName = "localhost";
		
		System.out.println("Client running. Writing to port " + portNumber);

		try {
			Socket socket = new Socket(hostName, portNumber);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println("String 1");
			out.println("{lsdkj: [{lskdj: 32, lllx}]}");
			out.println("String 2");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
