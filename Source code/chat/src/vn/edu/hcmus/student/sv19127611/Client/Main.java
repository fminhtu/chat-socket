package vn.edu.hcmus.student.sv19127611.Client;

import vn.edu.hcmus.student.sv19127611.Server.ServerFrame;

import javax.swing.*;
import java.awt.*;

/**
 * vn.edu.hcmus.student.sv19127611.Client
 * Created by fminhtu
 * Date 1/7/2022 - 3:42 PM
 * Description: ...
 */
public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                    LoginFrame frame = new LoginFrame();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}