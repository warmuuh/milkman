package me.dinowernli.grpc.polyglot.protobuf;

import java.io.IOException;
import java.io.InputStream;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.ExtensionRegistryLite;

import com.google.protobuf.util.JsonFormat.TypeRegistry;
import io.grpc.MethodDescriptor.Marshaller;

/** A {@link Marshaller} for dynamic messages. */
public class DynamicMessageMarshaller implements Marshaller<DynamicMessage> {
  private final Descriptor messageDescriptor;

  public DynamicMessageMarshaller(Descriptor messageDescriptor) {
    this.messageDescriptor = messageDescriptor;
  }

  @Override
  public DynamicMessage parse(InputStream inputStream) {
    try {
      return DynamicMessage.newBuilder(messageDescriptor)
          .mergeFrom(inputStream, ExtensionRegistryLite.getEmptyRegistry())
          .build();
    } catch (IOException e) {
      throw new RuntimeException("Unable to merge from the supplied input stream", e);
    }
  }

  @Override
  public InputStream stream(DynamicMessage abstractMessage) {
    return abstractMessage.toByteString().newInput();
  }
}