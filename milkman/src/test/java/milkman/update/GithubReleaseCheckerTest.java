package milkman.update;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class GithubReleaseCheckerTest {

	@Test
	void test() throws IOException {
		
		assertTrue(new GithubReleaseChecker("warmuuh", "hardwire").hasNewerRelease("0.0.1"));
	}

}
