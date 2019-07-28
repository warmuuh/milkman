package milkman.plugin.privatebin;

import java.security.SecureRandom;

import milkman.plugin.privatebin.PrivateBinApi.PrivateBinDataV1;

public interface DeEncryptor {
	static SecureRandom secureRandom = new SecureRandom();

	PrivateBinDataV1 encrypt(String strToEncrypt) throws Exception;
	String decrypt(PrivateBinDataV1 data, String secret64) throws Exception; 
	

	static byte[] generateRandomKey(int lengthInBit) {
		byte[] key = new byte[lengthInBit / 8];
		secureRandom.nextBytes(key);
		return key;
	}

}
