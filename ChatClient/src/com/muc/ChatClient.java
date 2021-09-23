package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.ArrayList;

// This class will be the interface to the server
public class ChatClient
{
    // Members:
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferedIn;

    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();

    // Constructor
    public ChatClient(String serverName, int serverPort)
    {
        this.serverName = serverName;
        this.serverPort = serverPort;

    }
    // Methods

    // this main method is for testing
    public static void main(String[] args) throws IOException
    {
        // Create an instance of the chat client
        ChatClient client = new ChatClient("localhost", 8818);
        client.addUserStatusListener(new UserStatusListener()
        {
            @Override
            public void online(String login)
            {
                System.out.println("ONLINE: " + login);
            }

            @Override
            public void offline(String login)
            {
                System.out.println("OFFLINE: " + login);
            }
        });

        client.addMessageListener(new MessageListener()
        {
            @Override
            public void onMessage(
                String fromLogin,
                String msgBody)
            {
                System.out.println("You got a message from " + fromLogin + " ===>" + msgBody);
            }
        });
        // after we make an instance of the client we then want to connect --> in order to make a
        // connection we need to make a socket connection.
        if(!client.connect())
        {
            System.out.println("Connect failed");
        }
        else
        {
            System.out.println("Connect Successful");
            // before I login I want to register all my listeners

            if(client.login("guest", "guest"))
            {
                System.out.println("Login successful");
            }
            else
            {
                System.out.println("Login failed ");
            }

            // client.logoff();
        }
    }

    public void msg(String sendTo, String msgBody) throws IOException
    {
        String cmd = "msg " + sendTo + " " + msgBody + "\n";
        serverOut.write(cmd.getBytes());
    }

    public boolean login(String login, String password) throws IOException
    {
        String cmd = "login " + login + " " + password + "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Response line: " + response);

        if("ok login".equalsIgnoreCase(response))
        {
            startMessageReader();
            return true;
        }
        else
        {
            return false;
        }
    }

    public void  logoff() throws IOException
    {
        String cmd = "logoff\n";
        serverOut.write(cmd.getBytes());
    }

    private void startMessageReader()
    {
        // remember every thread has a run()
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                readMessageLoop();
            }
        };
        t.start();
    }

    /*
    *   Note To Self:
    *       + Anytime you use a method like close() or start() where there is the
    *         possibility that the execution of the method will fail, there must be some exception
    *         that is thrown. You can add the exception to the method signature or wrap the logic in
    *         a try/catch.
    *
    * */
    private void readMessageLoop()
    {
        try
        {
            String line;
            while((line = bufferedIn.readLine()) != null)
            {
                String[] tokens = StringUtils.split(line);
                if(tokens != null && tokens.length > 0)
                {
                    String cmd = tokens[0];
                    // 1st thing we want to handle are the offline/online presence messages
                    if("online".equalsIgnoreCase(cmd))
                    {
                        handleOnline(tokens);
                    }
                    else if("offline".equalsIgnoreCase(cmd))
                    {
                        handleOffline(tokens);
                    }
                    else if("msg".equalsIgnoreCase(cmd))
                    {
                        String[] tokenMsg = StringUtils.split(line, null, 3);
                        handleMessage(tokenMsg);
                    }
                }
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            try
            {
                socket.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void handleMessage(String[] tokensMsg)
    {
        String login = tokensMsg[1];
        String msgBody = tokensMsg[2];

        for(MessageListener listener : messageListeners)
        {
            listener.onMessage(login, msgBody);
        }

    }

    private void handleOffline(String[] tokens)
    {
        String logoff = tokens[1];
        for(UserStatusListener listener : userStatusListeners){
            listener.offline(logoff);
        }
    }

    private void handleOnline(String[] tokens)
    {
        // In this method we are going to call back all the UserStatusListeners
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners)
        {
            listener.online(login);
        }
    }

    public boolean connect()
    {
        try
        {
            this.socket = new Socket(serverName, serverPort);
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public void addUserStatusListener(UserStatusListener listener)
    {
        userStatusListeners.add(listener);
    }

    public void removeUserStatusListener(UserStatusListener listener)
    {
        userStatusListeners.remove(listener);
    }

    public void addMessageListener(MessageListener listener)
    {
        messageListeners.add(listener);
    };

    public void removeMessageListener(MessageListener listener)
    {
        messageListeners.remove(listener);
    };

}
