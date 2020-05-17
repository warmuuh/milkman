package milkman.plugin.jdbc.domain;

import lombok.Data;
import milkman.domain.ResponseAspect;

import java.util.LinkedList;
import java.util.List;

@Data
public class RowSetResponseAspect implements ResponseAspect {

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
