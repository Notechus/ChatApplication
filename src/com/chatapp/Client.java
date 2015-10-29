package com.chatapp;

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
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;

import com.chatapp.networking.CipherSystem;
import com.chatapp.networking.Packet;

/**
 * Chat Client class responsible for client side of chat connection
 * 
 * @author notechus
 */
public class Client
{
	/** UDP socket used to send and receive data */
	private DatagramSocket socket;
	/** Client's name and IP address */
	private String name, address;
	/** Client's port */
	private int port;
	/** Client's ID set by server */
	private int ID = -1;
	/** Connection flag */
	public boolean connected = false;
	/** Running flag */
	private boolean running = false;

	/** Client's IP converted to Inet class */
	private InetAddress ip;
	/** Client threads: sending and listening for data */
	private Thread send, sendDirect, listen;
	/** Reference to parent GUI window */
	private ClientWindow window_ref;

	public OnlineUsers users;

	/** Cipher object used to enc/dec */
	private Cipher cipher;
	/** KeyPair for enc/dec */
	private KeyPair key;
	/** Type of used cipher */

	/*
	 * TODO: add encrypting to packets(almost done), add user-user
	 * communication(necessary to add new packet type for this), add
	 * acknowledgement packet, get separate udp for ping and dc, add logging
	 * system, add exceptions management improve gui, add sounds, add database,
	 * change login for users from db, add registration, add new uid system,
	 * unique id for user stored in db -> will provide friends and will help
	 * with login stuff. Finally we might want to replace all chat with sth else
	 * like news feed. Consider using bcrypt. Think about SSL or TLS. add timer
	 * class to the server and client
	 */
	/**
	 * Constructs Client with given parameters
	 * 
	 * @param parent reference to parent GUI
	 * @param name_ Client's name
	 * @param address_ Client's IP address
	 * @param port_ Client's port
	 */
	public Client(ClientWindow parent, String name_, String address_, int port_)
	{
		this.name = name_;
		this.address = address_;
		this.port = port_;
		window_ref = parent;
		users = new OnlineUsers();
		running = true;

		// RSA
		KeyPairGenerator keyGen;
		try
		{
			keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(2048);
			key = keyGen.generateKeyPair();
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		} catch (NoSuchPaddingException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Will be used when user validation will be added
	 * 
	 * @param parent reference to gui window
	 * @param name_ user name
	 * @param ID user id received from server
	 */
	public Client(ClientWindow parent, String name_, String ID)
	{
		window_ref = parent;
		this.name = name_;
		this.ID = Integer.parseInt(ID);
		// do the rest here
	}

	/**
	 * Getter for name
	 * 
	 * @return Client's name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Getter for address
	 * 
	 * @return Client's ip address
	 */
	public String getAddress()
	{
		return address;
	}

	/**
	 * Getter for port
	 * 
	 * @return Client's port
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * Setter for Client's ID. Should be invisible outside class, because only
	 * Server can assign ID
	 * 
	 * @param id Id which will be assigned to user
	 */
	private void setID(int id)
	{
		this.ID = id;
	}

	/**
	 * Getter for Client's ID
	 * 
	 * @return Client's ID
	 */
	public int getID()
	{
		return ID;
	}

	/**
	 * Sets up the socket and opens connection with Server
	 * 
	 * @return <code>true</code> if connected correctly, <code>false</code>
	 *         otherwise
	 */
	public boolean openConnection()
	{
		try
		{
			socket = new DatagramSocket();
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

	/**
	 * Receives packet from the Server
	 * 
	 * @return <code>Packet</code> obtained from the Server
	 */
	public Packet receive()
	{
		byte[] data = new byte[65536];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		SealedObject d_packet = null;
		Packet p = null;
		try
		{
			socket.receive(packet);
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			ObjectInputStream is = new ObjectInputStream(in);
			d_packet = (SealedObject) is.readObject();
			p = CipherSystem.decrypt(d_packet);
		} catch (IOException ex)
		{
			ex.printStackTrace();
		} catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		} catch (NoSuchPaddingException e)
		{
			e.printStackTrace();
		}

		return p;
	}

	/**
	 * Sends <code>Packet</code> to the Server
	 * 
	 * @param type Type of packet
	 * @param message Message to send
	 * @see Packet.Type
	 */
	public void send(final Packet.Type type, final String message)
	{
		send = new Thread("Send")
		{
			public void run()
			{
				try
				{
					Packet p = new Packet(ID, type, message);
					SealedObject e_packet = CipherSystem.encrypt(p);
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					ObjectOutputStream os = new ObjectOutputStream(outputStream);
					os.writeObject(e_packet);
					byte[] data = outputStream.toByteArray();
					DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
					socket.send(packet);
				} catch (IOException ex)
				{
					ex.printStackTrace();
				} catch (NoSuchAlgorithmException e)
				{
					e.printStackTrace();
				} catch (NoSuchPaddingException e)
				{
					e.printStackTrace();
				}
			}
		};
		send.start();
	}

	/**
	 * Sends direct message to specified client(via server)
	 * 
	 * @param message
	 */
	public void sendDirect(final String message)
	{
		sendDirect = new Thread("Send Direct")
		{
			public void run()
			{
				try
				{
					Packet p = new Packet(ID, Packet.Type.DIRECT_MESSAGE, message);
					SealedObject e_packet = CipherSystem.encrypt(p);
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					ObjectOutputStream os = new ObjectOutputStream(outputStream);
					os.writeObject(e_packet);
					byte[] data = outputStream.toByteArray();
					DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
					socket.send(packet);
				} catch (IOException ex)
				{
					ex.printStackTrace();
				} catch (NoSuchAlgorithmException e)
				{
					e.printStackTrace();
				} catch (NoSuchPaddingException e)
				{
					e.printStackTrace();
				}
			}
		};
		sendDirect.start();
	}

	/**
	 * Waits for packet from the Server
	 * 
	 */
	public void listen()
	{
		listen = new Thread("Listen")
		{
			public void run()
			{
				while (running)
				{
					Packet packet = receive();
					if (packet.type == Packet.Type.CONNECT)
					{
						setID(Integer.parseInt(packet.message));
						connected = true;
						console("Succesfully connected to server with ID: " + getID());
					} else if (packet.type == Packet.Type.MESSAGE)
					{
						console(packet.message);
					} else if (packet.type == Packet.Type.PING)
					{
						send(packet.type, "ping");
					} else if (packet.type == Packet.Type.DISCONNECT)
					{
						console("You have timed out");
						connected = false;
					} else if (packet.type == Packet.Type.DIRECT_MESSAGE)
					{
						console(packet.message); // temporary
					} else if (packet.type == Packet.Type.USER_ONLINE)
					{
						console("User " + packet.message + " just came online!");
						String name[] = packet.message.split("\\.");
						users.add(new User(name[1].trim(), Integer.parseInt(name[0].trim())));
						window_ref.addUser(new User(name[1].trim(), Integer.parseInt(name[0].trim())));
					} else if (packet.type == Packet.Type.USER_OFFLINE)
					{
						console("User " + packet.message + " disconnected!");
						String name[] = packet.message.split("\\.");
						users.remove(new User(name[1].trim(), Integer.parseInt(name[0].trim())));
						window_ref.removeUser(new User(name[1].trim(), Integer.parseInt(name[0].trim())));
					}
				}
			}
		};
		listen.start();
	}

	/**
	 * Prints to console
	 * 
	 * @param message message to be printed
	 */
	public void console(String message)
	{
		window_ref.console(message);
	}

	/**
	 * Closes sockets and application
	 * 
	 */
	public void close()
	{
		new Thread()
		{
			public void run()
			{
				synchronized (socket)
				{
					connected = false;
					socket.close();
					running = false;
				}
			}
		}.start();
	}
}
