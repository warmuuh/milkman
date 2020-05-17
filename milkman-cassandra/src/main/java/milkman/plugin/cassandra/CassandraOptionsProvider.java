package milkman.plugin.cassandra;

import lombok.Data;
import milkman.ui.main.options.OptionDialogBuilder;
import milkman.ui.main.options.OptionDialogPane;
import milkman.ui.plugin.OptionPageProvider;
import milkman.ui.plugin.OptionsObject;

public class CassandraOptionsProvider implements OptionPageProvider<CassandraOptionsProvider.CassandraOptions>{

	@Data
	public static class CassandraOptions implements OptionsObject {
		private int maxRowFetchLimit = 500;
	} 

	private static CassandraOptions currentOptions = new CassandraOptions();
	public static CassandraOptions options() {
		return currentOptions;
	}
	
	@Override
	public CassandraOptions getOptions() {
		return currentOptions;
	}

	@Override
	public void setOptions(CassandraOptions options) {
		currentOptions = options;
	}

	@Override
	public OptionDialogPane getOptionsDialog(OptionDialogBuilder builder) {
		return builder.page("Cassandra", getOptions())
				.section("Cassandra Fetch")
					.numberInput("Row Fetch limit", CassandraOptions::getMaxRowFetchLimit, CassandraOptions::setMaxRowFetchLimit)
				.endSection()
				.build();
	}

	@Override
	public int getOrder() {
		return 500;
	}
}
