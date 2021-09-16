package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/*
* By extending the Thread abstract class we make it so that when ever we make an instance of the ServerWorker
* class that object represents a new thread.
*
* */
public class ServerWorker extends Thread
{
    // Members:
    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;
    private HashSet<String> topicSet = new HashSet<>();

    // Constructor:
    public ServerWorker(Server server, Socket clientSocket)
    {
        this.server = server;
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
        this.outputStream = this.clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        // In this while loop the server is reading commands from the client
        while((line = reader.readLine()) != null)
        {
            // split my lines into individual tokens
            String[] tokens = StringUtils.split(line);
            if (tokens != null && tokens.length > 0)
            {
                String cmd = tokens[0]; // token header
                if ("logoff".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd))
                {
                    handleLogoff();
                    break;
                }
                else if("login".equalsIgnoreCase(cmd))
                {
                    handleLogin(outputStream, tokens);
                }
                else if("msg".equalsIgnoreCase(cmd))
                {
                    String[] tokensMsg = StringUtils.split(line, null, 3);
                    handleMessage(tokensMsg);
                }
                else if("join".equalsIgnoreCase(cmd))
                {
                    String msg = "Type Message to group:" + "\n";
                    outputStream.write(msg.getBytes());
                    handleJoin(tokens);
                }
                else if("leave".equalsIgnoreCase(cmd))
                {
                    String msg = "You have left " + tokens[1];
                    outputStream.write(msg.getBytes());
                    handleLeave(tokens);
                }
                else
                {
                    String msg = "You typed: " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }
        this.clientSocket.close();
    }

    private void handleLeave(String[] tokens)
    {
        if(tokens.length > 1)
        {
            String topic = tokens[1];
            topicSet.remove(topic);
        }
    }

    public boolean isMemberOfTopic(String topic)
    {
        // this method is checking if the user is a member of the given topic
        return topicSet.contains(topic);
    }
    private void handleJoin(String[] tokens)
    {
        // meaning its a token cmd + plus a topic to join
        if(tokens.length > 1)
        {
            String topic = tokens[1];
            /*
            *  State: We are going to store the membership of the user to a topic inside this
            *  serviceWorker instance.
            *
            */
            topicSet.add(topic);
        }
    }

    // format: "msg" "login" msg...
    private void handleMessage(String[] tokens) throws IOException
    {
        //Server Side
        String sendTo = tokens[1];
        String body = tokens[2];

        boolean isTopic = sendTo.charAt(0) == '#';

        List<ServerWorker> workerList = server.getWorkerList();
        for(ServerWorker worker : workerList)
        {
            if(isTopic)
            {
                if (worker.isMemberOfTopic(sendTo))
                {
                    String outMsg = "msg " + sendTo + " " + login + " " + body + "\n";
                    worker.send(outMsg);
                }
            }
            else
            {
                if(sendTo.equalsIgnoreCase(worker.getLogin()))
                {
                    String outMsg = "msg " + login + " " + body + "\n";
                    worker.send(outMsg);
                }
            }
        }
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

    public String getLogin()
    {
        return login;
    }


    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException
    {
        if(tokens.length == 3)
        {
            String login = tokens[1];
            String password = tokens[2];

            if((login.equals("guest") && password.equals("guest")) ||
                (login.equals("jim") && password.equals("jim")) ||
                (login.equals("chris") && password.equals("chris")) )
            {
                String msg = "ok login\n";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("User logged in successfully: " + login);


                List<ServerWorker> workerList = server.getWorkerList();
                // Sends current user all other online logins
                for (ServerWorker worker : workerList)
                {
                    // This conditional statement prevents you from sending your own presence to yourself
                    if(worker.getLogin() != null)
                    {
                        if(!login.equals(worker.getLogin()) )
                        {
                            String msg2 = "online " + worker.getLogin() + "\n";
                            send(msg2);
                        }
                    }
                }

                // send other online users current users status
                String onlineMsg = "online " + login + "\n";
                for (ServerWorker worker : workerList)
                {
                    if(!login.equals(worker.getLogin()) )
                    {
                        worker.send(onlineMsg);
                    }
                }
            }
            else
            {
                String msg = "error login\n";
                outputStream.write(msg.getBytes());
                System.err.println("Login failed for " + login);
            }
        }
    }

    private void handleLogoff() throws IOException
    {
        // removes user from workerList when they log off the system.
        server.removeWorker(this);

        List<ServerWorker> workerList = server.getWorkerList();
        // send other online users current users status
        String offlineMsg = "offline " + login + "\n";
        for (ServerWorker worker : workerList)
        {
            if(!login.equals(worker.getLogin()) )
            {
                worker.send(offlineMsg);
            }
        }

        clientSocket.close();
    }

    private void send(String msg) throws IOException
    {
        if(login != null)
        {
            // will access the output stream of the current client socket and then send a message to the users
            outputStream.write(msg.getBytes());
        }
    }
}
