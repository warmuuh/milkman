package me.dinowernli.grpc.polyglot.protobuf;

import com.github.os72.protocjar.Protoc;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A utility class which facilitates invoking the protoc compiler on all proto
 * files in a directory tree.
 */
@Slf4j
public class ProtocInvoker {
	private InputStream inputStream;

	/** Creates a new {@link ProtocInvoker} with the supplied stream. */
	public static ProtocInvoker forStream(InputStream inputStream) {
		return new ProtocInvoker(inputStream);
	}

	public ProtocInvoker(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	/**
	 * Executes protoc on all .proto files in the subtree rooted at the supplied
	 * path and returns a {@link FileDescriptorSet} which describes all the protos.
	 */
	public FileDescriptorSet invoke() throws ProtocInvocationException {
//		Path wellKnownTypesInclude;
//		try {
//			wellKnownTypesInclude = setupWellKnownTypes();
//		} catch (IOException e) {
//			throw new ProtocInvocationException("Unable to extract well known types", e);
//		}

		Path descriptorPath;
		try {
			descriptorPath = Files.createTempFile("descriptor", ".pb.bin");
		} catch (IOException e) {
			throw new ProtocInvocationException("Unable to create temporary file", e);
		}

		var inputFile = writeContentsToTempFile(inputStream);
		ImmutableList<String> protocArgs = ImmutableList.<String>builder()
				.add(inputFile)
				.addAll(includePathArgs(inputFile))
				.add("--include_std_types")
				.add("--descriptor_set_out=" + descriptorPath.toAbsolutePath().toString())
				.add("--include_imports")
				.build();

		invokeBinary(protocArgs);

		try {
			return FileDescriptorSet.parseFrom(Files.readAllBytes(descriptorPath));
		} catch (IOException e) {
			throw new ProtocInvocationException("Unable to parse the generated descriptors", e);
		}
	}

	@SneakyThrows
	private String writeContentsToTempFile(InputStream input) {
		var tempFile = File.createTempFile("temp", ".proto");

		FileUtils.copyInputStreamToFile(input, tempFile);

		return tempFile.getAbsolutePath();
	}

	private ImmutableList<String> includePathArgs(String inputFilePath) {
		ImmutableList.Builder<String> resultBuilder = ImmutableList.builder();
		
		// Add the include path which makes sure that protoc finds the well known types.
		// Note that we
		// add this *after* the user types above in case users want to provide their own
		// well known
		// types.
//		resultBuilder.add("-I" + wellKnownTypesInclude.toString());

		// Protoc requires that all files being compiled are present in the subtree
		// rooted at one of
		// the import paths (or the proto_root argument, which we don't use). Therefore,
		// the safest
		// thing to do is to add the discovery path itself as the *last* include.
		resultBuilder.add("-I" + new File(inputFilePath).getParent());

		return resultBuilder.build();
	}

	private void invokeBinary(ImmutableList<String> protocArgs) throws ProtocInvocationException {
		int status;
		String[] protocLogLines;


		try {
			ByteArrayOutputStream protocStdout = new ByteArrayOutputStream();
			status = Protoc.runProtoc(protocArgs.toArray(new String[0]), protocStdout, System.err);
			protocLogLines = protocStdout.toString().split("\n");
		} catch (IOException | InterruptedException e) {
			throw new ProtocInvocationException("Unable to execute protoc binary", e);
		}

		if (status != 0) {
			// If protoc failed, we dump its output as a warning.
			log.warn("Protoc invocation failed with status: " + status);
			for (String line : protocLogLines) {
				log.warn("[Protoc log] " + line);
			}

			throw new ProtocInvocationException(
					String.format("Got exit code [%d] from protoc with args [%s]", status, protocArgs));
		}
	}

//	/**
//	 * Extracts the .proto files for the well-known-types into a directory and
//	 * returns a proto include path which can be used to point protoc to the files.
//	 */
//	private static Path setupWellKnownTypes() throws IOException {
//		Path tmpdir = Files.createTempDirectory("polyglot-well-known-types");
//		Path protoDir = Files.createDirectories(Paths.get(tmpdir.toString(), "google", "protobuf"));
//		for (String file : WellKnownTypes.fileNames()) {
//			Files.copy(ProtocInvoker.class.getResourceAsStream("/google/protobuf/" + file),
//					Paths.get(protoDir.toString(), file));
//		}
//		return tmpdir;
//	}

	/** An error indicating that something went wrong while invoking protoc. */
	public class ProtocInvocationException extends Exception {
		private static final long serialVersionUID = 1L;

		private ProtocInvocationException(String message) {
			super(message);
		}

		private ProtocInvocationException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
