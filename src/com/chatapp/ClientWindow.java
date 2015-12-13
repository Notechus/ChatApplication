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

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import com.chatapp.networking.Packet;

/**
 * Client window class
 * 
 * @author notechus
 *
 */
public class ClientWindow extends JFrame implements Runnable
{
	/** Default UID because this class is serializable */
	private static final long serialVersionUID = 1L;
	/** UDP client reference */
	private Client client;
	/** GUI running thread */
	private Thread run;

	/** GUI stuff */
	private JPanel contentPane;
	private JTextField txtMessage;
	private JTextArea txtrHistory;
	private DefaultCaret caret;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmOnlineUsers;
	private JMenuItem mntmExit;
	private JMenu mnHelp;
	private JList<User> list;
	private JLabel lblOnline;
	private DefaultListModel<User> listModel;

	// can use UUID class for ID or write own

	/**
	 * Create the frame.
	 */
	public ClientWindow(String name_, String address_, int port_)
	{
		setTitle("Chat Application: " + name_);
		client = new Client(this, name_, address_, port_);
		boolean connect = openConnection();
		if (!connect)
		{
			System.err.println("Connection failed");
			console("Connection failed");
		}
		createWindow();
		console("Attempting to connect as: " + name_);
		send(Packet.Type.CONNECT, name_);
		run = new Thread(this, "Running");
		run.start();
	}

	public ClientWindow(String name_, char[] passwd, int port_)
	{
		setTitle("Chat Application: " + name_);
		String ID = "";
		// should validate in server before connection and pass id here
		client = new Client(this, name_, ID);

		for (char i : passwd)
		{
			i = 0; // zeroes password out int memory
		}
		boolean connect = openConnection();
		if (!connect)
		{
			System.err.println("Connection failed");
			console("Connection failed");
		}
		createWindow();
		console("Attempting to connect as: " + name_);
		send(Packet.Type.CONNECT, name_);
		run = new Thread(this, "Running");
		run.start();

	}

	/**
	 * Opens connection socket
	 * 
	 * @return true if opened succesfully, false otherwise
	 */
	private boolean openConnection()
	{
		return client.openConnection();
	}

	/**
	 * Creates application window
	 * 
	 */
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
		setMinimumSize(new Dimension(880, 550)); // not sure if its platform
													// dependant
		setLocationRelativeTo(null);

		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				String disconnect = "" + client.getID();
				send(Packet.Type.DISCONNECT, disconnect);
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
		gbl_contentPane.rowWeights = new double[]
		{ 0.0, 1.0, 0.0 };
		gbl_contentPane.columnWeights = new double[]
		{ 0.0, 0.0, 1.0, 0.0 };
		gbl_contentPane.columnWidths = new int[]
		{ 28, 655, 180, 7 }; // sum 880
		gbl_contentPane.rowHeights = new int[]
		{ 25, 485, 40 }; // sum 550
		contentPane.setLayout(gbl_contentPane);

		txtrHistory = new JTextArea();
		txtrHistory.setFont(new Font("Monospaced", Font.PLAIN, 12));
		txtrHistory.setEditable(false);
		caret = (DefaultCaret) txtrHistory.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); // this is platform
															// dependant
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
						send(Packet.Type.MESSAGE, txtMessage.getText());
					}
				}
			}
		});

		lblOnline = new JLabel("Online");
		GridBagConstraints gbc_lblOnline = new GridBagConstraints();
		gbc_lblOnline.insets = new Insets(0, 0, 5, 5);
		gbc_lblOnline.gridx = 2;
		gbc_lblOnline.gridy = 0;
		contentPane.add(lblOnline, gbc_lblOnline);

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
					send(Packet.Type.MESSAGE, txtMessage.getText());
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

		listModel = new DefaultListModel();
		list = new JList<>(listModel);
		GridBagConstraints gbc_list = new GridBagConstraints();
		gbc_list.insets = new Insets(0, 15, 5, 5);
		gbc_list.fill = GridBagConstraints.BOTH;
		gbc_list.gridx = 2;
		gbc_list.gridy = 1;
		contentPane.add(list, gbc_list);

		// list.setListData(userArray);

		setVisible(true);
		txtMessage.requestFocusInWindow();
	}

	/**
	 * Getter for client
	 */
	public Client getClient()
	{
		return client;
	}

	/**
	 * Listening in here
	 * 
	 * @see ClientWindow#listen()
	 */
	public void run()
	{
		listen();
	}

	/**
	 * Sends packet to the server
	 * 
	 * @param type packet type
	 * @param message message to be sent
	 */
	public void send(Packet.Type type, String message)
	{
		if (!message.equals(""))
		{
			client.send(type, message);
			txtMessage.setText("");
		}
	}

	public void addUser(User user)
	{
		listModel.addElement(user);
	}

	public void removeUser(User user)
	{
		listModel.removeElement(user);
	}

	/**
	 * Listens for packets from server
	 */
	public void listen()
	{
		client.listen();
	}

	/**
	 * Prints to console
	 * 
	 * @param message message to be printed
	 */
	public void console(String message)
	{
		txtrHistory.append(message + "\n\r");
	}
}
