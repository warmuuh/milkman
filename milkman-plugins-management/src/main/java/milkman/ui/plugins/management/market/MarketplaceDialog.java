package milkman.ui.plugins.management.market;

import static milkman.utils.fxml.FxmlBuilder.button;
import static milkman.utils.fxml.FxmlBuilder.cancel;
import static milkman.utils.fxml.FxmlBuilder.hbox;
import static milkman.utils.fxml.FxmlBuilder.icon;
import static milkman.utils.fxml.FxmlBuilder.label;
import static milkman.utils.fxml.FxmlBuilder.text;
import static milkman.utils.fxml.FxmlBuilder.vbox;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXTextField;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.time.Duration;
import java.util.Comparator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import milkman.PlatformUtil;
import milkman.ui.plugins.management.market.GithubApiClient.MarketplacePlugin;
import milkman.utils.fxml.FxmlBuilder;
import milkman.utils.fxml.FxmlUtil;
import org.reactfx.EventStreams;

public class MarketplaceDialog {

  private JFXTextField txt_search;
  private GithubApiClient apiClient = new GithubApiClient();
  private Dialog dialog;
  private ListView<MarketplacePlugin> pluginList;
  @Getter
  private MarketplacePlugin chosenPlugin;


  @Getter
  boolean cancelled = true;

  public MarketplaceDialog() {
  }

  public void showAndWait() {
    JFXDialogLayout content = new MarketplaceDialogFxml(this);

    pluginList.setCellFactory(view -> new MarketplacePluginCell());
    pluginList.setMinWidth(600);
    pluginList.setBorder(new Border(
        new BorderStroke(Paint.valueOf("black"), BorderStrokeStyle.SOLID, null,
            new BorderWidths(1))));
    ObservableList<MarketplacePlugin> observableList =
        FXCollections.observableArrayList(apiClient.fetchPluginRepos());
    SortedList<MarketplacePlugin> sortedList =
        observableList.sorted(Comparator.comparing(MarketplacePlugin::name));
    FilteredList<MarketplacePlugin> filteredList = new FilteredList<>(sortedList);


    EventStreams.nonNullValuesOf(txt_search.textProperty())
        .successionEnds(Duration.ofMillis(250))
        .subscribe(qry -> setFilterPredicate(filteredList, qry));


    pluginList.setItems(filteredList);




    dialog = FxmlUtil.createDialog(content);
    dialog.showAndWait();
  }

  private void setFilterPredicate(FilteredList<MarketplacePlugin> filteredList, String searchTerm) {
    if (searchTerm != null && !searchTerm.isEmpty()) {
      filteredList.setPredicate(p -> p.name().toLowerCase().contains(searchTerm.toLowerCase())
          || p.description().toLowerCase().contains(searchTerm.toLowerCase())
          || p.author().toLowerCase().contains(searchTerm.toLowerCase()));
    } else {
      filteredList.setPredicate(o -> true);
    }

  }

  private void onSave(MarketplacePlugin chosenPlugin) {
    cancelled = false;
    this.chosenPlugin = chosenPlugin;
    dialog.close();
  }

  private void onCancel() {
    cancelled = true;
    dialog.close();
  }

  public static class MarketplaceDialogFxml extends JFXDialogLayout {

    public MarketplaceDialogFxml(MarketplaceDialog controller) {
      setHeading(label("Plugin Marketplace"));

      setBody(vbox(
              controller.txt_search = text("plugin-search", "search for plugins..."),
              controller.pluginList = new ListView<>()
          )
      );

      setActions(cancel(controller::onCancel));
    }
  }

  @RequiredArgsConstructor
  class MarketplacePluginCell extends JFXListCell<MarketplacePlugin> {
    @Override
    protected void updateItem(MarketplacePlugin plugin, boolean empty) {
      super.updateItem(plugin, empty);
      if (empty || plugin == null) {
        setText(null);
        setGraphic(null);
      } else {
        setText(null);
        setGraphic(createEntry(plugin));
      }
    }

    private Node createEntry(MarketplacePlugin plugin) {
      Label title = label(plugin.name());
      title.setFont(Font.font(title.getFont().getFamily(), FontWeight.EXTRA_BOLD,
          title.getFont().getSize() + 4));

      Label label = label("Author: " + plugin.author());
      label.setOpacity(0.7);

      Label desc = label(plugin.description());
      desc.setMaxWidth(400);
      FxmlBuilder.VboxExt pluginDesc = vbox(
          title,
          desc,
          hbox(label)
      );
      HBox.setHgrow(pluginDesc, Priority.ALWAYS);

      JFXButton visitButton = button("marketplace.goto-plugin", icon(FontAwesomeIcon.GLOBE), () -> {
            PlatformUtil.tryOpenBrowser(plugin.documentationUrl());
          }
      );
      JFXButton installButton = button("marketplace.install-plugin", icon(FontAwesomeIcon.DOWNLOAD), () -> {
            Platform.runLater(() -> onSave(plugin));
          }
      );

      FxmlBuilder.HboxExt content = hbox(pluginDesc, vbox(visitButton, installButton));
      return content;
    }
  }
}
