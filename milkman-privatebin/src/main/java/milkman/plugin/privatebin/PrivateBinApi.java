package milkman.plugin.privatebin;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class PrivateBinApi {
	
	private final String privateBinHost;

	private static final DeEncryptor deenc =  new DeflatingWrapper(new SjclDeEncryptor()); // new DeflatingWrapper(new JavaDeEncryptor());
	
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
		PrivateBinData encrypted = deenc.encrypt(content);
		String body = getUrlEncodedRequestBody(encrypted, burnAfterReading);
		String id = sendRequest(body);
		return privateBinHost + id + "#" + encrypted.getSecret();
	}

	public String readPaste(String url) throws Exception {
		String[] split = url.split("#");
		String secret64 = split[1];
		PrivateBinData pasteData = getPasteData(split[0]);
		return deenc.decrypt(pasteData, secret64);
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


}
