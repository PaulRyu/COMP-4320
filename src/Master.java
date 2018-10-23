/* Course: COMP-4320 Introduction to Networks
 * Institution: Auburn University
 * Submitting: Laboratory Assignment 2
 * Date: October 25, 2018
 * Authors: Paul Ryu, William Atkinson, Abriana Fornis
 * Emails: phc0004@auburn.edu, wja0007@auburn.edu, adf0018@auburn.edu
 * Resources used: JetBrains IntelliJ IDE, Github, Sublime Text 3
 *
 * Description: The objective of this lab is to create a virtual ring of nodes over the Internet. After joining
 *             the ring, the nodes on the ring MUST use only the ring to communicate. The ring is managed by a
 *             master which is part of the ring and has Ring ID 0 (zero). All the other nodes of the ring are slave
 *             nodes (clients) with a ring ID assigned by the master.
 */

// This is basic TCP server code found at ..
// Source: https://systembash.com/a-simple-java-tcp-server-and-tcp-client/
// This is not yet relevant to our code until we figure out what to do for Master.
import java.io.*;
import java.net.*;

class TCPServer {
    public static void main(String argv[]) throws Exception {
        String clientSentence;
        String capitalizedSentence;
        ServerSocket welcomeSocket = new ServerSocket(6789);

        while (true) {
            Socket connectionSocket = welcomeSocket.accept();
            BufferedReader inFromClient =
                    new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            clientSentence = inFromClient.readLine();
            System.out.println("Received: " + clientSentence);
            capitalizedSentence = clientSentence.toUpperCase() + 'n';
            outToClient.writeBytes(capitalizedSentence);
        }
    }
}