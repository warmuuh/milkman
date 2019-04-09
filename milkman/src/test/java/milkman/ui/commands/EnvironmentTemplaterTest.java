package milkman.ui.commands;


import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

import milkman.domain.Environment;
import milkman.domain.Environment.EnvironmentEntry;

class EnvironmentTemplaterTest {

	@Test
	void shouldReplaceVariousValues() {
		
		Environment globalEnvironment = new Environment();
		globalEnvironment.setGlobal(true);
		globalEnvironment.getEntries().add(new EnvironmentEntry("test2", "value2", true));
		globalEnvironment.getEntries().add(new EnvironmentEntry("test4", "value4", false));
		globalEnvironment.getEntries().add(new EnvironmentEntry("test6", "{{test2}}", true));
		
		EnvironmentTemplater sut = new EnvironmentTemplater(Optional.empty(), Collections.singletonList(globalEnvironment));
		String output = sut.replaceTags("test1 {{test2}} {{test3}} {{test4}} test5 {{test6}} someTail");
		assertThat(output).isEqualTo("test1 value2 {{test3}} {{test4}} test5 value2 someTail");
		
	}

}
