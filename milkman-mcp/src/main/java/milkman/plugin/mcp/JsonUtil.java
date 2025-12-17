package milkman.plugin.mcp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonUtil {

  public static String prettyPrint(Object obj) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
    return writer.writeValueAsString(obj);
  }
}
