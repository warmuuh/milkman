package milkman.plugin.privatebin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * todo: make it work with browser-client of privateBin (we cant use this because we apparently use a slightly different
 *  way of encryption. cant find the issue though
 *
 */
@RequiredArgsConstructor
@Slf4j
public class PrivateBinApi {
	private static final int ITERATIONS = 10000;
	private static final int AES_KEY_LENGTH = 256;
	private static final int AUTH_TAG_LENGTH = 128;
	private final String privateBinHost;
	static SecureRandom secureRandom = new SecureRandom();

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class PrivateBinResponse {
		private String id;
		private String message;
		private int status;
		private String data;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class PrivateBinData {
		@JsonProperty("iv")
		private String initializationVector;

		@JsonProperty("v")
		private int version;

		@JsonProperty("iter")
		private int iterations;

		@JsonProperty("ks")
		private int keyStrength;

		@JsonProperty("ts")
		private int authenticationTag;

		@JsonProperty("mode")
		private String mode;

		@JsonProperty("adata")
		private String adata;

		@JsonProperty("cipher")
		private String cipher;

		@JsonProperty("salt")
		private String salt;

		@JsonProperty("ct")
		private String content;

		@JsonIgnore
		private String secret;
	}

	public String createPaste(String content, boolean burnAfterReading) throws Exception {
		PrivateBinData encrypted = encrypt(content);
		String body = getUrlEncodedRequestBody(encrypted, burnAfterReading);
		String id = sendRequest(body);
		return privateBinHost + id + "#" + encrypted.getSecret();
	}

	public String readPaste(String url) throws Exception {
		String[] split = url.split("#");
		String secret64 = split[1];
		PrivateBinData pasteData = getPasteData(split[0]);
		return decrypt(pasteData, secret64);
	}

	private PrivateBinData getPasteData(String url) throws Exception {
		URL myURL = new URL(url);
		HttpURLConnection con = (HttpURLConnection) myURL.openConnection();

		con.setRequestMethod("GET");
		con.setRequestProperty("X-Requested-With", "JSONHttpRequest");

		con.setUseCaches(false);
		con.setDoInput(true);
		con.setDoOutput(false);

		PrivateBinResponse response = new ObjectMapper().readValue(con.getInputStream(), PrivateBinResponse.class);
		if (response.getStatus() != 0) {
			throw new Exception("failed to read privatebin paste: " + response.getMessage());
		}

		PrivateBinData data = new ObjectMapper().readValue(response.getData(), PrivateBinData.class);

		return data;
	}

	private String sendRequest(String body) throws Exception {
		URL myURL = new URL(privateBinHost);
		HttpURLConnection con = (HttpURLConnection) myURL.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty("X-Requested-With", "JSONHttpRequest");
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		con.setRequestProperty("Content-Length", "" + body.getBytes().length);

		con.setUseCaches(false);
		con.setDoInput(true);
		con.setDoOutput(true);

		OutputStream os = con.getOutputStream();
		os.write(body.getBytes());
		os.close();

		PrivateBinResponse response = new ObjectMapper().readValue(con.getInputStream(), PrivateBinResponse.class);
		if (response.getStatus() != 0) {
			throw new Exception(
					"failed to create privatebin paste: " + response.getMessage() + " (" + response.getStatus() + ")");
		}
		return response.getId();
	}

	private static String getUrlEncodedRequestBody(PrivateBinData data, boolean burnAfterReading) throws Exception {
		String jsonData = new ObjectMapper().writeValueAsString(data);
		String encodedData = URLEncoder.encode(jsonData);

		return "data=" + encodedData + "&expire=1week" + "&formatter=plaintext" + "&burnafterreading="
				+ (burnAfterReading ? "1" : "0") + "&opendiscussion=0";
	}

	private static PrivateBinData encrypt(String strToEncrypt) throws Exception {
		byte[] iv = randomInitializationVector();
		byte[] salt = generateRandomSalt();
		byte[] key = generateRandomKey();
		SecretKey secretKey = generateDerivedkey(key, salt);

		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		GCMParameterSpec parameterSpec = new GCMParameterSpec(AUTH_TAG_LENGTH, iv);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
		
		String compressedData = deflate(strToEncrypt.getBytes("UTF-8"));
		byte[] encrypted = cipher.doFinal(compressedData.getBytes());
		
		return new PrivateBinData(
				Base64.getEncoder().encodeToString(iv), 
				1, ITERATIONS, AES_KEY_LENGTH, AUTH_TAG_LENGTH, "gcm", "", "aes",
				Base64.getEncoder().encodeToString(salt),
				Base64.getEncoder().encodeToString(encrypted),
				Base64.getEncoder().encodeToString(key));
	}
	
	private static String decrypt(PrivateBinData data, String secret64) throws Exception {
		byte[] iv = Base64.getDecoder().decode(data.getInitializationVector());
		byte[] salt = Base64.getDecoder().decode(data.getSalt());
		byte[] secretBytes = Base64.getDecoder().decode(secret64);
		byte[] encrypted = Base64.getDecoder().decode(data.getContent());

		SecretKey secretKey = generateDerivedkey(secretBytes, salt);

		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		GCMParameterSpec parameterSpec = new GCMParameterSpec(data.getAuthenticationTag(), iv);
		cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

		byte[] decrypted = cipher.doFinal(encrypted);
		String inflated = new String(inflate(new String(decrypted)));
		return inflated;
	}
	
	private static SecretKey generateDerivedkey(byte[] key, byte[] salt) throws Exception {
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
	    SecretKey derivedKey = factory.generateSecret(new PBEKeySpec(new String(key).toCharArray(), salt, ITERATIONS, AES_KEY_LENGTH));
	    SecretKey secretKey = new SecretKeySpec(derivedKey.getEncoded(), "AES");
	    return secretKey;
	}

	private static byte[] generateRandomKey() {
		byte[] key = new byte[AES_KEY_LENGTH / 8];
		secureRandom.nextBytes(key);
		return key;
	}

	
	
	private static byte[] generateRandomSalt() {
		byte[] key = new byte[8];
		secureRandom.nextBytes(key);
		return key;
	}

	private static byte[] randomInitializationVector() {
		byte[] iv = new byte[12]; // NEVER REUSE THIS IV WITH SAME KEY
		secureRandom.nextBytes(iv);
		return iv;
	}



	private static String deflate(byte[] data) throws Exception {
		DeflaterOutputStream def = null;
		String compressed = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		// create deflater without header
		def = new DeflaterOutputStream(out, new Deflater(Deflater.DEFAULT_COMPRESSION, true));
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
