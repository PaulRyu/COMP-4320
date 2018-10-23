<<<<<<< HEAD
/* Course: COMP-4320 Introduction to Networks
 * Institution: Auburn University
 * Submitting: Laboratory Assignment 2
 * Date: October 25, 2018
 * Authors: Paul Ryu, William Atkinson, Abriana Fornis
 * Emails: phc0004@auburn.edu, wja0007@auburn.edu, adf0018@auburn.edu
 * Resources used: JetBrains CLion IDE, Github, Sublime Text 3
 *
 * Description: The objective of this lab is to create a virtual ring of nodes over the Internet. After joining
 *             the ring, the nodes on the ring MUST use only the ring to communicate. The ring is managed by a
 *             master which is part of the ring and has Ring ID 0 (zero). All the other nodes of the ring are slave
 *             nodes (clients) with a ring ID assigned by the master.
*/

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h> 
#include <sys/types.h> 
#include <sys/socket.h> 
#include <netinet/in.h> 
#include <netdb.h> 
#include <arpa/inet.h> 
#include <sys/wait.h> 
#include <signal.h> 

#define PORTNUM "10120" // needs to change for command line function
#define BACKLOG 10
int main(int argc, char *argv[]) {

    int myRID = 0; // ring ID for master
    int slaveRID = 1; // assign & increment whenever a node joins
    int myGID = 22;
    unsigned const int magic_number = 0x4A6F7921;
    const char* portNum;
    int packed_clientIP = -1;

    struct sockaddr_storage their_addr;
    socklen_t addr_size;
    struct addrinfo hints, *res;
    int sockfd, new_fd;

    // insert error checking here
    if (argc == 2) {
        portNum = argv[1];
    }
    else if (argc < 2) {
        printf("Error: please provide the port number\n!");
        exit(1);
    }
    else {
        printf("Error: too many arguments\n!");
        exit(1);
    }

    // load address structs with getaddrinfo()
    memset(&hints, 0, sizeof hints);
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags = AI_PASSIVE; // fills in my IP address automatically

    getaddrinfo(NULL, portNum, &hints, &res);

    // create socket, bind it, and listen on it

    sockfd = socket(res->ai_family, res->ai_socktype, res->ai_protocol);
    printf("Socket created.\n");
    bind(sockfd, res->ai_addr, res->ai_addrlen);
    printf("Master bound to socket\n");
    listen(sockfd, BACKLOG);

    printf("Master listening on port ", portNum);
    // accepting incoming connections

    //while (true) {

    //}
    return 0;
}