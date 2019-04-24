package milkman.plugin.privatebin;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PrivateBinApiTest {

	@Test
	void testPasteCreation() throws Exception {
		PrivateBinApi sut = new PrivateBinApi("https://privatebin.net/?");
		String id = sut.createPaste("some test content", false);
		System.out.println("ID: " + id);
		assertThat(id).isNotBlank();
	}
	
	
	@Test
	void testPasteLookup() throws Exception {
		PrivateBinApi sut = new PrivateBinApi("https://privatebin.net/?");
		String content = sut.readPaste("https://privatebin.net/?b67cd2775e22d8ed#XNsfay6QBL0NFzA/KPDkLA==");
		
		
		System.out.println("content: " + content);
		assertThat(content).isNotBlank();
	}
}
