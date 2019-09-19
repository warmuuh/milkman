package milkmancli;

import javax.inject.Singleton;

import milkmancli.TerminalCommand.Option;
import milkmancli.TerminalCommand.Parameter;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.ISetter;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Model.UsageMessageSpec;

@Singleton
public class CommandSpecFactory {

	
	CommandSpec getSpecFor(TerminalCommand cmd) {
        var cmdSpec = CommandSpec.wrapWithoutInspection(cmd);
        cmdSpec.name(cmd.getName())
        	.aliases(cmd.getAlias())
        	.usageMessage(new UsageMessageSpec()
        			.description(cmd.getDescription()));
        
        int idx = 0;
        for (Parameter parameter : cmd.getParameters()) {
        	addPositionalParameter(cmdSpec, parameter, idx);
        	idx++;
        }
        
        for(Option option : cmd.getOptions()) {
        	addBooleanOption(cmdSpec, option);
        }
		return cmdSpec;
	}

	private void addBooleanOption(CommandSpec cmdSpec, Option option) {
		cmdSpec.addOption(OptionSpec
				.builder("--"+option.getName(), "-"+option.getAlias())
				.description(option.getDescription())
				.arity("0")
				.required(false)
				.setter(new ISetter() {

					@Override
					public <T> T set(T value) throws Exception {
						option.setValue(Boolean.TRUE.equals(value));
						return null;
					}
					
				})
				.build());
	}

	private CommandSpec addPositionalParameter(CommandSpec cmdSpec, Parameter parameter, int idx) {
		return cmdSpec.addPositional(PositionalParamSpec.builder()
				.paramLabel(parameter.getName())
				.arity("1")
				.index(""+idx)
				.required(parameter.isRequired())
				.setter(new ISetter() {

					@Override
					public <T> T set(T value) throws Exception {
						parameter.setValue((String)value);
						return null;
					}
					
				})
				.description(parameter.getDescription())
				.completionCandidates(parameter.getCompletion())
				.build());
	}
}
