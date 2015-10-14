package com.chatapp;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import java.awt.GridBagLayout;
import javax.swing.JTextArea;
import java.awt.GridBagConstraints;
import javax.swing.JButton;
import java.awt.Insets;
import javax.swing.JTextField;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ClientWindow extends JFrame implements Runnable
{
	private static final long serialVersionUID = 1L;
	private Client client;
	private Thread run, listen;
	private boolean running = false;

	private JPanel contentPane;
	private JTextField txtMessage;
	private JTextArea txtrHistory;
	private DefaultCaret caret;

	// can use UUID class for ID or write own

	/**
	 * Create the frame.
	 */
	public ClientWindow(String name_, String address_, int port_)
	{

		setTitle("Chat Application: " + name_);
		client = new Client(name_, address_, port_);
		boolean connect = openConnection(address_);
		if (!connect)
		{
			System.err.println("Connection failed");
			console("Connection failed");
		}
		createWindow();
		console("Attempting to connect as: " + name_);
		String connection = "/c/" + name_;
		client.send(connection.getBytes());
		running = true;
		run = new Thread(this, "Running");
		run.start();

	}

	private boolean openConnection(String address)
	{
		return client.openConnection(address);
	}

	private void createWindow()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(880, 550);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		addWindowListener(new WindowAdapter()
		{
			public void windowClosed(WindowEvent arg0)
			{
				String disconnect = "/dc/" + client.getID();
				client.send(disconnect.getBytes());
				client.close();
				running = false;
			}
		});

		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 28, 680, 165, 7 }; // sum 880
		gbl_contentPane.rowHeights = new int[] { 35, 475, 40 }; // sum 550
		gbl_contentPane.columnWeights = new double[] { 1.0, 1.0 };
		gbl_contentPane.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		txtrHistory = new JTextArea();
		txtrHistory.setFont(new Font("Monospaced", Font.PLAIN, 12));
		txtrHistory.setEditable(false);
		caret = (DefaultCaret) txtrHistory.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); // this is platform dependant
		JScrollPane scroll = new JScrollPane(txtrHistory);
		GridBagConstraints scrollConstraints = new GridBagConstraints();
		scrollConstraints.insets = new Insets(0, 0, 5, 5);
		scrollConstraints.fill = GridBagConstraints.BOTH;
		scrollConstraints.gridx = 0;
		scrollConstraints.gridy = 0;
		scrollConstraints.gridwidth = 2;
		scrollConstraints.gridheight = 2;
		scrollConstraints.insets = new Insets(5, 5, 0, 0);
		contentPane.add(scroll, scrollConstraints);

		txtMessage = new JTextField();
		txtMessage.addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					send(txtMessage.getText());
				}
			}
		});
		GridBagConstraints gbc_txtMessage = new GridBagConstraints();
		gbc_txtMessage.insets = new Insets(0, 5, 0, 0);
		gbc_txtMessage.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMessage.gridx = 0;
		gbc_txtMessage.gridy = 2;
		gbc_txtMessage.gridwidth = 2;
		contentPane.add(txtMessage, gbc_txtMessage);
		txtMessage.setColumns(10);

		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				send(txtMessage.getText());
			}
		});
		GridBagConstraints gbc_btnSend = new GridBagConstraints();
		gbc_btnSend.insets = new Insets(0, 10, 0, 0);
		gbc_btnSend.gridx = 2;
		gbc_btnSend.gridy = 2;
		contentPane.add(btnSend, gbc_btnSend);

		setVisible(true);
		txtMessage.requestFocusInWindow();
	}

	public void run()
	{
		listen();
	}

	public void send(String message)
	{
		if (!message.equals(""))
		{
			message = client.getName() + ": " + message;
			message = "/m/" + message;
			client.send(message.getBytes());
			txtMessage.setText("");
		}
	}

	public void listen()
	{
		listen = new Thread("Listen")
		{
			public void run()
			{
				while (running)
				{
					String message = client.receive();
					if (message.startsWith("/c/"))
					{
						client.setID(Integer.parseInt(message.substring(3, message.length())));
						console("Succesfully connected to server with ID: " + client.getID());
					} else if (message.startsWith("/m/"))
					{
						String text = message.substring(3, message.length());
						console(text);
					}
				}
			}
		};
		listen.start();
	}

	public void console(String message)
	{
		txtrHistory.append(message + "\n\r");
	}
}
