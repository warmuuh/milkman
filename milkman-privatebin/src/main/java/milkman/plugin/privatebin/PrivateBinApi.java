package milkman.plugin.privatebin;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.plugin.privatebin.PrivateBinApi.PrivateBinDataV2.PrivateBinDataMeta;

@RequiredArgsConstructor
@Slf4j
//TODO: current version of encryption does not work via browser-access (privatebin changed their encryption algorithm with v1.3)
public class PrivateBinApi {
	
	private final String privateBinHost;

	private static final DeEncryptor deenc =  new DeflatingWrapper(new SjclDeEncryptor()); // new DeflatingWrapper(new JavaDeEncryptor());
	
	public String createPaste(String content, boolean burnAfterReading) throws Exception {
		PrivateBinDataV1 encrypted = deenc.encrypt(content);
		PrivateBinDataV2 v2Format = translateToV2(burnAfterReading, encrypted);
		String body = new ObjectMapper().writeValueAsString(v2Format);
		String id = sendRequest(body);
		return privateBinHost + id + "#" + encrypted.getSecret();
	}

	public String readPaste(String url) throws Exception {
		String[] split = url.split("#");
		String secret64 = split[1];
		PrivateBinDataV1 pasteData = getPasteData(split[0]);
		return deenc.decrypt(pasteData, secret64);
	}

	private PrivateBinDataV1 getPasteData(String url) throws Exception {
		URL myURL = new URL(url);
		HttpURLConnection con = (HttpURLConnection) myURL.openConnection();

		con.setRequestMethod("GET");
		con.setRequestProperty("X-Requested-With", "JSONHttpRequest");

		con.setUseCaches(false);
		con.setDoInput(true);
		con.setDoOutput(false);

		PrivateBinResponseV2 response = new ObjectMapper().readValue(con.getInputStream(), PrivateBinResponseV2.class);
		if (response.getStatus() != 0) {
			throw new Exception("failed to read privatebin paste: " + response.getMessage());
		}

		return translateToV1(response);
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

	private static String getUrlEncodedRequestBody(PrivateBinDataV1 data, boolean burnAfterReading) throws Exception {
		String jsonData = new ObjectMapper().writeValueAsString(data);
		String encodedData = URLEncoder.encode(jsonData);

		return "data=" + encodedData + "&expire=1week" + "&formatter=plaintext" + "&burnafterreading="
				+ (burnAfterReading ? "1" : "0") + "&opendiscussion=0";
	}

	protected PrivateBinDataV1 translateToV1(PrivateBinResponseV2 response) {
		Object[] algorithmCfg = ((ArrayList) response.getAdata()[0]).toArray();
		var privateBinDataV1 = new PrivateBinDataV1(
				(String)algorithmCfg[0], 
				1, 
				10_000, //we fall back to 10.000 cause we still use sjcl for decryption, they changed to 100_000 
				(Integer)algorithmCfg[3], 
				(Integer)algorithmCfg[4], 
				(String)algorithmCfg[6], 
				"", 
				(String)algorithmCfg[5],
				(String)algorithmCfg[1],
				response.getContent(), 
				null);
		return privateBinDataV1;
	}
	
	protected PrivateBinDataV2 translateToV2(boolean burnAfterReading, PrivateBinDataV1 encrypted) {
		return new PrivateBinDataV2(2, new Object[] {
				new Object[] {
						encrypted.getInitializationVector(),
						encrypted.getSalt(),
						100_000, //they changed something and sjcl defaults to 10.000, we have to figure out whats going on here
						encrypted.getKeyStrength(),
						encrypted.getAuthenticationTag(),
						encrypted.getCipher(),
						encrypted.getMode(),
						"zlib"
				},
						"plaintext",
						(burnAfterReading ? 1 : 0),
						0 // open for discussion
				}, 
				encrypted.getContent(), 
				new PrivateBinDataMeta("1week"), 
				encrypted.getSecret());
	}
	
	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class PrivateBinResponse {
		private String id;
		private String message;
		private int status;
		private String data;
	}
	
	
	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class PrivateBinResponseV2 extends PrivateBinDataV2 {
		private String id;
		private String message;
		private int status;
	}
	

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class PrivateBinDataV1 {
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

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class PrivateBinDataV2 {
		@JsonProperty("v")
		private int version;

		@JsonProperty("adata")
		private Object[] adata;

		@JsonProperty("ct")
		private String content;

		@JsonProperty("meta")
		private PrivateBinDataMeta meta;
		
		@JsonIgnore
		private String secret;
		
		@Data
		@NoArgsConstructor
		@AllArgsConstructor
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class PrivateBinDataMeta{
			private String expire;
		}
	}
}
