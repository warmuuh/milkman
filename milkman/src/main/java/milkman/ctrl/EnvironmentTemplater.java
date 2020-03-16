package milkman.ctrl;

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

	Map<String, String> values;
	
//	private final Pattern tagPattern = Pattern.compile("(?<=[\\{]{2})([^}]+)(?=[\\}]{2})");
	private final Pattern tagPattern = Pattern.compile("([\\{]{2}[^}]+[\\}]{2})");
	private Map<String, String> entries;
	
	
	
	
	public EnvironmentTemplater(Optional<Environment> activeEnvironment, List<Environment> globalEnvironments) {
		super();
		
		List<EnvironmentEntry> envEntries = activeEnvironment.map(env -> new LinkedList<>(env.getEntries())).orElse(new LinkedList<>());
		globalEnvironments.forEach(ge -> envEntries.addAll(ge.getEntries()));
		
		
		entries = envEntries.stream()
			.filter(e -> e.isEnabled())
			.collect(Collectors.toMap(
					(EnvironmentEntry e) -> e.getName(), 
					(EnvironmentEntry e) -> e.getValue() == null ? "" : e.getValue(),
					(String a, String b) -> a)); //in case several keys have the same name, use first one
	}


	@Override
	public String replaceTags(String input) {
		Matcher matcher = tagPattern.matcher(input);
		
		StringBuffer bufStr = new StringBuffer();
		while(matcher.find()) {
			String matchedGroup = matcher.group();
			String value = getValueForTag(matchedGroup.substring(2, matchedGroup.length()-2));
			matcher.appendReplacement(bufStr, value);
		}
		matcher.appendTail(bufStr);
//		for(int i = 1; i <= matcher.groupCount(); ++i) {
//			String replacementValue = getValueForTag(matcher.group(i));
//			
//			matcher.replaceAll(replacement)
//		}
		return bufStr.toString();
	}


	private String getValueForTag(String tagName) {
		if (entries.containsKey(tagName))
			return replaceTags(entries.get(tagName));
		else
			return "{{" + tagName + "}}";
	}

}
