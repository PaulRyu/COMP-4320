/* Course: COMP-4320 Introduction to Networks
 * Institution: Auburn University
 * Submitting: Laboratory Assignment 2
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
 *      Step 2: java Master 10120
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

    private void getNode() {
        try {
            ServerSocket ss = new ServerSocket(this.PORT_NUMBER);
            System.out.println("Listening on receiving PORT_NUMBER TCP: " + this.PORT_NUMBER);
            byte[] slaveInput;
            while (true) {
                Socket sock = ss.accept();
                InputStream input = sock.getInputStream();
                if(input.available() <= 0)
                    continue;
                slaveInput = new byte[input.available()];
                JoinRequest request = new JoinRequest(sock.getInetAddress().getHostAddress(), slaveInput);
                request.readBytes();
                Confirmation conf = this.getInputStream(request);
                OutputStream os = sock.getOutputStream();
                byte[] output = conf.getRequest().getRequest();
                os.write(output, 0, output.length);
                sock.close();
            }
        } catch (Exception e) {
            System.out.println("Error.");
        }
    }


    private void listen() {
        try {
            DatagramSocket ds = new DatagramSocket(10010 + (OUR_GROUP_ID * 5));
            System.out.println("Listening on: " + this.PORT_NUMBER);
            byte[] data = new byte[100];
            DatagramPacket dp =  new DatagramPacket(data, 100);
            while(true) {
                ds.receive(dp);
                data = new byte[dp.getLength()];
                System.arraycopy(dp.getData(), dp.getOffset(), data, 0, dp.getLength());
                ConfirmMaster conf = new ConfirmMaster(data);
                MessageConstruction message = new MessageConstruction(conf);
                if(message.ifValid()) {
                    if(message.getDestinationNode() == 0)
                        System.out.print(message.getMessage());
                    else if(message.ifLive())
                        message.sendMessage(this.NEXT_ADDRESS, this.NEXT_SLAVE_PORT);
                }
            }
        } catch (Exception e) {
            System.out.println("Error.");
        }
    }

    private void getUserInput() {
        Scanner s = new Scanner(System.in);
        try {
            while(true) {
                System.out.print("Enter the Ring ID: ");
                byte RING_ID = s.nextByte();
                System.out.print("Enter your message here: ");
                String userInput = s.next();
                MessageConstruction messageConstruction =
                        new MessageConstruction(RING_ID, 0, userInput);
                System.out.println("Sending: "
                        + messageConstruction.getDestinationNode()
                        + " "
                        + messageConstruction.getDestinationNode()
                        + " Checksum: " + messageConstruction.getChecksum());
                messageConstruction.sendMessage(this.NEXT_ADDRESS, this.NEXT_SLAVE_PORT);
            }
        } catch (Exception e) {
            System.out.println("Error.");
        }
    }


    private Confirmation getInputStream(JoinRequest request) throws UnknownHostException {
        Confirmation confirmation = new Confirmation(OUR_GROUP_ID, request.getMagic(), this.RING_ID, this.NEXT_SLAVE_IP
        );
        this.NEXT_SLAVE_PORT = 10010 + (5 * request.getGroupID()) + this.RING_ID;
        this.NEXT_SLAVE_IP = request.getIP_ADDRESS();
        this.NEXT_ADDRESS = InetAddress.getByName(this.NEXT_SLAVE_IP);
        this.RING_ID++;
        return confirmation;
    }

    private class JoinRequest {
        private String IP_ADDRESS;
        private byte[] requestData;
        private byte GROUP_ID;
        private int MAGIC_NUMBER;

        JoinRequest(String IP_ADDRESS, byte[] requestData) {
            this.IP_ADDRESS = IP_ADDRESS;
            this.requestData = requestData;
        }

        String getIP_ADDRESS() {
            return IP_ADDRESS;
        }

        byte getGroupID() {
            return GROUP_ID;
        }

        int getMagic() {
            return MAGIC_NUMBER;
        }

        void readBytes() {
            ConfirmMaster confirmMaster = new ConfirmMaster(this.requestData);
            this.GROUP_ID = confirmMaster.read();
            this.MAGIC_NUMBER = confirmMaster.readAllBytes();
        }
    }

    private class Confirmation {
        private byte RING_ID;
        private byte GROUP_ID;
        private int MAGIC_NUMBER;
        private int NEXT_SLAVE_IP;

        Confirmation(int groupID, int magicNumber, byte ringID, String nextSlaveIP) {
            this.MAGIC_NUMBER = magicNumber;
            this.RING_ID = ringID;
            this.GROUP_ID = (byte) groupID;
            this.NEXT_SLAVE_IP = this.parseIP(nextSlaveIP);
        }

        byte getGroupID() {
            return GROUP_ID;
        }

        int getMagic() {
            return MAGIC_NUMBER;
        }

        byte getRingID() {
            return RING_ID;
        }

        int getSlave() {
            return NEXT_SLAVE_IP;
        }

        ConfirmMaster getRequest() {
            ConfirmMaster confirmMaster = new ConfirmMaster(10);
            confirmMaster.packByte(this.getGroupID());
            confirmMaster.packAllBytes(this.getMagic());
            confirmMaster.packByte(this.getRingID());
            confirmMaster.packAllBytes(this.getSlave());
            return confirmMaster;
        }

        private int parseIP(String ipAddress) {
            int calculatedIP = 0;
            String[] IPAddress = ipAddress.split("\\.");
            for (String elem : IPAddress) {
                int x = Integer.parseInt(elem);
                calculatedIP = calculatedIP << 8 | (x & 0xFF);
            }
            return calculatedIP;
        }

    }

    private class MessageConstruction {
        private byte CHECKSUM;
        private byte GROUP_ID;
        private String message;
        private int MAGIC_NUMBER;
        private byte ARRIVAL_NODE;
        private byte DESTINATION_NODE;
        private short TIME_TO_LIVE = 255;

        MessageConstruction(int destinationNode, int arrivalNode, String message) {
            this.message = message;
            this.GROUP_ID = OUR_GROUP_ID;
            this.CHECKSUM = this.getCheckSum();
            this.ARRIVAL_NODE = (byte) arrivalNode;
            this.MAGIC_NUMBER = Master.MAGIC_NUMBER;
            this.DESTINATION_NODE = (byte) destinationNode;
        }

        MessageConstruction(ConfirmMaster stub) {
            this.readMessage(stub);
        }

        byte getDestinationNode() {
            return DESTINATION_NODE;
        }

        String getMessage() {
            return message;
        }

        short getChecksum() {
            // Incompatible data types, short casted
            return (short)(CHECKSUM & 0xFF);
        }

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
            this.GROUP_ID = confirmMaster.read();
            this.MAGIC_NUMBER = confirmMaster.readAllBytes();
            this.TIME_TO_LIVE = (short)(confirmMaster.read() & 0xFF);
            this.DESTINATION_NODE = confirmMaster.read();
            this.ARRIVAL_NODE = confirmMaster.read();
            this.message = confirmMaster.readMessage(
                    confirmMaster.getRequest().length
                            - 1 - confirmMaster.getIndex());
            this.CHECKSUM = confirmMaster.read();
        }

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

        boolean ifValid() {
            byte checksum = this.getCheckSum();
            if(checksum != this.CHECKSUM) {
                System.out.println("Computed CHECKSUM: "
                        + (checksum & 0xFF) + " did not match received CHECKSUM: "
                        + (this.CHECKSUM & 0xFF));
                return false;
            } return true;
        }

        boolean ifLive() {
            this.TIME_TO_LIVE--;
            if(this.TIME_TO_LIVE <= 1) {
                System.out.println("TIME_TO_LIVE at 1, message discarded.");
                return false;
            } this.CHECKSUM = this.getCheckSum();
            return true;
        }

        void sendMessage(InetAddress master, int portNumber) {
            try {
                ConfirmMaster confirmMaster = this.packMessage();
                byte[] byteArray = confirmMaster.getRequest();
                DatagramSocket ds = new DatagramSocket();
                DatagramPacket dp = new DatagramPacket(byteArray, byteArray.length, master, portNumber);
                ds.send(dp);
            } catch (Exception e) {
                System.out.println("Error.");
            }
        }
    }

    public class ConfirmMaster {
        private int index = 0;
        private byte[] request;

        ConfirmMaster(int size) {
            this.request = new byte[size];
        }

        ConfirmMaster(byte[] request) {
            this.request = request;
        }

        byte[] getRequest() {
            return request;
        }

        // Overload with index
        byte[] getRequest(int index) {
            return Arrays.copyOfRange(this.request, 0, index);
        }

        int getIndex() {
            return index;
        }

        void packByte(int data) {
            request[index++] = (byte) data;
        }


        byte read() {
            return (byte) (request[index++] & 255);
        }

        void packAllBytes(int data) {
            request[index++] = (byte) (data >> 24);
            request[index++] = (byte) (data >> 16);
            request[index++] = (byte) (data >> 8);
            request[index++] = (byte) (data);
        }


        int readAllBytes() {
            return ((request[index++] & 255)

                    //Third
                    << 24) + ((request[index++] & 255)

                    //Second
                    << 16) + ((request[index++] & 255)

                    //First
                    << 8)  +  (request[index++] & 255);
        }


        void packMessage(String messageToPack) {
            String byteString = messageToPack.length()
                    > 64 ? messageToPack.substring(0, 64)
                    : messageToPack;
            byte[] data = byteString.getBytes();
            for (byte elem : data)
                request[index++] = elem;
        }


        private String readMessage(int messageLength) {
            byte[] data = new byte[messageLength];
            for(int i = 0; i < messageLength; i++)
                data[i] = request[index++];
            return new String(data);
        }

    }
}