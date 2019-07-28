package milkman.plugin.privatebin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.commons.io.IOUtils;

import lombok.Data;
import milkman.plugin.privatebin.PrivateBinApi.PrivateBinDataV1;

@Data
public class DeflatingWrapper implements DeEncryptor {

	private final DeEncryptor delegate;

	@Override
	public PrivateBinDataV1 encrypt(String strToEncrypt) throws Exception {
		return delegate.encrypt(deflate(strToEncrypt.getBytes()));
	}

	@Override
	public String decrypt(PrivateBinDataV1 data, String secret64) throws Exception {
		return new String(inflate(delegate.decrypt(data, secret64)));
	}
	

	private static String deflate(byte[] data) throws Exception {
		DeflaterOutputStream def = null;
		String compressed = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		// create deflater without header
		def = new DeflaterOutputStream(out, new Deflater(Deflater.NO_COMPRESSION, true)); //we have to use no-compression, bc java and js inflate are not compatible
		def.write(data);
		def.close();
		compressed = Base64.getEncoder().encodeToString(out.toByteArray());
		return compressed;
	}
	

	private static byte[] inflate(String data) throws Exception {
		byte[] compressed = Base64.getDecoder().decode(data);
		ByteArrayInputStream in = new ByteArrayInputStream(compressed);
		InflaterInputStream inf = new InflaterInputStream(in, new Inflater(true));
		byte[] byteArray = IOUtils.toByteArray(inf);
		return byteArray;
	}
	
	
}
