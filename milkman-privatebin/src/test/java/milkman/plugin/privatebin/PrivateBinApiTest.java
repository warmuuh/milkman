package milkman.plugin.privatebin;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PrivateBinApiTest {

	@Test
	void testPasteDeEncryption() throws Exception {
		PrivateBinApi sut = new PrivateBinApi("https://privatebin.net/?");
		String id = sut.createPaste("some test content", false);
		System.out.println("ID: " + id);
		assertThat(id).isNotBlank();

		
		String content = sut.readPaste(id);
		
		System.out.println("content: " + content);
		assertThat(content).isEqualTo("some test content");
	}

	
	@Test
	void testPasteLookup() throws Exception {
		PrivateBinApi sut = new PrivateBinApi("https://privatebin.net/?");
		String content = sut.readPaste("https://privatebin.net/?05a52b2790940344#rtZTv6VVAjzrHzIKV1uHdJbnfygWmA+8eJXJ5PFzy4Y=");
		
		
		System.out.println("content: " + content);
		assertThat(content).isNotBlank();
	}
}
