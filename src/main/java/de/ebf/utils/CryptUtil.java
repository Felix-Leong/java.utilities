package de.ebf.utils;

import de.ebf.constants.BaseConstants;
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

    private static final Logger log = Logger.getLogger(CryptUtil.class);
    private static final String algorithm = "PBEWITHSHA256AND128BITAES-CBC-BC";
    private static byte[] salt;

    static {
        try {
            salt = "5h6jC5xY7aTS".getBytes(BaseConstants.UTF8);
            Security.insertProviderAt(new BouncyCastleProvider(), 1);
        } catch (Exception e) {
            log.error(e);
        }
    }

    public static byte[] encrypt(String data, String passphrase) throws Exception {
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 20);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(passphrase.toCharArray());
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(algorithm);
        SecretKey secretKey = secretKeyFactory.generateSecret(pbeKeySpec);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, pbeParamSpec);
        return cipher.doFinal(data.getBytes(BaseConstants.UTF8));
    }

    public static String decrypt(byte[] data, String passphrase) throws Exception {
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 20);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(passphrase.toCharArray());
        SecretKeyFactory secretKeyFactory;
        secretKeyFactory = SecretKeyFactory.getInstance(algorithm);
        SecretKey secretKey = secretKeyFactory.generateSecret(pbeKeySpec);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, pbeParamSpec);
        return new String(cipher.doFinal(data), BaseConstants.UTF8);
    }

    public static boolean checkPassword(byte[] data, String passphrase) throws Exception {
        try {
            decrypt(data, passphrase);
        } catch (BadPaddingException e) {
            return false;
        }
        return true;
    }
}
