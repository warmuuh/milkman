package co.poynt.postman.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostmanVariables {
	public static final String POSTMAN_EXP = "\\{\\{[^\\}]+\\}\\}";
	public static final String GUID = "{{$guid}}";
	public static final String TIMESTAMP = "{{$timestamp}}";
	public static final String RANDOMINT = "{{$randomInt}}";
	private Random r = new Random(1000);
	private PostmanEnvironment env;

	public PostmanVariables(PostmanEnvironment env) {
		this.env = env;
	}

	private String getConstantVal(String exp) {
		if (exp.equalsIgnoreCase(GUID)) {
			return UUID.randomUUID().toString();
		} else if (exp.equalsIgnoreCase(TIMESTAMP)) {
			return Long.toString(System.currentTimeMillis() / 1000);
		} else if (exp.equalsIgnoreCase(RANDOMINT)) {
			return Integer.toString(r.nextInt(1000));
		} else {
			throw new IllegalArgumentException(
					"Invalid POSTMAN dynamic variable " + exp);
		}
	}
	
	private String getVal(String name) {
		if (name.startsWith("{{$")) {
			return getConstantVal(name);
		}
		
		String key = name.substring(2, name.length()-2);
		PostmanEnvValue val = this.env.lookup.get(key);
		if (val == null) {
			//throw new IllegalArgumentException("Invalid dynamic variable: " + name);
			return "UNDEFINED";
		}
		return val.value;
	}

	/**
	 * Replace all {{dynamic variable}} in orig string with values
	 * found in the environment. If variable is not found, replace it
	 * with constant string "UNDEFINED".
	 * @param orig
	 * @return
	 */
	public String replace(String orig) {
		if (orig == null || orig.isEmpty()) {
			return orig;
		}
		// Get all the dynamic variables
		List<String> allMatches = new ArrayList<String>();
		Matcher m = Pattern.compile(POSTMAN_EXP).matcher(orig);
		while (m.find()) {
			allMatches.add(m.group());
		}

		//TODO: this is not the most efficient way to do it
		// but it is the simplest in term of code and this is not
		// production code anyway.
		String result = orig;
		for (String var : allMatches) {
			String varVal = getVal(var);
			//System.out.println(var + " ==> " + varVal);
			result = result.replace((CharSequence) var, (CharSequence) varVal);
		}
		
		return result;
	}
	
	public PostmanEnvironment getEnv() {
		return env;
	}
}
