package milkman.utils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

public class FunctionalUtils {

	
	
	@AllArgsConstructor
	public static class AugBiConsumer<T1, T2> implements BiConsumer<T1, T2>{

		@Delegate
		BiConsumer<T1, T2> delegate;
		
		public AugBiConsumer<T1, T2> andThen(Runnable sideEffect) {
			return new AugBiConsumer<T1, T2>((T1 t1, T2 t2) -> {
				delegate.accept(t1, t2);
				sideEffect.run();
			});
		}
	}
	
	public static <T, R> AugBiConsumer<T, R> run(BiConsumer<T, R> f1) {
		return new AugBiConsumer<T, R>(f1);
	}
	
	
	
	@AllArgsConstructor
	public static class AugConsumer<T1> implements Consumer<T1>{

		@Delegate
		Consumer<T1> delegate;
		
		public AugConsumer<T1> andThen(Runnable sideEffect) {
			return new AugConsumer<T1>((T1 t1) -> {
				delegate.accept(t1);
				sideEffect.run();
			});
		}
	}
	
	public static <T> AugConsumer<T> run(Consumer<T> f1) {
		return new AugConsumer<T>(f1);
	}
	
}
