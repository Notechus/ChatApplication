package com.chatapp.networking;

import java.io.Serializable;

/**
 * This is serializable packet class for networking
 * 
 * @author notechus
 *
 */
public class Packet implements Serializable
{
	private static final long serialVersionUID = 2L;
	/** ID given from server(server has 0 reserved for itself) */
	public int ID;
	/** Type of packet - enum */
	public Type type;
	/** Message to be sent */
	public String message;
	/** Destination ID */
	public int dID;
	/** Packet size in bytes */
	private int size;

	/**
	 * Constructs <code>Packet</code>
	 * 
	 * @param ID_
	 *            ID of client
	 * @param type_
	 *            type of packet
	 * @param message_
	 *            message to be sent
	 */
	public Packet(int ID_, Type type_, String message_)
	{
		this.ID = ID_;
		this.type = type_;
		this.message = message_;
		this.dID = 0; // normally we send to server which ID is 0
		this.size = 4 * 4 * 4 * message.length();
	}

	/**
	 * Constructs <code>Packet</code> with specified destination ID
	 * 
	 * @param ID_
	 *            client's ID
	 * @param type_
	 *            type of packet
	 * @param message_
	 *            message to be sent
	 * @param destinationID
	 *            destination client ID
	 */
	public Packet(int ID_, Type type_, String message_, int destinationID)
	{
		this.ID = ID_;
		this.type = type_;
		this.message = message_;
		this.dID = destinationID;
		this.size = 4 * 4 * 4 * message.length();
	}

	/**
	 * Overrides default <code>toString()</code>
	 */
	public String toString()
	{
		return type + " " + message + " " + ID;
	}

	/**
	 * Enum class for packet
	 * 
	 * @author notechus
	 *
	 */
	public enum Type
	{
		CONNECT, DISCONNECT, MESSAGE, DIRECT_MESSAGE, PING, ACK, USER_ONLINE, USER_OFFLINE, LOGIN
		// ACK - acknowledgement from server, will be used later
	}

}
