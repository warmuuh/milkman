package milkman.utils;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Stopwatch {

	private static Map<String, Long> watches = new HashMap<String, Long>();

	private static boolean enabled = false;
	
	public static void start(String stopwatchId) {
		if(!enabled)
			return;
		
		watches.put(stopwatchId, System.currentTimeMillis());
		log.info("[{}] started", stopwatchId);

	}
	
	public static void logTime(String stopwatchId, String msg) {
		if(!enabled)
			return;
		
		Long startingTime = watches.get(stopwatchId);
		if (startingTime == null) {
			log.warn("No stopwatch found with id {}", stopwatchId);
		}
		
		log.info("[{}] {}: {} ms", stopwatchId, msg, System.currentTimeMillis() - startingTime);
	}
	
	public static void stop(String stopwatchId) {
		if(!enabled)
			return;
		
		Long startingTime = watches.get(stopwatchId);
		if (startingTime == null) {
			log.warn("No stopwatch found with id {}", stopwatchId);
		}
		
		log.info("[{}] finished in {} ms", stopwatchId, System.currentTimeMillis() - startingTime);
		watches.remove(stopwatchId);
	}
	
}
