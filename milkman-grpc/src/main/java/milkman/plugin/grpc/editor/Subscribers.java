package milkman.plugin.grpc.editor;

import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.function.Consumer;
import java.util.function.Function;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Subscribers {

	public static <T> Subscriber<T> subscriber(Consumer<T> onNext, Consumer<Throwable> onError){
		return subscriber(onNext, onError, () -> {}); 
	}
	public static <T> Subscriber<T> subscriber(Consumer<T> onNext, Consumer<Throwable> onError, Runnable onComplete){
		return new Subscriber<T>() {
		        Subscription subscription;
		        public final void onSubscribe(Subscription subscription) {
		            this.subscription = subscription;
	                subscription.request(Long.MAX_VALUE);
		        }
		        public final void onError(Throwable ex) {
		        	onError.accept(ex);
		        }
		        public final void onComplete() {
		        	onComplete.run();
		        }
		        public final void onNext(T item) {
		            try {
		            	onNext.accept(item);
		            } catch (Throwable ex) {
		                subscription.cancel();
		                onError.accept(ex);
		            }
		        }
		};
	}
	
	/**
	 * subscriptions are buffering (for {@link SubmissionPublisher} at least).
	 * so not to loose any message between execution and actual subscription, 
	 * we add a bogus subscriber that does not call {@link Subscription#request(long)}
	 * leading to messages put into a buffer.
	 * 
	 * 
	 * On adding an actual subscriber to this publisher, the subscription will be forwarded.
	 * 
	 * remark: for sake of simplicity, this Publisher only supports one subscriber for now
	 * 
	 * remark: this might only work for SubmissionPublisher though.
	 * 
	 * @param <T>
	 * @param publisher
	 * @return
	 */
	public static <T> Publisher<T> buffer(Publisher<T> publisher){
		Processor<T, T> proc = new Processor<T, T>() {
			private Subscription subscription;
			private Subscriber<? super T> subscriber;

			@Override
			public void onSubscribe(Subscription subscription) {
				this.subscription = subscription;
			}

			@Override
			public void onNext(T item) {
				subscriber.onNext(item);
			}

			@Override
			public void onError(Throwable throwable) {
				subscriber.onError(throwable);
			}

			@Override
			public void onComplete() {
				subscriber.onComplete();
			}

			@Override
			public void subscribe(Subscriber<? super T> subscriber) {
				this.subscriber = subscriber;
				subscriber.onSubscribe(subscription);
				
			}
		};
		publisher.subscribe(proc);
		return proc;
	}
	
	public static <T, R> Publisher<R> map(Publisher<T> publisher, Function<T, R> transformation){
		var proc = new Processor<T, R>() {
			private Subscription subscription;
			private Subscriber<? super R> subscriber;

			@Override
			public void onSubscribe(Subscription subscription) {
				this.subscription = subscription;
			}

			@Override
			public void onNext(T item) {
				subscriber.onNext(transformation.apply(item));
			}

			@Override
			public void onError(Throwable throwable) {
				subscriber.onError(throwable);
			}

			@Override
			public void onComplete() {
				subscriber.onComplete();
			}

			@Override
			public void subscribe(Subscriber<? super R> subscriber) {
				this.subscriber = subscriber;
				subscriber.onSubscribe(subscription);
				
			}
		};
		publisher.subscribe(proc);
		return proc;
	}
	
	
	
}
