package milkman.utils;

import java.util.concurrent.CompletableFuture;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import milkman.domain.ResponseContainer;

/**
 * for signaling of request state between main application and plugins.
 * 
 * @author peter
 *
 */
@RequiredArgsConstructor
public class AsyncResponseControl {

	@Getter
	private final AsyncControl cancellationControl = new AsyncControl();
	public final Event0 onRequestStarted = new Event0();
	public final CompletableFuture<Void> onRequestSucceeded = new CompletableFuture<>();
	public final CompletableFuture<Throwable> onRequestFailed = new CompletableFuture<>();
	
	@Getter @Setter
	private ResponseContainer response;
	
	public void cancleRequest() {
		if (onRequestSucceeded.isDone() && onRequestFailed.isDone())
			return;
		onRequestStarted.clear();
		cancellationControl.onCancellationRequested.invoke();
	}
	
	
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public class AsyncControl {
		public final Event0 onCancellationRequested = new Event0();		
		public void triggerReqeuestStarted() {
			onRequestStarted.invoke();
			onRequestStarted.clear();
		}
		public void triggerRequestSucceeded() {
			onRequestSucceeded.complete(null);
			onRequestFailed.cancel(true);
			
		}
		public void triggerRequestFailed(Throwable exception) {
			onRequestFailed.complete(exception);
			onRequestSucceeded.cancel(true);
		}
	}
	
	
}
