package milkman.plugin.jdbc.domain;

import java.util.LinkedList;
import java.util.List;

import lombok.Data;
import milkman.domain.ResponseAspect;

@Data
public class RowSetResponseAspect implements ResponseAspect {

	List<String> columnNames = new LinkedList<String>();
	List<List<Object>> rows = new LinkedList<List<Object>>();
	
	
	@Override
	public String getName() {
		return "result";
	}


	public void addRow(List<Object> row) {
		rows.add(row);
	}
	
}
