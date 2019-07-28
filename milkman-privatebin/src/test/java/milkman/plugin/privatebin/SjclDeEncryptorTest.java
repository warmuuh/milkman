package milkman.plugin.privatebin;

import org.junit.jupiter.api.Test;

import milkman.plugin.privatebin.PrivateBinApi.PrivateBinDataV1;

class SjclDeEncryptorTest {

	@Test 
	void testEncryption() throws Exception {
		SjclDeEncryptor sut = new SjclDeEncryptor();
		PrivateBinDataV1 data = sut.encrypt("test");
		System.out.println(data);
	}


	@Test
	void testDecryption() throws Exception {
		String secret64 = "b5E1pTdK/VYvHSZcmCd0gZxJrkJnlVZ6l6wQxgGDgm4=";
		PrivateBinDataV1 data = new PrivateBinDataV1("YNY2RpljXcT3f4HpSqUfLg==", 1, 10_000, 256, 128, "gcm", "", "aes", "V1oFfs4uKYw=", "SFZEZC4rkcZ0fuZ9m25UvFHu/K8=", null);

		SjclDeEncryptor sut = new SjclDeEncryptor();
		String decrypted = sut.decrypt(data, secret64);
		System.out.println(decrypted);
	}
}
