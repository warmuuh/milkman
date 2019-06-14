package milkman.plugin.jdbc;

import lombok.Data;
import milkman.ui.main.options.OptionDialogBuilder;
import milkman.ui.main.options.OptionDialogPane;
import milkman.ui.plugin.OptionPageProvider;
import milkman.ui.plugin.OptionsObject;

public class JdbcOptionsProvider implements OptionPageProvider<JdbcOptionsProvider.JdbcOptions>{

	@Data
	public static class JdbcOptions implements OptionsObject {
		private int maxRowFetchLimit = 500;
	} 

	private static JdbcOptions currentOptions = new JdbcOptions();
	public static JdbcOptions options() {
		return currentOptions;
	}
	
	@Override
	public JdbcOptions getOptions() {
		return currentOptions;
	}

	@Override
	public void setOptions(JdbcOptions options) {
		currentOptions = options;
	}

	@Override
	public OptionDialogPane getOptionsDialog(OptionDialogBuilder builder) {
		return builder.page("Jdbc", getOptions())
				.section("Jdbc Fetch")
					.numberInput("Row Fetch limit", JdbcOptions::getMaxRowFetchLimit, JdbcOptions::setMaxRowFetchLimit)
				.endSection()
				.build();
	}

	@Override
	public int getOrder() {
		return 400;
	}
}
