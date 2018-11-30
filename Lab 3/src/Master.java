/* Course: COMP-4320 Introduction to Networks
 * Institution: Auburn University
 * Submitting: Laboratory Assignment 3
 * Date: November 29, 2018
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
 *      Step 2: java Master
 *
 */

//---------------------------------- Imports ----------------------------------

import java.net.*;
import java.util.Arrays;
import java.util.Scanner;
import java.io.InputStream;
import java.io.OutputStream;


/*
//------------------------------ Main Master Class ----------------------------
// Class name: Master
// Function: Main class, acts as the entry point for code and functions for Master.
// Main variables: GID, RID, Port, SlaveIP
*/
public class Master {

    //------------------------------- Variables -------------------------------
    private byte RING_ID;
    private int NEXT_SLAVE_PORT;
    private short PORT_NUMBER;
    private String NEXT_SLAVE_IP;
    private InetAddress NEXT_ADDRESS;
    private static final int OUR_GROUP_ID = 22;
    private static final int MAGIC_NUMBER = 0x4A6F7921;
    private static final int CALCULATED_PORT_NUMBER = 10010 + (OUR_GROUP_ID % 30) * 5;

    // Method name: main
    // Function: main() function, entry point for arguments put into command line
    // Behaviors: Open and close socket
    public static void main(String[] args) {

        // Ensure arguments are valid
        if (args.length > 1)
            throw new IllegalArgumentException("Parameter(s): [<Port>]");

        short masterPortNumber = (args.length == 1) ? Short.parseShort(args[0]) : CALCULATED_PORT_NUMBER;

        final Master master = new Master(masterPortNumber);
        Thread thread = new Thread(master::getNode);
        Thread thread2 = new Thread(master::listen);
        thread.start();
        thread2.start();
        master.getUserInput();

    }

    // Constructor for Master.java
    public Master(short PORT_NUMBER) {
        this.PORT_NUMBER = PORT_NUMBER;
        this.RING_ID = 1;
        try {
            this.NEXT_SLAVE_IP = InetAddress.getLocalHost().getHostAddress();
            this.NEXT_ADDRESS = InetAddress.getByName(this.NEXT_SLAVE_IP);
            this.NEXT_SLAVE_PORT = 10010 + (OUR_GROUP_ID * 5);
            System.out.println("Master IP: " + this.NEXT_SLAVE_IP);
        } catch (Exception e) {
            System.out.println("Error.");
        }
    }


    /*
    //---------------------------- ConfirmMaster Class --------------------------
    // Class name: ConfirmMaster
    // Function: Handles data operations for Confirmation, such as bitwise AND'ing
    // and iterating through message bytes after confirming the Slave input.
    // Main variables: index, request
    */
    public class ConfirmMaster {

        //--------------------- Variables ----------------------
        private int index = 0;
        private byte[] request;

        // Constructor
        ConfirmMaster(int size) {
            this.request = new byte[size];
        }

        // Constructor Overload
        ConfirmMaster(byte[] request) {
            this.request = request;
        }

        // Get pointer position
        int getIndex() {
            return index;
        }

        // Iterate through the data and return the message.
        private String readMessage(int messageLength) {

            // Use bytearray like Python
            byte[] data = new byte[messageLength];

            // Iteration
            for (int i = 0; i < messageLength; i++)

                //Increment by moving the pointer
                data[i] = request[index++];
            return new String(data);
        }

        // Similar to Slave.py --> Converted to Java, pack by bytes.
        void packMessage(String messageToPack) {

            // Message constraint: 64 bytes
            String byteString = messageToPack.length()
                    > 64 ? messageToPack.substring(0, 64)
                    : messageToPack;

            // Py bytearray
            byte[] data = byteString.getBytes();

            // Iterate through, opposite of reading.
            for (byte elem : data)
                request[index++] = elem;
        }

        // Get JoinRequest obj
        byte[] getRequest() {
            return request;
        }

        // Overload with index
        byte[] getRequest(int index) {
            return Arrays.copyOfRange(this.request, 0, index);
        }

        // Method name: packByte
        // Variables: data, index
        void packByte(int data) {
            request[index++] = (byte) data;
        }

        // # Method name: bytesAND
        // # Function: Performing an "unsigned char" operation for two's complement arithmetic.
        // # Variables: bytesToTransform
        byte bytesAND() {
            return (byte) (request[index++] & 255);
        }

        // # Method name: packAllBytes
        // # Function: This function packs the bytes into the message being used for the
        // #           join request.
        // # Variables: index, data
        void packAllBytes(int data) {
            request[index++] = (byte) (data >> 24);
            request[index++] = (byte) (data >> 16);
            request[index++] = (byte) (data >> 8);
            request[index++] = (byte) (data);
        }

        // # Method name: getAllBytes
        // # Function: This function shifts the bits in order to get the correct
        // #           indexes for byte extraction.
        // # Variables: index
        int getAllBytes() {

            // bytesAND use optional here
            return ((request[index++] & 255)

                    //Third
                    << 24) + ((request[index++] & 255)

                    //Second
                    << 16) + ((request[index++] & 255)

                    //First
                    << 8)  +  (request[index++] & 255);
        }
    }

    //  ------------- Class To Handle Join Request Functions -------------
    //  Class name: JoinRequest
    //  Input: Bytes of data, specifically the request to join the node ring.
    //  Variables: request, index
    private class JoinRequest {

        //--------------------- Variables ----------------------
        private byte GROUP_ID;
        private int MAGIC_NUMBER;
        private String IP_ADDRESS;
        private byte[] requestData;

        // Constructor
        JoinRequest(String IP_ADDRESS, byte[] requestData) {
            this.IP_ADDRESS = IP_ADDRESS;
            this.requestData = requestData;
        }

        // Getter for GID
        byte getGroupID() {
            return GROUP_ID;
        }

        // Getter for Magic Num
        int getMagic() {
            return MAGIC_NUMBER;
        }

        // Getter for IP
        String getIP() {
            return IP_ADDRESS;
        }

        // Bitwise AND the GID and get the Magic Number
        void readBytes() {
            ConfirmMaster confirmMaster = new ConfirmMaster(this.requestData);
            this.GROUP_ID = confirmMaster.bytesAND();
            this.MAGIC_NUMBER = confirmMaster.getAllBytes();
        }
    }

    //  ------------- Class To Handle Confirmation Functions -------------
    //  Class name: Confirmation
    //  Input: Handles basic variable initializations and getting Slave
    //         information after accepting the socket connection.
    //  Variables: RID, GID, Magic Number, IP
    private class Confirmation {

        //--------------------- Variables ----------------------
        private byte RING_ID;
        private byte GROUP_ID;
        private int MAGIC_NUMBER;
        private int NEXT_SLAVE_IP;

        // Constructor for Confirmation class
        Confirmation(int groupID, int magicNumber, byte ringID, String nextSlaveIP) {
            this.MAGIC_NUMBER = magicNumber;
            this.RING_ID = ringID;
            this.GROUP_ID = (byte) groupID;
            this.NEXT_SLAVE_IP = this.parseIP(nextSlaveIP);
        }

        // Getter for GID
        byte getGroupID() {
            return GROUP_ID;
        }

        // Getter for Magic Num
        int getMagic() {
            return MAGIC_NUMBER;
        }

        // Getter for RID
        byte getRingID() {
            return RING_ID;
        }

        // Getter for Slave IP
        int getSlave() {
            return NEXT_SLAVE_IP;
        }

        // Abstract Data Type (ADT) for getting message.
        ConfirmMaster getRequest() {
            ConfirmMaster messageToPack = new ConfirmMaster(10);
            messageToPack.packByte(this.getGroupID());
            messageToPack.packAllBytes(this.getMagic());
            messageToPack.packByte(this.getRingID());
            messageToPack.packAllBytes(this.getSlave());
            return messageToPack;
        }

        // Converting the IP.
        private int parseIP(String ipAddress) {

            // Initialize an int
            int FORMATTED_IP_ADDRESS = -1;

            // Switched Regex from String[] IPAddress = "\\.".split(ipAddress);
            // Weird outputs if not.
            // xxx.xxx.x.x required
            String[] IPAddress = ipAddress.split("\\.");

            // Iterate through dotted IP
            for (String elem : IPAddress) {

                // String --> Int
                int x = Integer.parseInt(elem);
                FORMATTED_IP_ADDRESS = FORMATTED_IP_ADDRESS
                        << 8 | (x & 0xFF);
            } return FORMATTED_IP_ADDRESS;
        }

    }

    //  ------------- Class To MessageConstruction Functions -------------
    //  Class name: MessageConstruction
    //  Input: Handles basic variable initializations and getting Slave
    //         information after accepting the socket connection.
    //  Variables: RID, GID, Magic Number, IP,j Checksum, TTL, Arr Node, Dest Node
    private class MessageConstruction {

        //--------------------- Variables ----------------------
        private byte CHECKSUM;
        private byte GROUP_ID;
        private String message;
        private int MAGIC_NUMBER;
        private byte ARRIVAL_NODE;
        private byte DESTINATION_NODE;
        private short TIME_TO_LIVE = 255;

        // Constructor for MessageConstruction class
        MessageConstruction(int destinationNode, int arrivalNode, String message) {
            this.message = message;
            this.GROUP_ID = OUR_GROUP_ID;
            this.CHECKSUM = this.getCheckSum();
            this.ARRIVAL_NODE = (byte) arrivalNode;
            this.MAGIC_NUMBER = Master.MAGIC_NUMBER;
            this.DESTINATION_NODE = (byte) destinationNode;
        }

        // Overload, stub, not much use.
        MessageConstruction(ConfirmMaster stub) {
            this.readMessage(stub);
        }

        // Getter for message
        String getMessage() {
            return message;
        }

        // Getter for the node to send
        byte getDestinationNode() {
            return DESTINATION_NODE;
        }

        // Getter for the Checksum
        short getChecksum() {

            // Incompatible data types, short casted
            // equal int (255)
            return (short)(CHECKSUM & 0xFF);
        }

        // Returns the checksum after computation.
        private byte getCheckSum() {
            ConfirmMaster conf = this.packMessage();
            byte[] front = conf.getRequest(conf.getRequest().length - 1);
            short checksum = 0;
            for(byte x : front) {
                checksum += (x & 0xFF);
                byte errorBit = (byte)(checksum >> 8);
                if(errorBit > 0) {
                    checksum &= 0xFF;
                    checksum += errorBit;
                }
            //Incompatible type casting errors
            } checksum = (byte)((~checksum));
            return (byte)checksum;
        }

        private void readMessage(ConfirmMaster confirmMaster) {
            this.GROUP_ID = confirmMaster.bytesAND();
            this.MAGIC_NUMBER = confirmMaster.getAllBytes();
            this.TIME_TO_LIVE = (short)(confirmMaster.bytesAND() & 0xFF);
            this.DESTINATION_NODE = confirmMaster.bytesAND();
            this.ARRIVAL_NODE = confirmMaster.bytesAND();
            this.message = confirmMaster.readMessage(
                    confirmMaster.getRequest().length
                            - 1 - confirmMaster.getIndex());
            this.CHECKSUM = confirmMaster.bytesAND();
        }

        // Abstract Data Type (ADT) for getting message.
        private ConfirmMaster packMessage() {
            ConfirmMaster messageToPack = new ConfirmMaster(9 + this.message.length());
            messageToPack.packByte(this.GROUP_ID);
            messageToPack.packAllBytes(this.MAGIC_NUMBER);
            messageToPack.packByte(this.TIME_TO_LIVE);
            messageToPack.packByte(this.DESTINATION_NODE);
            messageToPack.packByte(this.ARRIVAL_NODE);
            messageToPack.packMessage(this.message);
            messageToPack.packByte(this.CHECKSUM);
            return messageToPack;
        }

        // Check to see if the checksums are equivalent.
        boolean ifValid() {
            byte checksum = this.getCheckSum();
            if(checksum != this.CHECKSUM) {
                System.out.println("Try again. Checksum: "
                        + (checksum & 0xFF) + " did not match: "
                        + (this.CHECKSUM & 0xFF));
                return false;
            } return true;
        }

        // Check Time To Live (TTL) to see if it has reached / exceeded 1
        boolean ifLive() {

            // Decrement from 1 / 2
            this.TIME_TO_LIVE--;

            // Exit and re-prompt
            if (this.TIME_TO_LIVE <= 1) {
                System.out.println("TTL = 1, try again.");
                return false;

            // Pass through
            } this.CHECKSUM = this.getCheckSum();
            return true;
        }

        // Handles sending the message via standard Datagram functions.
        // Datagram code was pulled from earlier lecture slides.
        void sendMessage(InetAddress master, int portNumber) {
            try {
                // Initialize
                ConfirmMaster conf = this.packMessage();
                byte[] byteArray = conf.getRequest();

                // Create the message and send it to the specified socket
                DatagramSocket ds = new DatagramSocket();
                DatagramPacket dp = new DatagramPacket(byteArray, byteArray.length, master, portNumber);
                ds.send(dp);

            // This should never occur. Putting in just in case.
            } catch (Exception e) {
                System.out.println("Message failed to deliver.");
            }
        }
    }

    // Gets the node sent by Slave and closes the socket afterwards.
    private void getNode() {

        // Try and catch required by sockets in Java
        try {

            // Create a new socket, await connection
            ServerSocket ss = new ServerSocket(this.PORT_NUMBER);
            System.out.println("TCP --- Listening on: " + this.PORT_NUMBER);
            byte[] slaveInput;

            // Accept input
            while (true) {
                Socket sock = ss.accept();
                InputStream input = sock.getInputStream();
                if(input.available() <= 0)
                    continue;
                slaveInput = new byte[input.available()];

                // Get the information
                JoinRequest request = new JoinRequest(sock.getInetAddress().getHostAddress(), slaveInput);
                request.readBytes();

                // Send back information
                Confirmation conf = this.getInputStream(request);
                OutputStream os = sock.getOutputStream();
                byte[] output = conf.getRequest().getRequest();
                os.write(output, 0, output.length);

                // Close socket
                sock.close();
            }

        // Try / catch required by .accept()
        } catch (Exception e) {
            System.out.println("Error.");
        }
    }

    // Handles infinitely listening for information from any Slave files.
    private void listen() {

        // Try and catch required by Java sockets
        try {

            // Create our own socket and broadcast our information
            DatagramSocket ds = new DatagramSocket(10010 + (OUR_GROUP_ID * 5));
            System.out.println("UDP --- Listening on: " + this.PORT_NUMBER);
            byte[] data = new byte[100];

            // Create new packet
            DatagramPacket dp =  new DatagramPacket(data, 100);
            while(true) {

                // Get information from slaves
                ds.receive(dp);
                data = new byte[dp.getLength()];
                System.arraycopy(dp.getData(), dp.getOffset(), data, 0, dp.getLength());

                // Create message and send back information to slave if
                // the TTL is valid and the Slave is able to accept information.
                ConfirmMaster conf = new ConfirmMaster(data);
                MessageConstruction message = new MessageConstruction(conf);

                // Check equivalency
                if(message.ifValid()) {
                    if(message.getDestinationNode() == 0)
                        System.out.print(message.getMessage());

                    // Check TTL
                    else if(message.ifLive())
                        message.sendMessage(this.NEXT_ADDRESS, this.NEXT_SLAVE_PORT);
                }
            }

        // Required
        } catch (Exception e) {
            System.out.println("Error.");
        }
    }

    // Infinitely get input from the user until user types CTRL + C or ends the session.
    private void getUserInput() {

        // Standard scanner to get keyboard input.
        Scanner s = new Scanner(System.in);

        // Try to get input
        try {
            while(true) {

                // Get RID
                System.out.print("Enter the Ring ID: ");
                byte RING_ID = s.nextByte();

                // Get M
                System.out.print("Enter your message here: ");
                String userInput = s.next();

                // Begin creating the message
                MessageConstruction constructedMessage =
                        new MessageConstruction(RING_ID, 0, userInput);

                // Print that message is being sent
                System.out.println("Sending: "
                        + constructedMessage.getDestinationNode()
                        + " "
                        + constructedMessage.getDestinationNode()
                        + " Checksum: " + constructedMessage.getChecksum());

                // Send the message
                constructedMessage.sendMessage(this.NEXT_ADDRESS, this.NEXT_SLAVE_PORT);
            }

        // This shouldn't ever occur. Putting in just in case.
        } catch (Exception e) {
            System.out.println("Error. User input failed.");
        }
    }

    // Handle the IS opened at the beginning. Ensure that the port number is calculated correctly.
    private Confirmation getInputStream(JoinRequest request) throws UnknownHostException {
        Confirmation conf = new Confirmation(OUR_GROUP_ID,
                request.getMagic(), this.RING_ID, this.NEXT_SLAVE_IP
        );
        this.NEXT_SLAVE_PORT = 10010 + (5 * request.getGroupID()) + this.RING_ID;
        this.NEXT_SLAVE_IP = request.getIP();
        this.NEXT_ADDRESS = InetAddress.getByName(this.NEXT_SLAVE_IP);
        this.RING_ID++;
        return conf;
    }
}