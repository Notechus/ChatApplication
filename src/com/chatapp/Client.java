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

public class Client extends JFrame
{

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private String name, address;
	private int port;
	private JTextField txtMessage;
	private JTextArea txtrHistory;
	private DefaultCaret caret;

	private DatagramSocket socket;
	private InetAddress ip;

	private Thread send_thread;

	/**
	 * Create the frame.
	 */
	public Client(String name_, String address_, int port_)
	{
		setTitle("Chat Application Client");
		this.name = name_;
		this.address = address_;
		this.port = port_;
		boolean connect = openConnection(address, port);
		if (!connect)
		{
			System.err.println("Connection failed");
			return;
		}
		createWindow();
		console("Connected as: " + name);
	}

	private boolean openConnection(String address, int port)
	{
		try
		{
			socket = new DatagramSocket(port);
			ip = InetAddress.getByName(address);
		} catch (UnknownHostException ex)
		{
			ex.printStackTrace();
			return false;
		} catch (SocketException ex)
		{
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	private String receive()
	{
		byte[] data = new byte[1024];
		DatagramPacket packet = new DatagramPacket(data, data.length);

		try
		{
			socket.receive(packet);
		} catch (IOException ex)
		{
			ex.printStackTrace();
		}
		String message = new String(packet.getData());

		return message;
	}

	private void send(final byte[] data)
	{
		send_thread = new Thread("Send")
		{
			public void run()
			{
				DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
				try
				{
					socket.send(packet);
				} catch (IOException ex)
				{
					ex.printStackTrace();
				}
			}
		};
		send_thread.start();
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

		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 28, 815, 30, 7 }; // sum 880
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
		scrollConstraints.gridwidth = 3;
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
		gbc_txtMessage.insets = new Insets(0, 0, 0, 5);
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
		gbc_btnSend.insets = new Insets(0, 0, 0, 5);
		gbc_btnSend.gridx = 2;
		gbc_btnSend.gridy = 2;
		contentPane.add(btnSend, gbc_btnSend);

		setVisible(true);
		txtMessage.requestFocusInWindow();
	}

	public void send(String message)
	{
		if (!message.equals(""))
		{
			message = name + ": " + message;
			console(message);
			txtMessage.setText("");
		}
	}

	public void console(String message)
	{
		txtrHistory.append(message + "\n\r");
	}

}
