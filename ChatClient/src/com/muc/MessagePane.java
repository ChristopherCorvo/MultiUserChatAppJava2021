package com.muc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class MessagePane extends JPanel implements MessageListener
{
    // Members:
    private final ChatClient client;
    private final String login;

    private DefaultListModel<String> listModel = new DefaultListModel<>();
    // Will show you current conversations
    private JList<String> messageList = new JList<>(listModel);
    private JTextField inputField = new JTextField();


    // Constructors:
    public MessagePane(ChatClient client, String login)
    {
        this.client = client;
        this.login = login;

        //add ourself as a messenger to listener
        client.addMessageListener(this);

        // create a UI
        setLayout(new BorderLayout());
        add(new JScrollPane(messageList), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        //add an eventListener to inputField
        inputField.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    // taking in a message as text
                    String text = inputField.getText();
                    // sending text to the clients login()
                    client.msg(login, text);
                    // display message
                    listModel.addElement("You: " + text);
                    // after message is sent reset text field to empty
                    inputField.setText("");
                } catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onMessage(String fromLogin, String msgBody)
    {
        // filter to make sure the message is actually for the message pane it was intended for
        if(login.equalsIgnoreCase(fromLogin))
        {
            String line = fromLogin + ": " + msgBody;
            listModel.addElement(line);
        }
    }

    // Methods:

}
