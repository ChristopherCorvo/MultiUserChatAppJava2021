package com.muc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerMain
{
    /*
    *   + When you make a chat server its basically a network server.
    *   + In order to make a network server you need to make a webSocket.
    *
    *   + WebSocket:
    *       ++ A WebSocket is a computer communications protocol, providing a
    *          a full-duplex communication channels over a single TCP connection.
    *          At its core, the WebSocket protocol facilitates message passing between
    *          client and server.
    * */
    public static void main(String[] args)
    {
        int port = 8818;
        try
        {
            /*
             *   Creates a serverSocket bound to a specific port. If you use
             *   port 0 then it will choose a port automatically.
             */
            ServerSocket serverSocket = new ServerSocket(port);
            /*
             *   the accept() is listening for a connection to be made to the
             *   designated port.
             *
             *   clientSocket is in a while loop because its going to keep
             *   listening until the connection is made.
             *
             * */
            while (true)
            {
                System.out.println("About to accept client connection...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket);

                /*
                *   Everytime clientSocket makes a new connection a new thread will be created by
                *   making a new instance of the Service Worker class which makes a new thread.
                *   The ServerWorker class handles the communication once the ServerSocket has made
                *   a connection to the specified port.
                *
                * */
                ServerWorker worker = new ServerWorker(clientSocket);
                worker.start();
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
