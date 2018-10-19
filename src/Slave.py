import socket
import sys
import string
import struct
import binascii
import array
import numpy as np

MAXDATASIZE = 100
INET6_ADDRSTRLEN = 16

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)


def main(argc, argv=[]):
    sockfd = 0
    numbytes = 0
    rv = 0
    masterPortNumber = 0

    # buffer[MAXDATASIZE] = 0
    buffer = np.chararray(MAXDATASIZE)

    # s[INET6_ADDRSTRLEN] = 0
    s = np.chararray(INET6_ADDRSTRLEN)

    received_GID = 0
    received_MagicNumber = 0
    received_nextSlaveIP = 0
    myRID = 0
    nextSlaveIP = 0

    nextSlaveIP_String[INET_ADDRSTRLEN] = 0

    GID_Struct = 0

    magicNumber_Struct = 0

    if argc != 3:
        rando = 0

    return 0