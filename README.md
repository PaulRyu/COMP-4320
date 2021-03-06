<p align="center">

DISCLAIMER: If you're also in our class this semester, don't bother looking at our code. We don't know what the hell we're doing either. Copy us if you want to get a 0 like we did the first lab. ;)

# COMP-4320 Laboratory Assignment 2 and 3

## Auburn University | Fall 2018

### Lab Group 22:
    Paul Ryu
    William Atkinson
    Abriana Fornis
  
</p>

Data Structures Implemented: Socket, Python Server, Python Client, Java Server, Java Client\
Languages Used: Python, Java

> __Description__: 
> _The objective of this lab is to create a 
virtual ring of nodes over the Internet. After joining the ring, 
the nodes on the ring MUST use only the ring to communicate. The 
ring is managed by a master which is part of the ring and has Ring 
ID 0 (zero). All the other nodes of the ring are slave nodes 
(clients) with a ring ID assigned by the master._

> a) ___Slave Node Operations___ \
Write a client (Slave.py) in any language other than Java, C, or C++. The Client must
>> i. accept a command line of the form: Slave MasterHostname MasterPort# where
>>> 1. Slave is the executable,
>>> 2. MasterHostname is the master’s hostname,
>>> 3. MasterPort# is the master’s port number.

>> ii. form and send a Join Request following the protocol described below.

>> iii. set itself as a slave node on the ring following the protocol described below and
obtain its ring ID (myRID) from the master.

>> iv. Display the GID of the master, its own ring ID, and the IP address (in dotted decimal
format) of its next slave.

>>v. (Lab 3 only), repeatedly prompt the user for a ring ID RID and a message m.

>>vi. (Lab 3 only), send the message m to the trusted node with ring ID RID following
the protocol described below.
>>> 1. (Lab 3 only), display any message received in a packet that contains ring ID myRID.
>>> 2. (Lab 3 only), forward any message destined to a node that has a ring ID different from myRID. 
Forward only message with a TTL (time to live) higher than 1.

> b) ___Master Node Operations___ \
Write a Master (Master.java). The Master must
>>i. accept a command line of the form: Master MasterPort# where
>>> 1. Master is your executable,
>>> 2. MasterPort# is the port number (where the master must bind)

>> ii. set itself as a stream (TCP) server. The master must maintain ring IDs and the
variable nextSlaveIP to assign to slave nodes on the ring following the protocol
described above. The master’s ring ID is 0. The master must maintain two
variables: nextRID and nextSlaveIP.
The variable nextRID is initially set to 1. Whenever a slave joins the ring, the
master will send it the value contained in Variable nextRID and will increment
nextRID.
The variable nextSlaveIP is initially set to the IP address of the machine on which
the master is running. Whenever a slave S joins the ring, the master will send it the
IP address contained in Variable nextSlaveID and will set its variable nextSlaveIP to
the IP address of Node S (who just requested to join).

>> iii. (Lab 3 only), repeatedly prompt the user to ask a ring ID RID and a message m.

>> iv. (Lab 3 only), send the message m to the trusted node with ring ID RID
following the protocol described below.

>> v. (Lab 3 only), display any message received in a packet that contains ring ID 0.

>> vi. (Lab 3 only), forward any message destined to a node that has an RID
different from myRID.
