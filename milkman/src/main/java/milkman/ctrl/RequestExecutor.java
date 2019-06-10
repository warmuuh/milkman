package milkman.ctrl;

import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.ui.plugin.CustomCommand;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.Templater;

@RequiredArgsConstructor
@Slf4j
public class RequestExecutor extends Service<ResponseContainer> {

	private final RequestContainer request; 
	private final RequestTypePlugin plugin;
	private final Templater templater;
	private final Optional<CustomCommand> customCommand;
	
	@Override
	protected Task<ResponseContainer> createTask() {
		return new Task<ResponseContainer>() {
			
			@Override
			protected ResponseContainer call() {
				try {
					if (customCommand.isPresent()) {
						String commandId = customCommand.get().getCommandId();
						log.info("Execute custom command: " + commandId);
						return plugin.executeCustomCommand(commandId, request, templater);
					} else {
						log.info("Execute request");
						return plugin.executeRequest(request, templater);
					}
				} catch (Throwable e) {
					log.error("Execution of request failed", e);
					String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
					updateMessage(rootCauseMessage);
					throw e;
				}
			}
		};
	}

}
