package milkman.plugin.privatebin;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStreamReader;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import milkman.plugin.privatebin.PrivateBinApi.PrivateBinDataV1;

class PrivateBinApiTest {

	@Test
	void testPasteDeEncryption() throws Exception {
		PrivateBinApi sut = new PrivateBinApi("https://privatebin.net/?");
		String id = sut.createPaste("some test content", false);
		System.out.println("ID: " + id);
		assertThat(id).isNotBlank();

		String content = sut.readPaste(id);

		System.out.println("content: " + content);
		assertThat(content).isEqualTo("some test content");
	}
//
//	@Test 
//	void testPasteLookup() throws Exception {
//		PrivateBinApi sut = new PrivateBinApi("https://privatebin.net/?");
//		String content = sut.readPaste("https://privatebin.net/?99eb04fd237326b7#IfYuT4ZZ/EeUjo3u1H9Hd3yULpspFRF8CSgiECC/xqU=");
//		
//		
//		System.out.println("content: " + content);
//		assertThat(content).isNotBlank();
//	}
//
//	@Test
//	void decodeInSjcl() throws Exception {
//		PrivateBinApi sut = new PrivateBinApi("https://privatebin.net/?");
//		PrivateBinData data = sut.encrypt("1");
//		decodeSjcl(data);
//	}

//	@SneakyThrows
//	private void decodeSjcl(PrivateBinDataV1 data) {
//		ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
//		engine.eval(new InputStreamReader(getClass().getResourceAsStream("/js/sjcl-1.0.7.js")));
//		String _sFuncPattern = "function %s(pass,data){try{return sjcl.%s(pass,data);}catch(e){return e;}}";
//		engine.eval(String.format(_sFuncPattern, "encrypt", "encrypt"));
//		engine.eval(String.format(_sFuncPattern, "decrypt", "decrypt"));
//
////			Bindings bindings = engine.createBindings();
//		ObjectMapper m = new ObjectMapper();
//		Bindings eval = (Bindings) engine.eval("decrypt(\"" + data.getSecret() + "\",\"" + m.writeValueAsString(data).replace("\"", "\\\"") + "\")");
//		if (eval.containsKey("message"))
//		{
//			System.err.println(eval.get("message"));
//		} else {
//			System.out.println(eval);
//		}
//
//	}
}
