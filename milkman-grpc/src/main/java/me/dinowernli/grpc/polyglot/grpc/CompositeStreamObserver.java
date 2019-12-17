package me.dinowernli.grpc.polyglot.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.StreamObserver;

/**
 * A {@link StreamObserver} which groups multiple observers and executes them all.
 */
public class CompositeStreamObserver<ReqT, ResT> implements ClientResponseObserver<ReqT, ResT> {
  private static final Logger logger = LoggerFactory.getLogger(CompositeStreamObserver.class);
  private final ImmutableList<StreamObserver<ResT>> observers;

  @SafeVarargs
  public static <R, T> CompositeStreamObserver<R, T> of(StreamObserver<T>... observers) {
    return new CompositeStreamObserver<R, T>(ImmutableList.copyOf(observers));
  }

  private CompositeStreamObserver(ImmutableList<StreamObserver<ResT>> observers) {
    this.observers = observers;
  }

  @Override
  public void onCompleted() {
    for (StreamObserver<ResT> observer : observers) {
      try {
        observer.onCompleted();
      } catch (Throwable t) {
        logger.error("Exception in composite onComplete, moving on", t);
      }
    }
  }

  @Override
  public void onError(Throwable t) {
    for (StreamObserver<ResT> observer : observers) {
      try {
        observer.onError(t);
      } catch (Throwable s) {
        logger.error("Exception in composite onError, moving on", s);
      }
    }
  }

  @Override
  public void onNext(ResT value) {
    for (StreamObserver<ResT> observer : observers) {
      try {
        observer.onNext(value);
      } catch (Throwable t) {
        logger.error("Exception in composite onNext, moving on", t);
      }
    }
  }

	@Override
	public void beforeStart(ClientCallStreamObserver<ReqT> requestStream) {
		for (StreamObserver<ResT> observer : observers) {
			if (observer instanceof ClientResponseObserver) {
				((ClientResponseObserver)observer).beforeStart(requestStream);
			}
		}
	}
}