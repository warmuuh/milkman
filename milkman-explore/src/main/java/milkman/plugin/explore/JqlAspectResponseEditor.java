package milkman.plugin.explore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTooltip;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPath;
import io.burt.jmespath.jackson.JacksonRuntime;
import io.burt.jmespath.parser.ParseException;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import milkman.PlatformUtil;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.ui.components.AutoCompleter;
import milkman.ui.components.ContentEditor;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.rest.contenttype.JsonContentType;
import milkman.ui.plugin.rest.domain.RestResponseBodyAspect;
import milkman.utils.fxml.GenericBinding;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class JqlAspectResponseEditor implements ResponseAspectEditor {

	private final AutoCompleter completer;

	private Label compilationWarning;
	private JFXTooltip compilationTooltip;
	private TextField qryInput;

	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		JqlQueryAspect qryAspect = request.getAspect(JqlQueryAspect.class).get();
		Tab tab = new Tab("Explore");

		ContentEditor contentView = new ContentEditor();
		contentView.setEditable(false);
		contentView.setContentTypePlugins(Collections.singletonList(new JsonContentType()));
		contentView.setContentType("application/json");
		contentView.setHeaderVisibility(false);

		VBox.setVgrow(contentView, Priority.ALWAYS);

		GenericBinding<JqlQueryAspect, String> binding = GenericBinding.of(JqlQueryAspect::getQuery,
				JqlQueryAspect::setQuery, qryAspect);

		qryInput = new JFXTextField();
		completer.attachDynamicCompletionTo(qryInput, input -> {
			return qryAspect.getQueryHistory().stream()
					.filter(qry -> input.isBlank() || StringUtils.containsIgnoreCase(qry, input))
					.collect(Collectors.toList());
		});
//		qryInput.setEditable(true);
//		qryInput.getItems().addAll(qryAspect.getQueryHistory());
//		qryInput.textProperty().addListener((obs, o, n) -> {
//			if (n == null)
//				return;
//			String body = response.getAspect(RestResponseBodyAspect.class).map(b -> b.getBody()).orElse("");
//			String jmesRes = executeJmesQuery(n, body);
//			contentView.setContent(() -> jmesRes, s -> {});
//		});
		// the included textfield does not commit value to parent until it looses focus
		// we have to "bind" commiting to textchange for interactivity
//		qryInput.getEditor().textProperty().addListener((obs, oldText, newText) -> {
//			qryInput.commitValue();
//	    });
		qryInput.textProperty().bindBidirectional(binding);
		qryInput.setUserData(binding);

		binding.toStream().successionEnds(Duration.ofMillis(500))
				.subscribe(qry -> evaluateExpression(request, response, contentView, qry));

		HBox.setHgrow(qryInput, Priority.ALWAYS);

		if (compilationWarning == null) {
			compilationWarning = new Label();
			compilationWarning.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.EXCLAMATION_TRIANGLE, "1em"));
			compilationWarning.setVisible(false);
			compilationTooltip = new JFXTooltip();
			JFXTooltip.setVisibleDuration(javafx.util.Duration.millis(10000));
			JFXTooltip.install(compilationWarning, compilationTooltip, Pos.BOTTOM_CENTER);
		}

		JFXButton helpBtn = new JFXButton();
		helpBtn.setOnAction(e -> PlatformUtil.tryOpenBrowser("http://jmespath.org/"));
		helpBtn.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.QUESTION_CIRCLE, "1.5em"));
		tab.setContent(new VBox(new HBox(qryInput, compilationWarning, helpBtn), contentView));

		// initial evaluation
		evaluateExpression(request, response, contentView, qryAspect.getQuery());

		return tab;
	}

	private void evaluateExpression(RequestContainer request, ResponseContainer response, ContentEditor contentView,
			String qry) {
		var aspect = response.getAspect(RestResponseBodyAspect.class);

		CompletableFuture<String> body = aspect
				.map(b -> b.getBody())
				.map(b -> {
					var buffer = new StringBuffer();
					var f = new CompletableFuture<String>();
					b.subscribe(value -> buffer.append(new String(value, StandardCharsets.UTF_8)),
							buffer::append, () -> f.complete(buffer.toString()));
					return f;
				}).orElse(CompletableFuture.completedFuture(""));

		String executeQry = StringUtils.isBlank(qry) ? "@" : qry;

		body.thenAccept(b -> executeJmesQuery(executeQry, b)
				.ifPresent(jmesRes -> {
					addToQueryHistory(qry, jmesRes, request);
					Platform.runLater(() -> contentView.setContent(() -> jmesRes, s -> {}));
		}));
		
	}

	private void addToQueryHistory(String qry, String qryResult, RequestContainer request) {
		if (StringUtils.isBlank(qryResult) || qryResult.equalsIgnoreCase("null"))
			return;
		if (StringUtils.isBlank(qry) || qry.equals("@"))
			return;

		request.getAspect(JqlQueryAspect.class).ifPresent(aspect -> {
			aspect.getQueryHistory().remove(qry);
			aspect.getQueryHistory().add(qry);

			if (aspect.getQueryHistory().size() > 10) {
				aspect.getQueryHistory().remove(0);
			}

			// and update ui:
//			qryInput.getItems().clear();
//			qryInput.getItems().addAll(aspect.getQueryHistory());
		});

	}

	private void setCompilationWarning(Optional<String> error) {
		compilationWarning.setVisible(error.isPresent());
		error.ifPresent(compilationTooltip::setText);
	}

	private Optional<String> executeJmesQuery(String query, String body) {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		try {
			Expression<JsonNode> expression = jmespath.compile(query);
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT); // formatting
			JsonNode input = mapper.readTree(body);
			JsonNode result = expression.search(input);
			setCompilationWarning(Optional.empty());
			return Optional.ofNullable(mapper.writeValueAsString(result));
		} catch (ParseException e) {
			setCompilationWarning(Optional.of(e.getMessage()));
			return Optional.empty();
		} catch (Exception e) {
			setCompilationWarning(Optional.of(e.getMessage()));
			return Optional.empty();
		}
	}

	@Override
	public boolean canHandleAspect(RequestContainer request, ResponseContainer response) {
		return request.getAspect(JqlQueryAspect.class).isPresent();
//				&& response.getAspect(RestResponseHeaderAspect.class)
//							.filter(h -> h.contentType().contentEquals("application/json")).isPresent();
	}

}
