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

	/** Client's IP converted to Inet class */
	private InetAddress ip;
	/** Reference to parent GUI window */
	private GUIWindow window_ref;

	public UDPSocket(GUIWindow parent, String address_, int port_) throws UnknownHostException
	{
		this.address = address_;
		this.port = port_;
		this.window_ref = parent;
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
	 * Sends <code>Packet</code> to the Server
	 * 
	 * @param type Type of packet
	 * @param message Message to send
	 * @see Packet.Type
	 */
	public void send(final Packet.Type type, final String message)
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

	/**
	 * Sends direct message to specified client(via server)
	 * 
	 * @param message
	 */
	public void sendDirect(final String message)
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

	/**
	 * Waits for packet from the Server
	 * 
	 */
	public void listen()
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

	/**
	 * Prints to console
	 * 
	 * @param message message to be printed
	 */
	public void console(String message)
	{
		window_ref.console(message);
	}

}
