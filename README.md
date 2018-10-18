# COMP-4320 Laboratory Assignment 2

## Auburn University | Fall 2018

### Group Members:
    Paul Ryu
    William Atkinson
    Abriana Fornis
    

Data Structures Implemented: Socket, Python Server, Python Client, C Server, C Client\
Languages Used: Python, C

Description: The objective of this lab is to create a 
virtual ring of nodes over the Internet. After joining the ring, 
the nodes on the ring MUST use only the ring to communicate. The 
ring is managed by a master which is part of the ring and has Ring 
ID 0 (zero). All the other nodes of the ring are slave nodes 
(clients)with a ring ID assigned by the master.