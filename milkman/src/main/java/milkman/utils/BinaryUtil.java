package milkman.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class BinaryUtil {

	public static byte[] concat(List<byte[]> byteArrays) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byteArrays.forEach(b -> {
			try {
				baos.write(b);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		return baos.toByteArray();
	}
}
