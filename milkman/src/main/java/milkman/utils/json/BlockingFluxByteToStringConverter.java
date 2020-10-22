package milkman.utils.json;

import com.fasterxml.jackson.databind.util.StdConverter;
import milkman.utils.BinaryUtil;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;

public class BlockingFluxByteToStringConverter extends StdConverter<Flux<byte[]>, String> {

	@Override
	public String convert(Flux<byte[]> value) {
		return value.collectList()
				.map(BinaryUtil::concat)
				.map(bytes -> new String(bytes, StandardCharsets.UTF_8))
				.block();
	}

}
