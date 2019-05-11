package milkman.plugin.privatebin;

import java.security.SecureRandom;

import milkman.plugin.privatebin.PrivateBinApi.PrivateBinData;

public interface DeEncryptor {
	static SecureRandom secureRandom = new SecureRandom();

	PrivateBinData encrypt(String strToEncrypt) throws Exception;
	String decrypt(PrivateBinData data, String secret64) throws Exception; 
	

	static byte[] generateRandomKey(int lengthInBit) {
		byte[] key = new byte[lengthInBit / 8];
		secureRandom.nextBytes(key);
		return key;
	}

}
