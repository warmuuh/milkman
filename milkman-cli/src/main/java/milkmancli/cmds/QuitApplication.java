package milkmancli.cmds;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;
import milkmancli.CliContext;
import milkmancli.TerminalCommand;

/**
 * Command that clears the screen.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class QuitApplication extends TerminalCommand {

	private final CliContext context;

	public Void call() throws IOException {
		System.exit(0);
		return null;
	}


	@Override
	public String getName() {
		return "quit";
	}

	@Override
	public String getAlias() {
		return "q";
	}

	@Override
	public String getDescription() {
		return "Quits Application";
	}

}