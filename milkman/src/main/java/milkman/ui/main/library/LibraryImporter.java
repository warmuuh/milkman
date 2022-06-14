package milkman.ui.main.library;

import static milkman.utils.fxml.FxmlBuilder.hbox;
import static milkman.utils.fxml.FxmlBuilder.label;
import static milkman.utils.fxml.FxmlBuilder.vbox;

import java.util.List;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Value;
import milkman.domain.Collection;
import milkman.domain.Workspace;
import milkman.ui.components.TinySpinner;
import milkman.ui.main.Toaster;
import milkman.ui.main.library.LibraryOptionsProvider.LibraryDefinition;
import milkman.ui.plugin.ImporterPlugin;
import milkman.ui.plugin.LibraryPlugin;
import milkman.ui.plugin.LibraryPlugin.LibraryEntry;
import milkman.ui.plugin.LibraryPluginAware;
import milkman.utils.fxml.FxmlBuilder.VboxExt;
import milkman.utils.javafx.AutoCompleteBox;

public class LibraryImporter implements ImporterPlugin, LibraryPluginAware {

  private List<LibraryPlugin> plugins;
  private AutoCompleteBox<SearchEntry> completeBox;
  private ComboBox<SearchEntry> searchBox;

  @Override
  public String getName() {
    return "Library";
  }

  @Override
  public Node getImportControls() {
//    MappedList<String, LibraryEntry> mappedList = new MappedList<>(data, LibraryEntry::getDisplayName);
    this.searchBox = new ComboBox<>();

    this.completeBox = new AutoCompleteBox<>(searchBox, this::searchLibraries);
//    EventStreams.nonNullValuesOf(box.getTextbox().textProperty())
//        .filter(s -> s.length() > 3)
//        .successionEnds(Duration.ofMillis(250))
//        .subscribe(qry -> {
//          List<LibraryEntry> entries = searchLibraries(qry);
//          data.setAll(entries.stream().map(e -> e.getDisplayName()).collect(Collectors.toList()));
//        });
    searchBox.setMinWidth(200);
    searchBox.setPrefWidth(200);


    Label searchLabel = label("Search library");
    VboxExt vbox = vbox(searchLabel);
    vbox.add(searchBox, true);
    VBox.setVgrow(vbox, Priority.ALWAYS);

    if (LibraryOptionsProvider.options().getLibraryDefinitions().isEmpty()) {
      vbox.add(label("No library definitions found. Add some definitions in the settings."));
    }

    return vbox;
  }

  private List<SearchEntry> searchLibraries(String qry) {
    List<SearchEntry> foundEntries = LibraryOptionsProvider.options()
        .getLibraryDefinitions().parallelStream()
        .flatMap(ld -> plugins.stream()
            .filter(p -> p.getName().equals(ld.getType()))
            .flatMap(plugin -> plugin.lookupEntry(ld.getUrl(), qry).stream().map(
                libraryEntry -> new SearchEntry(ld, libraryEntry)
            ))
        ).collect(Collectors.toList());
    return foundEntries;
  }

  @Override
  public boolean importInto(Workspace workspace, Toaster toaster) {
    SearchEntry selectedItem = searchBox.getSelectionModel().getSelectedItem();
    if (selectedItem == null) {
      return false;
    }

    Collection importedCollection = plugins.stream()
        .filter(p -> p.getName().equals(selectedItem.getDefinition().getType()))
        .map(plugin -> plugin.importCollection(selectedItem.getDefinition().getUrl(), selectedItem.getEntry().getId()))
        .findAny()
        .orElseThrow(() -> new IllegalArgumentException(
            "No importer found for library type " + selectedItem.getDefinition().getType()));

    workspace.getCollections().add(importedCollection);
    toaster.showToast("New collection imported: " + importedCollection.getName());
    return true;
  }

  @Override
  public void setLibraryPlugins(List<LibraryPlugin> plugins) {
    this.plugins = plugins;
  }

  @Value
  static class SearchEntry {
    LibraryDefinition definition;
    LibraryEntry entry;

    @Override
    public String toString() {
      return entry.getDisplayName();
    }
  }
}
