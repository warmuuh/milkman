package milkman.plugin.grpc.editor;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.grpc.domain.GrpcResponseHeaderAspect;
import milkman.plugin.grpc.domain.HeaderEntry;
import milkman.ui.components.TableEditor;
import milkman.ui.plugin.ResponseAspectEditor;

public class GrpcResponseHeaderAspectEditor implements ResponseAspectEditor {

	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		GrpcResponseHeaderAspect headers = response.getAspect(GrpcResponseHeaderAspect.class).get();
		TableEditor<HeaderEntry> editor = new TableEditor<HeaderEntry>("grpc.resheaders.list");
		editor.disableAddition();
		editor.addReadOnlyColumn("Name", HeaderEntry::getName);
		editor.addReadOnlyColumn("Value", HeaderEntry::getValue);
		editor.setRowToStringConverter(this::headerToString);
		
		headers.getEntries().thenAccept(headerList -> {Platform.runLater(() -> {
				editor.setItems(headerList);	
			});
		});
		
		
		return new Tab("Response Headers", editor);
	}

	@Override
	public boolean canHandleAspect(RequestContainer request, ResponseContainer response) {
		return response.getAspect(GrpcResponseHeaderAspect.class).isPresent();
	}

	private String headerToString(HeaderEntry header) {
		return header.getName() + ": " + header.getValue();
	}
}
