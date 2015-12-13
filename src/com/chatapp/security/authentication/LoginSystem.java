package com.chatapp.security.authentication;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class LoginSystem
{
	private String username;
	private MessageDigest md;
	byte[] hash;
	private byte[] salt;

	public LoginSystem()
	{

	}

	public boolean authenticate()
	{
		// TODO: connect to the server and authenticate user
		return false;
	}

	public boolean logout()
	{
		// TODO: connect to server and logout user, then logout here
		return false;
	}
}
