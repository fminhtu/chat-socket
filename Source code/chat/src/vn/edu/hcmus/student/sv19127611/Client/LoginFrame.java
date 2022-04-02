package vn.edu.hcmus.student.sv19127611.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * vn.edu.hcmus.student.sv19127611.client
 * Created by fminhtu
 * Date 1/5/2022 - 8:26 AM
 * Description: ...
 */
public class LoginFrame extends  JFrame {
    private JPanel contentPane;
    private JTextField nameTextField;
    private JPasswordField passField;
    private JButton registerButton;
    private JButton loginButton;
    private JPasswordField confirmPassField;
    private JPanel buttonPane;
    private JPanel signPane;
    private JLabel passLabel;
    private JLabel nameLabel;
    private JLabel confirmPassLabel;
    private JLabel titleLabel;
    private JLabel notifyLabel;
    private JPanel headerPane;

    private String host = "localhost";
    private int port = 8080;
    private Socket socket;

    private DataInputStream dis;
    private DataOutputStream dos;

    private String username;
    boolean isRegister;

    static final String LOGIN = "Log in";
    static final String REGISTER = "Sign up";

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception ignored) { }

        LoginFrame frame = new LoginFrame();
    }

    public LoginFrame() {
        isRegister = false;
        setContentPane(contentPane);
        addActionListener();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(300, 100, 800, 400);
        setVisible(true);
        setTitle("Login");
    }

    private void addActionListener() {
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isRegister) {
                    String response = Login(nameTextField.getText(), String.copyValueOf(passField.getPassword()));

                    if (response.equals("Log in successful") ) {
                        username = nameTextField.getText();

                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                ChatFrame frame = new ChatFrame(username, dis, dos);
                                frame.setVisible(true);
                            }
                        });
                        dispose();

                    } else {
                        passField.setText("");
                        showNotification(true, response);
                    }
                } else {
                    showConfirmPassword(false);
                    showNotification(false);
                }
            }
        });

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isRegister) {
                    if (String.copyValueOf(confirmPassField.getPassword()).equals(String.copyValueOf(passField.getPassword()))) {
                        String response = Signup(nameTextField.getText(), String.copyValueOf(passField.getPassword()));

                        if (response.equals("Sign up successful") ) {
                            username = nameTextField.getText();

                            EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    try {
                                        int confirm = JOptionPane.showConfirmDialog(
                                                null,
                                                "Successful registration. \n" +
                                                        "You will be redirected to the chat interface",
                                                "Sign up successful", JOptionPane.DEFAULT_OPTION);

                                        ChatFrame frame = new ChatFrame(username, dis, dos);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            dispose();

                        } else {
                            showNotification(true, response);
                            passField.setText("");
                            confirmPassField.setText("");
                        }
                    }

                } else {
                    showConfirmPassword(true);
                    showNotification(false);
                }
            }
        });
    }

    private void showConfirmPassword(boolean value) {
        String title;
        isRegister = value;

        if (isRegister) {
            title = REGISTER;
        } else {
            title = LOGIN;
        }

        setTitle(title);
        titleLabel.setText(title);
        contentPane.setBorder(BorderFactory.createTitledBorder(title));
        confirmPassLabel.setVisible(value);
        confirmPassField.setVisible(value);
    }

    private void showNotification(boolean value) {
        notifyLabel.setVisible(value);
    }

    private void showNotification(boolean value, String text) {
        notifyLabel.setVisible(value);
        notifyLabel.setText(text);
    }

    public String Login(String username, String password) {
        try {
            Connect();
            dos.writeUTF(LOGIN);
            dos.writeUTF(username);
            dos.writeUTF(password);
            dos.flush();

            return dis.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
            return "Network error: Log in fail";
        }
    }


    public String Signup(String username, String password) {
        try {
            Connect();
            dos.writeUTF(REGISTER);
            dos.writeUTF(username);
            dos.writeUTF(password);
            dos.flush();

            return dis.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
            return "Network error: Register fail";
        }
    }

    public void Connect() {
        try {
            if (socket != null) {
                socket.close();
            }
            socket = new Socket(host, port);
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            ex.printStackTrace();

        }
    }

}
