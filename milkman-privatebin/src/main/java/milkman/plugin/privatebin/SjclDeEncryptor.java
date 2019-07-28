package milkman.plugin.privatebin;

import java.io.InputStreamReader;
import java.util.Base64;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import milkman.plugin.privatebin.PrivateBinApi.PrivateBinDataV1;

/**
 * uses sjcl for de-/encryption. i cannot find another way to make it work in browser as well.
 * this is slower for obvious reasons than JavaDeEncryptor
 * 
 * @author peter
 *
 */
public class SjclDeEncryptor implements DeEncryptor {
	private static final int AES_KEY_LENGTH = 256;

	private ScriptEngine engine;
	private SimpleScriptContext ctx;

	public SjclDeEncryptor() {
		init();
	}
	
	@SneakyThrows
	private void init() {
		engine = new ScriptEngineManager().getEngineByName("nashorn");
		ctx = new SimpleScriptContext();
		ctx.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
		engine.eval(new InputStreamReader(getClass().getResourceAsStream("/js/sjcl-1.0.7.js")), ctx);
		engine.eval( "function encrypt(pass,data){try{return sjcl.encrypt(pass,data, {mode:'gcm',ks:256,ts:128});}catch(e){return e;}}", ctx);
		engine.eval( "function decrypt(pass,data){try{return sjcl.decrypt(pass,data);}catch(e){return e;}}", ctx);
	}
	
	@Override
	public PrivateBinDataV1 encrypt(String strToEncrypt) throws Exception {
		byte[] key = DeEncryptor.generateRandomKey(AES_KEY_LENGTH);
		return encodeInSjcl(strToEncrypt, Base64.getEncoder().encodeToString(key));
	}

	@Override
	public String decrypt(PrivateBinDataV1 data, String secret64) throws Exception {
		return decodeSjcl(data, secret64 );
	}
	
	private PrivateBinDataV1 encodeInSjcl(String strToEncrypt, String secret64) throws Exception {
		ObjectMapper m = new ObjectMapper();
		Bindings bindings = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
		bindings.put("secret", secret64);
		bindings.put("data", strToEncrypt);
		Object evaluation = engine.eval("encrypt(secret,data)", ctx);
		if (evaluation instanceof Bindings) {
			Bindings eval = (Bindings) evaluation;
			if (eval.containsKey("message"))
			{
				System.err.println();
				throw new RuntimeException("Failed to encrypt: " + eval.get("message"));
			}
		}
		PrivateBinDataV1 data = m.readValue((String)evaluation, PrivateBinDataV1.class);
		data.setSecret(secret64);
		return data;
	}
	
	private String decodeSjcl(PrivateBinDataV1 data, String secret64) throws Exception {
		ObjectMapper m = new ObjectMapper();
		Bindings bindings = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
		bindings.put("secret", secret64);
//		bindings.put("data", "\"" + m.writeValueAsString(data).replace("\"", "\\\"") + "\"");
		bindings.put("data", m.writeValueAsString(data));
		Object evaluation = engine.eval("decrypt(secret,data)", ctx);
		if (evaluation instanceof Bindings) {
			Bindings eval = (Bindings) evaluation;
			if (eval.containsKey("message"))
			{
				System.err.println();
				throw new RuntimeException("Failed to decrypt: " + eval.get("message"));
			}
		}
		return (String)evaluation;
	}
}
