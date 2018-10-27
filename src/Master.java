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
 *
 * Master Port Number = (Group ID % 30) * 5 + 10010
 *      Step 1: 22 % 30 = 22
 *      Step 2: 22 * 5  = 110
 *      Step 3: 110 + 10010 = 10120
 *      Result: 10120
 *
 *
 * Compilation Instructions on the Tux Machines for This File (Group 22)
 *      Step 1: javac Master.java
 *      Step 2: java Master 10120
 *
 */

//---------------------------------- Imports ----------------------------------
import java.io.*;
import java.net.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.net.InetAddress;
import java.net.ServerSocket;

/*
//------------------------------ Main Master Class ----------------------------
// Class name: Master
// Function: Main class, acts as the entry point for code and functions for Master.
// Main variables: GID, RID, Port, SlaveIP
*/
public class Master {

	/*
    Method name: main
    Function: main() function, entry point for arguments put into command line
    Behaviors: Open and close socket
    */
    public static void main(String[] args) throws IOException {

        //------------------------------- Variables -------------------------------

        // Initialize NEXT_RING_ID
        byte NEXT_RING_ID = 0;

        // A byte array sent as a byte array in Slave.py
        byte[] requestFromSlave;

        /*
        https://stackoverflow.com/questions/9481865/getting-the
            -ip-address-of-the-current-machine-using-java
            This obtains the IP of the current machine.
        */
        byte[] NEXT_SLAVE_IP = InetAddress.getLocalHost().getAddress();

	    // Only the Master Port Number should be put into the command line.
		if (args.length != 1) {
			throw new IllegalArgumentException("Invalid port number or too many arguments " +
                                               "entered. Please try again.");
		}

		// Convert the Port Number entered in: java Master 10120
		int masterPortNumber = Integer.parseInt(args[0]);

		// Create new socket in Java
		ServerSocket masterSocket = new ServerSocket(masterPortNumber);

        /* Loop infinitely until new Slaves (Nodes) send join requests for the ring. */
		while(true) {

		    // Accept Slave.py
            Socket sock = masterSocket.accept();

            // Get the NEXT_SLAVE_IP of Slave.py
            byte[] incoming_NEXT_SLAVE_IP = sock.getInetAddress().getAddress();

            // Get the IP address of Slave.py
            String incomingHostAddress = sock.getInetAddress().getHostAddress();

            // Get the port number of Slave.py
            int incomingPortNumber = sock.getPort();

            // Initialize a bytearray to be read from the Slave.py join request.
            requestFromSlave = new byte[32];

            /*
            Organize print statements to display to the screen the
                IP address of Slave.py and the Port Number of Slave.py
            */
            System.out.println(" ------------------------------------------------ ");
            System.out.println("\nConnected to IP Address: " + incomingHostAddress
                    + "\n"
                    + "Port Number: " + incomingPortNumber + "\n");

            // Create a bytearray or message to send back to Slave.py
			byte[] packedMessage = new byte[10];

			// Pack the magic number in four parts due to it being hexadecimal.
			packedMessage[1] = 0x4A;
			packedMessage[2] = 0x6F;
			packedMessage[3] = 0x79;
			packedMessage[4] = 0x21;

			// Handle getting input from Slave.py
			InputStream input = sock.getInputStream();

			// Setup ability to give output to Slave.py
			OutputStream output = sock.getOutputStream();

			// Get input from Slave.py in a bytearray, size 32.
			input.read(requestFromSlave);

			// Display the magic number from the bytearray
			System.out.println("Magic Number: ");
			System.out.print(requestFromSlave[0] + " ");

			// Loop until the complete Magic Number bytes are attained.
			for (int i = 1; i < 5; i++) {
				System.out.print(Integer.toHexString((int)requestFromSlave[i]) + " ");
				} System.out.println("\n");

			// Increment the number of slaves since a slave has been added to the ring.
			NEXT_RING_ID++;

			/*
            Pack the message to send back to Slave.py
            The Group ID are the first few bytes.
            */
            byte GROUP_ID = requestFromSlave[0];
			packedMessage[0] = GROUP_ID;

			// Insert the RID and Slave IP AFTER the magic number.
			packedMessage[5] = NEXT_RING_ID;
			packedMessage[6] = NEXT_SLAVE_IP[0];
			packedMessage[7] = NEXT_SLAVE_IP[1];
			packedMessage[8] = NEXT_SLAVE_IP[2];
			packedMessage[9] = NEXT_SLAVE_IP[3];

			// Join the nodes
			NEXT_SLAVE_IP = incoming_NEXT_SLAVE_IP;

            // Complete, ready to close socket.
			output.write(packedMessage, 0, 10);

			/*
            Display to the user that the slave was successfully added to the ring
                with the index counter.
            */
            System.out.println("Slave " + NEXT_RING_ID + " attached to node ring.\n");

			// Close socket.
			sock.close();
		}
	}
}
