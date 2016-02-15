package com.chatapp.networking;

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
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;

import com.chatapp.Client;
import com.chatapp.GUIWindow;
import com.chatapp.User;
import com.chatapp.security.CipherSystem;

public class UDPSocket
{
	/** UDP socket used to send and receive data */
	private DatagramSocket socket;
	/** Client's name and IP address */
	private String address;
	/** Client's port */
	private int port;
	/** Client's ID set by server */
	private int ID = -1;
	/** Connection flag */
	public boolean connected = false;
	/** Running flag */
	private boolean running = false;

	/** Client threads: sending and listening for data */
	private Thread send, sendDirect, listen, login;

	/** Client's IP converted to Inet class */
	private InetAddress ip;
	/** Reference to parent GUI window */
	private GUIWindow window_ref;

	public UDPSocket(GUIWindow parent, int ID, String address_, int port_) throws UnknownHostException
	{
		this.address = address_;
		this.port = port_;
		this.window_ref = parent;
		this.ID = ID;
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
	 * Setter for Client's ID. Should be invisible outside class, because only Server can assign ID
	 * 
	 * @param id Id which will be assigned to user
	 */
	private void setID(int id)
	{
		this.ID = id;
	}

	public void setParent(GUIWindow parent)
	{
		this.window_ref = parent;
	}

	/**
	 * 
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
	 * @return <code>true</code> if connected correctly, <code>false</code> otherwise
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
	 * Sends login packet to the server and gets reply
	 */
	public void login()
	{
		login = new Thread("Login")
		{
			public void run()
			{
				try
				{
					send(Packet.Type.LOGIN, "Login");
					Thread.sleep(3000);
					Packet p = receive();
					if (p.message == "TRUE") System.out.println("");// set authenticated otherwise dont
				} catch (InterruptedException ex)
				{
					ex.printStackTrace();
				}
			}
		};
		login.start();
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
						// users.add(new User(name[1].trim(), Integer.parseInt(name[0].trim())));
						window_ref.addUser(new User(name[1].trim(), Integer.parseInt(name[0].trim())));
					} else if (packet.type == Packet.Type.USER_OFFLINE)
					{
						console("User " + packet.message + " disconnected!");
						String name[] = packet.message.split("\\.");
						// users.remove(new User(name[1].trim(), Integer.parseInt(name[0].trim())));
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
