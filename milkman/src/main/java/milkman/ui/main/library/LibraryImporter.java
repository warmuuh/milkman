package milkman.ui.main.library;

import static milkman.utils.fxml.FxmlBuilder.hbox;
import static milkman.utils.fxml.FxmlBuilder.label;
import static milkman.utils.fxml.FxmlBuilder.vbox;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import milkman.domain.Workspace;
import milkman.ui.main.Toaster;
import milkman.ui.main.library.LibraryOptionsProvider.LibraryOptions;
import milkman.ui.plugin.ImporterPlugin;
import milkman.ui.plugin.LibraryPlugin;
import milkman.ui.plugin.LibraryPlugin.LibraryEntry;
import milkman.ui.plugin.LibraryPluginAware;
import milkman.utils.javafx.MappedList;
import np.com.ngopal.control.AutoFillTextBox;
import np.com.ngopal.control.AutoFillTextBoxSkin;
import org.reactfx.EventStreams;

public class LibraryImporter implements ImporterPlugin, LibraryPluginAware {

  private List<LibraryPlugin> plugins;

  @Override
  public String getName() {
    return "Library";
  }

  @Override
  public Node getImportControls() {
    ObservableList<String> data = FXCollections.observableArrayList();
//    MappedList<String, LibraryEntry> mappedList = new MappedList<>(data, LibraryEntry::getDisplayName);
    AutoFillTextBox<String> box = new AutoFillTextBox<>(data);
    box.setSkin(new AutoFillTextBoxSkin<>(box));
    EventStreams.nonNullValuesOf(box.getTextbox().textProperty())
        .filter(s -> s.length() > 3)
        .successionEnds(Duration.ofMillis(250))
        .subscribe(qry -> {
          List<LibraryEntry> entries = searchLibraries(qry);
          data.setAll(entries.stream().map(e -> e.getDisplayName()).collect(Collectors.toList()));
        });

    return vbox(label("Search library:"))
        .add(box, true);
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
