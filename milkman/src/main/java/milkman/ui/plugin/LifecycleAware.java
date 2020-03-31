package milkman.ui.plugin;

public interface LifecycleAware {

    default void onPostConstruct() {};
}
