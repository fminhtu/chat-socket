package vn.edu.hcmus.student.sv19127611.Server;


import java.awt.EventQueue;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.awt.event.ActionEvent;

/**
 * vn.edu.hcmus.student.sv19127611.Server
 * Created by fminhtu
 * Date 12/30/2021 - 10:13 PM
 * Description: ...
 */
public class ServerFrame extends JFrame {
	JPanel contentPane, panel;
	JButton startButton;
	JLabel title;
	String FONT = "Consolas";

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

	public ServerFrame() {
		initComponents();
		addComponents();
		setBorders();
		setActionListener();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 200);
		setVisible(true);
		setTitle("Server");
	}

	private void initComponents() {
		panel = new JPanel();
		startButton = new JButton("Start server");
		startButton.setFont(new Font(FONT, Font.BOLD, 14));

		title = new JLabel("Click here to start server");
		title.setFont(new Font(FONT, Font.PLAIN, 14));
	}

	private void setBorders() {

	}

	private void addComponents() {
		contentPane = new JPanel();
		contentPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
		setContentPane(contentPane);

		panel.setBorder(new EmptyBorder(50,10,10,10));
		panel.add(startButton);
		panel.add(title);

		contentPane.add(panel);
	}

	private void setActionListener() {
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread t = new Thread(){
					public void run() {
						try {
							new ServerCore();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
				};
				t.start();

				startButton.setEnabled(false);
				title.setText("Server is running");
			}
		});
	}

}
