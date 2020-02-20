package milkman.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@RequiredArgsConstructor
public class MapDbJsonSerializer<T> extends Serializer<T> {

    private final ObjectMapper objMapper;
    private final Class<T> type;

    @Override
    public void serialize(DataOutput out, T t) throws IOException {
        BYTE_ARRAY.serialize(out, objMapper.writeValueAsBytes(t));
    }

    @Override
    public T deserialize(DataInput in, int available) throws IOException {
        return objMapper.readValue(BYTE_ARRAY.deserialize(in,available), type);
    }
}
