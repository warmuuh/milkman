package milkman.templater;

import milkman.domain.Environment;
import milkman.domain.Environment.EnvironmentEntry;
import milkman.ui.plugin.Templater;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EnvironmentTemplater implements Templater{


//	private final Pattern tagPattern = Pattern.compile("(?<=[\\{]{2})([^}]+)(?=[\\}]{2})");
	private final Pattern tagPattern = Pattern.compile("([\\{]{2}[^{]+?[\\}]{2})");
	private final Optional<Environment> activeEnvironment;
	private final List<Environment> globalEnvironments;
	private final PrefixedTemplaterResolver resolver;


	public EnvironmentTemplater(Optional<Environment> activeEnvironment, List<Environment> globalEnvironments, PrefixedTemplaterResolver resolver) {
		this.activeEnvironment = activeEnvironment;
		this.globalEnvironments = globalEnvironments;
		this.resolver = resolver;
	}


	@Override
	public String replaceTags(String input) {
		boolean matched;
		String oldInput;
		String curInput = input;
		do{
			if (curInput == null){
				return "";
			}

			oldInput = curInput;
			Matcher matcher = tagPattern.matcher(curInput);
			matched = false;
			StringBuffer bufStr = new StringBuffer();
			while(matcher.find()) {
				String matchedGroup = matcher.group();
				String value = getValueForTag(matchedGroup.substring(2, matchedGroup.length()-2));
				matcher.appendReplacement(bufStr, Matcher.quoteReplacement(value));
				matched = true;
			}
			matcher.appendTail(bufStr);
			curInput = bufStr.toString();
		} while(matched && !oldInput.equals(curInput));

//		for(int i = 1; i <= matcher.groupCount(); ++i) {
//			String replacementValue = getValueForTag(matcher.group(i));
//			
//			matcher.replaceAll(replacement)
//		}
		return curInput;
	}


	private Map<String, String> getMergedEnvironment(Optional<Environment> activeEnvironment, List<Environment> globalEnvironments) {
		Map<String, String> entries;
		List<EnvironmentEntry> envEntries = activeEnvironment.map(env -> new LinkedList<>(env.getEntries())).orElse(new LinkedList<>());
		globalEnvironments.forEach(ge -> envEntries.addAll(ge.getEntries()));


		entries = envEntries.stream()
				.filter(e -> e.isEnabled())
				.collect(Collectors.toMap(
						(EnvironmentEntry e) -> e.getName(),
						(EnvironmentEntry e) -> e.getValue() == null ? "" : e.getValue(),
						(String a, String b) -> a)); //in case several keys have the same name, use first one
		return entries;
	}


	private String getValueForTag(String tagName) {
		Map<String, String> entries = getMergedEnvironment(activeEnvironment, globalEnvironments);
		String trimmed = tagName.trim();
		if (entries.containsKey(trimmed)){
			return replaceTags(entries.get(trimmed));
		} else {
			return resolver.resolveViaPluginTemplater(trimmed)
					.orElse("{{" + tagName + "}}");
		}
	}

}
