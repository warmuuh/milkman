package milkman.ui.main.library;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import milkman.ui.main.dialogs.EditLibraryDialog;
import milkman.ui.main.options.OptionDialogBuilder;
import milkman.ui.main.options.OptionDialogPane;
import milkman.ui.plugin.LibraryPlugin;
import milkman.ui.plugin.LibraryPluginAware;
import milkman.ui.plugin.OptionPageProvider;
import milkman.ui.plugin.OptionsObject;


public class LibraryOptionsProvider implements OptionPageProvider<LibraryOptionsProvider.LibraryOptions>,
    LibraryPluginAware {

  private List<LibraryPlugin> plugins;

  @Override
  public void setLibraryPlugins(List<LibraryPlugin> plugins) {
    this.plugins = plugins;
  }

  @Data
  public static class LibraryOptions implements OptionsObject {
    private List<LibraryDefinition> libraryDefinitions = new LinkedList<>();
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class LibraryDefinition {
    String type;
    String url;
  }

  private static LibraryOptions currentOptions = new LibraryOptions();
  public static LibraryOptions options() {
    return currentOptions;
  }

  @Override
  public LibraryOptions getOptions() {
    return currentOptions;
  }

  @Override
  public void setOptions(LibraryOptions options) {
    currentOptions = options;
  }

  @Override
  public OptionDialogPane getOptionsDialog(OptionDialogBuilder builder) {
    return builder.page("Library", getOptions())
        .section("Registered Libraries")
        .list(
            LibraryOptions::getLibraryDefinitions,
            Map.of(
                "Type", LibraryDefinition::getType,
                "Url", LibraryDefinition::getUrl
            ),
            this::createNewLibraryDefinition,
            this::editLibraryDefinition
        )
        .endSection()
        .build();
  }

  private void editLibraryDefinition(LibraryDefinition libraryDefinition) {
    EditLibraryDialog dialog = new EditLibraryDialog();
    dialog.showAndWait(Optional.of(libraryDefinition), plugins);
    if (!dialog.isCancelled()) {
      libraryDefinition.setType(dialog.getLibraryType());
      libraryDefinition.setUrl(dialog.getLibraryUrl());
    }
  }

  private LibraryDefinition createNewLibraryDefinition() {
    EditLibraryDialog dialog = new EditLibraryDialog();
    dialog.showAndWait(Optional.empty(), plugins);
    if (dialog.isCancelled()) {
      return null;
    }

    return new LibraryDefinition(dialog.getLibraryType(), dialog.getLibraryUrl());
  }

  @Override
  public int getOrder() {
    return 1000;
  }



}
