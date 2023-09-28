package milkman.plugin.nosql.domain;

import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import milkman.domain.ResponseAspect;

@Data
public class NosqlResponseAspect implements ResponseAspect {

	List<String> columnNames = new LinkedList<>();
	List<List<String>> rows = new LinkedList<>();
	
	
	@Override
	public String getName() {
		return "result";
	}


	public void addRow(List<String> row) {
		rows.add(row);
	}
	
}
