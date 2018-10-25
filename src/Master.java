/* Course: COMP-4320 Introduction to Networks
 * Institution: Auburn University
 * Submitting: Laboratory Assignment 2
 * Date: October 25, 2018
 * Authors: Paul Ryu, William Atkinson, Abriana Fornis
 * Emails: phc0004@auburn.edu, wja0007@auburn.edu, adf0018@auburn.edu
 * Resources used: JetBrains CLion IDE, Github, Sublime Text 3, Visual Studio Code
 *
 * Description: The objective of this lab is to create a virtual ring of nodes over the Internet. After joining
 *             the ring, the nodes on the ring MUST use only the ring to communicate. The ring is managed by a
 *             master which is part of the ring and has Ring ID 0 (zero). All the other nodes of the ring are slave
 *             nodes (clients) with a ring ID assigned by the master.
*/

import java.net.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;
import java.nio.ByteBuffer;

public class Master {
	private static final int BUFSIZE = 32; // Size of receive buffer

	public static void main(String[] args) throws IOException {
		//Confirms correct arg
		if (args.length != 1) {
			throw new IllegalArgumentException("");
		}

		//Server socket setup
		int masterPort = Integer.parseInt(args[0]);
		ServerSocket masterSocket = new ServerSocket(masterPort);

        //Variables
        byte nextRID = 0;
        byte[] nextSlaveIP = InetAddress.getLocalHost().getAddress();
        byte[] request;


        //Continuous loop for slave connections
		while(true) {
			int replyLength = 10;
			byte[] reply = new byte[replyLength];
			reply[1] = 0x4A;
			reply[2] = 0x6F;
			reply[3] = 0x79;
			reply[4] = 0x21;
			request = new byte[BUFSIZE];
			Socket slaveSocket = masterSocket.accept();

			//Debug print
			System.out.println("Connected with " + slaveSocket.getInetAddress().getHostAddress()
			    + " , port: " + slaveSocket.getPort() + "\n");

			//Get slave address info to update nextSlaveIP later
			byte[] slaveAddress = slaveSocket.getInetAddress().getAddress();

			InputStream in = slaveSocket.getInputStream();
			OutputStream out = slaveSocket.getOutputStream();
			in.read(request);

			//Debug print
			System.out.println("Request:");
			System.out.print(request[0] + " ");
			for (int i = 1; i < 5; i++) {
				System.out.print(Integer.toHexString((int)request[i]) + " ");
				}
			System.out.println();

			nextRID++;
			byte GID = request[0];
			reply[0] = GID;
			reply[5] = nextRID;
			reply[6] = nextSlaveIP[0];
			reply[7] = nextSlaveIP[1];
			reply[8] = nextSlaveIP[2];
			reply[9] = nextSlaveIP[3];

			//Update nextSlaveIP to slave address instead of master
			nextSlaveIP = slaveAddress;

            //End it all, on to the next one
			out.write(reply, 0, replyLength);

			//Debug print
			System.out.println("New slave added to ring, with ID: " + nextRID);

			slaveSocket.close();
		}
	}
}
