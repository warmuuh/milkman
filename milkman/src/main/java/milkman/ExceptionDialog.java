package milkman;

import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class ExceptionDialog extends Alert {

	public ExceptionDialog(Throwable ex) {
		this("Exception during startup of application", ex);
	}

	public ExceptionDialog(String headerText, Throwable ex) {
		this(headerText, ex.getClass().getName() + ": " + ex.getMessage(), ex);
	}

	public ExceptionDialog(String headerText, String contentText, Throwable ex) {
		this("Exception", headerText, contentText, ex);
	}

	public ExceptionDialog(String title, String headerText, String contentText, Throwable ex) {
		super(AlertType.ERROR);
		setTitle(title);
		setHeaderText(headerText);
		setContentText(contentText);


		// Create expandable Exception.
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		String exceptionText = sw.toString();

		Label label = new Label("The exception stacktrace was:");

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		getDialogPane().setExpandableContent(expContent);
	}

	public static void showExceptionDialog(String headerText, Throwable ex) {
		new ExceptionDialog(headerText, ex.getClass().getName() + ": " + ex.getMessage(), ex).showAndWait();
	}

	public static void showExceptionDialog(String headerText, String contentText, Throwable ex) {
		new ExceptionDialog(headerText, contentText, ex).showAndWait();
	}

	public static void showExceptionDialog(String title, String headerText, String contentText, Throwable ex) {
		new ExceptionDialog(title, headerText, contentText, ex).showAndWait();
	}
}
