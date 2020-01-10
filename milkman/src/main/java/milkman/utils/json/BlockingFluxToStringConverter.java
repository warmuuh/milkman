package milkman.utils.json;

import com.fasterxml.jackson.databind.util.StdConverter;
import reactor.core.publisher.Flux;

public class BlockingFluxToStringConverter extends StdConverter<Flux<String>, String> {

    @Override
    public String convert(Flux<String> value) {
        StringBuilder b = new StringBuilder();
        value.subscribe(b::append, b::append);
        value.blockLast();
        return b.toString();
    }

}
