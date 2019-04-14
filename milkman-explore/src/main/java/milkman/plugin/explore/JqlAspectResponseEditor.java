package milkman.plugin.explore;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.controls.JFXButton;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPath;
import io.burt.jmespath.jackson.JacksonRuntime;
import io.burt.jmespath.parser.ParseException;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.ui.components.ContentEditor;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.rest.contenttype.JsonContentType;
import milkman.ui.plugin.rest.domain.RestResponseBodyAspect;
import milkman.ui.plugin.rest.domain.RestResponseHeaderAspect;
import milkman.utils.fxml.GenericBinding;

@Slf4j
public class JqlAspectResponseEditor implements ResponseAspectEditor {
	
	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request, ResponseContainer response) {
		JqlQueryAspect qryAspect = request.getAspect(JqlQueryAspect.class).get();
		Tab tab = new Tab("Explore");
		

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/ContentEditor.fxml"));
		ContentEditor contentView = loader.load();
		contentView.setEditable(false);
		contentView.setContentTypePlugins(Collections.singletonList(new JsonContentType()));
		contentView.setContentType("application/json");
		contentView.setHeaderVisibility(false);
		
		VBox.setVgrow(contentView, Priority.ALWAYS);
		
		GenericBinding<JqlQueryAspect, String> binding = GenericBinding.of(
				JqlQueryAspect::getQuery, 
				JqlQueryAspect::setQuery, 
				qryAspect); 

		TextField qryInput = new TextField();
//		qryInput.textProperty().addListener((obs, o, n) -> {
//			if (n == null)
//				return;
//			String body = response.getAspect(RestResponseBodyAspect.class).map(b -> b.getBody()).orElse("");
//			String jmesRes = executeJmesQuery(n, body);
//			contentView.setContent(() -> jmesRes, s -> {});
//		});
		qryInput.textProperty().bindBidirectional(binding);
		qryInput.setUserData(binding);
		
		
		if (StringUtils.isNotBlank(qryAspect.getQuery())) {
			evaluateExpression(response, contentView, qryAspect.getQuery());
		}
		
		binding.toStream().successionEnds(Duration.ofMillis(250))
			.subscribe(qry -> evaluateExpression(response, contentView, qry));
		
		
		HBox.setHgrow(qryInput, Priority.ALWAYS);
		
		JFXButton helpBtn = new JFXButton();
		helpBtn.setOnAction(e -> {
			 try {
				Desktop.getDesktop().browse(new URI("http://jmespath.org/"));
			} catch (IOException | URISyntaxException e1) {
				e1.printStackTrace();
			}
			
		});
		helpBtn.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.QUESTION_CIRCLE, "1.5em"));
		tab.setContent(new VBox(new HBox(qryInput, helpBtn), contentView));
		return tab;
	}

	private void evaluateExpression(ResponseContainer response, ContentEditor contentView, String qry) {
		String body = response.getAspect(RestResponseBodyAspect.class).map(b -> b.getBody()).orElse("");
		String jmesRes = executeJmesQuery(qry, body);
		contentView.setContent(() -> jmesRes, s -> {});
	}

	private String executeJmesQuery(String query, String body) {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		try {
			Expression<JsonNode> expression = jmespath.compile(query);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode input = mapper.readTree(body);
			JsonNode result = expression.search(input);
			return result.toString();
		} catch (ParseException e) {
			return e.getMessage();
		} catch (Exception e) {
			log.error("Failed to parse body", e);
			return e.getMessage();
		}
	}

	@Override
	public boolean canHandleAspect(RequestContainer request, ResponseContainer response) {
		return request.getAspect(JqlQueryAspect.class).isPresent() 
				&& response.getAspect(RestResponseHeaderAspect.class)
							.filter(h -> h.contentType().contentEquals("application/json"))
							.isPresent();
	}

}
