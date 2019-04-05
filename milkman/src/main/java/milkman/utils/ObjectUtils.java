package milkman.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.SneakyThrows;

public class ObjectUtils {

	
	@SneakyThrows
	public static <T> T deepClone(T original) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		
		String jsonContent = mapper.writeValueAsString(original);
		T clone = (T) mapper.readValue(jsonContent, original.getClass());
		return clone;
	}
	
	
}
