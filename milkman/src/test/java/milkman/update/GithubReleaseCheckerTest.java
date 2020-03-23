package milkman.update;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GithubReleaseCheckerTest {

	@Test
	void test() throws IOException {
		
		assertTrue(new GithubReleaseChecker("warmuuh", "hardwire").getNewerRelease("0.0.1").isPresent());
	}
	

	@Test
	void testUnusualVersion() throws IOException {
		
		assertTrue(new GithubReleaseChecker("warmuuh", "hardwire").getNewerRelease("0.0.1.beta").isPresent());
	}

}
