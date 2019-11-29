package milkman.plugin.grpc.processor;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.TypeRegistry;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageWriter<T extends Message> {

  private final JsonFormat.Printer jsonPrinter;

  /**
   * Creates a new {@link MessageWriter} which writes the messages it sees to the supplied
   */
  public static <T extends Message> MessageWriter<T> create(TypeRegistry registry) {
    return new MessageWriter<>(JsonFormat.printer().usingTypeRegistry(registry));
  }
  
  
  public String convertMessage(T message) {
    try {
      return jsonPrinter.print(message);
    } catch (InvalidProtocolBufferException e) {
      log.error("Skipping invalid response message", e);
      return "<invalid msg>";
    }
  }
}
