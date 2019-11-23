package me.dinowernli.grpc.polyglot.io;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.TypeRegistry;

import milkman.plugin.grpc.processor.MessageWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

/** A utility class which knows how to read proto files written using {@link MessageWriter}. */
public class MessageReader {
  private final JsonFormat.Parser jsonParser;
  private final Descriptor descriptor;
  private final BufferedReader bufferedReader;
  private final String source;

  /** Creates a {@link MessageReader} which reads messages from a stream. */
  public static MessageReader forStream(InputStream stream, Descriptor descriptor, TypeRegistry registry) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    return new MessageReader(
        JsonFormat.parser().usingTypeRegistry(registry),
        descriptor,
        reader,
        "STDIN");
  }

  /** Creates a {@link MessageReader} which reads the messages from a file. */
  public static MessageReader forFile(Path path, Descriptor descriptor) {
    return forFile(path, descriptor, TypeRegistry.getEmptyTypeRegistry());
  }

  /** Creates a {@link MessageReader} which reads the messages from a file. */
  public static MessageReader forFile(Path path, Descriptor descriptor, TypeRegistry registry) {
    try {
      return new MessageReader(
          JsonFormat.parser().usingTypeRegistry(registry),
          descriptor,
          Files.newBufferedReader(path),
          path.toString());
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to read file: " + path.toString(), e);
    }
  }

  @VisibleForTesting
  MessageReader(
      JsonFormat.Parser jsonParser,
      Descriptor descriptor,
      BufferedReader bufferedReader,
      String source) {
    this.jsonParser = jsonParser;
    this.descriptor = descriptor;
    this.bufferedReader = bufferedReader;
    this.source = source;
  }

  /** Parses all the messages and returns them in a list. */
  public ImmutableList<DynamicMessage> read() {
    ImmutableList.Builder<DynamicMessage> resultBuilder = ImmutableList.builder();
    try {
      String line;
      boolean wasLastLineEmpty = false;
      while (true) {
        line = bufferedReader.readLine();

        // Two consecutive empty lines mark the end of the stream.
        if (Strings.isNullOrEmpty(line)) {
          if (wasLastLineEmpty) {
            return resultBuilder.build();
          }
          wasLastLineEmpty = true;
          continue;
        } else {
          wasLastLineEmpty = false;
        }

        // Read the next full message.
        StringBuilder stringBuilder = new StringBuilder();
        while (!Strings.isNullOrEmpty(line)) {
          stringBuilder.append(line);
          line = bufferedReader.readLine();
        }
        wasLastLineEmpty = true;

        DynamicMessage.Builder nextMessage = DynamicMessage.newBuilder(descriptor);
        jsonParser.merge(stringBuilder.toString(), nextMessage);

        // Clean up and prepare for next message.
        resultBuilder.add(nextMessage.build());
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Unable to read messages from: " + source, e);
    }
  }
}
