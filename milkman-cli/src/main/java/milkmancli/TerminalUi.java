package milkmancli;

import static milkmancli.utils.StringUtil.stringToId;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.MaskingCallback;
import org.jline.reader.ParsedLine;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import milkmancli.cmds.ChangeCollection;
import milkmancli.cmds.ChangeWorkspace;
import milkmancli.cmds.EditRequestAspect;
import milkmancli.cmds.ExecRequest;
import milkmancli.cmds.QuitApplication;
import picocli.CommandLine;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.Model.CommandSpec;
import picocli.shell.jline3.PicocliJLineCompleter;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
@Slf4j
public class TerminalUi {

	private final CommandSpecFactory commandSpecFactory;
	private final CliContext cliContext;

	private final ChangeWorkspace wsCmd;
	private final ChangeCollection colCmd;
	private final ExecRequest execCmd;
	private final QuitApplication quitCmd;
	private final EditRequestAspect editCmd;
	
	
	private LineReader reader;
	private static Terminal terminal;
	private String rightPrompt;


	private CommandLine cmd;

	@PostConstruct
	@SneakyThrows
	public void setup() {
		cmd = new CommandLine(CommandSpec.create()
				.name("")
				.addSubcommand(null, commandSpecFactory.getSpecFor(wsCmd))
				.addSubcommand(null, commandSpecFactory.getSpecFor(execCmd))
				.addSubcommand(null, commandSpecFactory.getSpecFor(editCmd))
				.addSubcommand(null, commandSpecFactory.getSpecFor(quitCmd))
				.addSubcommand(null, commandSpecFactory.getSpecFor(colCmd)));
        
		cmd.setExecutionExceptionHandler(new IExecutionExceptionHandler() {
			
			@Override
			public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult)
					throws Exception {
				System.out.println(ex.getMessage());
				log.error("Command execution failed", ex);
				return 0;
			}
		});
        
        terminal = TerminalBuilder.builder()
        		.build();
        reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(new PicocliJLineCompleter(cmd.getCommandSpec()))
                .parser(new DefaultParser())
                .build();
        rightPrompt = null;
	}

	public void runCommandLoop() {
		try {
			greet();
            // start the shell and process input until the user quits with Ctl-D
            while (true) {
                try {
                	String prompt = buildPrompt(cliContext);
                    String line = reader.readLine(prompt, rightPrompt, (MaskingCallback) null, null);
            		ParsedLine pl = reader.getParser().parse(line, 0);
            		String[] arguments = pl.words().toArray(new String[0]);
                    executeCommand(arguments);
                } catch (UserInterruptException e) {
                    // Ignore
                } catch (EndOfFileException e) {
                    return;
                }                    
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
	}

	protected void greet() {
		try {
			String banner = IOUtils.toString(getClass().getResourceAsStream("/banner.txt"));
			System.out.print(banner);
		} catch (IOException e) {
			/* ignore */
		}
		cmd.usage(System.out);
	}

	public void executeCommand(String[] arguments) {
		cmd.execute(arguments);
	}

	private String buildPrompt(CliContext ctx) {
		StringBuilder b = new StringBuilder();
		if (ctx.getCurrentWorkspace() != null)
			b.append(stringToId(ctx.getCurrentWorkspace().getName()));
		
		if (ctx.getCurrentCollection() != null)
			b.append("/")
			 .append(stringToId(ctx.getCurrentCollection().getName()));
		
		b.append(">");
		return  b.toString();
	}

	public static Terminal getTerminal() {
		return terminal;
	}
}
