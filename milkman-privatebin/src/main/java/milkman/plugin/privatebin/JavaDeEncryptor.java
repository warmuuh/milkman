package milkman.plugin.privatebin;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import milkman.plugin.privatebin.PrivateBinApi.PrivateBinDataV1;

/**
 * uses java to de / encrypt data
 * 
 * is faster than sjcl but data cannot be viewed via browser bc i am unable to encrypt in a sjcl-compliant way
 * TODO: make it work with browser-client of privateBin (we cant use this because we apparently use a slightly different
 *  way of encryption. cant find the issue though
 *
 * @author peter
 *
 */
public class JavaDeEncryptor implements DeEncryptor {
	private static final int SALT_LENGTH = 8;
	private static final int IV_LENGTH = 12;
	private static final int ITERATIONS = 10000;
	private static final int AES_KEY_LENGTH = 256;
	private static final int AUTH_TAG_LENGTH = 128;
	

	
	public PrivateBinDataV1 encrypt(String strToEncrypt) throws Exception {
		byte[] iv = randomInitializationVector();
		byte[] salt = generateRandomSalt();
		byte[] key = DeEncryptor.generateRandomKey(AES_KEY_LENGTH);
		SecretKey secretKey = generateDerivedkey(key, salt);

		Cipher cipher = getCipher();
		GCMParameterSpec parameterSpec = new GCMParameterSpec(AUTH_TAG_LENGTH, iv);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
		
		cipher.updateAAD("".getBytes());
		byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes());
		
		return new PrivateBinDataV1(
				Base64.getEncoder().encodeToString(iv), 
				1, ITERATIONS, AES_KEY_LENGTH, AUTH_TAG_LENGTH, "gcm", "", "aes",
				Base64.getEncoder().encodeToString(salt),
				Base64.getEncoder().encodeToString(encrypted),
				Base64.getEncoder().encodeToString(key));
	}

	private static Cipher getCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
		return Cipher.getInstance("AES/GCM/NoPadding");
	}
	
	public String decrypt(PrivateBinDataV1 data, String secret64) throws Exception {
		byte[] iv = Base64.getDecoder().decode(data.getInitializationVector());
		byte[] salt = Base64.getDecoder().decode(data.getSalt());
		byte[] secretBytes = Base64.getDecoder().decode(secret64);
		byte[] encrypted = Base64.getDecoder().decode(data.getContent());

		SecretKey secretKey = generateDerivedkey(secretBytes, salt);

		Cipher cipher = getCipher();
		GCMParameterSpec parameterSpec = new GCMParameterSpec(data.getAuthenticationTag(), iv);
		cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

		byte[] decrypted = cipher.doFinal(encrypted);
		return new String(decrypted);
	}
	
	private static SecretKey generateDerivedkey(byte[] key, byte[] salt) throws Exception {
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
	    SecretKey derivedKey = factory.generateSecret(new PBEKeySpec(new String(key).toCharArray(), salt, ITERATIONS, AES_KEY_LENGTH));
	    SecretKey secretKey = new SecretKeySpec(derivedKey.getEncoded(), "AES");
	    return secretKey;
	}

	
	
	private static byte[] generateRandomSalt() {
		byte[] key = new byte[SALT_LENGTH];
		secureRandom.nextBytes(key);
		return key;
	}

	private static byte[] randomInitializationVector() {
		byte[] iv = new byte[IV_LENGTH]; // NEVER REUSE THIS IV WITH SAME KEY
		secureRandom.nextBytes(iv);
		return iv;
	}

}
