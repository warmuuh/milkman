package milkman.plugin.explore;

import java.util.LinkedList;
import java.util.List;

import lombok.Data;
import milkman.domain.RequestAspect;

@Data
public class JqlQueryAspect extends RequestAspect {
	private String query = "";
	private List<String> queryHistory = new LinkedList<>();
	
	public JqlQueryAspect() {
		super("jql");
	}
}
