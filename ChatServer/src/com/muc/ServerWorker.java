package com.muc;

import java.io.*;
import java.net.Socket;
import java.util.Date;

/*
* By extending the Thread abstract class we make it so that when ever we make an instance of the ServerWorker
* class that object represents a new thread.
*
* */
public class ServerWorker extends Thread
{
    // Members:
    private final Socket clientSocket;

    // Constructor:
    public ServerWorker(Socket clientSocket)
    {
        this.clientSocket = clientSocket;
    }

    // Methods:
    /*
     *   IOException: ->
     *      + input/output exceptions that occur whenever an input of output operation is failed or
     *        interrupted.
     *      + For example, if you were trying to read in a file that didn't exist java would throw an
     *        I/O exception.
     *
     *   InterruptedException: ->
     *      + Thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted,
     *        either before or during the activity.
     *
     *   Thread: ->
     *      + Multi-threading is a java feature that allows concurrent execution of two or more parts of a
     *        program for maximum utilization of CPU. Each part of such program is called a thread. So threads
     *        are light-weight processes within a process.
     *      + Threads allows a program to operate more efficiently by doing multiple things at the same time.
     *      + Threads can be used to perform complicated tasks in the background without interrupting the main
     *        program.
     *
     *   IO Multiplexing: ->
     *      + An alternative to multi-threading
     *      + the capacity to tell the kernel that we want to be notified if one or more I/O conditions are ready,
     *          like input is ready to be read.
     *
     * BufferedReader: ->
     *      + is a java class that reads the text from an input stream(like a file) by buffering characters
     *          that seamlessly reads characters, arrays or lines. In general, each read request made of a Reader
     *          causes a corresponding read request to be made of the underlying character or byte stream.
     *
     *   Currently our handleClientSocket() is setup to handle multiple clients but there is a limit to how
     *   many connections can be made and how many threads you can have in operation. In order to increase
     *   the amount of threads even further will entail employing 'multiplexing io'.
     *
     * */
    private void handleClientSocket() throws IOException, InterruptedException
    {
        // setting up biDirectional communication
        InputStream inputStream = this.clientSocket.getInputStream();
        OutputStream outputStream = this.clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        // In this while loop the server is reading commands from the client
        while((line = reader.readLine()) != null)
        {
            String[] tokens = StringUtils
            if("quit".equalsIgnoreCase(line))
            {
                break;
            }
            String msg = "You typed: " + line + "\n";
            outputStream.write(msg.getBytes());
        }
        this.clientSocket.close();
    }

    // Every thread has a run method.
    @Override
    public void run()
    {
        try
        {
            handleClientSocket();
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
