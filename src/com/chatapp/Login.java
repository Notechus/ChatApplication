package com.chatapp;

import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class Login extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtName;
	private JTextField txtIP;
	private JLabel lblIpAdress;
	private JTextField txtPort;
	private JLabel lblPort;

	/**
	 * Create the frame.
	 */
	public Login() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}

		setResizable(false);
		setTitle("Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300, 380);
		// setBounds(100, 100, 300, 380);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		txtName = new JTextField();
		txtName.setBounds(82, 65, 130, 20);
		contentPane.add(txtName);
		txtName.setColumns(10);

		JLabel lblName = new JLabel("Name:");
		lblName.setBounds(124, 45, 46, 14);
		contentPane.add(lblName);

		txtIP = new JTextField();
		txtIP.setBounds(82, 121, 130, 20);
		contentPane.add(txtIP);
		txtIP.setColumns(10);

		lblIpAdress = new JLabel("IP Adress:");
		lblIpAdress.setBounds(118, 103, 57, 14);
		contentPane.add(lblIpAdress);

		txtPort = new JTextField();
		txtPort.setColumns(10);
		txtPort.setBounds(82, 173, 130, 20);
		contentPane.add(txtPort);

		lblPort = new JLabel("Port:");
		lblPort.setBounds(129, 154, 35, 14);
		contentPane.add(lblPort);

		JButton btnLogin = new JButton("Login");
		btnLogin.setBounds(102, 246, 89, 23);
		contentPane.add(btnLogin);
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login frame = new Login();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
