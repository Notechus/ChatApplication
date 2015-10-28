package com.chatapp;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.chatapp.networking.Packet;

/**
 * Login window class
 * 
 * @author notechus
 *
 */
public class Login extends JFrame
{
	/** Default UID because this class is serializable */
	private static final long serialVersionUID = 1L;
	/** GUI stuff */
	private JPanel contentPane;
	private JTextField txtName;
	private JLabel lblPassword;
	private JPasswordField pwdPassword;

	private InetAddress ip;

	/**
	 * Create the frame.
	 */
	public Login()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}

		setResizable(false);
		setTitle("Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300, 380);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		txtName = new JTextField();
		txtName.setHorizontalAlignment(SwingConstants.CENTER);
		txtName.setBounds(82, 65, 130, 25);
		contentPane.add(txtName);
		txtName.setColumns(10);

		JLabel lblName = new JLabel("Name:");
		lblName.setBounds(118, 45, 46, 14);
		contentPane.add(lblName);

		lblPassword = new JLabel("Password:");
		lblPassword.setBounds(118, 103, 89, 14);
		contentPane.add(lblPassword);

		JButton btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String name = txtName.getText();
				String address = "localhost";
				char[] passwd = pwdPassword.getPassword();
				boolean validated = false;
				int port = 8192;
				try
				{
					ip = InetAddress.getByName("localhost");
					// this is so bad...we dont have encryption here yet
					DatagramSocket socket = new DatagramSocket();
					Packet p = new Packet(0, Packet.Type.LOGIN, name + "|" + passwd.toString());
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					ObjectOutputStream os = new ObjectOutputStream(outputStream);
					os.writeObject(p);
					byte[] data = outputStream.toByteArray();
					DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
					socket.send(packet);
					Thread.sleep(1000);
					byte[] response = new byte[1024];
					// byte[] decrypted_packet = null;
					DatagramPacket resp = new DatagramPacket(response, response.length);
					socket.receive(resp);
					ByteArrayInputStream in = new ByteArrayInputStream(response);
					ObjectInputStream is = new ObjectInputStream(in);
					p = (Packet) is.readObject();
					if (p.message == "true")
					{
						validated = true;
					}
				} catch (SocketException ex)
				{
					ex.printStackTrace();
				} catch (UnknownHostException e1)
				{
					e1.printStackTrace();
				} catch (ClassNotFoundException e1)
				{
					e1.printStackTrace();
				} catch (IOException e1)
				{
					e1.printStackTrace();
				} catch (InterruptedException e1)
				{
					e1.printStackTrace();
				}
				if (validated)
				{
					login(name, address, port);
				}
			}
		});
		btnLogin.setBounds(102, 246, 89, 23);
		contentPane.add(btnLogin);

		pwdPassword = new JPasswordField();
		pwdPassword.setBounds(82, 124, 130, 25);
		contentPane.add(pwdPassword);
	}

	/**
	 * Login happens here
	 */
	private void login(String name, String address, int port)
	{
		dispose();
		new ClientWindow(name, address, port);
		// new ClientWindow(name, passwd, port);
		/*
		 * for (char i : passwd) { i = 0; // zeroes password in memory }
		 */
	}

	/**
	 * Main function of application
	 * 
	 * @param args no use of arguments here
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					Login frame = new Login();
					frame.setVisible(true);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
}
