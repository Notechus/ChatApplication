package com.chatapp;

import com.chatapp.networking.Packet.Type;
import com.chatapp.networking.UDPSocket;

/**
 * Chat Client class responsible for client side of chat connection
 * 
 * @author notechus
 */
public class Client
{
	/** Client's name and IP address */
	private String name, address;
	/** Client's ID set by server */
	private static int ID = -1;
	/** Connection flag */
	public boolean connected = false;
	/** Reference to parent GUI window */
	private ClientWindow window_ref;
	/** Reference to socket */
	private UDPSocket socket_ref;

	private boolean authenticated = false;

	public OnlineUsers users;

	/**
	 * Constructs Client with given parameters
	 * 
	 * @param parent
	 *            reference to parent GUI
	 * @param name_
	 *            Client's name
	 * @param address_
	 *            Client's IP address
	 * @param port_
	 *            Client's port
	 */
	public Client(ClientWindow parent, UDPSocket sock, String name_, String address_, int port_)
	{
		this.name = name_;
		this.address = address_;
		this.socket_ref = sock;
		window_ref = parent;
		users = new OnlineUsers();
	}

	/**
	 * Will be used when user validation will be added
	 * 
	 * @param parent
	 *            reference to gui window
	 * @param name_
	 *            user name
	 * @param ID
	 *            user id received from server
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
	 * 
	 */
	public void setAuthenticated(boolean auth)
	{
		this.authenticated = auth;
	}

	/**
	 * Setter for Client's ID. Should be invisible outside class, because only Server can assign ID
	 * 
	 * @param id
	 *            Id which will be assigned to user
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
	public static int getID()
	{
		return ID;
	}

	/**
	 * 
	 */
	public void close()
	{
		socket_ref.close();
	}

	/**
	 * @param message
	 * @param type
	 * 
	 */
	public void send(Type type, String message)
	{
		socket_ref.send(type, message);
	}

	/**
	 * Prints to console
	 * 
	 * @param message
	 *            message to be printed
	 */
	public void console(String message)
	{
		window_ref.console(message);
	}

}
