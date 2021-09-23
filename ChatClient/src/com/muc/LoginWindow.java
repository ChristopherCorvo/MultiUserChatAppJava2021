package com.muc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class LoginWindow extends JFrame
{
    // Members:
    // Create an instance of a Chat Client but it has not been instantiated
    private final ChatClient client;
    JTextField loginField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JButton loginButton = new JButton();

    // Constructor:
    public LoginWindow()
    {
        super("Login");

        this.client = new ChatClient("localhost", 8818);
        client.connect();

        // controls what happens when the user closes the window --> exit the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Below are the contents that will go inside the Window
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(loginField);
        p.add(passwordField);
        p.add(loginButton);

        // add actions
        loginButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doLogin();
            }
        });

        getContentPane().add(p, BorderLayout.CENTER);
        // pack() will resize window to fit all components
        pack();

        // make GUI visible
        setVisible(true);
    }



    // Methods:
    public static void main(String[] args)
    {
        // create an instance of the LoginWindow
        LoginWindow loginWindow = new LoginWindow();

        // set the LoginWindow to be visible
        loginWindow.setVisible(true);
    }
    private void doLogin()
    {
        String login = loginField.getText();
        String password = passwordField.getText();

        try
        {
            if(client.login(login, password))
            {
                // if successfully logged in hide login window
                setVisible(false);
                
                //bring up UserList Pane
                UserListPane userListPane = new UserListPane(client);
                JFrame frame = new JFrame("User List");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(400,600);

                frame.getContentPane().add(userListPane, BorderLayout.CENTER);
                frame.setVisible(true);
            }
            else
            {
                // show error message
                JOptionPane.showMessageDialog(this, "Invalid Login/Password.");
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }


    }
}
