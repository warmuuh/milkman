package milkman.plugin.grpc.processor;

import com.google.protobuf.DescriptorProtos.*;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;
import com.google.protobuf.Descriptors.FieldDescriptor;
import lombok.Value;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class ProtoDescriptorSerializer {

	@Value
	public static class FileContent{
		String fileName;
		String contents;
	}
	
	public List<FileContent> descriptorToString(FileDescriptorSet descriptorSet) {
		
		List<FileContent> result = new LinkedList<>();
		
		for (FileDescriptorProto protoFileDesc : descriptorSet.getFileList()) {
			StringBuilder buffer = new StringBuilder();
			buffer.append("syntax = \"").append(protoFileDesc.getSyntax()).append("\";\n");
			
			for (Entry<FieldDescriptor, Object> opt : protoFileDesc.getOptions().getAllFields().entrySet()) {
				buffer.append("option ").append(opt.getKey().getName()).append(" = ").append(opt.getValue()).append(";\n");
			}
			
			buffer.append("package ").append(protoFileDesc.getPackage()).append(";\n");
			
			buffer.append("\n");
			
			for (DescriptorProto msgType : protoFileDesc.getMessageTypeList()) {
				buffer.append("message ").append(msgType.getName()).append(" {\n");
				for (FieldDescriptorProto field : msgType.getFieldList()) {
					String type = getType(field.getType());
					buffer.append("    ")
					.append(toLabel(field.getLabel()))
					.append(type)
					.append(" ")
					.append(field.getName())
					.append(" = ")
					.append(field.getNumber())
					.append(";\n");
				}
				buffer.append("}\n\n");
			}
			
			boolean firstService = true;
			for (ServiceDescriptorProto serviceDesc : protoFileDesc.getServiceList()) {
				if (!firstService) {
					buffer.append("\n");
				}
				firstService = false;
				
				buffer.append("service ").append(serviceDesc.getName()).append(" {\n");
				for (MethodDescriptorProto method : serviceDesc.getMethodList()) {
					buffer.append("    rpc ")
					.append(method.getName())
					.append("(");
					if (method.getClientStreaming())
						buffer.append("stream ");
					
					buffer.append(method.getInputType())
					.append(") returns (");
					
					if (method.getServerStreaming())
						buffer.append("stream ");
					
					buffer
					.append(method.getOutputType())
					.append(");\n");
				}
				buffer.append("}\n");
			}
			
			result.add(new FileContent(protoFileDesc.getName(), buffer.toString()));
		}
		
		return result;
	}

	private String toLabel(FieldDescriptorProto.Label label) {
		if (label == null){
			return "";
		}
		switch (label){
			case LABEL_OPTIONAL: return ""; //optional is implicit
			case LABEL_REQUIRED: return "required ";
			case LABEL_REPEATED: return "repeated ";
			default: return "";
		}
	}

	private String getType(Type type) {
		switch (type) {
		case TYPE_BOOL:
			return "bool";
		case TYPE_BYTES:
			return "bytes";
		case TYPE_DOUBLE:
			return "double";
		case TYPE_ENUM:
			return "enum";
		case TYPE_FIXED32:
			return "fixed32";
		case TYPE_FIXED64:
			return "fixed64";
		case TYPE_FLOAT:
			return "float";
		case TYPE_GROUP:
			return "group";
		case TYPE_INT32:
			return "int32";
		case TYPE_INT64:
			return "int64";
		case TYPE_MESSAGE:
			return "message";
		case TYPE_SFIXED32:
			return "sfixed32";
		case TYPE_SFIXED64:
			return "sfixed64";
		case TYPE_SINT32:
			return "sint32";
		case TYPE_SINT64:
			return "sint64";
		case TYPE_STRING:
			return "string";
		case TYPE_UINT32:
			return "uint32";
		case TYPE_UINT64:
			return "uint64";
		default:
			break;
		
		
		}
		throw new IllegalArgumentException("Unknown type: " + type);
	}
}
