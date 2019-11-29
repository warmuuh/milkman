package milkman.plugin.grpc.processor;


import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import com.google.protobuf.DescriptorProtos.FileDescriptorSet;

import me.dinowernli.grpc.polyglot.protobuf.ProtocInvoker;
import me.dinowernli.grpc.polyglot.protobuf.ProtocInvoker.ProtocInvocationException;

class ProtoDescriptorSerializerTest {

	@Test
	void test() throws Exception {
		String file = IOUtils.toString(getClass().getResourceAsStream("/HelloService.proto"));
		FileDescriptorSet descriptorSet = toDescriptor(file);
		
		var sut = new ProtoDescriptorSerializer();
		var generatedFiles = sut.descriptorToString(descriptorSet);
		var generatedContent = generatedFiles.get(0).getContents(); 
		var generatedDesc = toDescriptor(generatedContent);
		
		assertThat(generatedDesc.getFile(0)).isEqualToIgnoringGivenFields(descriptorSet.getFile(0), "name_", "package_", "syntax_");
		
	}

	protected FileDescriptorSet toDescriptor(String file) throws ProtocInvocationException {
		ProtocInvoker invoker = new ProtocInvoker(IOUtils.toInputStream(file));
		FileDescriptorSet descriptorSet = invoker.invoke();
		return descriptorSet;
	}

}
