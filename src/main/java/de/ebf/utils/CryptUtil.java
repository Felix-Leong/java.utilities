package de.ebf.utils;

import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class CryptUtil {

	private static Logger log = Logger.getLogger(CryptUtil.class);
	private static String algorithm = "PBEWITHSHA256AND128BITAES-CBC-BC";
	private static byte[] salt = "5h6jC5xY7aTS".getBytes();
	
	static {
		try {
			Security.insertProviderAt(new BouncyCastleProvider(),1);
		} catch (Exception e) {
			log.error(e);
		}
	}

	public static byte[] encrypt(String data, String passphrase) throws Exception{
		PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 20);
		PBEKeySpec pbeKeySpec = new PBEKeySpec(passphrase.toCharArray());
		SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(algorithm);
		SecretKey secretKey = secretKeyFactory.generateSecret(pbeKeySpec);
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, pbeParamSpec);
		return cipher.doFinal(data.getBytes());
	}

	
	public static String decrypt(byte[] data, String passphrase) throws Exception {
		PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 20);
		PBEKeySpec pbeKeySpec = new PBEKeySpec(passphrase.toCharArray());
		SecretKeyFactory secretKeyFactory;
		secretKeyFactory = SecretKeyFactory.getInstance(algorithm);
		SecretKey secretKey = secretKeyFactory.generateSecret(pbeKeySpec);
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, secretKey, pbeParamSpec);
		return new String(cipher.doFinal(data));
	}
	
	public static boolean checkPassword(byte[] data, String passphrase) throws Exception{
		try {
			decrypt(data, passphrase);
		} catch (BadPaddingException e) {
			return false;
		} 
		return true;
	}
}