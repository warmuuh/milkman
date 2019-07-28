package milkman.plugin.privatebin;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import milkman.plugin.privatebin.PrivateBinApi.PrivateBinDataV1;

class JavaDeEncryptorTest {

	@Test
	void testEncryption() throws Exception {
		JavaDeEncryptor sut = new JavaDeEncryptor();
		PrivateBinDataV1 data = sut.encrypt("test");
		System.out.println(data);
	}


//	@Test
//	void testDecryption() throws Exception {
//		String secret64 = "KEreOsL0dxwUTvtKoj1nFuNkzKb7adJcQrzgTeSJSD8=";
//		PrivateBinData data = new PrivateBinData("Vlw/sC8aXrwJyJ9wTtdw9Q==", 1, 10000, 256, 128, "gcm", "", "aes", "rrOClBKft3U=", "0HeMzIwJ/C0QFuIJ7+EOC0TWmY4=", null);
//
//		SjclDeEncryptor sut = new SjclDeEncryptor();
//		String decrypted = sut.decrypt(data, secret64);
//		System.out.println(decrypted);
//	}

}
