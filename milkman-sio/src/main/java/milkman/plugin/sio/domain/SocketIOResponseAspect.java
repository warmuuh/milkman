package milkman.plugin.sio.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import milkman.domain.ResponseAspect;
import milkman.plugin.sio.MilkmanSocketIOClient;

/**
 * TODO: this will be transformed into holding client & body, so that we can implement
 * list-view on incoming/outgoing events.
 * For now, we use RestResponseBodyAspect though, so this aspect only holds the client for further interactions
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocketIOResponseAspect implements ResponseAspect {

	@JsonIgnore
	private MilkmanSocketIOClient client;

	@Override
	public String getName() {
		return "sioBody";
	}
}
