package vn.edu.hcmus.student.sv19127611.Client;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * vn.edu.hcmus.student.sv19127611.Client
 * Created by fminhtu
 * Date 1/5/2022 - 3:13 PM
 * Description: ...
 */
public class ChatFrame extends JFrame {
    private JComboBox<String> onlineUsers;
    private JButton sendButton;
    private JButton fileButton;
    private JTextField texting;
    private JPanel contentPane;
    private JLabel titleLabel;
    private JScrollPane chatWindowPanel;
    private JTextPane chatWindow;
    private JPanel profilePane;
    private JLabel userImage;
    private JLabel profileLabel;
    private JLabel ownerLabel;
    private JPanel headerPane;
    private JLabel goalLabel;
    private JPanel packetPane;
    private JPanel titlePane;
    private JLabel tutorLabel;
    private JProgressBar progressBar;

    private String nickname, goal;
    private DataInputStream dis;
    private DataOutputStream dos;
    private HashMap<String, JTextPane> chatWindows = new HashMap<String, JTextPane>();

    Thread receiver;
    Object sharedObject;
    int sttLink;

    static final String MESSAGE = "Text";
    static final String FILE = "File";
    static final String ONLINE_USERS = "Online users";
    static final String SPLIT = "#==";
    static final String USERNAME_STYLE = "Username";
    static final String MESSAGE_STYLE = "Message";
    static final String HYPERLINK_STYLE = "Link";
    static final String LOGOUT = "Log out";
    static final String SAFE_TO_LEAVE = "Safe to leave";


    public ChatFrame(String username, DataInputStream dis, DataOutputStream dos){
        this.nickname = username;
        this.goal = " ";
        this.dis = dis;
        this.dos = dos;
        this.sttLink = 0;

        this.sharedObject = new Object();
        setContentPane(contentPane);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(300, 100, 900, 600);
        setVisible(true);
        setTitle("Chatroom");

        addActionListener();
        addItemListener();
        addWindowListener();

        customizeComponents();

        receiver = new Thread(new ChatFrame.Receiver(dis));
        receiver.start();
    }

    private void customizeComponents() {
        ImageIcon imageIcon = new ImageIcon("resources\\user\\userImage.jpg");
        Image image = imageIcon.getImage().getScaledInstance(120, 120,  java.awt.Image.SCALE_SMOOTH);
        userImage.setIcon(new ImageIcon(image));

        ownerLabel.setText(nickname);
    }

    private void autoScroll() {
        chatWindowPanel.getVerticalScrollBar().setValue(chatWindowPanel.getVerticalScrollBar().getMaximum());
    }

    private void showNewMessageToGUI(String username, String message, Boolean mine) {
        try {
            StyledDocument doc = setStyledDocument(username);
            Style userStyle = setNameStyle(doc, mine);
            Style messageStyle = setContentStyle(doc, mine);

            doc.insertString(doc.getLength(), username + ": ", userStyle);
            doc.insertString(doc.getLength(), message + "\n", messageStyle);
        } catch (BadLocationException ignored){}

        autoScroll();
    }

    StyledDocument setStyledDocument(String username) {
        StyledDocument document;

        if (username.equals(this.nickname)) {
            document = chatWindows.get(goal).getStyledDocument();
        } else {
            document = chatWindows.get(username).getStyledDocument();
        }

        return document;
    }

    Style setNameStyle(StyledDocument document, boolean mine) {
        Style userStyle = document.getStyle(USERNAME_STYLE + sttLink);

        if (userStyle == null) {
            userStyle = document.addStyle(USERNAME_STYLE + sttLink, null);
            StyleConstants.setBold(userStyle, true);
        }

        if (mine) {
            StyleConstants.setForeground(userStyle, Color.PINK);
        } else {
            StyleConstants.setForeground(userStyle, Color.DARK_GRAY);
        }

        return userStyle;
    }

    Style setContentStyle(StyledDocument document, boolean mine)
    {
        Style messageStyle = document.getStyle(MESSAGE_STYLE + sttLink);
        if (messageStyle == null) {
            messageStyle = document.addStyle(MESSAGE_STYLE + sttLink, null);
            StyleConstants.setForeground(messageStyle, Color.BLACK);
            StyleConstants.setBold(messageStyle, false);
        }
        return messageStyle;
    }

    Style setContentHyperLinkStyle(StyledDocument document, String filename, byte[] file) {
        Style linkStyle = document.getStyle(HYPERLINK_STYLE);

        if (linkStyle == null) {
            sttLink++;
            linkStyle = document.addStyle(HYPERLINK_STYLE + sttLink, null);
            StyleConstants.setForeground(linkStyle, Color.BLACK);
            StyleConstants.setUnderline(linkStyle, true);
            StyleConstants.setBold(linkStyle, true);
            linkStyle.addAttribute("link" + sttLink, new HyperlinkListener(filename, file));
        }
        return linkStyle;
    }

    private void showNewFileToGUI(String username, String filename, byte[] file, Boolean mine) {
        String window = username;
        if (username.equals(this.nickname)) {
            window = goal;
        }

        StyledDocument doc = setStyledDocument(window);
        Style userStyle = setNameStyle(doc, mine);
        Style linkStyle = setContentHyperLinkStyle(doc, filename, file);

        if (chatWindows.get(window).getMouseListeners() != null) {
            chatWindows.get(window).addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    Element ele = doc.getCharacterElement(chatWindow.viewToModel2D(e.getPoint()));
                    AttributeSet as = ele.getAttributes();
                    HyperlinkListener listener = (HyperlinkListener)as.getAttribute("link" + sttLink);

                    if(listener != null)
                    {
                        listener.execute(filename);
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {}
                @Override
                public void mouseReleased(MouseEvent e) {}
                @Override
                public void mouseEntered(MouseEvent e) {}
                @Override
                public void mouseExited(MouseEvent e) {}
            });
        }

        try {
            doc.insertString(doc.getLength(), username + ": ", userStyle);
            doc.insertString(doc.getLength(), filename, linkStyle);
            doc.insertString(doc.getLength(), "\n", userStyle);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        autoScroll();
    }

    private void addItemListener() {
        onlineUsers.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    goal = (String) onlineUsers.getSelectedItem();
                    if (chatWindow != chatWindows.get(goal)) {
                        chatWindow = chatWindows.get(goal);
                        chatWindowPanel.setViewportView(chatWindow);
                        chatWindowPanel.validate();
                        texting.setText("");
                    }
                }

            }
        });
    }

    private boolean validAction() {
        return !goal.equals(" ");
    }

    private void addActionListener() {
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (validAction()) {
                    try {
                        dos.writeUTF(MESSAGE);
                        dos.writeUTF(goal);
                        dos.writeUTF(texting.getText());
                        dos.flush();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        showNewMessageToGUI("Error" , "Something wrong !!!", true);
                    }

                    showNewMessageToGUI(nickname, texting.getText(), true);
                    texting.setText("");
                }
            }
        });

        getRootPane().setDefaultButton(sendButton);

        fileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (validAction()) {
                    JFileChooser fileChooser = new JFileChooser();
                    int option = fileChooser.showOpenDialog(contentPane.getParent());
                    if (option == JFileChooser.APPROVE_OPTION) {
                        byte[] selectedFile = new byte[(int) fileChooser.getSelectedFile().length()];
                        BufferedInputStream bis;
                        try {
                            bis = new BufferedInputStream(new FileInputStream(fileChooser.getSelectedFile()));
                            bis.read(selectedFile, 0, selectedFile.length);

                            dos.writeUTF(FILE);
                            dos.writeUTF(goal);
                            dos.writeUTF(fileChooser.getSelectedFile().getName());
                            dos.writeUTF(String.valueOf(selectedFile.length));

                            int fileSize = selectedFile.length;
                            int totalBytesUnread = fileSize;
                            int bufferSize = 2048;
                            int offset = 0;

                            while (totalBytesUnread > 0) {
                                dos.write(selectedFile, offset, Math.min(totalBytesUnread, bufferSize));
                                offset += Math.min(totalBytesUnread, bufferSize);
                                totalBytesUnread -= bufferSize;
                            }

                            dos.flush();
                            bis.close();

                            showNewFileToGUI(nickname, fileChooser.getSelectedFile().getName(), selectedFile, true);

                            progressBar.setValue(100);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }

                }
            }
        });
    }


    private void addWindowListener() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    dos.writeUTF(LOGOUT);
                    dos.flush();
                    receiver.join();

                    if (dos != null) {
                        dos.close();
                    }
                    if (dis != null) {
                        dis.close();
                    }
                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    class DownloadTask implements  Runnable {
        int fileSize;
        int bufferSize;
        int totalBytesRead;
        int totalBytesUnread;
        int percentCompleted;

        public DownloadTask(int fileSize) {
            this.fileSize = fileSize;
            this.bufferSize = 2048;
            this.percentCompleted = 0;
            this.totalBytesRead = 0;
            this.totalBytesUnread = fileSize;
        }

        @Override
        public void run() {
            while (totalBytesRead > 0) {
                totalBytesUnread -= bufferSize;
                totalBytesRead += bufferSize;
                percentCompleted = (int) (totalBytesRead * 100 / fileSize);
                progressBar.setValue(percentCompleted);
            }
        }
    }

    class Receiver implements Runnable{
        private DataInputStream dis;

        public Receiver(DataInputStream dis) {
            this.dis = dis;
        }

        @Override
        public void run() {
            try {
                label:
                while (true) {
                    String method = dis.readUTF();

                    switch (method) {
                        case MESSAGE: {
                            String sender = dis.readUTF();
                            String message = dis.readUTF();
                            showNewMessageToGUI(sender, message, false);
                            break;
                        }
                        case FILE: {
                            String sender = dis.readUTF();
                            String filename = dis.readUTF();
                            int size = Integer.parseInt(dis.readUTF());
                            int bufferSize = 2048;
                            byte[] buffer = new byte[bufferSize];
                            ByteArrayOutputStream file = new ByteArrayOutputStream();

                            while (size > 0) {
                                dis.read(buffer, 0, Math.min(bufferSize, size));
                                file.write(buffer, 0, Math.min(bufferSize, size));
                                size -= bufferSize;
                            }

                            showNewFileToGUI(sender, filename, file.toByteArray(), false);
                            break;
                        }
                        case ONLINE_USERS:
                            String[] users = dis.readUTF().split(SPLIT);
                            String chatting = goal;
                            boolean isChattingOnline = false;

                            onlineUsers.removeAllItems();

                            for (String user : users) {
                                if (!user.equals(nickname)) {
                                    onlineUsers.addItem(user);
                                    if (chatWindows.get(user) == null) {
                                        JTextPane temp = new JTextPane();
                                        temp.setFont(new Font("Consolas", Font.PLAIN, 14));
                                        temp.setEditable(false);
                                        chatWindows.put(user, temp);
                                    }
                                }
                                if (chatting.equals(user)) {
                                    isChattingOnline = true;
                                }
                            }

                            if (!isChattingOnline) {
                                onlineUsers.setSelectedItem(" ");
                                JOptionPane.showMessageDialog(null,
                                        chatting + " is offline !!!\n" +
                                                "You will be redirect to default chat window.");
                            } else {
                                onlineUsers.setSelectedItem(chatting);
                            }

                            onlineUsers.validate();
                            break;

                        case SAFE_TO_LEAVE:
                            break label;
                    }
                }
            } catch(IOException ex) {
                System.err.println(ex);
            } finally {
                try {
                    if (dis != null) {
                        dis.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class HyperlinkListener extends AbstractAction {
        String filename;
        byte[] file;

        public HyperlinkListener(String filename, byte[] file) {
            this.filename = filename;
            this.file = Arrays.copyOf(file, file.length);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.execute(this.filename);
        }

        public  void execute(String filename) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(filename));

            int rVal = fileChooser.showSaveDialog(contentPane.getParent());
            BufferedOutputStream bos = null;

            if (rVal == JFileChooser.APPROVE_OPTION) {
                File saveFile = fileChooser.getSelectedFile();

                try {
                    bos = new BufferedOutputStream(new FileOutputStream(saveFile));
                    int openFileAction = JOptionPane.showConfirmDialog(null,
                            "Saved file to " + saveFile.getAbsolutePath()
                                    + "\nDo you want to open this file?",
                            "Save file successful",
                            JOptionPane.YES_NO_OPTION);

                    if (openFileAction == JOptionPane.YES_OPTION) {
                        try {
                            Desktop.getDesktop().open(saveFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    bos.write(this.file);
                    bos.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
