package milkman.ui.plugin.rest;

import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.ui.components.TableEditor;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.rest.domain.QueryParamEntry;
import milkman.ui.plugin.rest.domain.RestQueryParamAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

import java.util.UUID;

import static milkman.utils.FunctionalUtils.run;

public class RequestQueryParamTabController implements RequestAspectEditor {


	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request) {
		RestQueryParamAspect qryParams = request.getAspect(RestQueryParamAspect.class).get();
		TableEditor<QueryParamEntry> editor = new TableEditor<QueryParamEntry>("rest.params.list");
		editor.enableAddition(() -> {
			updateDirtyState(qryParams, request);
			return new QueryParamEntry(UUID.randomUUID().toString(), "", "");
		});
		editor.addColumn("Name", QueryParamEntry::getName, run(QueryParamEntry::setName).andThen(() -> updateDirtyState(qryParams, request)));
		editor.addColumn("Value", QueryParamEntry::getValue,run(QueryParamEntry::setValue).andThen(() -> updateDirtyState(qryParams, request)));
		editor.addDeleteColumn("Delete", (removed) -> updateDirtyState(qryParams, request));
		
		//TODO: the listener is fired before the item gets added
		editor.setItems(qryParams.getEntries());
		
		qryParams.onInvalidate.clear();
		qryParams.onInvalidate.add(() -> editor.setItems(qryParams.getEntries()));
		
		return new Tab("Parameter", editor);
	}

	private void updateDirtyState(RestQueryParamAspect qryParams, RequestContainer request) {
		qryParams.setDirty(true);
		if (request instanceof RestRequestContainer) {
			RestRequestContainer restRequestContainer = (RestRequestContainer) request;
			String newUrl = qryParams.generateNewUrl(restRequestContainer.getUrl());
			restRequestContainer.setUrl(newUrl);
			request.onInvalidate.invoke(); //this issues a refresh of the url field
		}
	}

	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(RestQueryParamAspect.class).isPresent();
	}

}
