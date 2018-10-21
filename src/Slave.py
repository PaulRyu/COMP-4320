import socket
import sys
import string
import struct
import binascii
import array
import struct
import numpy as np

MAXDATA = 100
INET6_ADDRSTRLEN = 16

# Information found at: https://wiki.python.org/moin/TcpCommunication
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)


def getAddress():
    return 0


def main(argc, argv = []):

    # ----------------------------- Beej ------------------------------
    sockfd = 0
    numbytes = 0
    rv = 0
    MasterPort = 0
    buffer = np.chararray(MAXDATA)
    s = np.chararray(INET6_ADDRSTRLEN)

    # --------------------------- Constants ---------------------------

    # This is our group ID. Lab Group 22.
    OUR_GID = 22
    MAGIC_NUMBER = 0x4A6F7921
    MAGIC_NUMBER_BINARY = struct.pack('>I', MAGIC_NUMBER)

    # --------------------------- Variables ---------------------------

    receivedGID = 0
    received_MagicNumber = 0
    received_nextSlaveIP = 0
    myRID = 0
    nextSlaveIP = 0

    nextSlaveIP_String = np.chararray(INET6_ADDRSTRLEN)

    message = struct.pack_into(OUR_GID, MAGIC_NUMBER_BINARY)

    magicNumber_Struct = 0

    if argc != 3:
        print("oh noooo")

    if MasterPort < 0 or MasterPort > 65535:
        print("Slave Error")
        exit(1)

    return 0
