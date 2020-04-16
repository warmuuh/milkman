package milkman.ui.commands;


import milkman.domain.Environment;
import milkman.domain.Environment.EnvironmentEntry;
import milkman.templater.EnvironmentTemplater;
import milkman.templater.PrefixedTemplaterResolver;
import milkman.ui.plugin.TemplateParameterResolverPlugin;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentTemplaterTest {

	TemplateParameterResolverPlugin lowerCasePlugin = new TemplateParameterResolverPlugin() {
		@Override
		public String getPrefix() {
			return "LOW";
		}

		@Override
		public String lookupValue(String input) {
			return input.toLowerCase();
		}
	};


	@Test
	void shouldReplaceVariousValues() {
		
		Environment globalEnvironment = new Environment();
		globalEnvironment.setGlobal(true);
		globalEnvironment.getEntries().add(new EnvironmentEntry(UUID.randomUUID().toString(),"test2", "value2", true));
		globalEnvironment.getEntries().add(new EnvironmentEntry(UUID.randomUUID().toString(),"test4", "value4", false));
		globalEnvironment.getEntries().add(new EnvironmentEntry(UUID.randomUUID().toString(),"test6", "{{test2}}", true));

		PrefixedTemplaterResolver res = new PrefixedTemplaterResolver(List.of());
		EnvironmentTemplater sut = new EnvironmentTemplater(Optional.empty(), Collections.singletonList(globalEnvironment), res);
		String output = sut.replaceTags("test1 {{test2}} {{test3}} {{test4}} test5 {{test6}} someTail");
		assertThat(output).isEqualTo("test1 value2 {{test3}} {{test4}} test5 value2 someTail");
		
	}


	@Test
	void shouldReplaceRecursiveValues() {

		Environment globalEnvironment = new Environment();
		globalEnvironment.setGlobal(true);
		globalEnvironment.getEntries().add(new EnvironmentEntry(UUID.randomUUID().toString(),"valPart1", "variable", true));
		globalEnvironment.getEntries().add(new EnvironmentEntry(UUID.randomUUID().toString(),"valPart2", "WithId1", true));
		globalEnvironment.getEntries().add(new EnvironmentEntry(UUID.randomUUID().toString(),"variableWithId1", "1", true));


		PrefixedTemplaterResolver res = new PrefixedTemplaterResolver(List.of());
		EnvironmentTemplater sut = new EnvironmentTemplater(Optional.empty(), Collections.singletonList(globalEnvironment), res);
		assertThat(sut.replaceTags("{{{{valPart1}}{{valPart2}}}}")).isEqualTo("1");
		assertThat(sut.replaceTags("{{ {{valPart1}}{{valPart2}} }}")).isEqualTo("1");

	}


	@Test
	void shouldReplaceRecursiveValuesWithCustomResolvers() {

		Environment globalEnvironment = new Environment();
		globalEnvironment.setGlobal(true);
		globalEnvironment.getEntries().add(new EnvironmentEntry(UUID.randomUUID().toString(),"valPart1", "variable", true));
		globalEnvironment.getEntries().add(new EnvironmentEntry(UUID.randomUUID().toString(),"valPart2", "WithId1", true));
		globalEnvironment.getEntries().add(new EnvironmentEntry(UUID.randomUUID().toString(),"variableWithId1", "SOME_VALUE", true));


		PrefixedTemplaterResolver res = new PrefixedTemplaterResolver(List.of(lowerCasePlugin));
		EnvironmentTemplater sut = new EnvironmentTemplater(Optional.empty(), Collections.singletonList(globalEnvironment), res);
		assertThat(sut.replaceTags("{{LOW:{{ {{valPart1}}{{valPart2}} }} }}")).isEqualTo("some_value");

	}

}
