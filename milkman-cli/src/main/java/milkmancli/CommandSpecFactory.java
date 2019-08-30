package milkmancli;

import javax.inject.Singleton;

import milkmancli.TerminalCommand.Parameter;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.ISetter;
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
        
        
        for (Parameter parameter : cmd.getParameters()) {
        	cmdSpec.addPositional(PositionalParamSpec.builder()
        			.paramLabel(parameter.getName())
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
        cmdSpec.mixinStandardHelpOptions(true);
		return cmdSpec;
	}
}
