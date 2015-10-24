package com.chatapp.networking;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;

/**
 * This is encrypted version of networking packet
 * 
 * @author notechus
 * 
 */
// TODO check DTLS
public class EPacket extends Packet
{
	private static final long serialVersionUID = 7L;

	// type of currently used cipher - this will be moved to other class
	private final String cipher_const = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
	// password for encrypting packets - this will be moved to other class
	private final String encrypt_passwd = "Somerandompasswd"; // 16

	private Cipher c;

	/**
	 * 
	 * @param msg message you want to have encrypted
	 * @throws IOException
	 * @throws IllegalBlockSizeException
	 */
	protected EPacket(int ID_, Type type_, String message_)
	{
		super(ID_, type_, message_);
		try
		{
			c = Cipher.getInstance(cipher_const);
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		} catch (NoSuchPaddingException e)
		{
			e.printStackTrace();
		}
	}

	// TODO
	protected SealedObject encrypt(final Packet p) throws NoSuchAlgorithmException, NoSuchPaddingException
	{
		SecretKeySpec message = new SecretKeySpec(encrypt_passwd.getBytes(), cipher_const);
		Cipher c = Cipher.getInstance(cipher_const);
		SealedObject encrypted_message = null;
		try
		{
			c.init(Cipher.ENCRYPT_MODE, message);
			encrypted_message = new SealedObject(p, c);
		} catch (InvalidKeyException e)
		{
			e.printStackTrace();
		} catch (IllegalBlockSizeException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return encrypted_message;
	}

	// TODO
	protected Packet decrypt(final SealedObject p) throws NoSuchAlgorithmException, NoSuchPaddingException
	{
		SecretKeySpec message = new SecretKeySpec(encrypt_passwd.getBytes(), cipher_const);
		Cipher c = Cipher.getInstance(cipher_const);
		Packet decrypted_message = null;
		try
		{
			c.init(Cipher.DECRYPT_MODE, message);
			decrypted_message = (Packet) p.getObject(c);
		} catch (InvalidKeyException e)
		{
			e.printStackTrace();
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		} catch (IllegalBlockSizeException e)
		{
			e.printStackTrace();
		} catch (BadPaddingException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return decrypted_message;
	}
}
