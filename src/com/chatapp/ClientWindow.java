package com.chatapp;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JList;

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
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmOnlineUsers;
	private JMenuItem mntmExit;
	private JMenu mnHelp;
	private JList list;

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
		setMinimumSize(new Dimension(880, 550)); // not sure if its platform dependant
		setLocationRelativeTo(null);

		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				String disconnect = "/dc/" + client.getID();
				client.send(disconnect.getBytes());
				running = false;
				client.close();
			}
		});

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmOnlineUsers = new JMenuItem("Online Users");
		mntmOnlineUsers.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

			}
		});
		mnFile.add(mntmOnlineUsers);

		mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);

		mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.rowWeights = new double[] { 0.0, 1.0, 0.0 };
		gbl_contentPane.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0 };
		gbl_contentPane.columnWidths = new int[] { 28, 655, 180, 7 }; // sum 880
		gbl_contentPane.rowHeights = new int[] { 25, 485, 40 }; // sum 550
		// gbl_contentPane.columnWeights = new double[] { 1.0, 1.0 };
		// gbl_contentPane.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		txtrHistory = new JTextArea();
		txtrHistory.setFont(new Font("Monospaced", Font.PLAIN, 12));
		txtrHistory.setEditable(false);
		caret = (DefaultCaret) txtrHistory.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); // this is platform dependant
		JScrollPane scroll = new JScrollPane(txtrHistory);
		GridBagConstraints scrollConstraints = new GridBagConstraints();
		scrollConstraints.insets = new Insets(5, 5, 5, 5);
		scrollConstraints.fill = GridBagConstraints.BOTH;
		scrollConstraints.gridx = 0;
		scrollConstraints.gridy = 0;
		scrollConstraints.weightx = 1;
		scrollConstraints.weighty = 1;
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
					if (client.connected)
					{
						send(txtMessage.getText());
					}
				}
			}
		});

		GridBagConstraints gbc_txtMessage = new GridBagConstraints();
		gbc_txtMessage.insets = new Insets(0, 5, 0, 5);
		gbc_txtMessage.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMessage.gridx = 0;
		gbc_txtMessage.gridy = 2;
		gbc_txtMessage.gridwidth = 2;
		gbc_txtMessage.weightx = 1;
		gbc_txtMessage.weighty = 0;
		contentPane.add(txtMessage, gbc_txtMessage);
		txtMessage.setColumns(10);

		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (client.connected)
				{
					send(txtMessage.getText());
				}
			}
		});
		GridBagConstraints gbc_btnSend = new GridBagConstraints();
		gbc_btnSend.insets = new Insets(0, 10, 0, 5);
		gbc_btnSend.gridx = 2;
		gbc_btnSend.gridy = 2;
		gbc_btnSend.weightx = 0;
		gbc_btnSend.weighty = 0;
		contentPane.add(btnSend, gbc_btnSend);

		list = new JList();
		GridBagConstraints gbc_list = new GridBagConstraints();
		gbc_list.insets = new Insets(0, 15, 0, 0);
		gbc_list.fill = GridBagConstraints.BOTH;
		gbc_list.gridx = 2;
		gbc_list.gridy = 1;
		contentPane.add(list, gbc_list);

		String[] listd = { "Ja", "Ty", "On", "Ona", "Nikt" };
		list.setListData(listd);

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
						client.connected = true;
						console("Succesfully connected to server with ID: " + client.getID());
					} else if (message.startsWith("/m/"))
					{
						String text = message.substring(3, message.length());
						console(text);
					} else if (message.startsWith("/p/"))
					{
						client.send(("/p/" + client.getID()).getBytes());
					} else if (message.startsWith("/dc/"))
					{
						client.connected = false; // just in case(same as messages and send button)
						console("You have timed out\n");
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
