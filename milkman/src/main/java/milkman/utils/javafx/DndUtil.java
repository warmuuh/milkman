package milkman.utils.javafx;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.input.DataFormat;
import lombok.SneakyThrows;
import milkman.persistence.UnknownPluginHandler;

public class DndUtil {
	public static final DataFormat JAVA_FORMAT = new DataFormat("application/x-java-serialized-object");

	@SneakyThrows
	public static String serialize(Object obj) {
		return createMapper().writeValueAsString(obj);
	}

	@SneakyThrows
	public static <T> T deserialize(String content, Class<T> type) {
		return createMapper().readValue(content, type);
	}

	private static ObjectMapper createMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.addHandler(new UnknownPluginHandler());
		return mapper;
	}
}
