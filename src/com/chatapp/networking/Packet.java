package com.chatapp.networking;

import java.io.Serializable;

public class Packet implements Serializable
{
	private static final long serialVersionUID = 1L;
	public int ID; // server will have reserver 0 ID
	public Type type; // enum type
	public String message;
	// remember: shouldn't add InetAddres or port here
	// actually we should on the server - extract from DatagramPacket

	public Packet()
	{

	}

	public Packet(int ID_, Type type_, String message_)
	{
		this.type = type_;
		this.message = message_;
	}

	public enum Type
	{
		CONNECT, DISCONNECT, MESSAGE, DIRECT_MESSAGE, PING, ACK // ACK - acknowlegment from server, will be used later
	}
}
