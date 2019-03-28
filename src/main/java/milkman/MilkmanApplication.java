package milkman;


import io.vavr.collection.List;
import javafx.application.Application;
import javafx.stage.Stage;
import milkman.domain.Collection;
import milkman.domain.RequestContainer;
import milkman.domain.Workspace;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

public class MilkmanApplication extends Application {
	
	private MainModule module;

	@Override
	public void init() throws Exception {
		super.init();
		module = new MainModule();
		module.start();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		module.getMainWindow().start(primaryStage);
		module.getWorkspaceController().loadWorkspace(createDummyModel());
	}

	private Workspace createDummyModel() {
		RequestTypePlugin requestTypePlugin = module.getUiPluginManager().loadRequestTypePlugins().get(0);
		RequestContainer request = requestTypePlugin.createNewRequest();
		module.getUiPluginManager().loadRequestAspectPlugins().forEach(p -> p.initializeAspects(request));
		Collection collection1 = new Collection("jsonPlaceholder", List.of(request).asJava());
		return new Workspace(List.of(collection1).asJava());
	}

	public static void main(String[] args) {
		launch(args);
	}

}
