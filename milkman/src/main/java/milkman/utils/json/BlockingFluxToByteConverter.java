package milkman.utils.json;

import com.fasterxml.jackson.databind.util.StdConverter;
import milkman.utils.BinaryUtil;
import reactor.core.publisher.Flux;

public class BlockingFluxToByteConverter extends StdConverter<Flux<byte[]>, byte[]> {

	@Override
	public byte[] convert(Flux<byte[]> value) {
		return value.collectList()
				.map(BinaryUtil::concat)
				.block();
	}

}
