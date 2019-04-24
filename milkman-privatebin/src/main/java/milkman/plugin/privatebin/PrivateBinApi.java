package milkman.plugin.privatebin;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import milkman.persistence.UnknownPluginHandler;

/**
 * todo: how to use salt?
 * 
 * @author peter
 *
 */
@RequiredArgsConstructor
public class PrivateBinApi {
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
		
		@JsonProperty("cypher")
		private String cypher;

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
		String secret64 = url.split("#")[1];
		PrivateBinData pasteData = getPasteData(url);
		return decrypt(pasteData, secret64);
	}
	
	
	private PrivateBinData getPasteData(String url) throws Exception {
		URL myURL = new URL(url);
		HttpURLConnection con = (HttpURLConnection)myURL.openConnection();

		con.setRequestMethod("GET");
		con.setRequestProperty ("X-Requested-With", "JSONHttpRequest");
		
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
		HttpURLConnection con = (HttpURLConnection)myURL.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty ("X-Requested-With", "JSONHttpRequest");
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		con.setRequestProperty("Content-Length", "" + body.getBytes().length);
		
		con.setUseCaches(false);
		con.setDoInput(true);
		con.setDoOutput(true);
		
		OutputStream os = con.getOutputStream();
		os.write( body.getBytes() );    
		os.close();
		
		
		PrivateBinResponse response = new ObjectMapper().readValue(con.getInputStream(), PrivateBinResponse.class);
		if (response.getStatus() != 0) {
			throw new Exception("failed to create privatebin paste: " + response.getMessage() + " (" + response.getStatus() + ")");
		}
		return response.getId();
	}
	
	private static String getUrlEncodedRequestBody(PrivateBinData data, boolean burnAfterReading) throws Exception {
//		String jsonData = new ObjectMapper().writeValueAsString(data);
		String jsonData = "{"
				+ "\"iv\":\""+data.getInitializationVector()+"\","
				+ "\"v\":1,"
				+ "\"iter\":10000,"
				+ "\"ks\":256,"
				+ "\"ts\":128,"
				+ "\"mode\":\"gcm\","
				+ "\"adata\":\"\","
				+ "\"cipher\":\"aes\","
				+ "\"salt\":\""+data.getSalt()+"\","
				+ "\"ct\":\""+data.getContent()+"\"}"; 
				
		String encodedData = URLEncoder.encode(jsonData); 
		
		return "data="+encodedData
				+ "&expire=1week"
				+ "&formatter=plaintext"
				+ "&burnafterreading=" + (burnAfterReading ? "1" : "0")
				+ "&opendiscussion=0";
	}
	
	private static PrivateBinData encrypt(String strToEncrypt)  throws Exception {
        byte[] iv = randomInitializationVector();
        SecretKey secretKey = generateRandomKey();
        byte[] salt = generateRandomSalt();
        
        
//        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
//        KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt.getBytes(), 65536, 256);
//        SecretKey tmp = factory.generateSecret(spec);
//        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
        
//		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
//		IvParameterSpec ivspec = new IvParameterSpec(iv);
//		cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);

        
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        
        
        return new PrivateBinData(
        		Base64.getEncoder().encodeToString(iv),
        		1,
        		10000,
        		256,
        		128,
        		"gcm",
        		"",
        		"aes",
        		Base64.getEncoder().encodeToString(salt),
        		Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8"))),
        		Base64.getEncoder().encodeToString(secretKey.getEncoded())
        		);
	}
	
	private static SecretKey generateRandomKey() {
		byte[] key = new byte[16];
		secureRandom.nextBytes(key);
		SecretKey secretKey = new SecretKeySpec(key, "AES");
		return secretKey;
	}

	private static byte[] generateRandomSalt() {
		byte[] key = new byte[8];
		secureRandom.nextBytes(key);
		return key;
	}
	private static byte[] randomInitializationVector() {
		byte[] iv = new byte[12]; //NEVER REUSE THIS IV WITH SAME KEY
		secureRandom.nextBytes(iv);
		return iv;
	}
	
	private static String decrypt(PrivateBinData data, String secret64) throws Exception {
        byte[] iv = Base64.getDecoder().decode(data.getInitializationVector());
        byte[] salt =  Base64.getDecoder().decode(data.getSalt());
        
//        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
//        KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt, 65536, 256);
//        SecretKey tmp = factory.generateSecret(spec);
//        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
        byte[] secretBytes =  Base64.getDecoder().decode(secret64);
        SecretKey secretKey = new SecretKeySpec(secretBytes, "AES");
        
        
//		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
//		IvParameterSpec ivspec = new IvParameterSpec(iv);
//		cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
		
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
		
		
        return new String(cipher.doFinal(Base64.getDecoder().decode(data.getContent())));
	}
	
}
