package milkman.ui.main.dialogs;

import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXSpinner;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import lombok.Getter;
import lombok.SneakyThrows;
import milkman.utils.fxml.FxmlUtil;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static milkman.utils.fxml.FxmlBuilder.*;

public class WaitForMonoDialog<T> {

	private Dialog dialog;
	@Getter boolean cancelled;

	Label title;

	private T value;
	private Throwable error;

	private Disposable subscription;

	public void showAndWait(String title, Mono<T> mono) {
		JFXDialogLayout content = new StringInputDialogFxml(this);
		this.title.setText(title);
		subscription = mono
				.doOnNext(v -> value = v)
				.doOnError(ex -> error = ex)
				.doFinally(s -> dialog.close())
				.subscribeOn(Schedulers.single())
				.subscribe();
		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}

	@SneakyThrows
	public T getValue() {
		if (error != null){
			throw error;
		}
		return value;
	}
	
	 private void onCancel() {
		cancelled = true;
		subscription.dispose();
		dialog.close();
	}

	public static class StringInputDialogFxml extends JFXDialogLayout {
		public StringInputDialogFxml(WaitForMonoDialog controller){
			setHeading(controller.title = label("Title"));

			var vbox = new VboxExt();
			vbox.add(new JFXSpinner());
			setBody(vbox);

			setActions(cancel(controller::onCancel));
		}
	}
}
