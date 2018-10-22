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
import array
import binascii
import numpy as np
import socket
import string
import struct
import sys

# ------------------------ Global Variables ------------------------
# Source: https://wiki.python.org/moin/TcpCommunication
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
MAX_BYTES = 100
ADDRESS_LENGTH = 16


# ---------------- Linked List Data Structure Setup -----------------
# Source: https://www.tutorialspoint.com/python/python_linked_lists.htm
class Node:
    def __init__(self, dataval=None):
        self.dataval = dataval
        self.nextval = None


class SLinkedList:
    def __init__(self):
        self.headval = None


# --------------------- Function: getAddress() ---------------------
def getAddress():
    return 0


# ------------------------ Function: main() ------------------------
def main(argc, argv=[]):
    ##### Variables used by and in the Beej guide #####
    sockfd = 0
    numbytes = 0
    rv = 0
    master_port = 0
    buffer = np.chararray(MAX_BYTES)
    s = np.chararray(ADDRESS_LENGTH)

    ##### Constants #####
    # This is our group ID. Lab Group 22.
    our_gid = 22
    magic_number = 0x4A6F7921
    magic_number_binary = struct.pack('>I', magic_number)

    ##### General Variables #####
    receivedGID = 0
    received_MagicNumber = 0
    received_nextSlaveIP = 0
    myRID = 0
    nextSlaveIP = 0
    nextSlaveIP_String = np.chararray(ADDRESS_LENGTH)
    magicNumber_Struct = 0

    ##### Packing Mechanism #####
    # This is the data to be packed into the packet.
    # The packet will be then packed into a frame for delivery.
    message = struct.pack_into(our_gid, magic_number_binary)

    # Not sure how to handle argc yet. // NEED TO DO
    if argc != 3:
        print("oh noooo")

    # Check if port number is invalid.
    if master_port < 0 or master_port > 65535:
        print("Slave Error")
        exit(1)

    ##### Linked List Implementation #####
    # This sets up the nodes, using the Linked List data structure above.
    # This is what we will use to set up our virtual ring of nodes (trust).
    list1 = SLinkedList()
    list1.headval = Node("First node")
    e2 = Node("Second node")
    e3 = Node("Third node")
    # This connects e1 to e2 and e2 to e3.
    list1.headval.nextval = e2
    e2.nextval = e3

    ##### Handling Response from Master #####
    # //////// Insert methods here

    ##### Printing Final Conclusions #####
    # //////// Insert methods here
    print("Slave: GID of Master = %d \n", receivedGID);
    print("Slave: My RID = %d \n", myRID);
    print("Slave: Next Slave's IP Address = %s \n", nextSlaveIP_String);
    return 0
