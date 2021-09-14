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
        Server server = new Server(port);
        // starts the thread
        server.start();

    }
}
