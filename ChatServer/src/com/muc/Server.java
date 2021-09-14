package com.muc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/*
* Every thread needs to override the run()
*
* */
public class Server extends Thread
{
    // Members:
    private final int serverPort;
    private ArrayList<ServerWorker> workerList = new ArrayList<>();

    // Constructor:
    public Server(int serverPort)
    {
        this.serverPort = serverPort;
    }
    // Methods:
    public List<ServerWorker> getWorkerList()
    {
        return this.workerList;
    }

    @Override
    public void run()
    {
        try
        {
            /*
             *   Creates a serverSocket bound to a specific port. If you use
             *   port 0 then it will choose a port automatically.
             */
            ServerSocket serverSocket = new ServerSocket(serverPort);

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
                ServerWorker worker = new ServerWorker(this, clientSocket);
                workerList.add(worker);
                worker.start();
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
