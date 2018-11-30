# Course: COMP-4320 Introduction to Networks
# Institution: Auburn University
# Submitting: Laboratory Assignment 2
# Date: October 25, 2018
# Authors: Paul Ryu, William Atkinson, Abriana Fornis
# Emails: phc0004@auburn.edu, wja0007@auburn.edu, adf0018@auburn.edu
# Resources used: JetBrains Pycharm IDE, Github, Sublime Text 3
#
# Description: The objective of this lab is to create a virtual ring of nodes over the Internet. After joining
#              the ring, the nodes on the ring MUST use only the ring to communicate. The ring is managed by a
#              master which is part of the ring and has Ring ID 0 (zero). All the other nodes of the ring are slave
#              nodes (clients) with a ring ID assigned by the master.


# ---------------------------- Imports ----------------------------
import socket
import sys
import struct

# ------------------------ Global Variables ------------------------
# Source: https://wiki.python.org/moin/TcpCommunication
# This is the socket created for the slave, or in other words - the client as in Lab 1.
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# This is the maximum amount of bytes that our packet will handle.
MAX_BYTES = 100

# Hexadecimal Address Length
ADDRESS_LENGTH = 16

# The first argument is the executable, Slave. Nothing is needed code-wise to handle this situation
#           as it is handled outside the scope of the code in this file.
# The second argument is the Master's host name.
MasterHostName = sys.argv[1]

# The third argument is the Master's port number.
MasterPortNumber = int(sys.argv[2])

# Lab Group 22
OurGroupID = 22

# Given Magic Number
MagicNumber = "0x4A6F7921"


# ------------------------- Global Methods -------------------------

# Method name: bytesAND
# Function: Performing an "unsigned char" operation for two's complement arithmetic.
# Variables: bytesToTransform
def bytesAND(byteToTransform):
    return byteToTransform & 255


# Method name: bitmask
# Function: Select the bottom bits of the original value and shift left by
#           24 bits to move it from the bottom 8 bits to the top.
# Variables:
def bitmask(bytesToMask):
    return bytesToMask & 0x000000ff


# Method names: getFirstByte and shiftFirstByte
# Function: To shift 24 bits left or right to get the desired byte.
# Variables: byte
def getFirstByte(byte):
    return byte >> 24


def shiftFirstByte(byte):
    return byte << 24


# Method name: getSecondByte and shiftSecondByte
# Function: To shift 16 bits left or right to get the desired byte.
# Variables: byte
def getSecondByte(byte):
    return byte >> 16


def shiftSecondByte(byte):
    return byte << 16


# Method name: getThirdByte and shiftThirdByte
# Function: To shift 8 bits left or right to get the desired byte.
# Variables: byte
def getThirdByte(byte):
    return byte >> 8


def shiftThirdByte(byte):
    return byte << 8


# ------------------ Error Check Before Connecting -----------------
# If the format: Slave | MasterHostName | MasterPortNumber is not followed, exit the system.
if len(sys.argv) != 3:
    print("Invalid arguments. Try again.")
    sys.exit()

# Check if the Master Port Number is between 0 and 65535.
if MasterPortNumber < 0 or MasterPortNumber > 65535:
    print("Slave Error")
    sys.exit()

# ----------------- Connecting Slave to the Master -----------------
sock.connect((MasterHostName, MasterPortNumber))


# TODO (Phase 2 only), repeatedly prompt the user for a ring ID RID and a message m.
RID_AND_MSG_RECEIVED = False
userRingID = ""
userMessage = ""
while not RID_AND_MSG_RECEIVED:
    userRingID = raw_input('RingID: ')
    userMessage = raw_input('Message to send: ')
    if len(userRingID) > 0 and len(userMessage) > 0:
        print(userRingID, userMessage)

# ------------- Class To Handle Join Request Functions -------------
# Class name: JoinRequest
# Input: Bytes of data, specifically the request to join the node ring.
# Variables: request, index
class JoinRequest:
    def __init__(self, bytesInRequest=None):
        if bytesInRequest is None:
            bytesInRequest = []
        self.index = 0
        self.request = bytearray(bytesInRequest)

    # Method name: getID
    # Output: The first byte, or ID, in the request.
    # Variables: request, index
    def getID(self):
        # Byte: Range from 0 to 255.
        # Source: https://stackoverflow.com/questions/32277760
        #                /what-is-the-point-of-bitwise-and-ing
        #                -with-255-when-using-bitshifting-method-of-b
        dataByte = bytesAND(self.request[self.index])

        # Shift the index so that it points to the next 4 bytes, such as the magic number.
        self.index += 1

        # Return the first byte, or the GID / RID.
        return dataByte

    # Method name: packByte
    # Function: This method does a bitwise AND operation with the byte to get the correct
    #  value and appends the value to the Join Request.
    # Variables: dataByte, index
    def packByte(self, byteToPack):
        # 0x000000ff is the bit-mask
        # Source: https://stackoverflow.com/questions/39719866/bit
        #                 -masking-char0xffffffb8-0xff-doesnt-work-in-c
        dataByte = bitmask(byteToPack)
        self.request.append(dataByte)
        self.index += 1

    # Method name: getAllBytes
    # Function: This function shifts the bits in order to get the correct
    #           indexes for byte extraction.
    # Variables: index
    def getAllBytes(self):
        # Shift left by 3 bytes. This gets the first byte in the WORD.
        firstByte = shiftFirstByte(bytesAND(self.request[self.index]))
        self.index += 1

        # Shift left by 2 bytes. This gets the second byte in the WORD.
        secondByte = shiftSecondByte(bytesAND(self.request[self.index]))
        self.index += 1

        # Shift left by a byte. This gets the third byte in the WORD.
        thirdByte = shiftThirdByte(bytesAND(self.request[self.index]))
        self.index += 1

        # Do not shift at all. This gets the fourth byte in the WORD.
        fourthByte = bytesAND(self.request[self.index])
        self.index += 1

        # All bytes combined. This creates the WORD given in the Request.
        allBytes = (firstByte + secondByte
                    + thirdByte + fourthByte)

        # Return WORD
        return allBytes

    # Method name: packAllBytes
    # Function: This function packs the bytes into the message being used for the
    #           join request.
    # Variables: index
    def packAllBytes(self, bytesToPack):
        firstByte = getFirstByte(bytesToPack)
        self.request.append(firstByte)
        self.index += 1

        secondByte = bitmask(getSecondByte(bytesToPack))
        self.request.append(secondByte)
        self.index += 1

        thirdByte = bitmask(getThirdByte(bytesToPack))
        self.request.append(thirdByte)
        self.index += 1

        fourthByte = bitmask(bytesToPack)
        self.request.append(fourthByte)
        self.index += 1


# Create a join request and include our Group ID for the Master.
request = JoinRequest()
request.packByte(OurGroupID)

# int(value, base)
# Base = 16 because of MagicNumber is Base 16, Hexadecimal
# Include the magic number in the join request.
request.packAllBytes(int(MagicNumber, 16))

# Send the join request.
sock.send(request.request)


# ---------- Class To Handle Message Received From Master ----------
# Class name: ConfirmMaster
# Function: This class holds variables taken from JoinRequest (too crowded)
#           and prints all information given back from the Master file.
# Variables: GID, RID, IP
class ConfirmMaster:

    # Setup all Master variables
    def __init__(self, confirmationMessage):
        self.CONFIRMATION = confirmationMessage
        self.REQUEST = JoinRequest(self.CONFIRMATION)
        self.MASTER_ID = self.REQUEST.getID()
        self.MAGIC_NUMBER = self.REQUEST.getAllBytes()
        self.RING_ID = self.REQUEST.getID()
        self.NEXT_SLAVE_IP = self.REQUEST.getAllBytes()

    # Method name: printEverything
    # Function: This function prints the information as requested from the
    #           Lab 2 specifications.
    # Variables: GID, RID, IP
    def printEverything(self):
        # print("GID of the Master: ", self.MASTER_ID)
        print("GID of the Master: %d" % self.MASTER_ID)

        # print('Own Ring ID: ', self.RING_ID)
        print("Own Ring ID: %d" % self.RING_ID)

        # Source: https://stackoverflow.com/questions/9590965/
        #         convert-an-ip-string-to-a-number-and-vice-versa
        # Source User: Not_A_Golfer
        print('IP Address in Dotted Decimal Form: %s' %
              socket.inet_ntoa(struct.pack('!L', self.NEXT_SLAVE_IP)))

        print("\n")


# ----------------- Connecting Master to the Slave -----------------
# Receives 4KB information.
ConfirmationFromServer = sock.recv(4096)

# Prints information extracted from the Master confirmation.
# Uses Class: ConfirmMaster
confirmation = ConfirmMaster(ConfirmationFromServer)
confirmation.printEverything()

# ---------------------- METHODOLOGY ABANDONED ---------------------
# This approach is discarded because Python does not have an entry point post-compilation
# into a main() function like other popular OOP languages. Python instead reads the first
# line of code and proceeds down. Archiving this in case we need it in the future but
# there is no need to convert any kind of .C or .CPP code into Python like this.
#
# def main(self, argv=[]):
#     self.position = 0
#     ##### Variables used by and in the Beej guide #####
#     sockfd = 0
#     numbytes = 0
#     rv = 0
#     master_port = 0
#     buffer = bytearray(MAX_BYTES)
#     s = bytearray(ADDRESS_LENGTH)
#
#     class structs:
#         def __init__(self):
#             # socket.addr
#             # self.hints
#             self.hints = 0
#
#     ##### Constants #####
#     # This is our group ID. Lab Group 22.
#     our_gid = 22
#     magic_number = 0x4A6F7921
#     magic_number_binary = struct.pack('>I', magic_number)
#
#     ##### General Variables #####
#     receivedGID = 0
#     received_MagicNumber = 0
#     received_nextSlaveIP = 0
#     myRID = 0
#     nextSlaveIP = 0
#     nextSlaveIP_String = bytearray(ADDRESS_LENGTH)
#     magicNumber_Struct = 0
#
#     ##### Packing Mechanism #####
#     # This is the data to be packed into the packet.
#     # The packet will be then packed into a frame for delivery.
#     message = struct.pack_into(our_gid, magic_number_binary)
#
#     rv = socket.getaddrinfo(argv[1], argv[2])
#
#     if (rv != 0):
#         print("Error, getaddrinfo() failed")
#
#     ##### Handling Response from Master #####
#     # //////// Insert methods here
#
#     ##### Printing Final Conclusions #####
#     # //////// Insert methods here
#     print("Group ID: \n", receivedGID)
#     print("Ring ID:  \n", myRID)
#     print("IP: \n", nextSlaveIP_String)
#     return 0
