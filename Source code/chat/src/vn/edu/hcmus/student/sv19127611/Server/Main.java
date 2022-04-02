package vn.edu.hcmus.student.sv19127611.Server;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                    ServerFrame frame = new ServerFrame();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
