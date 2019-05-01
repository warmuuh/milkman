package milkman.ui.plugin;

public interface Orderable {
	
	/**
	 * defines the order between objects, the higher, the more to the right or bottom usually
	 */
	default int getOrder() {
		return Integer.MAX_VALUE;
	}
	
}
