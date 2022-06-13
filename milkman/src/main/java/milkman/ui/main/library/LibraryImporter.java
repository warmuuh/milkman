package milkman.ui.main.library;

import static milkman.utils.fxml.FxmlBuilder.label;
import static milkman.utils.fxml.FxmlBuilder.vbox;

import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import milkman.domain.Workspace;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.ImporterPlugin;
import milkman.ui.plugin.LibraryPlugin;
import milkman.ui.plugin.LibraryPlugin.LibraryEntry;
import milkman.ui.plugin.LibraryPluginAware;
import milkman.utils.fxml.FxmlBuilder.VboxExt;
import milkman.utils.javafx.AutoCompleteBox;

public class LibraryImporter implements ImporterPlugin, LibraryPluginAware {

  private List<LibraryPlugin> plugins;
  private AutoCompleteBox<LibraryEntry> completeBox;

  @Override
  public String getName() {
    return "Library";
  }

  @Override
  public Node getImportControls() {
//    MappedList<String, LibraryEntry> mappedList = new MappedList<>(data, LibraryEntry::getDisplayName);
    ComboBox<LibraryEntry> box = new ComboBox<>();
    this.completeBox = new AutoCompleteBox<>(box, this::searchLibraries);
//    EventStreams.nonNullValuesOf(box.getTextbox().textProperty())
//        .filter(s -> s.length() > 3)
//        .successionEnds(Duration.ofMillis(250))
//        .subscribe(qry -> {
//          List<LibraryEntry> entries = searchLibraries(qry);
//          data.setAll(entries.stream().map(e -> e.getDisplayName()).collect(Collectors.toList()));
//        });
    box.setMinWidth(200);
    box.setPrefWidth(200);

    VboxExt vbox = vbox(label("Search library:"));
    vbox.add(box, true);
    VBox.setVgrow(vbox, Priority.ALWAYS);
    return vbox;
  }

  private List<LibraryEntry> searchLibraries(String qry) {
    return LibraryOptionsProvider.options()
        .getLibraryDefinitions().parallelStream()
        .flatMap(ld -> plugins.stream()
            .filter(p -> p.getName().equals(ld.getType()))
            .flatMap(plugin -> plugin.lookupEntry(ld.getUrl(), qry).stream())
        ).collect(Collectors.toList());
  }

  @Override
  public boolean importInto(Workspace workspace, Toaster toaster) {

    return false;
  }

  @Override
  public void setLibraryPlugins(List<LibraryPlugin> plugins) {
    this.plugins = plugins;
  }
}
