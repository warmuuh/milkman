package milkman.plugin.grpc.processor;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.util.JsonFormat.TypeRegistry;

import lombok.Getter;
import me.dinowernli.grpc.polyglot.io.MessageReader;
import me.dinowernli.grpc.polyglot.protobuf.ProtoMethodName;
import me.dinowernli.grpc.polyglot.protobuf.ServiceResolver;

public class DynamicMessageDeEncoder {

	@Getter
	private MethodDescriptor methodDefinition;
	private MessageWriter<DynamicMessage> writer;
	private TypeRegistry registry;

	public DynamicMessageDeEncoder(ProtoMethodName protoMethod, FileDescriptorSet fileDescriptor) {
		var resolver = ServiceResolver.fromFileDescriptorSet(fileDescriptor);
		methodDefinition = resolver.resolveServiceMethod(protoMethod);

		registry = TypeRegistry.newBuilder().add(resolver.listMessageTypes()).build();

		writer = MessageWriter.create(registry);
	}

	ImmutableList<DynamicMessage> deserializeFromJson(String input) {
		MessageReader reader = MessageReader.forStream(IOUtils.toInputStream(input), methodDefinition.getInputType(),
				registry);
		return reader.read();
	}

	String serializeToJson(DynamicMessage message) {
		return writer.convertMessage(message);
	}

}
